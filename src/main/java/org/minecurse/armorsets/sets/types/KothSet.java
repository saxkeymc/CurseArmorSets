package org.minecurse.armorsets.sets.types;

import com.rit.sucy.EnchantmentAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.util.ColorUtil;
import org.minecurse.commons.item.ItemBuilder;

@ArmorCrystal(name = "Koth", lore = "", outgoing = 5.0, incoming = 0.0)
public class KothSet extends ArmorSet {
   public KothSet(DefaultConfig defaultConfig) {
      super(
         "Koth",
         "&f&l&ki&c&lK&6&l.&e&lO&2&l.&b&lT&5&l.&d&lH&f&l&ki&f&l",
         ChatColor.WHITE,
         new ItemBuilder(Material.REDSTONE_TORCH_ON),
         defaultConfig.getArmorOutgoing("koth"),
         defaultConfig.getArmorIncoming("koth"),
         10.0,
         0.0
      );
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.BOW) {
         return null;
      }

      if (armorPiece != ArmorPiece.AXE && armorPiece != ArmorPiece.SWORD) {
         return this.addNBT(
            new ItemBuilder(armorPiece.getDefaultMaterial())
               .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5)
               .enchantment(Enchantment.DURABILITY, 3)
               .name(ColorUtil.translate(this.getPieceName(armorPiece)))
               .lore(
                  new String[]{
                     " ",
                     "&7&oAn armorset rewarded to only those",
                     "&7&owho known throughout the pirate realm",
                     "&7&oas Kings!",
                     "",
                     "&f&lEffects:",
                     "&f • 50% chance to negate damage from mobs.",
                     "&f • Permanent Speed III",
                     "&f • Receives 10% less damage from players & mobs.",
                     "",
                     "&f&lAbility:",
                     "&fWhile wearing this set, you are able",
                     "&fto capture outposts much faster!",
                     ""
                  }
               ),
            this.getInternalName()
         );
      }

      ItemBuilder builder = new ItemBuilder(armorPiece.getDefaultMaterial())
         .enchantment(Enchantment.DAMAGE_ALL, 5)
         .enchantment(Enchantment.DURABILITY, 3)
         .name(this.getPieceName(armorPiece));
      
      if (armorPiece != ArmorPiece.SWORD) {
         builder.lore(new String[]{
            "&7Lifesteal V",
            "&7Silence IV",
            "", 
            "&f&lEffects:", 
            "&f • Deal 15% more damage to enemies.", 
            "&f • Deal 20% more damage to mobs.", 
            ""
         });
      } else {
         builder.lore(new String[]{
            "", 
            "&f&lEffects:", 
            "&f • Deal 15% more damage to enemies.", 
            "&f • Deal 20% more damage to mobs.", 
            ""
         });
      }

      return this.addNBT(builder, this.getInternalName());
   }

   @Override
   public String getPieceName(ArmorPiece armorPiece) {
      switch (armorPiece) {
         case HELMET:
            return "&8» &f&l&ki&c&lK&6&l.&e&lO&2&l.&b&lT&5&l.&d&lH&f&l&ki&f&l Cap of the Hill &8«";
         case CHESTPLATE:
            return "&8» &f&l&ki&c&lK&6&l.&e&lO&2&l.&b&lT&5&l.&d&lH&f&l&ki&f&l Shield of the Hill &8«";
         case LEGGINGS:
            return "&8» &f&l&ki&c&lK&6&l.&e&lO&2&l.&b&lT&5&l.&d&lH&f&l&ki&f&l Trousers of the Hill &8«";
         case BOOTS:
            return "&8» &f&l&ki&c&lK&6&l.&e&lO&2&l.&b&lT&5&l.&d&lH&f&l&ki&f&l Shoes of the Hill &8«";
         case SWORD:
            return "&8» &f&l&ki&c&lK&6&l.&e&lO&2&l.&b&lT&5&l.&d&lH&f&l&ki&f&l Poker &8«";
         case AXE:
            return "&8» &f&l&ki&c&lK&6&l.&e&lO&2&l.&b&lT&5&l.&d&lH&f&l&ki&f&l Stabber &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      if (ArmorSetPlugin.getInstance().getPotionEffectAPI().canApplyPotionEffect(player, PotionEffectType.SPEED)) {
         player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
      }
   }

   @Override
   public void onUnEquip(Player player) {
      player.removePotionEffect(PotionEffectType.SPEED);
   }
}
