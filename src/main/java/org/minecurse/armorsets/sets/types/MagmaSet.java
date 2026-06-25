package org.minecurse.armorsets.sets.types;

import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.struct.task.MagmaAbilityTask;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;

@ArmorCrystal(name = "Magma", lore = "", outgoing = 5.0, incoming = 5.0)
public class MagmaSet extends ArmorSet {
   public MagmaSet(DefaultConfig defaultConfig) {
      super(
         "Magma",
         "&4&lMagma",
         ChatColor.DARK_RED,
         new ItemBuilder(Material.MAGMA_CREAM),
         defaultConfig.getArmorOutgoing("magma"),
         defaultConfig.getArmorIncoming("magma"),
         10.0,
         0.0
      );
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.BOW || armorPiece == ArmorPiece.AXE) {
         return null;
      } else if (armorPiece == ArmorPiece.SWORD) {
         ItemBuilder itemBuilder = new ItemBuilder(armorPiece.getDefaultMaterial())
            .enchantment(Enchantment.DAMAGE_ALL, 5)
            .enchantment(Enchantment.DURABILITY, 3)
            .name(this.getPieceName(armorPiece))
            .lore(new String[]{"", "&4&lEffects: ", "&f • Deal an extra 10% damage to all enemies.", ""});
         return this.addNBT(itemBuilder, this.getInternalName());
      } else {
         return this.addNBT(
            new ItemBuilder(armorPiece.getDefaultMaterial())
               .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
               .enchantment(Enchantment.DURABILITY, 3)
               .name(this.getPieceName(armorPiece))
               .lore(
                  new String[]{
                     "",
                     "&7&oThe magmas were once superior in",
                     "&7&oall the land. Now gone, their remains",
                     "&7&oare used in the power of this set!",
                     "",
                     "&4&lEffects: ",
                     "&f • Permanent Speed IV effect.",
                     "&f • Deal an extra 20% damage to all enemies.",
                     "&f • Enjoy a 20% damage reduction from enemies.",
                     "&f • Take 30% more damage from bows.",
                     "",
                     "&4&lAbility:",
                     "&fVolcanic Barrier Ability &7&o(Summon a damaging veil of flames)",
                     ""
                  }
               ),
            this.getInternalName()
         );
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
   public String getPieceName(ArmorPiece armorPiece) {
      switch (armorPiece) {
         case HELMET:
            return "&8» &4&lMagma Crown &8«";
         case CHESTPLATE:
            return "&8» &4&lMagma Breastplate &8«";
         case LEGGINGS:
            return "&8» &4&lMagma Trousers &8«";
         case BOOTS:
            return "&8» &4&lMagma Sandals &8«";
         case SWORD:
            return "&8» &4&lMagma Fanny Pack &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onDamaged(Player armorHolder, LivingEntity damager, EntityDamageByEntityEvent event) {
      if (RandomUtil.getChance(5.0)) {
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
                  armorHolder, "&4&lVolcanic Barrier", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(140, 180)));
            } else {
               this.sendAbilityMessage(armorHolder, "&4&lVolcanic Barrier", "{0}", armorHolder.getName());
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(140, 180)));
               new MagmaAbilityTask(armorHolder, 7).runTaskTimerAsynchronously(ArmorSetPlugin.getInstance(), 10L, 10L);
            }
         }
      }
   }
}
