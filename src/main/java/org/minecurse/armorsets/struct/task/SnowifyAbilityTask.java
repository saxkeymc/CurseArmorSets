package org.minecurse.armorsets.struct.task;

import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.minecurse.armorsets.ArmorSetPlugin;

public class SnowifyAbilityTask extends BukkitRunnable {
   private final Player player;
   private int waveCount;

   public SnowifyAbilityTask(Player player, int waveCount) {
      this.player = player;
      this.waveCount = waveCount;
   }

   public void run() {
      if (this.waveCount > 0) {
         this.waveCount--;

         for (int x = -1; x <= 2; x++) {
            for (int z = -1; z <= 2; z++) {
               Snowball snowball = (Snowball)this.player.launchProjectile(Snowball.class);
               snowball.setVelocity(new Vector(x * 0.25, 1.0, z * 0.25));
               snowball.setMetadata("blizzardSnowballs", new FixedMetadataValue(ArmorSetPlugin.getInstance(), this.player.getName()));
            }
         }
      } else {
         this.cancel();
      }
   }
}
