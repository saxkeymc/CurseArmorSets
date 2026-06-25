package org.minecurse.armorsets.struct;

import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBTItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.commons.item.ItemBuilder;

public class CrystalBuilder {
   private final List<ArmorSet> crystals;
   private int chance;

   public CrystalBuilder(List<ArmorSet> crystals) {
      this.crystals = crystals;
      this.chance = 100;
   }

   public CrystalBuilder(ArmorSet armorSet) {
      this.crystals = Lists.newArrayList(new ArmorSet[]{armorSet});
      this.chance = 100;
   }

   public CrystalBuilder add(ArmorSet armor) {
      this.crystals.add(armor);
      return this;
   }

   public CrystalBuilder chance(int c) {
      this.chance = c;
      return this;
   }

   public ItemStack build() {
      ChatColor c = this.chance <= 60 ? (this.chance < 40 ? ChatColor.RED : ChatColor.YELLOW) : ChatColor.GREEN;
      ItemBuilder builder = new ItemBuilder(Material.NETHER_STAR).name("&8» &3&lArmor Crystal &8«");
      builder.lore("" + c + this.chance + "% Success Rate");
      builder.lore("");
      builder.lore("&7Attach me to any armor piece");
      builder.lore("&7and gain a passive advantage.");
      builder.lore("");
      builder.lore("&3&lBonuses: &7(" + this.crystals.size() + " " + (this.crystals.size() == 1 ? "Set)" : "Sets)"));
      builder.lore("");

      for (ArmorSet set : this.crystals) {
         builder.lore(set.getDisplayName() + set.getSetColor() + "&l Armor Set");
         if (set.getCrystal().outgoing() > 0.0) {
            builder.lore("&f • " + set.getSetColor() + "Extra Damage: &f+" + set.getCrystal().outgoing() + "%");
         }

         if (set.getCrystal().incoming() > 0.0) {
            builder.lore("&f • " + set.getSetColor() + "Incoming Damage: &f-" + set.getCrystal().incoming() + "%");
         }

         if (set.getCrystal().abilityChance() > 0.0) {
            builder.lore("&f • " + set.getSetColor() + "Set Ability Chances: &f+" + set.getCrystal().abilityChance() + "%");
         }
      }

      builder.lore("");
      NBTItem item = new NBTItem(builder);
      List<String> crystalNBt = new ArrayList<>();
      this.crystals.forEach(armorSet -> crystalNBt.add(armorSet.getInternalName()));
      String joinedCrystals = String.join(",", crystalNBt);
      item.setInteger("crystalPercent", this.chance);
      item.setString("crystalType", joinedCrystals);
      return item.getItem();
   }
}
