package org.minecurse.armorsets.tasks;

import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.types.PirateSet;
import org.minecurse.armorsets.sets.types.RoverSet;
import org.minecurse.commons.particle.ParticleEffect;
import org.minecurse.commons.utils.LocUtil;

public class ParticleTask extends BukkitRunnable {
   public void run() {
      for (UUID uuid : ArmorSetPlugin.getInstance().getArmorSetRegistry().getActivePlayerSet().keySet()) {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null && player.isOnline()) {
            if (ArmorSetPlugin.getInstance().getArmorSetRegistry().getActiveSet(player) instanceof PirateSet) {
               if (player.hasMetadata("vanish") || player.hasMetadata("invis") || !LocUtil.canPvp(player, player.getLocation())) {
                  continue;
               }

               ParticleEffect.LAVA
                  .sendToPlayers(
                     Bukkit.getOnlinePlayers().stream().collect(Collectors.toList()),
                     player.getLocation(),
                     (float)Math.random(),
                     (float)Math.random(),
                     (float)Math.random(),
                     0.5F,
                     20
                  );
            }

            if (ArmorSetPlugin.getInstance().getArmorSetRegistry().getActiveSet(player) instanceof RoverSet
               && !player.hasMetadata("vanish")
               && !player.hasMetadata("invis")
               && LocUtil.canPvp(player, player.getLocation())) {
               ParticleEffect.CLOUD
                  .sendToPlayers(
                     Bukkit.getOnlinePlayers().stream().collect(Collectors.toList()),
                     player.getLocation(),
                     (float)Math.random(),
                     (float)Math.random(),
                     (float)Math.random(),
                     0.3F,
                     17
                  );
            }
         }
      }
   }
}
