package org.minecurse.armorsets.sets.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.util.ColorUtil;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;

@ArmorCrystal(name = "Haven", lore = "", outgoing = 5.0, incoming = 10.0, abilityChance = 5.0)
public class HavenSet extends ArmorSet {
   /**
    * Tracks the epoch-millis when Haven Blessing expires for each player.
    * While active, the player takes an additional 5% less damage (on top of
    * the set's base 10% incoming damage reduction).
    */
   private final Map<UUID, Long> blessingExpiry = new HashMap<>();

   public HavenSet(DefaultConfig defaultConfig) {
      super(
         "Haven",
         "&5&lHaven",
         ChatColor.DARK_PURPLE,
         new ItemBuilder(Material.DIAMOND_CHESTPLATE),
         defaultConfig.getArmorOutgoing("haven"),
         defaultConfig.getArmorIncoming("haven"),
         0.0,
         0.0
      );
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      // Haven is armor-only — no sword, axe, or bow.
      if (armorPiece == ArmorPiece.SWORD || armorPiece == ArmorPiece.AXE || armorPiece == ArmorPiece.BOW) {
         return null;
      } else {
         return this.addNBT(
            new ItemBuilder(armorPiece.getDefaultMaterial())
               .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
               .enchantment(Enchantment.DURABILITY, 3)
               .name(ColorUtil.translate(this.getPieceName(armorPiece)))
               .lore(
                  new String[]{
                     "",
                     "&7&oBecome a guardian of life while wearing",
                     "&7&othis set, protecting yourself and surviving",
                     "&7&oeven the fiercest battles.",
                     "",
                     "&5&lEffects:",
                     "&f • Deal an extra 5% damage to all enemies.",
                     "&f • Enjoy a 10% damage reduction.",
                     "",
                     "&5&lAbility:",
                     "&fWhile wearing this set, you can activate",
                     "&fHaven Blessing, instantly restoring yourself",
                     "&fto full health while gaining additional",
                     "&fdamage reduction for 10 seconds.",
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
            return "&8» &5&lHaven Headgear &8«";
         case CHESTPLATE:
            return "&8» &5&lHaven Chestplate &8«";
         case LEGGINGS:
            return "&8» &5&lHaven Leggings &8«";
         case BOOTS:
            return "&8» &5&lHaven Boots &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onDamaged(Player armorHolder, LivingEntity damager, EntityDamageByEntityEvent event) {
      // ── Apply active Blessing bonus ─────────────────────────────────────
      // If Blessing is currently active, take an additional 5% less damage
      // on top of the base incoming reduction that was already applied by
      // the listener.
      Long expiry = this.blessingExpiry.get(armorHolder.getUniqueId());
      if (expiry != null && expiry > System.currentTimeMillis()) {
         double currentBase = event.getDamage(DamageModifier.BASE);
         event.setDamage(DamageModifier.BASE, currentBase * 0.95);
      } else if (expiry != null) {
         // Expired — clean up the stale entry.
         this.blessingExpiry.remove(armorHolder.getUniqueId());
      }

      // ── Roll for ability trigger ────────────────────────────────────────
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
                  armorHolder, "&5&lHaven Blessing", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(60, 80)));
            } else {
               this.procBlessing(armorHolder, damager);
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(60, 80)));
            }
         }
      }
   }

   /**
    * Activate Haven Blessing — instantly heal the player to full HP and grant
    * an additional 5% incoming damage reduction for the next 10 seconds.
    * Also applies a brief Resistance potion effect as visual feedback.
    */
   private void procBlessing(Player player, LivingEntity damager) {
      this.sendAbilityMessage(player, "&5&lHaven Blessing", "{0}", player.getName());

      // Instantly heal to full HP.
      player.setHealth(player.getMaxHealth());

      // Activate the 10-second damage reduction buff.
      this.blessingExpiry.put(player.getUniqueId(), System.currentTimeMillis() + 10000L);

      // Visual feedback — Resistance I for 10 seconds (200 ticks).
      player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 0), true);

      // Schedule cleanup of the buff entry after 10 seconds so the map
      // doesn't grow unbounded.
      ArmorSetPlugin.getInstance().getServer().getScheduler().runTaskLater(
         ArmorSetPlugin.getInstance(),
         () -> {
            Long exp = this.blessingExpiry.get(player.getUniqueId());
            if (exp != null && exp <= System.currentTimeMillis()) {
               this.blessingExpiry.remove(player.getUniqueId());
            }
         },
         220L // 11 seconds — slightly after expiry to be safe
      );
   }
}
