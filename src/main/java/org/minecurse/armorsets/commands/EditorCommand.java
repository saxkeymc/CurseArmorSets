package org.minecurse.armorsets.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;
import org.minecurse.armorsets.ArmorSetPlugin;

@CommandAlias("seteditor|armoreditor")
@CommandPermission("curse.admin")
public class EditorCommand extends BaseCommand {
   @Default
   public void onOpen(Player player) {
      ArmorSetPlugin.getInstance().getEditorMenuManager().openMainMenu(player);
   }

   @Subcommand("reload")
   public void onReload(Player player) {
      ArmorSetPlugin.getInstance().getDefaultConfig().reload();
      player.sendMessage(ArmorSetPlugin.getPrefix("&aConfig reloaded successfully! All cached values refreshed.", false));
   }
}
