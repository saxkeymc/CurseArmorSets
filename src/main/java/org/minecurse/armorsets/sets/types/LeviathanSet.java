package org.minecurse.armorsets.sets.types;

import com.rit.sucy.EnchantmentAPI;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
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
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;
import org.minecurse.commons.utils.StringUtil;

@ArmorCrystal(name = "Leviathan", lore = "", outgoing = 5.0, incoming = 1.25)
public class LeviathanSet extends ArmorSet {
   public LeviathanSet(DefaultConfig defaultConfig) {
      super(
         "Leviathan",
         "&3&lLeviathan",
         ChatColor.DARK_AQUA,
         new ItemBuilder(Material.INK_SACK).durability(DyeColor.CYAN.getDyeData()),
         defaultConfig.getArmorOutgoing("leviathan"),
         defaultConfig.getArmorIncoming("leviathan"),
         10.0,
         5.0
      );
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.SWORD || armorPiece == ArmorPiece.BOW) {
         return null;
      } else if (armorPiece == ArmorPiece.AXE) {
         ItemBuilder builder = new ItemBuilder(armorPiece.getDefaultMaterial())
            .enchantment(Enchantment.DAMAGE_ALL, 5)
            .enchantment(Enchantment.DURABILITY, 3)
            .name(this.getPieceName(armorPiece))
            .lore(new String[]{
               "&7Lifesteal V",
               "&7Silence IV",
               "",
               "&3&lEffects:",
               "&f • Deal 10% more damage in water.",
               "&f • Deal 5% more damage to players holding swords.",
               ""
            });
         return this.addNBT(builder, this.getInternalName());
      } else {
         return this.addNBT(
            new ItemBuilder(armorPiece.getDefaultMaterial())
               .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
               .enchantment(Enchantment.DURABILITY, 3)
               .name(StringUtil.color(this.getPieceName(armorPiece)))
               .lore(
                  new String[]{
                     " ",
                     "&7&oAn armorset that is used to",
                     "&7&oconquer and win battles all",
                     "&7&othroughout the ocean!",
                     "",
                     "&3&lEffects:",
                     "&f • +4 Max Hearts.",
                     "&f • Deal an extra 15% damage.",
                     "&f • Enjoy a 5% damage reduction.",
                     "",
                     "&3&lAbility:",
                     "&f5% to dodge Incoming Hits",
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
            return "&8» &3&lWater Proof Goggles &8«";
         case CHESTPLATE:
            return "&8» &3&lSoaked Vest &8«";
         case LEGGINGS:
            return "&8» &3&lEmpowered Legs of Water &8«";
         case BOOTS:
            return "&8» &3&lWater Dripped Shoes &8«";
         case AXE:
            return "&8» &3&lAqua Axe &8«";
         default:
            return this.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      double bonusHealth = 0.0;
      boolean santa = player.hasMetadata("santaMask");
      boolean vandalCrew = player.hasMetadata("vandalCrew");
      boolean lovers = player.hasMetadata("loversBalloon");
      if (santa) {
         bonusHealth += 4.0;
      }

      if (vandalCrew) {
         bonusHealth += 4.0;
      }

      if (lovers) {
         bonusHealth += 4.0;
      }

      player.setMaxHealth(20.0 + bonusHealth + 8.0);
      player.setMetadata("levi", new FixedMetadataValue(ArmorSetPlugin.getInstance(), true));
   }

   @Override
   public void onUnEquip(Player player) {
      player.setMaxHealth(20.0);
      player.removeMetadata("levi", ArmorSetPlugin.getInstance());
   }

   @Override
   public void onAttackWithWeapon(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      if (attacked instanceof Player) {
         Player player = (Player)attacked;
         if (armorHolder.getLocation().getBlock().getType().equals(Material.WATER)
            || armorHolder.getLocation().getBlock().getType().equals(Material.STATIONARY_WATER)) {
            event.setDamage(event.getDamage() + event.getDamage() * 0.1);
         }

         if (player.getItemInHand() != null && player.getItemInHand().getType().name().endsWith("_SWORD")) {
            event.setDamage(event.getDamage() + event.getDamage() * 0.05);
         }
      }
   }

   @Override
   public void onDamaged(Player armorHolder, LivingEntity damager, EntityDamageByEntityEvent event) {
      if (RandomUtil.getChance(0.5)) {
         Optional<Player> hasKingslayer = armorHolder.getNearbyEntities(32.0, 32.0, 32.0)
            .stream()
            .filter(Player.class::isInstance)
            .map(Player.class::cast)
            .filter(player -> LocUtil.canPvp(player, player.getLocation()))
            .filter(player -> !FactionUtil.isAlly(player, armorHolder))
            .filter(player -> player.hasMetadata("kingslayer"))
            .findAny();
         if (!FounderShard.hasFounderShardEquipped(armorHolder) && hasKingslayer.isPresent()) {
            this.sendAbilityMessage(
               armorHolder, "&3&lLeviathan Dodge", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
            );
         } else {
            this.sendAbilityMessage(armorHolder, "&3&lLeviathan Dodge", "{0}", armorHolder.getName());
            event.setDamage(0.0);
         }
      }
   }
}
