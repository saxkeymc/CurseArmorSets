package org.minecurse.armorsets.sets.types;

import com.rit.sucy.EnchantmentAPI;
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
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.util.ColorUtil;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.EffectUtil;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;

@ArmorCrystal(name = "Goblin", lore = "", outgoing = 2.5, incoming = 2.5)
public class OrcSet extends ArmorSet {
   public OrcSet(DefaultConfig defaultConfig) {
      super(
         "Goblin",
         "&b&lGoblin",
         ChatColor.AQUA,
         new ItemBuilder(Material.EMERALD),
         defaultConfig.getArmorOutgoing("goblin"),
         defaultConfig.getArmorIncoming("goblin"),
         5.0,
         0.0
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
               " ",
               "&b&lEffects:",
               "&f • Deal 10% more durability damage.",
               "&f • Deal 5% more damage to players holding axes.",
               ""
            });
         return this.addNBT(builder, this.getInternalName());
      } else {
         return this.addNBT(
            new ItemBuilder(armorPiece.getDefaultMaterial())
               .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
               .enchantment(Enchantment.DURABILITY, 3)
               .name(ColorUtil.translate(this.getPieceName(armorPiece)))
               .lore(
                  new String[]{
                     " ",
                     "&7&oBecome enraged like a beast while wearing",
                     "&7&othis set for those who play both passive",
                     "&7&odealers!",
                     "",
                     "&b&lEffects:",
                     "&f • Deal an extra 10% damage to all enemies.",
                     "&f • Enjoy a 10% damage reduction.",
                     "",
                     "&b&lAbility:",
                     "&fWhile wearing this set, you can become",
                     "&fEnraged and recieve a great amount of",
                     "&fstrength for a limited time",
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
            return "§8» §b§lGoblin Headgear §8«";
         case CHESTPLATE:
            return "§8» §b§lBloody Goblin Torso §8«";
         case LEGGINGS:
            return "§8» §b§lFuzzy Goblin Legs §8«";
         case BOOTS:
            return "§8» §b§lBig-Goblin Boots §8«";
         case AXE:
            return "§8» §b§lGoblin Axe §8«";
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
                  armorHolder, "&b&lGoblin Enrage", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(60, 80)));
            } else {
               this.sendAbilityMessage(armorHolder, "&b&lGoblin Enrage", "{0}", armorHolder.getName());
               EffectUtil.applyEffect(armorHolder, new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 2));
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(60, 80)));
            }
         }
      }
   }
}
