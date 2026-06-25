package org.minecurse.armorsets.sets.types;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.ArmorSetRegistry;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.util.ColorUtil;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.Logger;

@ArmorCrystal(name = "Lucky", lore = "", outgoing = 0.0, incoming = 5.0)
public class LuckySet extends ArmorSet {
   private final ArmorSetRegistry registry = ArmorSetPlugin.getInstance().getArmorSetRegistry();

   public LuckySet(DefaultConfig defaultConfig) {
      super(
         "Lucky",
         "&2&lLucky",
         ChatColor.DARK_GREEN,
         new ItemBuilder(Material.WATER_LILY),
         defaultConfig.getArmorOutgoing("lucky"),
         defaultConfig.getArmorIncoming("lucky"),
         0.0,
         0.0
      );
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.SWORD || armorPiece == ArmorPiece.BOW) {
         return null;
      } else {
         return armorPiece == ArmorPiece.AXE
            ? this.addNBT(
               new ItemBuilder(armorPiece.getDefaultMaterial())
                  .enchantment(Enchantment.DAMAGE_ALL, 5)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(this.getPieceName(armorPiece))
                  .lore(new String[]{"", "&2&lEffects:", "&f • Reduce enemies' enchantment luck by -25% proc rate.", ""}),
               this.getInternalName()
            )
            : this.addNBT(
               new ItemBuilder(armorPiece.getDefaultMaterial())
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(ColorUtil.translate(this.getPieceName(armorPiece)))
                  .lore(
                     new String[]{
                        "",
                        "&7&oFeeling like you just can't get a break?",
                        "&7&oFeeling like nothing is going your way?",
                        "&7&oWell look no further, this set will give",
                        "&7&oyou all the Luck you'll ever need.",
                        "",
                        "&2&lEffects:",
                        "&f • Deal an extra 20% more damage.",
                        "&f • Enjoy a 10% damage reduction.",
                        "&f • Lucky Star ability.",
                        "",
                        "&2&lAbility:",
                        "&fLucky Star ability &7&o(Harness the power of a random Armor Set ability for 15 seconds.)",
                        ""
                     }
                  ),
               this.getInternalName()
            );
      }
   }

   @Override
   public String getPieceName(ArmorPiece armorPiece) {
      switch (armorPiece) {
         case HELMET:
            return "&8» &2&lFortune Helm &8«";
         case CHESTPLATE:
            return "&8» &2&lCloverplate &8«";
         case LEGGINGS:
            return "&8» &2&lDestiny Greaves &8«";
         case BOOTS:
            return "&8» &2&lGolden Steps &8«";
         case AXE:
            return "&8» &2&lLuck Axe &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      player.setMetadata("luckySet", new FixedMetadataValue(ArmorSetPlugin.getInstance(), true));
   }

   @Override
   public void onUnEquip(Player player) {
      player.removeMetadata("luckySet", ArmorSetPlugin.getInstance());
   }

   @Override
   public void onAttack(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      if (LocUtil.canPvp(armorHolder, armorHolder.getLocation())) {
         ArmorSet armorSet = this.registry.getRandomAbility();
         Logger.log("Lucky Armor Set: " + armorSet.getInternalName());
         armorSet.onAttack(armorHolder, attacked, event);
      }
   }

   @Override
   public void onDamaged(Player armorHolder, LivingEntity damager, EntityDamageByEntityEvent event) {
      if (LocUtil.canPvp(armorHolder, armorHolder.getLocation())) {
         ArmorSet armorSet = this.registry.getRandomAbility();
         Logger.log("Lucky Armor Set: " + armorSet.getInternalName());
         armorSet.onDamaged(armorHolder, damager, event);
      }
   }
}
