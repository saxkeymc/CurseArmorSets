package org.minecurse.armorsets.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.orbs.item.OrbItem;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.DiamondHook;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.sets.types.nextmap.DemonicSet;
import org.minecurse.armorsets.struct.task.DiabloAbilityTask;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.StringUtil;

@CommandAlias("armorset|givearmorset")
@CommandPermission("curse.admin")
public class ArmorSetCommand extends BaseCommand {
   @HelpCommand
   public void onHelp(CommandSender sender) {
      sender.sendMessage(ArmorSetPlugin.getPrefix("&cUsage: /armorset <name> (player) (heroic=false)", false));
   }

   @Default
   @CommandCompletion("@armorsets @players true|false")
   public void onArmorSet(CommandSender sender, ArmorSet armorSet, @Optional OnlinePlayer targetPlayer) {
      Player target;
      if (targetPlayer != null) {
         target = targetPlayer.getPlayer();
      } else {
         target = (Player)sender;
      }

      target.getInventory().addItem(new ItemStack[]{armorSet.getRedeemItem(false)});
      target.updateInventory();
      target.sendMessage(
         ArmorSetPlugin.getPrefix(StringUtil.format("&7You've received a redeem item for the {0} &7armor set.", new Object[]{armorSet.getDisplayName()}), false)
      );
      if (target != sender) {
         sender.sendMessage(
            ArmorSetPlugin.getPrefix(
               StringUtil.format("&7You've given {0} a redeem item for the {1} &7armor set.", new Object[]{target.getName(), armorSet.getDisplayName()}), false
            )
         );
      }
   }

   @Subcommand("hook")
   @CommandCompletion("@players @armorsets")
   public void onDiamondHookGive(CommandSender sender, OnlinePlayer target, ArmorSet set, @Optional Integer extraDamage, @Optional Integer extraSharpness) {
      target.getPlayer().sendMessage(ArmorSetPlugin.getPrefix("&7You have received a diamond hook!", false));
      sender.sendMessage(ArmorSetPlugin.getPrefix("&7You have given out a diamond hook.", false));
      if (extraDamage != null && extraSharpness != null) {
         target.getPlayer().getInventory().addItem(new ItemStack[]{DiamondHook.buildItem(set, extraDamage + 7, extraSharpness + 5)});
      } else {
         target.getPlayer().getInventory().addItem(new ItemStack[]{DiamondHook.buildItem(set)});
      }
   }

   @Subcommand("giverandompiece")
   @CommandCompletion("@armorsets @players")
   public void onRandomItem(CommandSender sender, ArmorSet armorSet, @Optional OnlinePlayer targetPlayer) {
      Player target;
      if (targetPlayer != null) {
         target = targetPlayer.getPlayer();
      } else {
         target = (Player)sender;
      }

      target.getInventory().addItem(new ItemStack[]{armorSet.getRandomItem(false)});
      target.updateInventory();
      target.sendMessage(
         ArmorSetPlugin.getPrefix(StringUtil.format("&7You've received a random item for the {0} &7armor set.", new Object[]{armorSet.getDisplayName()}), false)
      );
      if (target != sender) {
         sender.sendMessage(
            ArmorSetPlugin.getPrefix(
               StringUtil.format("&7You've given {0} a random item for the {1} &7armor set.", new Object[]{target.getName(), armorSet.getDisplayName()}), false
            )
         );
      }
   }

   @Subcommand("giveorb")
   @CommandCompletion("@armorsets @players")
   public void onOrb(CommandSender sender, ArmorSet armorSet, @Optional OnlinePlayer targetPlayer) {
      Player target;
      if (targetPlayer != null) {
         target = targetPlayer.getPlayer();
      } else {
         target = (Player)sender;
      }

      target.getInventory().addItem(new ItemStack[]{OrbItem.buildOrb(armorSet)});
      target.updateInventory();
      target.sendMessage(
         ArmorSetPlugin.getPrefix(StringUtil.format("&7You've received a orb item for the {0} &7armor set.", new Object[]{armorSet.getDisplayName()}), false)
      );
      if (target != sender) {
         sender.sendMessage(
            ArmorSetPlugin.getPrefix(
               StringUtil.format("&7You've given {0} a orb item for the {1} &7armor set.", new Object[]{target.getName(), armorSet.getDisplayName()}), false
            )
         );
      }
   }

   @Subcommand("giveshard")
   @CommandCompletion("@players @nothing")
   public void onFounderShard(CommandSender sender, OnlinePlayer player) {
      sender.sendMessage(ArmorSetPlugin.getPrefix("&7You have given " + player.getPlayer().getName() + " a &4&lFounder Shard&7!", false));
      player.getPlayer().sendMessage(ArmorSetPlugin.getPrefix("&7You have received a &4&lFounder Shard&7 from " + sender.getName() + "&7!", false));
      PlayerUtils.giveOrDropProtectedItem(player.getPlayer(), FounderShard.buildItem(), -1);
   }

   @Subcommand("testrot")
   public void onTestRot(Player player) {
      ArmorSetPlugin.getInstance().getAbilityManager().getRotManager().activateRot(player.getUniqueId());
   }

   @Subcommand("testhell")
   public void onTestHell(Player player) {
      new DemonicSet.ActiveHellTrap(player);
   }

   @Subcommand("testswords")
   @CommandCompletion("@players @nothing")
   public void onTestSwords(Player user, OnlinePlayer target) {
      new DiabloAbilityTask(user, target.getPlayer()).runTaskTimer(ArmorSetPlugin.getInstance(), 0L, 1L);
   }
}
