package org.minecurse.armorsets.abilities.rotability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.util.WorldGuardSupport;

public class RotManager {
   private final ArmorSetPlugin plugin;
   private final Map<Block, RotBlock> rotBlockMap = new HashMap<>();
   private final Set<UUID> activeRotPlayers = new HashSet<>();
   private BukkitTask rotDamageTask;

   public RotManager(ArmorSetPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean hasActiveRot(UUID uuid) {
      return this.activeRotPlayers.contains(uuid);
   }

   public boolean isStandingInRot(UUID uuid) {
      Block block = Bukkit.getPlayer(uuid).getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock();
      return this.rotBlockMap.containsKey(block);
   }

   public boolean playerOwnsBlock(UUID uuid) {
      Block block = Bukkit.getPlayer(uuid).getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock();
      if (!this.isStandingInRot(uuid)) {
         return false;
      }

      RotBlock rotBlock = this.rotBlockMap.get(block);
      return rotBlock == null ? false : rotBlock.isOwner(uuid);
   }

   public void activateRot(UUID uuid) {
      Player player = Bukkit.getPlayer(uuid);
      if (player != null && player.isOnline() && !this.hasActiveRot(uuid)) {
         List<Block> eligibleRotBlocks = this.getEligibleRotBlocks(uuid);
         if (!eligibleRotBlocks.isEmpty()) {
            this.activeRotPlayers.add(uuid);
            eligibleRotBlocks.forEach(
               block -> {
                  RotBlock var10000 = this.rotBlockMap
                     .compute(block, (keyBlock, valueRotBlock) -> valueRotBlock == null ? new RotBlock(block).addOwner(uuid) : valueRotBlock.addOwner(uuid));
               }
            );
            this.doBlockChanges(uuid, eligibleRotBlocks);
            this.startRotDamageTask();
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.removeRotPlayer(uuid), 100L);
         }
      }
   }

   public void removeRotPlayer(UUID uuid) {
      this.activeRotPlayers.remove(uuid);
      Iterator<RotBlock> rotBlockIterator = this.rotBlockMap.values().iterator();

      while (rotBlockIterator.hasNext()) {
         RotBlock rotBlock = rotBlockIterator.next();
         if (rotBlock.isOwner(uuid)) {
            rotBlock.removeOwner(uuid);
            if (rotBlock.getBlockOwners().isEmpty()) {
               rotBlockIterator.remove();
            }
         }
      }
   }

   public void updateRotPlayer(UUID uuid) {
      Player player = Bukkit.getPlayer(uuid);
      if (player != null && this.hasActiveRot(uuid)) {
         List<Block> eligibleRotBlocks = this.getEligibleRotBlocks(uuid);
         Iterator<RotBlock> rotBlockIterator = this.rotBlockMap.values().iterator();

         while (rotBlockIterator.hasNext()) {
            RotBlock rotBlock = rotBlockIterator.next();
            if (rotBlock.isOwner(uuid)) {
               rotBlock.updatePlayer(uuid, eligibleRotBlocks);
               if (rotBlock.getBlockOwners().isEmpty()) {
                  rotBlockIterator.remove();
               }
            }
         }

         this.doBlockChanges(uuid, eligibleRotBlocks);
      }
   }

   private void doBlockChanges(UUID uuid, List<Block> eligibleRotBlocks) {
      Player player = Bukkit.getPlayer(uuid);
      if (player != null && this.hasActiveRot(uuid)) {
         eligibleRotBlocks.forEach(
            block -> {
               RotBlock var10000 = this.rotBlockMap
                  .compute(block, (keyBlock, valueRotBlock) -> valueRotBlock == null ? new RotBlock(block).addOwner(uuid) : valueRotBlock.addOwner(uuid));
            }
         );
         eligibleRotBlocks.forEach(block -> player.sendBlockChange(block.getLocation(), Material.WOOL, (byte)10));

         for (Player worldPlayer : player.getWorld().getPlayers()) {
            if (worldPlayer != player) {
               Material rotMaterial = Material.WOOL;
               eligibleRotBlocks.forEach(block -> worldPlayer.sendBlockChange(block.getLocation(), rotMaterial, (byte)10));
            }
         }
      }
   }

