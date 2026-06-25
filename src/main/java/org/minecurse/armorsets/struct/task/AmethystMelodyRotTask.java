package org.minecurse.armorsets.struct.task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.PlayerUtils;

public class AmethystMelodyRotTask extends BukkitRunnable {
   private final Player user;
   private final ExpiringMap<Vector, AmethystMelodyRotTask.BlockData> locations;
   private int time;

   public AmethystMelodyRotTask(Player player) {
      this.user = player;
      this.locations = ExpiringMap.builder().expiration(200L, TimeUnit.MILLISECONDS).<Object, Object>asyncExpirationListener((key, value) -> {
         Vector vector = (Vector)key;
         AmethystMelodyRotTask.BlockData data = (AmethystMelodyRotTask.BlockData)value;
         this.user.getWorld().getPlayers().forEach(player1 -> player1.sendBlockChange(vector.toLocation(this.user.getWorld()), data.getType(), data.getData()));
      }).build();
   }

   public void run() {
      if (this.time >= 80) {
         this.cancel();
      } else {
         Chunk chunk = this.user.getLocation().getChunk();
         if (!chunk.isLoaded()) {
            chunk.load();
         }

         for (Vector vector : this.getVectors(this.user, 6)) {
            for (Player player : PlayerUtils.getNearbyPlayers(this.user.getLocation(), 32.0, 32.0, 32.0)) {
               player.sendBlockChange(vector.toLocation(this.user.getWorld()), Material.WOOL, DyeColor.PURPLE.getWoolData());
               if (this.locations.containsKey(vector)) {
                  this.locations.resetExpiration(vector);
               } else {
                  this.locations
                     .put(
                        vector,
                        new AmethystMelodyRotTask.BlockData(
                           vector.toLocation(this.user.getWorld()).getBlock().getType(), vector.toLocation(this.user.getWorld()).getBlock().getData()
                        )
                     );
               }
            }
         }

         List<Player> players = this.user
            .getNearbyEntities(6.0, 6.0, 6.0)
            .stream()
            .filter(Player.class::isInstance)
            .map(Player.class::cast)
            .filter(playerx -> playerx != this.user)
            .filter(playerx -> LocUtil.canPvp(playerx, playerx.getLocation()))
            .filter(playerx -> !FactionUtil.isAlly(this.user, playerx))
            .filter(playerx -> !playerx.hasMetadata("vanish"))
            .collect(Collectors.toList());
         players.forEach(playerx -> playerx.damage(12.0));
         this.time++;
      }
   }

   private Set<Vector> getVectors(Player player, int radius) {
      Location center = player.getLocation().clone();
      Set<Vector> vectors = new HashSet<>();
      int cx = center.getBlockX();
      int cz = center.getBlockZ();
      int radiusSquared = radius * radius;

      for (int x = cx - radius; x <= cx + radius; x++) {
         for (int z = cz - radius; z <= cz + radius; z++) {
            if ((cx - x) * (cx - x) + (cz - z) * (cz - z) <= radiusSquared) {
               Location location = new Location(player.getWorld(), x, 0.0, z);
               location.setY(this.getHighestY(location.getWorld(), x, z, center.getBlockY() + 3));
               if (this.isValid(location.getBlock())) {
                  vectors.add(location.getBlock().getLocation().toVector());
               }
            }
         }
      }

      return vectors;
   }

   private boolean isValid(Block block) {
      return block.getType() != Material.AIR
         && block.getType().isSolid()
         && !block.getType().name().contains("SLAB")
         && !block.getType().name().contains("STAIR")
         && !block.getType().name().contains("SIGN");
   }

   private int getHighestY(World world, int x, int z, int startingY) {
      int i;
      for (i = startingY; i > 0; i--) {
         Location location = new Location(world, x, i, z);
         Material material = location.getBlock().getType();
         if (material != Material.AIR && material.isSolid() && !material.name().contains("SIGN")) {
            return i;
         }
      }

      return i;
   }

   private class BlockData {
      private final Material type;
      private final byte data;

      public Material getType() {
         return this.type;
      }

      public byte getData() {
         return this.data;
      }

      public BlockData(Material type, byte data) {
         this.type = type;
         this.data = data;
      }
   }
}
