package org.minecurse.armorsets.sets.types;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.abilities.loversability.LoversManager;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.util.ChanceUtil;
import org.minecurse.commons.item.ItemBuilder;

@ArmorCrystal
public class LoverSet extends ArmorSet {
   private final DefaultConfig defaultConfig;
   private final ArmorSetPlugin plugin;
   private final Map<UUID, BukkitTask> regenDisabledPlayers = new HashMap<>();

   public LoverSet(DefaultConfig defaultConfig, ArmorSetPlugin plugin) {
      super(
         "Lovers",
         "&d&lL&f&lo&d&lv&f&le&d&lr",
         ChatColor.LIGHT_PURPLE,
         new ItemStack(Material.REDSTONE, 1, (short)0),
         defaultConfig.getArmorOutgoing("lovers"),
         defaultConfig.getArmorIncoming("lovers"),
         5.0,
         0.0
      );
      this.defaultConfig = defaultConfig;
      this.plugin = plugin;
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
                  .lore(new String[]{"", "&d&lEffects", " &fEnjoy a 5% damage reduction.", ""}),
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
                        "&d&lEffects",
                        " &f• Deal an extra 15% more damage.",
                        "&f • Enjoy a 10% damage reduction from all enemies.",
                        "&f • Time Love Ability",
                        "&f • Chance to disable opponents regen.",
                        "",
                        "&d&lAbility:",
                        "&fTime Love ability &7&o(Chance to gain more hearts during combat)",
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
            return "&8» &d&lLover &f&lEyes &8«";
         case CHESTPLATE:
            return "&8» &d&lFull &f&lHearted &8«";
         case LEGGINGS:
            return "&8» &d&lHardened &f&lLover Legs &8«";
         case BOOTS:
            return "&8» &d&lValentine &f&lDunk &d&lLows &8«";
         case SWORD:
            return "&8» &d&lH&f&le&d&la&f&lr&d&lt&f&ll&d&le&f&ls&d&ls &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onDamaged(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      if (ChanceUtil.doChance(this.defaultConfig.getLoversActivationChance())) {
         LoversManager loversManager = this.plugin.getAbilityManager().getLoversManager();
         loversManager.activateLovers(armorHolder.getUniqueId());
      }

      if (attacked instanceof Player) {
         if (ChanceUtil.doChance(50)) {
            this.regenDisableAbility(armorHolder.getUniqueId(), attacked.getUniqueId());
         }
      }
   }

   private void regenDisableAbility(UUID playerUUID, UUID targetUUID) {
      if (Bukkit.getPlayer(targetUUID) != null
         && Bukkit.getPlayer(targetUUID).isOnline()
         && Bukkit.getPlayer(playerUUID) != null
         && Bukkit.getPlayer(playerUUID).isOnline()) {
         Player targetPlayer = Bukkit.getPlayer(targetUUID);
         if (targetPlayer.hasPotionEffect(PotionEffectType.REGENERATION)) {
            int potionAmplifier = targetPlayer.getPotionEffect(PotionEffectType.REGENERATION).getAmplifier();
            int potionDuration = targetPlayer.getPotionEffect(PotionEffectType.REGENERATION).getDuration();
            targetPlayer.removePotionEffect(PotionEffectType.REGENERATION);
            Player player = Bukkit.getPlayer(playerUUID);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l*&d&l*&f&l* &d&lREGEN DISABLED &f&l*&d&l*&f&l*"));
            player.playSound(player.getLocation(), Sound.ANVIL_LAND, 7.0F, 9.0F);
            if (!this.regenDisabledPlayers.containsKey(targetUUID)) {
               this.regenDisabledPlayers.put(targetUUID, Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                  if (targetPlayer != null && targetPlayer.isOnline()) {
                     targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, potionDuration, potionAmplifier));
                     this.regenDisabledPlayers.remove(targetPlayer.getUniqueId());
                  }
               }, 100L));
            }
         }
      }
   }
}
