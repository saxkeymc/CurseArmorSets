package org.minecurse.armorsets.orbs;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.ArmorSetRegistry;

public class OrbManager {
   private final ArmorSetRegistry armorSetRegistry;

   public OrbManager(ArmorSetRegistry armorSetRegistry) {
      this.armorSetRegistry = armorSetRegistry;
   }

   public boolean isOrb(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         NBTItem item = new NBTItem(itemStack);
         return item.getBoolean("armorOrb");
      } else {
         return false;
      }
   }

   public ArmorSet getArmorSetFromOrb(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         NBTItem item = new NBTItem(itemStack);
         return this.armorSetRegistry.getByName(item.getString("armorOrb"));
      } else {
         return null;
      }
   }

   public void activate(Player player, ArmorSet armorSet, EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player) {
         armorSet.onAttack(player, (LivingEntity)event.getEntity(), event);
      }
   }
}
