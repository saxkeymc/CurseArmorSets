package org.minecurse.armorsets.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.commons.utils.LocUtil;

public class CageUtil {
   private static final HashMap<UUID, Integer> cooldownTime = new HashMap<>();
   private static final HashMap<UUID, BukkitRunnable> cooldownTask = new HashMap<>();
   public static HashMap<UUID, List<Block>> cages = new HashMap<>();

   public static void cage(Player player, Material material, Material material2, Material material3, int n, int n2, int n3, int i, int[] array) {
      final UUID uniqueId = player.getUniqueId();
      Location location = player.getLocation();
      Location subtract = location.subtract(n2 / 2, 1.0, n / 2);
      if (LocUtil.canPvp(player, location) && !cooldownTime.containsKey(uniqueId)) {
         final List<Block> generateCube = generateCube(subtract, array, material, material2, material3);
         cooldownTime.put(uniqueId, i);
         cooldownTask.put(uniqueId, new BukkitRunnable() {
            public void run() {
               CageUtil.cooldownTime.put(uniqueId, CageUtil.cooldownTime.get(uniqueId) - 1);
               if (CageUtil.cooldownTime.get(uniqueId) == 0) {
                  CageUtil.cooldownTime.remove(uniqueId);
                  CageUtil.cooldownTask.remove(uniqueId);
                  CageUtil.removeCage(generateCube);
                  CageUtil.cages.remove(uniqueId);
                  this.cancel();
               }
            }
         });
         cooldownTask.get(uniqueId).runTaskTimer(ArmorSetPlugin.getInstance(), 20L, 20L);
      }
   }

   public static List<Block> generateCube(Location location, int[] array, Material type, Material type2, Material type3) {
      ArrayList<Block> list = new ArrayList<>();
      if (array.length == 3) {
         for (int i = 0; i < array[0]; i++) {
            for (int j = 0; j < array[1]; j++) {
               for (int k = 0; k < array[2]; k++) {
                  if (i == 0 || j == 0 || k == 0 || i == array[0] - 1 || j == array[1] - 1 || k == array[2] - 1) {
                     Block block = location.clone().add(i, j, k).getBlock();
                     if (block.getType().equals(Material.AIR)) {
                        if (j == 0) {
                           block.setType(type3);
                        } else if (j == array[1] - 1) {
                           block.setType(type);
                        } else {
                           block.setType(type2);
                        }

                        list.add(block);
                     }
                  }
               }
            }
         }
      }

      return list;
   }

   public static void removeCage(List<Block> list) {
      Iterator<Block> iterator = list.iterator();

      while (iterator.hasNext()) {
         iterator.next().setType(Material.AIR);
      }
   }
}
