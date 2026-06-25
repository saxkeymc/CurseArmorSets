package org.minecurse.armorsets.orbs.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.orbs.OrbManager;
import org.minecurse.armorsets.sets.ArmorSet;

public class OrbListener implements Listener {
   private final OrbManager orbManager;

   public OrbListener(OrbManager orbManager) {
      this.orbManager = orbManager;
   }

   @EventHandler
   public void onDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
         Player attacker = (Player)event.getDamager();
         Player attacked = (Player)event.getEntity();
         ItemStack itemStack = attacked.getItemInHand();
         if (itemStack != null && itemStack.getType() != Material.AIR) {
            Bukkit.getLogger().info("Attacker: " + attacker.getName());
            Bukkit.getLogger().info("Attacked: " + attacked.getName());
            Bukkit.getLogger().info("Item in hand: " + itemStack.getType());
            if (this.orbManager.isOrb(itemStack)) {
               ArmorSet armorSet = this.orbManager.getArmorSetFromOrb(itemStack);
               if (armorSet != null) {
                  this.orbManager.activate(attacker, armorSet, event);
                  Bukkit.getLogger().info("Activated orb for attacker: " + attacker.getName());
                  if (itemStack.getAmount() > 1) {
                     itemStack.setAmount(itemStack.getAmount() - 1);
                  } else {
                     attacker.setItemInHand(null);
                  }
               }
            }
         }
      }
   }
}
