package org.minecurse.armorsets.items;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.DiamondHook;
import org.minecurse.armorsets.sets.types.BlizzardSet;
import org.minecurse.armorsets.sets.types.ColossalSet;
import org.minecurse.armorsets.sets.types.DiabloSet;
import org.minecurse.armorsets.sets.types.InfernoSet;
import org.minecurse.armorsets.sets.types.KothSet;
import org.minecurse.armorsets.sets.types.LeviathanSet;
import org.minecurse.armorsets.sets.types.MagmaSet;
import org.minecurse.armorsets.sets.types.MarksManSet;
import org.minecurse.armorsets.sets.types.RogueSet;
import org.minecurse.armorsets.sets.types.RoverSet;
import org.minecurse.armorsets.sets.types.WardenSet;
import org.minecurse.armorsets.sets.types.WraithSet;
import org.minecurse.armorsets.sets.types.nextmap.DemonicSet;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.RandomUtil;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.commons.utils.inventory.InvUtil;
import org.minecurse.features.types.clickitems.ClickItem;

public class MysteryHookItem extends ClickItem {
   public String getName() {
      return "mystery_diamond_hook";
   }

   public ItemStack createItem() {
      return new ItemBuilder(Material.DIAMOND_HOE)
         .name("&8» &b&lThe Diamond Hook &8«")
         .enchantment(Enchantment.DAMAGE_ALL, 5)
         .lore("")
         .lore(
            new String[]{
               "&7A powerful hoe that has the ability",
               "&7to harness Axe & Sword enchants.",
               "",
               "&610 slots",
               "&6This hoe acts like a diamond sword.",
               "",
               "&7Right click to get a mystery",
               "&7armor set weapon effect.",
               ""
            }
         );
   }

   public boolean onClick(Player p0, ItemStack p1, Action p2) {
      if (p2.name().contains("LEFT")) {
         p0.sendMessage(ArmorSetPlugin.getPrefix("&fRight-Click inorder to receive a random hook!", false));
         return false;
      }

      if (p2.name().contains("RIGHT")) {
         List<ArmorSet> sets = new ArrayList<>(ArmorSetPlugin.getInstance().getArmorSetRegistry().getRegisteredSets());
         sets.removeIf(ArmorSet::isHidden);
         this.rig(sets);
         ArmorSet chosen = sets.get(RandomUtil.getRandInt(0, sets.size() - 1));
         ItemStack item = DiamondHook.buildItem(chosen);
         p0.sendMessage(
            StringUtil.color(
               "&b&lDiamond Hook &8⤖ &7Your Diamond Hook has taken the effect of the "
                  + (
                     chosen.getDisplayName().equals("&f&lKoth")
                        ? "&f&l&ki&c&lK&6&l.&e&lO&2&l.&b&lT&5&l.&d&lH&f&l&ki&f&l"
                        : chosen.getDisplayName() + " &7armorset!"
                  )
            )
         );
         InvUtil.addItems(p0, new ItemStack[]{item});
      }

      return true;
   }

   public void rig(List<ArmorSet> sets) {
      sets.removeIf(
         set -> set instanceof InfernoSet
            || set instanceof RoverSet
            || set instanceof ColossalSet
            || set instanceof DiabloSet
            || set instanceof WraithSet
            || set instanceof MagmaSet
            || set instanceof BlizzardSet
            || set instanceof KothSet
            || set instanceof WardenSet
            || set instanceof LeviathanSet
            || set instanceof MarksManSet
            || set instanceof DemonicSet
            || set instanceof RogueSet
      );
   }
}
