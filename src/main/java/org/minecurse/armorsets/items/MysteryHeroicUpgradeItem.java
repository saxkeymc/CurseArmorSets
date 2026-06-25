package org.minecurse.armorsets.items;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.types.AlchemistSet;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.RandomUtil;
import org.minecurse.features.types.clickitems.ClickItem;

public class MysteryHeroicUpgradeItem extends ClickItem {
   public String getName() {
      return "mystery_heroic_upgrade";
   }

   public ItemStack createItem() {
      return new ItemBuilder(Material.INK_SACK, DyeColor.RED.getDyeData())
         .name("&4&lMystery &c&lHeroic &4&lUpgrade")
         .lore(new String[]{" ", "&fRight click me to receive a mystery", "&fheroic upgrade.", " "})
         .flag(ItemFlag.HIDE_ATTRIBUTES);
   }

   public boolean onClick(Player player, ItemStack itemStack, Action action) {
      boolean armorSet = RandomUtil.getChance(30.0);
      if (armorSet) {
         List<ArmorSet> sets = new ArrayList<>(ArmorSetPlugin.getInstance().getArmorSetRegistry().getRegisteredSets());
         sets.removeIf(ArmorSet::isHidden);
         sets.removeIf(set -> set instanceof AlchemistSet);
         ArmorSet randomSet = sets.get(RandomUtil.getRandInt(0, sets.size() - 1));
         boolean forArmor = RandomUtil.getChance(50.0);
         Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            "heroicarmor armorset " + player.getName() + " " + randomSet.getInternalName() + " " + RandomUtil.getRandInt(30, 100) + " " + forArmor
         );
      } else {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "heroicarmor normal " + player.getName() + " " + RandomUtil.getRandInt(30, 100));
      }

      return true;
   }
}
