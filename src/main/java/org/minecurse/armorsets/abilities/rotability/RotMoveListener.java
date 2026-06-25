package org.minecurse.armorsets.abilities.rotability;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.minecurse.armorsets.util.FactionsSupport;

public class RotMoveListener implements Listener {
   private final RotManager rotManager;

   public RotMoveListener(RotManager rotManager) {
      this.rotManager = rotManager;
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      Block initialBlock = event.getFrom().clone().subtract(0.0, 1.0, 0.0).getBlock();
      Block toBlock = event.getTo().clone().subtract(0.0, 1.0, 0.0).getBlock();
      if (!initialBlock.equals(toBlock)) {
         Player player = event.getPlayer();
         if (this.rotManager.hasActiveRot(player.getUniqueId())) {
            this.rotManager.updateRotPlayer(player.getUniqueId());
         }
      }
   }

   @EventHandler
   public void onRotDeath(PlayerDeathEvent event) {
      if (this.rotManager.hasActiveRot(event.getEntity().getUniqueId())) {
         this.rotManager.removeRotPlayer(event.getEntity().getUniqueId());
      }
   }

   @EventHandler
   public void onRotDamage(NovaRotDamagePlayerEvent event) {
      for (UUID blockOwner : event.getRotOwners()) {
         Player rotPlayer = Bukkit.getPlayer(blockOwner);
         if (FactionsSupport.isRelated(rotPlayer, event.getDamagedPlayer())) {
            event.setCancelled(true);
            return;
         }
      }
   }
}
