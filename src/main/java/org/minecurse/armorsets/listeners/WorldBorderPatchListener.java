package org.minecurse.armorsets.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class WorldBorderPatchListener implements Listener {
   @EventHandler
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      if (event.getCause() == TeleportCause.ENDER_PEARL) {
         double x = event.getTo().getX();
         double z = event.getTo().getZ();
         double borderSize = event.getTo().getWorld().getWorldBorder().getSize() / 2.0;
         double centerX = event.getTo().getWorld().getWorldBorder().getCenter().getX();
         double centerZ = event.getTo().getWorld().getWorldBorder().getCenter().getZ();
         if (Math.abs(x - centerX) > borderSize || Math.abs(z - centerZ) > borderSize) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You can't teleport outside the world border!");
         }
      }
   }
}
