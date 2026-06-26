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

@ArmorCrystal(name = "Goliath", lore = "", outgoing = 10.0, incoming = 5.0, abilityChance = 5.0)
public class GoliathSet extends ArmorSet {
   /**
    * Tracks the epoch-millis when Goliath Outrage expires for each player.
    * While active, every attack the player makes deals an additional 10% damage
    * (on top of the set's base +10% outgoing bonus).
    */
   private final Map<UUID, Long> outrageExpiry = new HashMap<>();

   public GoliathSet(DefaultConfig defaultConfig) {
      super(
         "Goliath",
         "&9&lGoliath",
         ChatColor.BLUE,
         new ItemBuilder(Material.PRISMARINE_SHARD),
         defaultConfig.getArmorOutgoing("goliath"),
         defaultConfig.getArmorIncoming("goliath"),
         0.0,
         0.0
      );
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.SWORD) {
         // ── Goliath's Scythe (Diamond Sword) ─────────────────────────────
         return this.addNBT(
            new ItemBuilder(armorPiece.getDefaultMaterial())
               .enchantment(Enchantment.DAMAGE_ALL, 5)
               .enchantment(Enchantment.DURABILITY, 3)
               .name(ColorUtil.translate(this.getPieceName(armorPiece)))
               .lore(new String[]{
                  "",
                  "&9&lEffects:",
                  "&f • Deal an extra 5% damage.",
                  ""
               }),
            this.getInternalName()
         );
      } else if (armorPiece == ArmorPiece.AXE || armorPiece == ArmorPiece.BOW) {
         return null;
      } else {
         // ── Armor pieces (Helmet / Chestplate / Leggings / Boots) ────────
         return this.addNBT(
            new ItemBuilder(armorPiece.getDefaultMaterial())
               .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
               .enchantment(Enchantment.DURABILITY, 3)
               .name(ColorUtil.translate(this.getPieceName(armorPiece)))
               .lore(
                  new String[]{
                     "",
                     "&7&oBecome an unstoppable titan while wearing",
                     "&7&othis set, crushing your enemies with",
                     "&7&ooverwhelming strength!",
                     "",
                     "&9&lEffects:",
                     "&f • Deal an extra 5% damage.",
                     "&f • Enjoy a 5% damage reduction.",
                     "",
                     "&9&lAbility:",
                     "&fWhile wearing this set, you can unleash",
                     "&fGoliath Outrage, greatly increasing your",
                     "&fstrength for 10 seconds.",
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
            return "&8» &9&lGoliath Headgear &8«";
         case CHESTPLATE:
            return "&8» &9&lGoliath Chestplate &8«";
         case LEGGINGS:
            return "&8» &9&lGoliath Leggings &8«";
         case BOOTS:
            return "&8» &9&lGoliath Boots &8«";
         case SWORD:
            return "&8» &9&lGoliath's Scythe &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onAttack(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      // ── Apply active Outrage bonus ──────────────────────────────────────
      // If Outrage is currently active, deal an additional 10% damage on top
      // of the base outgoing bonus that was already applied by the listener.
      Long expiry = this.outrageExpiry.get(armorHolder.getUniqueId());
      if (expiry != null && expiry > System.currentTimeMillis()) {
         double currentBase = event.getDamage(DamageModifier.BASE);
         event.setDamage(DamageModifier.BASE, currentBase * 1.10);
      } else if (expiry != null) {
         // Expired — clean up the stale entry.
         this.outrageExpiry.remove(armorHolder.getUniqueId());
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
                  armorHolder, "&9&lGoliath Outrage", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(60, 80)));
            } else {
               this.procOutrage(armorHolder, attacked);
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(60, 80)));
            }
         }
      }
   }

   /**
    * Activate Goliath Outrage — for the next 10 seconds, every attack the
    * player makes deals an additional 10% damage. Also applies a brief
    * Strength potion effect as visual feedback.
    */
   private void procOutrage(Player player, LivingEntity target) {
      this.sendAbilityMessage(player, "&9&lGoliath Outrage", "{0}", player.getName());
      this.outrageExpiry.put(player.getUniqueId(), System.currentTimeMillis() + 10000L);
      // Visual feedback — Strength II for 10 seconds (200 ticks).
      player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1), true);

      // Schedule cleanup of the buff entry after 10 seconds so the map
      // doesn't grow unbounded.
      ArmorSetPlugin.getInstance().getServer().getScheduler().runTaskLater(
         ArmorSetPlugin.getInstance(),
         () -> {
            Long exp = this.outrageExpiry.get(player.getUniqueId());
            if (exp != null && exp <= System.currentTimeMillis()) {
               this.outrageExpiry.remove(player.getUniqueId());
            }
         },
         220L // 11 seconds — slightly after expiry to be safe
      );
   }
}
