package org.minecurse.armorsets.struct.ability;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.commons.utils.EffectUtil;
import org.minecurse.commons.utils.LocationUtil;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.StringUtil;

public class ActiveHellTrap {
   private final List<Material> hellBlockTypes = Lists.newArrayList(new Material[]{Material.NETHERRACK, Material.NETHER_BRICK});
   private Player player;
   private int minX;
   private int minY;
   private int minZ;
   private int maxX;
   private int maxY;
   private int maxZ;

   public ActiveHellTrap(Player player) {
      this.player = player;
      this.setBlocks();
   }

   public boolean isInTrap(Player player) {
      Location loc = player.getLocation();
      int x = loc.getBlockX();
      int z = loc.getBlockZ();
      return x <= this.maxX && x >= this.minX && z <= this.maxZ && z >= this.minZ;
   }

   public void setBlocks() {
      Location loc = this.player.getLocation();
      this.minX = loc.getBlockX() - 5;
      this.minY = loc.getBlockY() - 2;
      this.minZ = loc.getBlockZ() - 5;
      this.maxX = loc.getBlockX() + 5;
      this.maxY = loc.getBlockY() + 1;
      this.maxZ = loc.getBlockZ() + 5;
      final List<Player> playerList = LocationUtil.getNearbyPlayers(loc, 30.0);
      final List<Block> blockList = Lists.newArrayList();

      for (int x = this.minX; x <= this.maxX; x++) {
         for (int y = this.minY; y < this.maxY; y++) {
            for (int z = this.minZ; z <= this.maxZ; z++) {
               Block block = loc.getWorld().getBlockAt(x, y, z);
               if (block.getType().isSolid()
                  && block.getType() != Material.AIR
                  && block.getLocation().clone().add(0.0, 1.0, 0.0).getBlock().getType() == Material.AIR) {
                  blockList.add(block);
               }
            }
         }
      }

      for (Player player : playerList) {
         if (this.isInTrap(player)) {
            EffectUtil.applyEffect(player, new PotionEffect(PotionEffectType.WITHER, 40, 1));
         }

         blockList.forEach(blockx -> {
            Material randMat = this.getRandomType();
            player.sendBlockChange(blockx.getLocation(), randMat, (byte)0);
            if (randMat == Material.NETHERRACK) {
               player.sendBlockChange(blockx.getRelative(BlockFace.UP).getLocation(), Material.FIRE, (byte)0);
            }
         });
         PlayerUtils.playSound(player, Sound.ENDERDRAGON_GROWL, 2.0F);
         player.sendMessage(ArmorSetPlugin.getPrefix(StringUtil.format("&c&l * Eternal Flame &7" + this.player.getName(), new Object[0]), false));
         this.player.sendMessage(ArmorSetPlugin.getPrefix(StringUtil.format("&c&l * Eternal Flame &7" + this.player.getName(), new Object[0]), false));
      }

      (new BukkitRunnable() {
         public void run() {
            blockList.forEach(block -> {
               block.getState().update();
               block.getRelative(BlockFace.UP).getState().update();
            });
            playerList.clear();
            blockList.clear();
            ActiveHellTrap.this.delete();
         }
      }).runTaskLater(ArmorSetPlugin.getInstance(), 600L);
   }

   public void delete() {
      this.player = null;
   }

   public Material getRandomType() {
      return this.hellBlockTypes.get(new Random().nextInt(this.hellBlockTypes.size()));
   }
}
