package org.minecurse.armorsets.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.commons.nbt.NBTEditor;
import org.minecurse.commons.utils.StringUtil;

@CommandAlias("fixkoth|kothfix|kothweapon")
public class FixKothCommand extends BaseCommand {
   public static boolean isKothSword(List<String> lore) {
      String koth = StringUtil.color("&f • Deal 20% more damage to mobs.");
      return lore.contains(koth);
   }

   @Default
   public void onDefault(Player player) {
      ItemStack itemStack = player.getItemInHand();
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         if (!isKothSword(new ArrayList<>(itemStack.getItemMeta().getLore()))) {
            player.sendMessage("This isnt a citadel sword");
         } else {
            player.setItemInHand((ItemStack)NBTEditor.set(itemStack, "Citadel", new Object[]{"armorSet"}));
            player.sendMessage("This a citadel sword has been fixed");
         }
      }
   }
}
