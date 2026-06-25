package org.minecurse.armorsets.sets.types;

import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.struct.task.SnowifyAbilityTask;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;

@ArmorCrystal
public class BlizzardSet extends ArmorSet {
   public BlizzardSet(DefaultConfig defaultConfig) {
      super(
         "Blizzard",
         "&3&lBlizzard",
         ChatColor.DARK_AQUA,
         new ItemStack(Material.SNOW_BALL, 1),
         defaultConfig.getArmorOutgoing("blizzard"),
         defaultConfig.getArmorIncoming("blizzard"),
         0.0,
         5.0
      );
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.AXE || armorPiece == ArmorPiece.BOW) {
         return null;
      } else {
         return armorPiece == ArmorPiece.SWORD
            ? this.addNBT(
               new ItemBuilder(armorPiece.getDefaultMaterial())
                  .enchantment(Enchantment.DAMAGE_ALL, 5)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(this.getPieceName(armorPiece))
                  .lore(new String[]{"", "&3&lEffects:", "&f • Enjoy a 5% damage reduction from all enemies.", ""}),
               this.getInternalName()
            )
            : this.addNBT(
               new ItemBuilder(armorPiece.getDefaultMaterial())
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(this.getPieceName(armorPiece))
                  .lore(
                     new String[]{
                        " ",
                        "&7&oForged in the heart of a relentless blizzard,",
                        "&7&othis armor is bestowed upon those who",
                        "&7&ocan command the fury of the frozen tempest.",
                        "&7&oA symbol of unrivaled resilience and power.",
                        "",
                        "&3&lEffects:",
                        " &f• Deal an extra 25% damage to all enemies.",
                        " &f• Enjoy a 20% damage reduction from all enemies.",
                        " &f• Permanent Speed IV effect.",
                        " &f• Snowify ability.",
                        "",
                        "&3&lAbility:",
                        "&fSnowify ability &7&o(Shoot waves of snowballs out your body to harm enemies)",
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
            return "&8» &3&lFrost Crown &8«";
         case CHESTPLATE:
            return "&8» &3&lBlizzard Chestguard &8«";
         case LEGGINGS:
            return "&8» &3&lGlacial Leggings &8«";
         case BOOTS:
            return "&8» &3&lSnowy Boots &8«";
         case SWORD:
            return "&8» &3&lGlacial Edge &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      if (ArmorSetPlugin.getInstance().getPotionEffectAPI().canApplyPotionEffect(player, PotionEffectType.SPEED)) {
         player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3), true);
      }
   }

   @Override
   public void onUnEquip(Player player) {
      player.removePotionEffect(PotionEffectType.SPEED);
   }

   @Override
   public void onAttack(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      if (RandomUtil.getChance(15.0)) {
         Cooldown cooldown = this.getAbilityCooldowns().get(armorHolder.getUniqueId());
         if (cooldown == null || cooldown.isOver()) {
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
                  armorHolder, "&3&lSnowify", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(140, 180)));
            } else {
               this.sendAbilityMessage(armorHolder, "&3&lSnowify", "&7{0}", armorHolder.getName());
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(140, 180)));
               new SnowifyAbilityTask(armorHolder, 5).runTaskTimer(ArmorSetPlugin.getInstance(), 0L, 20L);
            }
         }
      }
   }

   @EventHandler
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      Entity damager = event.getDamager();
      Entity entity = event.getEntity();
      if (damager instanceof Snowball && damager.hasMetadata("blizzardSnowballs")) {
         Player summoner = Bukkit.getPlayer(((MetadataValue)damager.getMetadata("blizzardSnowballs").get(0)).asString());
         if (entity instanceof Player && summoner != null) {
            if (((Player)entity).getPlayer() == summoner) {
               return;
            }

            if (FactionUtil.isAlly(((Player)entity).getPlayer(), summoner)) {
               return;
            }

            Player hitPlayer = (Player)entity;
            hitPlayer.damage(18.0);
            hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 3));
         }
      }
   }
}
