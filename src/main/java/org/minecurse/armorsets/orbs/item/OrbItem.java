package org.minecurse.armorsets.orbs.item;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.commons.item.ItemBuilder;

public class OrbItem {
   public static ItemStack buildOrb(ArmorSet armorSet) {
      ItemBuilder builder = new ItemBuilder(Material.SLIME_BALL);
      builder.name("&2&LArmor Orb [" + armorSet.getDisplayName() + "&2&l]");
      builder.lore("&fAn orb infused with powers ready to");
      builder.lore("&futilize the abilities of the " + armorSet.getDisplayName() + " &fset!");
      builder.lore("");
      builder.lore("&2Information: ");
      builder.lore("&f • Armor Set: " + armorSet.getDisplayName());
      builder.lore("&f • Usages: &71");
      builder.lore("");
      builder.lore("&7&oHit any player in combat to use");
      builder.lore("&7&owith orb and unlock its abilities!");
      builder.lore("");
      builder.lore("&7To use hit any player of your choice.");
      builder.lore("&7Once used, it cannot be returned!");
      NBTItem item = new NBTItem(builder);
      item.setString("armorOrb", armorSet.getInternalName());
      return item.getItem();
   }
}
