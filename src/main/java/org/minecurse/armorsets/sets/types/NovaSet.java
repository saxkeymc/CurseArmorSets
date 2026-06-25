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
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;

@ArmorCrystal
public class NovaSet extends ArmorSet {
   public NovaSet(DefaultConfig defaultConfig) {
      super(
         "Nova",
         "&d&lNova",
         ChatColor.LIGHT_PURPLE,
         new ItemStack(Material.DOUBLE_PLANT, 1, (short)5),
         defaultConfig.getArmorOutgoing("nova"),
         defaultConfig.getArmorIncoming("nova"),
         5.0,
         0.0
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
                  .lore(new String[]{"", "&d&lEffects:", "&f • Deal an extra 5% damage to all enemies.", ""}),
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
                        "&7&oThis legendary armor set is the pinnacle of",
                        "&7&oprotection, revered for its unmatched power",
                        "&7&oand resilience, commanding respect and fear",
                        "&7&oacross the cosmos.",
                        "",
                        "&d&lEffects:",
                        "&f • Deal an extra 25% more damage to all enemies.",
                        "&f • Enjoy a 20% damage reduction from all enemies.",
                        "&f • Permanent Speed IV effect.",
                        "&f • Galaxy Rot ability.",
                        "",
                        "&d&lAbility:",
                        "&fGalaxy Rot ability &7&o(Rot the planet below by summon some purple goo)",
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
            return "&8» &d&lNova Cap &8«";
         case CHESTPLATE:
            return "&8» &d&lNova Vest &8«";
         case LEGGINGS:
            return "&8» &d&lNova Greaves &8«";
         case BOOTS:
            return "&8» &d&lNova Footwraps &8«";
         case SWORD:
            return "&8» &d&lNova Knife &8«";
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
                  armorHolder, "&d&lGalaxy Rot", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(140, 180)));
            } else {
               this.sendAbilityMessage(armorHolder, "&d&lGalaxy Rot", "&7{0}", armorHolder.getName());
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(140, 180)));
               ArmorSetPlugin.getInstance().getAbilityManager().getRotManager().activateRot(armorHolder.getUniqueId());
            }
         }
      }
   }
}
