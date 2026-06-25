package org.minecurse.armorsets.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import de.tr7zw.nbtapi.NBTItem;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.ItemArmor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.commons.utils.StringUtil;

@CommandAlias("heroicarmor")
@CommandPermission("enforced.admin")
public class HeroicArmorCommand extends BaseCommand {
   @HelpCommand
   public void onHelp(CommandSender sender) {
      sender.sendMessage(ArmorSetPlugin.getPrefix("&cUsage: /heroicarmor <type> <player> (chance=100)", true));
   }

   @Subcommand("armorset")
   @CommandCompletion("@players @armorsets")
   public void onArmorSet(OnlinePlayer targetPlayer, ArmorSet armorSet, @Optional Integer givenChance, @Optional Boolean forArmor) {
      int chance = givenChance != null && givenChance > 0 ? givenChance : 100;
      boolean isForArmor = forArmor != null && forArmor;
      ItemStack itemStack = ArmorSetPlugin.getInstance().getApi().getArmorSetUpgrade(armorSet, chance, isForArmor);
      targetPlayer.getPlayer().getInventory().addItem(new ItemStack[]{itemStack});
      String type = isForArmor ? "Armor Piece" : "Weapon";
      targetPlayer.getPlayer()
         .sendMessage(
            ArmorSetPlugin.getPrefix(
               StringUtil.format(
                  "&7You were given an &6&lHeroic " + type + " Upgrade &7for the {0} &7armor set with a chance of &e{1}%&7!",
                  new Object[]{armorSet.getDisplayName(), chance}
               ),
               true
            )
         );
   }

   @Subcommand("normal")
   @CommandCompletion("@players")
   public void onNormal(OnlinePlayer targetPlayer, @Optional Integer givenChance) {
      int chance = givenChance != null && givenChance > 0 ? givenChance : 100;
      ItemStack itemStack = ArmorSetPlugin.getInstance().getApi().getNormalUpgrade(chance, false);
      targetPlayer.getPlayer().getInventory().addItem(new ItemStack[]{itemStack});
      targetPlayer.getPlayer()
         .sendMessage(
            ArmorSetPlugin.getPrefix(StringUtil.format("&7You were given an &6&lHeroic Upgrade &7with a chance of &e{0}%&7!", new Object[]{chance}), true)
         );
   }

   @Subcommand("hook")
   @CommandCompletion("@players")
   public void onHook(OnlinePlayer targetPlayer, @Optional Integer givenChance) {
      int chance = givenChance != null && givenChance > 0 ? givenChance : 100;
      ItemStack itemStack = ArmorSetPlugin.getInstance().getApi().getHookUpgrade(chance);
      targetPlayer.getPlayer().getInventory().addItem(new ItemStack[]{itemStack});
      targetPlayer.getPlayer()
         .sendMessage(
            ArmorSetPlugin.getPrefix(
               StringUtil.format("&7You were given an &6&lHeroic &b&lDiamond Hook &6&lUpgrade &7with a chance of &e{0}%&7!", new Object[]{chance}), true
            )
         );
   }

   @Subcommand("rainbow")
   @CommandCompletion("@players")
   public void onRainbow(OnlinePlayer targetPlayer, @Optional Integer givenChance) {
      int chance = givenChance != null && givenChance > 0 ? givenChance : 100;
      ItemStack itemStack = ArmorSetPlugin.getInstance().getApi().getRainbowUpgrade(chance);
      targetPlayer.getPlayer().getInventory().addItem(new ItemStack[]{itemStack});
      targetPlayer.getPlayer()
         .sendMessage(
            ArmorSetPlugin.getPrefix(
               StringUtil.format(
                  "&7You were given an &6&lHeroic &c&lR&6&la&e&li&a&ln&b&lb&d&lo&5&lw &6&lUpgrade &7with a chance of &e{0}%&7!", new Object[]{chance}
               ),
               true
            )
         );
   }

   @Subcommand("printnbt")
   @CommandCompletion("@players")
   public void onPrintNbt(Player player) {
      ItemStack itemStack = player.getItemInHand();
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         NBTItem nbtItem = new NBTItem(itemStack);
         player.sendMessage(nbtItem.toString());
      } else {
         player.sendMessage(ArmorSetPlugin.getPrefix("&cYou must be holding an item.", true));
      }
   }

   @Subcommand("getmodifiervalues")
   public void onGetModifierValues(Player player) {
      EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
      player.sendMessage(ArmorSetPlugin.getPrefix("&7Your armor modifier values:", true));
      player.sendMessage(ArmorSetPlugin.getPrefix("Helmet: &f" + this.getModifierFor(player.getInventory().getHelmet()), true));
      player.sendMessage(ArmorSetPlugin.getPrefix("Chestplate: &f" + this.getModifierFor(player.getInventory().getChestplate()), true));
      player.sendMessage(ArmorSetPlugin.getPrefix("Leggings: &f" + this.getModifierFor(player.getInventory().getLeggings()), true));
      player.sendMessage(ArmorSetPlugin.getPrefix("Boots: &f" + this.getModifierFor(player.getInventory().getBoots()), true));
      player.sendMessage(ArmorSetPlugin.getPrefix("Total value: &f" + entityPlayer.br(), true));
   }

   private int getModifierFor(ItemStack itemStack) {
      int modifier = 0;
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
         Item item = nmsStack.getItem();
         if (item instanceof ItemArmor) {
            modifier = ((ItemArmor)item).c;
         }

         NBTTagCompound nbtTag = nmsStack.getTag();
         if (nbtTag != null && nbtTag.hasKey("armorModifier")) {
            modifier = nbtTag.getInt("armorModifier");
         }
      }

      return modifier;
   }
}
