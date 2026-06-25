package org.minecurse.armorsets.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player;
import org.minecurse.armorsets.menus.ArmorSetMenu;

@CommandAlias("armorsets")
public class ArmorSetsCommand extends BaseCommand {
   @Default
   public void onCommand(Player player) {
      ArmorSetMenu.getInventory().open(player);
   }
}
