package org.minecurse.armorsets.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.types.AlchemistSet;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.menu.button.Button;
import org.minecurse.commons.menu.type.chest.ChestMenu;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.StringUtil;

@CommandAlias("alchemist|alchemy")
public class AlchemistCommand extends BaseCommand {
   @Default
   public void onAlchemist(Player player) {
      ChestMenu menu = new ChestMenu(StringUtil.color("&8All Alchemy Items"), 3);
      Button pane = new Button(new ItemBuilder(Material.STAINED_GLASS_PANE, (byte)15).name(" ").lore(""));

      for (int slot = 0; slot < 9; slot++) {
         menu.setButton(slot, pane);
      }

      menu.setButton(9, pane);
      menu.setButton(17, pane);

      for (int slot = 18; slot < 27; slot++) {
         menu.setButton(slot, pane);
      }

      int slot = 10;

      for (AlchemistSet.AlchemyItem alchemyItem : AlchemistSet.AlchemyItem.values()) {
         menu.setButton(slot++, new Button(alchemyItem.buildShopItem(), (clicker, clickInformation) -> this.purchase(clicker, alchemyItem)));
      }

      menu.buildInventory();
      menu.show(player);
   }

   private void purchase(Player player, AlchemistSet.AlchemyItem alchemyItem) {
      if (this.getTotalExperience(player) < alchemyItem.getXpCost()) {
         player.sendMessage(
            ArmorSetPlugin.getPrefix(StringUtil.format("&cYou need {0} XP to purchase this alchemy item.", new Object[]{alchemyItem.getXpCost()}), false)
         );
      } else {
         this.setTotalExperience(player, this.getTotalExperience(player) - alchemyItem.getXpCost());
         ItemStack itemStack = alchemyItem.buildUsableItem();
         itemStack.setAmount(64);
         PlayerUtils.giveOrDropProtectedItem(player, itemStack, -1);
         player.sendMessage(ArmorSetPlugin.getPrefix(StringUtil.format("&7You purchased &e{0} &7x64.", new Object[]{alchemyItem.getPlainName()}), false));
      }
   }

   private int getTotalExperience(Player player) {
      return this.getExperienceAtLevel(player.getLevel()) + Math.round(player.getExp() * this.getExperienceToNextLevel(player.getLevel()));
   }

   private void setTotalExperience(Player player, int experience) {
      player.setExp(0.0F);
      player.setLevel(0);
      player.setTotalExperience(0);
      player.giveExp(Math.max(0, experience));
   }

   private int getExperienceAtLevel(int level) {
      if (level <= 16) {
         return level * level + 6 * level;
      } else {
         return level <= 31 ? (int)(2.5 * level * level - 40.5 * level + 360.0) : (int)(4.5 * level * level - 162.5 * level + 2220.0);
      }
   }

   private int getExperienceToNextLevel(int level) {
      if (level <= 15) {
         return 2 * level + 7;
      } else {
         return level <= 30 ? 5 * level - 38 : 9 * level - 158;
      }
   }
}
