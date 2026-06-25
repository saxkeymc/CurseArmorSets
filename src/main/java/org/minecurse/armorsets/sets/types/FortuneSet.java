package org.minecurse.armorsets.sets.types;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.StringUtil;

@ArmorCrystal(name = "Fortune", lore = "", outgoing = 0.0, incoming = 0.0)
public class FortuneSet extends ArmorSet {
   public FortuneSet(DefaultConfig defaultConfig) {
      super(
         "Fortune",
         "&e&lFortune",
         ChatColor.YELLOW,
         new ItemBuilder(Material.EXP_BOTTLE),
         defaultConfig.getArmorOutgoing("fortune"),
         defaultConfig.getArmorIncoming("fortune"),
         0.0,
         0.0
      );
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.AXE || armorPiece == ArmorPiece.BOW) {
         return null;
      }

      if (armorPiece == ArmorPiece.SWORD) {
         return new ItemBuilder(armorPiece.getDefaultMaterial())
            .enchantment(Enchantment.DAMAGE_ALL, 5)
            .enchantment(Enchantment.DURABILITY, 3)
            .name(this.getPieceName(armorPiece))
            .lore(
               new String[]{
                  "",
                  "&e&lEffects:",
                  "&f • Deal 20% more damage to all mobs.",
                  "&f • Gain 2x more EXP",
                  "&f • Immunity to Purge",
                  "&f • Permanent Resistance II effect.",
                  "&f • Permanent Haste III effect.",
                  ""
               }
            );
      }

      ItemBuilder itemBuilder = new ItemBuilder(armorPiece.getDefaultMaterial())
         .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
         .enchantment(Enchantment.DURABILITY, 3)
         .name(StringUtil.color(this.getPieceName(armorPiece)))
         .lore(
            new String[]{
               " ",
               "&7&oAn armor set for those who are truly",
               "&7&oready for the grind and hustle in",
               "&7&obecoming successful!",
               "",
               "&e&lEffects:",
               "&f • Deal 20% more damage to all mobs.",
               "&f • Gain 2x more EXP",
               "&f • Immunity to Purge",
               "&f • Permanent Resistance II effect.",
               "&f • Permanent Haste III effect.",
               "",
               "&e&lAbility:",
               "&fWhile wearing this set, you can occasionally",
               "&fkill more than one mob off of a stack at once.",
               ""
            }
         );
      return this.addNBT(itemBuilder, this.getInternalName());
   }

   @Override
   public String getPieceName(ArmorPiece armorPiece) {
      switch (armorPiece) {
         case HELMET:
            return "&8» &e&lFortune Headband &8«";
         case CHESTPLATE:
            return "&8» &e&lFortune Coat &8«";
         case LEGGINGS:
            return "&8» &e&lFortune Shorts &8«";
         case BOOTS:
            return "&8» &e&lFortune Slippers &8«";
         case SWORD:
            return "&8» &e&lFortune Blade &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 2), true);
      player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1), true);
      if (!player.hasMetadata("purgeImmunity")) {
         player.setMetadata("purgeImmunity", new FixedMetadataValue(ArmorSetPlugin.getInstance(), true));
      }
   }

   @Override
   public void onUnEquip(Player player) {
      player.removePotionEffect(PotionEffectType.FAST_DIGGING);
      player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
      if (player.hasMetadata("purgeImmunity")) {
         player.removeMetadata("purgeImmunity", ArmorSetPlugin.getInstance());
      }
   }

   @Override
   public void onAttack(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      if (!(attacked instanceof Player)) {
         event.setDamage(event.getDamage() + event.getDamage() * 0.2);
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onDeath(EntityDeathEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = event.getEntity().getKiller();
         if (player != null) {
            ArmorSet set = ArmorSetPlugin.getInstance().getArmorSetRegistry().getActiveSet(player);
            if (set != null) {
               if (set == this) {
                  event.setDroppedExp(event.getDroppedExp() * 2);
               }
            }
         }
      }
   }
}
