package org.minecurse.armorsets.abilities.loversability;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.util.ChanceUtil;

public class LoversListener implements Listener {
   private final ArmorSetPlugin plugin;

   public LoversListener(ArmorSetPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerDefense(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (this.plugin.getAbilityManager().getLoversManager().hasLovers(player.getUniqueId())) {
            if (ChanceUtil.doChance(this.plugin.getDefaultConfig().getLoversActivationChance())) {
               this.plugin.getAbilityManager().getLoversManager().addLoversHearts(player.getUniqueId());
            }
         }
      }
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      LoversManager loversManager = this.plugin.getAbilityManager().getLoversManager();
      if (loversManager.hasLovers(event.getPlayer().getUniqueId())) {
         if (!loversManager.hasLoversTask(event.getPlayer().getUniqueId())) {
            loversManager.removeLovers(event.getPlayer().getUniqueId(), false);
         }
      }
   }

   @EventHandler
   public void onDeath(PlayerDeathEvent event) {
      LoversManager loversManager = this.plugin.getAbilityManager().getLoversManager();
      if (loversManager.hasLovers(event.getEntity().getUniqueId())) {
         loversManager.removeLovers(event.getEntity().getUniqueId(), true);
      }
   }
}
