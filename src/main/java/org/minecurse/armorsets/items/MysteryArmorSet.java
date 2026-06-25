package org.minecurse.armorsets.items;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.types.BlizzardSet;
import org.minecurse.armorsets.sets.types.ColossalSet;
import org.minecurse.armorsets.sets.types.DiabloSet;
import org.minecurse.armorsets.sets.types.InfernoSet;
import org.minecurse.armorsets.sets.types.KothSet;
import org.minecurse.armorsets.sets.types.LeviathanSet;
import org.minecurse.armorsets.sets.types.MagmaSet;
import org.minecurse.armorsets.sets.types.NovaSet;
import org.minecurse.armorsets.sets.types.RogueSet;
import org.minecurse.armorsets.sets.types.RoverSet;
import org.minecurse.armorsets.sets.types.WardenSet;
import org.minecurse.armorsets.sets.types.WraithSet;
import org.minecurse.armorsets.sets.types.nextmap.DemonicSet;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.item.ItemNames;
import org.minecurse.commons.utils.RandomUtil;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.commons.utils.inventory.InvUtil;
import org.minecurse.features.types.clickitems.ClickItem;

public class MysteryArmorSet extends ClickItem {
   public String getName() {
      return "mystery_armor_set";
   }

   public ItemStack createItem() {
      return new ItemBuilder(Material.CHEST)
         .name("&6&lMystery &e&lArmor Set")
         .amount(1)
         .lore(new String[]{"", "&fLeft click to view all", "&farmor sets.", "&fRight click me to receive", "&fa mystery armor set.", ""})
         .flag(ItemFlag.HIDE_ATTRIBUTES);
   }

   public boolean onClick(Player player, ItemStack itemStack, Action action) {
      if (action.name().contains("LEFT")) {
         player.chat("/armorsets");
      } else if (action.name().contains("RIGHT")) {
         List<ArmorSet> sets = new ArrayList<>(ArmorSetPlugin.getInstance().getArmorSetRegistry().getRegisteredSets());
         sets.removeIf(ArmorSet::isHidden);
         this.rig(sets);
         ArmorSet armorSet = sets.get(RandomUtil.getRandInt(0, sets.size() - 1));
         ItemStack item = armorSet.getRedeemItem(false);
         InvUtil.addItems(player, new ItemStack[]{item});
         player.sendMessage(ArmorSetPlugin.getPrefix(StringUtil.format("&7You've received the {0} &7armor set!", new Object[]{ItemNames.lookup(item)}), false));
         return true;
      }

      return false;
   }

   public void rig(List<ArmorSet> sets) {
      sets.removeIf(
         set -> set instanceof InfernoSet
            || set instanceof RoverSet
            || set instanceof ColossalSet
            || set instanceof DiabloSet
            || set instanceof WraithSet
            || set instanceof MagmaSet
            || set instanceof NovaSet
            || set instanceof KothSet
            || set instanceof WardenSet
            || set instanceof LeviathanSet
            || set instanceof DemonicSet
            || set instanceof RogueSet
            || set instanceof BlizzardSet
      );
   }
}
