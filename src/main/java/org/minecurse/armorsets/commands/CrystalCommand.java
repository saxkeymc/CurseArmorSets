package org.minecurse.armorsets.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.struct.CrystalBuilder;
import org.minecurse.commons.utils.PlayerUtils;

@CommandAlias("crystal")
@CommandPermission("enforced.admin")
public class CrystalCommand extends BaseCommand {
   private final ArmorSetPlugin plugin;

   public CrystalCommand(ArmorSetPlugin plugin) {
      this.plugin = plugin;
   }

   @Default
   @CommandCompletion("@players @armorsets")
   public void onCrystalGive(CommandSender sender, OnlinePlayer target, @Default("100") Integer percent, String types) {
      List<ArmorSet> crystalTypes = new ArrayList<>();

      for (String str : types.split(" ")) {
         ArmorSet armorSet = this.plugin.getArmorSetRegistry().getByName(str);
         if (armorSet == null) {
            sender.sendMessage(ArmorSetPlugin.getPrefix("&cInvalid Crystal argument!", false));
            return;
         }

         crystalTypes.add(armorSet);
      }

      CrystalBuilder builder = new CrystalBuilder(crystalTypes);
      builder.chance(percent);
      PlayerUtils.giveOrDropProtectedItem(target.getPlayer(), builder.build(), 30);
      sender.sendMessage(ArmorSetPlugin.getPrefix("&7You have given an armor crystal.", false));
      target.getPlayer().sendMessage(ArmorSetPlugin.getPrefix("&7You have been given an armor crystal.", false));
   }
}