   public void startRotDamageTask() {
      if (this.rotDamageTask == null || !Bukkit.getScheduler().isCurrentlyRunning(this.rotDamageTask.getTaskId())) {
         if (!this.activeRotPlayers.isEmpty()) {
            this.rotDamageTask = Bukkit.getScheduler()
               .runTaskTimer(
                  this.plugin,
                  () -> {
                     if (this.activeRotPlayers.isEmpty()) {
                        this.rotDamageTask.cancel();
                     }

                     Iterator<UUID> rotPlayerUUIDIterator = this.activeRotPlayers.iterator();

                     while (rotPlayerUUIDIterator.hasNext()) {
                        Player rotPlayer = Bukkit.getPlayer(rotPlayerUUIDIterator.next());
                        if (rotPlayer == null) {
                           rotPlayerUUIDIterator.remove();
                        } else {
                           for (Entity nearbyEntity : rotPlayer.getNearbyEntities(15.0, 15.0, 15.0)) {
                              if (nearbyEntity instanceof Player) {
                                 Player nearbyPlayer = (Player)nearbyEntity;
                                 if (nearbyPlayer != null
                                    && nearbyPlayer.isOnline()
                                    && !nearbyPlayer.isDead()
                                    && this.isStandingInRot(nearbyPlayer.getUniqueId())
                                    && !this.playerOwnsBlock(nearbyPlayer.getUniqueId())
                                    && !WorldGuardSupport.isPlayerInSafeZone(nearbyPlayer.getUniqueId())) {
                                    Block block = nearbyPlayer.getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock();
                                    if (this.rotBlockMap.containsKey(block)) {
                                       double damageAmount = 1.5;
                                       NovaRotDamagePlayerEvent event = new NovaRotDamagePlayerEvent(
                                          nearbyPlayer, damageAmount, this.rotBlockMap.get(block).getBlockOwners()
                                       );
                                       Bukkit.getPluginManager().callEvent(event);
                                       if (!event.isCancelled()) {
                                          nearbyPlayer.damage(0.01);
                                          nearbyPlayer.setHealth(Math.max(0.0, nearbyPlayer.getHealth() - event.getDamageAmount()));
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  },
                  20L,
                  10L
               );
         }
      }
   }

   public List<Block> getEligibleRotBlocks(UUID playerUUID) {
      List<Block> eligibleBlocks = new ArrayList<>();
      List<Material> invalidMaterials = Arrays.asList(
         Material.BIRCH_WOOD_STAIRS,
         Material.WATER,
         Material.STATIONARY_LAVA,
         Material.STATIONARY_WATER,
         Material.STEP,
         Material.COBBLESTONE_STAIRS,
         Material.QUARTZ_STAIRS,
         Material.BRICK_STAIRS,
         Material.AIR
      );
      Player player = Bukkit.getPlayer(playerUUID);
      Location centerLocation = player.getLocation().clone().subtract(0.0, 1.0, 0.0);
      int x = centerLocation.getBlockX();
      int y = centerLocation.getBlockY();
      int z = centerLocation.getBlockZ();
      int[] ranges = new int[]{4, 4, 3, 2, 1};

      for (int i = 0; i < ranges.length; i++) {
         int range = ranges[i];
         int currentZ = z - i;

         for (int dx = -range; dx <= range; dx++) {
            Location loc = new Location(player.getWorld(), x + dx, y, currentZ);
            Block locBlock = loc.getBlock();
            if (!invalidMaterials.contains(locBlock.getType())) {
               eligibleBlocks.add(locBlock);
            }
         }
      }

      for (int i = 0; i < ranges.length; i++) {
         int range = ranges[i];
         int currentZ = z + i + 1;

         for (int dx = -range; dx <= range; dx++) {
            Location loc = new Location(player.getWorld(), x + dx, y, currentZ);
            Block locBlock = loc.getBlock();
            if (!invalidMaterials.contains(locBlock.getType())) {
               eligibleBlocks.add(locBlock);
            }
         }
      }

      return eligibleBlocks;
   }
}
