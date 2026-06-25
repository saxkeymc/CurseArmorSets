package org.minecurse.armorsets.struct.task;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.minecurse.commons.runnable.RunnableBuilder;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;

public class MagmaAbilityTask extends BukkitRunnable {
   private final Player entity;
   private final int radius;
   private int counter;

   public MagmaAbilityTask(Player entity, int radius) {
      this.entity = entity;
      this.radius = radius;
      this.counter = 0;
   }

   public static List<Vector> getHollowCube(Location corner1, Location corner2) {
      List<Vector> result = new ArrayList<>();
      int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
      int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
      int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
      int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
      int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
      int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

      for (int i = minX; i <= maxX; i++) {
         for (int j = minZ; j <= maxZ; j++) {
            result.add(new Vector(i, minY, j));
            result.add(new Vector(i, maxY, j));
         }
      }

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            result.add(new Vector(x, y, minZ));
            result.add(new Vector(x, y, maxZ));
         }
      }

      for (int z = minZ; z <= maxZ; z++) {
         for (int y = minY; y <= maxY; y++) {
            result.add(new Vector(minX, y, z));
            result.add(new Vector(maxX, y, z));
         }
      }

      return result;
   }

   public void run() {
      if (!this.entity.isDead() && this.counter < 14) {
         Location location = this.entity.getLocation();
         Location cornerOne = new Location(location.getWorld(), location.getX() - this.radius, location.getY() - this.radius, location.getZ() - this.radius);
         Location cornerTwo = new Location(location.getWorld(), location.getX() + this.radius, location.getY() + this.radius, location.getZ() + this.radius);

         for (Vector vector : getHollowCube(cornerOne, cornerTwo)) {
            RunnableBuilder.bind(
                  () -> {
                     double searchRadius = this.radius / 2.0 + 1.0;
                     this.entity
                        .getNearbyEntities(searchRadius, searchRadius, searchRadius)
                        .stream()
                        .filter(Player.class::isInstance)
                        .map(Player.class::cast)
                        .filter(player -> LocUtil.canPvp(player, player.getLocation()))
                        .filter(player -> !FactionUtil.isAlly(player, this.entity))
                        .forEach(player -> player.damage(Math.max(player.getHealth(), 20.0) * 0.15, this.entity));
                  }
               )
               .runSync();
         }

         this.counter++;
      } else {
         this.cancel();
      }
   }
}
