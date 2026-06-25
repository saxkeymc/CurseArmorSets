package org.minecurse.armorsets.sets.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.runnable.RunnableBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;

@ArmorCrystal(name = "Wraith", lore = "", incoming = 3.75, outgoing = 5.0, abilityChance = 0.25)
public class WraithSet extends ArmorSet {
   public WraithSet(DefaultConfig defaultConfig) {
      super(
         "Wraith",
         "&d&lWraith",
         ChatColor.LIGHT_PURPLE,
         new ItemStack(Material.ENDER_PORTAL_FRAME),
         defaultConfig.getArmorOutgoing("wraith"),
         defaultConfig.getArmorIncoming("wraith"),
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
                  .lore(new String[]{"", "&d&lEffects:", "&f • Deal an extra 5% damage.", ""}),
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
                        "&7&oA versatile armorset that holds the",
                        "&7&ocapability of keeping you protected",
                        "&7&owhile having a form of aggression.",
                        "&7&oUtilize the power of the Void to your",
                        "&7&oadvantage.",
                        "",
                        "&d&lEffects:",
                        "&f • Permanent Speed IV effect.",
                        "&f • Deal an extra 20% damage.",
                        "&f • Enjoy a 15% damage reduction.",
                        "&f • Dimensional Rift ability.",
                        "",
                        "&d&lAbility:",
                        "&fDimensional Rift ability &7&o(Chance to go invisible and deal 2x damage)",
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
            return "&8» &d&lGlasses of Travel &8«";
         case CHESTPLATE:
            return "&8» &d&lCompressed Shield &8«";
         case LEGGINGS:
            return "&8» &d&lObsidian Plated Leggings &8«";
         case BOOTS:
            return "&8» &d&lAnkle Weighted Boots &8«";
         case SWORD:
            return "&8» &d&lVoider &8«";
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
                  armorHolder, "&d&lDimensional Rift", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(120, 160)));
            } else {
               armorHolder.getWorld().playSound(armorHolder.getLocation(), Sound.ENDERDRAGON_DEATH, 5.0F, 1.0F);
               this.sendAbilityMessage(armorHolder, "&d&lDimensional Rift", "{0}", armorHolder.getName());
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(160, 180)));
               armorHolder.setMetadata("wraithRift", new FixedMetadataValue(ArmorSetPlugin.getInstance(), true));
               Collection<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
               players.removeIf(player -> player.hasMetadata("warden") || player.hasMetadata("blindfoldStarItem"));
               players.removeIf(player -> player == armorHolder);
               players.forEach(player -> player.hidePlayer(armorHolder));
               RunnableBuilder.bind(() -> {
                  players.stream().filter(player -> !player.hasMetadata("vanish")).forEach(player -> player.showPlayer(armorHolder));
                  armorHolder.removeMetadata("wraithRift", ArmorSetPlugin.getInstance());
               }).runAsyncLater(RandomUtil.getRandInt(100, 120));
            }
         }
      }
   }

   @EventHandler
   public void onDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager().hasMetadata("wraithRift")) {
         event.setDamage(event.getDamage() * 1.6);
      }
   }
}
