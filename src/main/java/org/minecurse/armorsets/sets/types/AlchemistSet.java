package org.minecurse.armorsets.sets.types;

import com.rit.sucy.CustomEnchantment;
import com.rit.sucy.EnchantmentAPI;
import de.tr7zw.nbtapi.NBTItem;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.util.ActionBarUtil;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.enchants.enchant.armor.PlatemailEnchant;
import org.minecurse.enchants.enchant.armor.boots.AntiGravityEnchant;
import org.minecurse.enchants.enchant.armor.boots.JumpEnchant;
import org.minecurse.enchants.enchant.armor.boots.SpeedEnchant;
import org.minecurse.enchants.enchant.armor.helmets.DrunkEnchant;
import org.minecurse.features.FeatureManager;
import org.minecurse.features.types.crew.CrewFeature;
import org.minecurse.features.types.staritems.StarItemFeature;

@ArmorCrystal(name = "Alchemist", lore = "", outgoing = 0.0, incoming = 0.0)
public class AlchemistSet extends ArmorSet {
   private static final String ALCHEMY_KEY = "alchemyItem";
   private static final int MAX_ENERGY = 100;
   private static final int ENERGY_PER_SECOND = 1;
   private static final int EFFECT_RADIUS = 25;
   private static final long ACTIVATION_COOLDOWN_MILLIS = 5000L;
   private static final PotionEffect ALCHEMIST_SPEED = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2);
   private static final PotionEffect ALCHEMIST_STRENGTH = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2);
   private static final PotionEffect ALCHEMIST_RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1);
   private final Map<UUID, Integer> energy = new HashMap<>();
   private final Map<UUID, Long> cooldowns = new HashMap<>();

   public AlchemistSet(DefaultConfig defaultConfig) {
      super(
         "Alchemist",
         "&6&lAlchemist",
         ChatColor.GOLD,
         new ItemBuilder(Material.SUGAR),
         defaultConfig.getArmorOutgoing("alchemist"),
         defaultConfig.getArmorIncoming("alchemist"),
         0.0,
         0.0
      );
      Bukkit.getScheduler().runTaskTimer(ArmorSetPlugin.getInstance(), this::tickEnergy, 20L, 20L);
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (!armorPiece.isArmor()) {
         return null;
      }

      ItemStack itemStack = this.addNBT(
         new ItemBuilder(armorPiece.getDefaultMaterial())
            .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
            .enchantment(Enchantment.DURABILITY, 3)
            .name(this.getPieceName(armorPiece))
            .lore(
               new String[]{
                  "",
                  "&e&lALCHEMIST SET BONUS",
                  "&f• Permanent Speed III effect.",
                  "&f• Permanent Strength III effect.",
                  "&f• Permanent Resistance II effect.",
                  "&f• Grant effects to your allies",
                  "&f  using alchemy items. (/alchemist)",
                  "&7&o(Must wear all 4 pieces)",
                  " "
               }
            )
            .flag(ItemFlag.HIDE_ATTRIBUTES),
         this.getInternalName()
      );
      return ArmorSetPlugin.getInstance().getApi().makeHeroic(itemStack);
   }

   @Override
   public String getPieceName(ArmorPiece armorPiece) {
      switch (armorPiece) {
         case HELMET:
            return "&8» &6&lAlchemist Hood &8«";
         case CHESTPLATE:
            return "&8» &6&lAlchemist Robe &8«";
         case LEGGINGS:
            return "&8» &6&lAlchemist Leggings &8«";
         case BOOTS:
            return "&8» &6&lAlchemist Boots &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      this.energy.putIfAbsent(player.getUniqueId(), 0);
      this.applyAlchemistPotionEffects(player);
   }

   @Override
   public void onUnEquip(Player player) {
      this.energy.remove(player.getUniqueId());
      this.cooldowns.remove(player.getUniqueId());
      player.removePotionEffect(PotionEffectType.SPEED);
      player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
      player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
      resyncCurrentPotionSources(player, PotionEffectType.SPEED, ALCHEMIST_SPEED.getAmplifier());
      resyncCurrentPotionSources(player, PotionEffectType.INCREASE_DAMAGE, ALCHEMIST_STRENGTH.getAmplifier());
      resyncCurrentPotionSources(player, PotionEffectType.DAMAGE_RESISTANCE, ALCHEMIST_RESISTANCE.getAmplifier());
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onInteract(PlayerInteractEvent event) {
      Action action = event.getAction();
      if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
         Player player = event.getPlayer();
         ItemStack itemStack = player.getItemInHand();
         AlchemistSet.AlchemyItem alchemyItem = AlchemistSet.AlchemyItem.fromItem(itemStack);
         if (alchemyItem != null) {
            event.setCancelled(true);
            this.activateAlchemyItem(player, itemStack, alchemyItem);
         }
      }
   }

   private void activateAlchemyItem(Player player, ItemStack itemStack, AlchemistSet.AlchemyItem alchemyItem) {
      if (ArmorSetPlugin.getInstance().getArmorSetRegistry().getActiveSet(player) != this) {
         player.sendMessage(ArmorSetPlugin.getPrefix("&cYou must be wearing the full &6&lAlchemist &cset to use this.", false));
      } else if (!alchemyItem.canApplyTo(player)) {
         player.sendMessage(ArmorSetPlugin.getPrefix("&cYou can't use that Alchemist item while that potion effect is disabled.", false));
      } else {
         long cooldownLeft = this.getCooldownLeft(player);
         if (cooldownLeft > 0L) {
            player.sendMessage(
               ArmorSetPlugin.getPrefix(
                  StringUtil.format("&cYou can't use that for another {0} seconds!", new Object[]{Math.ceil(cooldownLeft / 1000.0)}), false
               )
            );
         } else {
            int currentEnergy = this.energy.getOrDefault(player.getUniqueId(), 0);
            if (currentEnergy < alchemyItem.getEnergyCost()) {
               player.sendMessage(
                  ArmorSetPlugin.getPrefix(
                     StringUtil.format(
                        "&cYou need at least {0} Alchemist energy to use this, you only have {1}!", new Object[]{alchemyItem.getEnergyCost(), currentEnergy}
                     ),
                     false
                  )
               );
            } else {
               for (Player target : PlayerUtils.getNearbyPlayers(player.getLocation(), 25.0, 25.0, 25.0)) {
                  if (target != player && FactionUtil.isAlly(player, target) && alchemyItem.canApplyTo(target)) {
                     alchemyItem.apply(target);
                  }
               }

               alchemyItem.apply(player);
               this.consumeOne(player, itemStack);
               this.energy.put(player.getUniqueId(), currentEnergy - alchemyItem.getEnergyCost());
               this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 5000L);
               player.playSound(player.getLocation(), Sound.DRINK, 1.0F, 1.0F);
               player.sendMessage(
                  ArmorSetPlugin.getPrefix(
                     StringUtil.format(
                        "&bYou just used &a{0} &bability! It cost &a{1}&b Alchemist energy, you now have &a{2}&b.",
                        new Object[]{alchemyItem.getPlainName(), alchemyItem.getEnergyCost(), currentEnergy - alchemyItem.getEnergyCost()}
                     ),
                     false
                  )
               );
            }
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      this.cleanup(event.getPlayer());
   }

   @EventHandler
   public void onKick(PlayerKickEvent event) {
      this.cleanup(event.getPlayer());
   }

   private void tickEnergy() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (ArmorSetPlugin.getInstance().getArmorSetRegistry().getActiveSet(player) == this) {
            int currentEnergy = this.energy.getOrDefault(player.getUniqueId(), 0);
            this.energy.put(player.getUniqueId(), Math.min(100, currentEnergy + 1));
            this.applyAlchemistPotionEffects(player);
            ActionBarUtil.send(player, StringUtil.format("&aYou have &e{0} &aAlchemist energy.", new Object[]{this.energy.get(player.getUniqueId())}));
         }
      }
   }

   private void applyAlchemistPotionEffects(Player player) {
      this.applyAlchemistPotionEffect(player, ALCHEMIST_SPEED);
      this.applyAlchemistPotionEffect(player, ALCHEMIST_STRENGTH);
      this.applyAlchemistPotionEffect(player, ALCHEMIST_RESISTANCE);
   }

   private void applyAlchemistPotionEffect(Player player, PotionEffect potionEffect) {
      if (ArmorSetPlugin.getInstance().getPotionEffectAPI().canApplyPotionEffect(player, potionEffect.getType())) {
         PotionEffect activeEffect = getActivePotionEffect(player, potionEffect.getType());
         if (activeEffect == null || activeEffect.getAmplifier() < potionEffect.getAmplifier() || activeEffect.getDuration() <= 40) {
            player.addPotionEffect(potionEffect, true);
         }
      }
   }

   private long getCooldownLeft(Player player) {
      long expiresAt = this.cooldowns.getOrDefault(player.getUniqueId(), 0L);
      return Math.max(0L, expiresAt - System.currentTimeMillis());
   }

   private void consumeOne(Player player, ItemStack itemStack) {
      if (itemStack.getAmount() <= 1) {
         player.setItemInHand(null);
      } else {
         itemStack.setAmount(itemStack.getAmount() - 1);
         player.setItemInHand(itemStack);
      }

      player.updateInventory();
   }

   private void cleanup(Player player) {
      this.energy.remove(player.getUniqueId());
      this.cooldowns.remove(player.getUniqueId());
   }

   private static void scheduleEnchantEffectResync(Player player, PotionEffectType effectType, int duration, int amplifier) {
      if (effectType != null) {
         Bukkit.getScheduler().runTaskLater(ArmorSetPlugin.getInstance(), () -> resyncCurrentPotionSources(player, effectType, amplifier), duration + 2L);
      }
   }

   private static void resyncCurrentPotionSources(Player player, PotionEffectType effectType, int temporaryAmplifier) {
      if (player != null && player.isOnline()) {
         PotionEffect activeEffect = getActivePotionEffect(player, effectType);
         if (activeEffect == null || activeEffect.getAmplifier() < temporaryAmplifier || activeEffect.getDuration() <= 40) {
            resyncFeaturePotionEffects(player);
            activeEffect = getActivePotionEffect(player, effectType);
            Map<CustomEnchantment, Integer> enchantments = EnchantmentAPI.getValidEnchantments(Arrays.asList(player.getInventory().getArmorContents()));

            for (Entry<CustomEnchantment, Integer> entry : enchantments.entrySet()) {
               CustomEnchantment enchantment = entry.getKey();
               if (shouldResyncForEffect(enchantment, effectType, activeEffect, entry.getValue())) {
                  applyCurrentEnchantEffect(player, enchantment, effectType, entry.getValue());
                  activeEffect = getActivePotionEffect(player, effectType);
               }
            }
         }
      }
   }

   private static void resyncFeaturePotionEffects(Player player) {
      StarItemFeature starItemFeature = (StarItemFeature)FeatureManager.getInstance().getByClass(StarItemFeature.class);
      if (starItemFeature != null) {
         starItemFeature.applyEquippedStarItemPotionEffects(player);
      }

      CrewFeature crewFeature = (CrewFeature)FeatureManager.getInstance().getByClass(CrewFeature.class);
      if (crewFeature != null) {
         crewFeature.applyPlayerCrewPotionEffects(player);
      }
   }

   private static PotionEffect getActivePotionEffect(Player player, PotionEffectType effectType) {
      for (PotionEffect potionEffect : player.getActivePotionEffects()) {
         if (potionEffect.getType().equals(effectType)) {
            return potionEffect;
         }
      }

      return null;
   }

   private static boolean shouldResyncForEffect(CustomEnchantment enchantment, PotionEffectType effectType, PotionEffect activeEffect, int level) {
      int amplifier = getEnchantAmplifier(enchantment, effectType, level);
      return amplifier < 0
         ? false
         : activeEffect == null || activeEffect.getAmplifier() < amplifier || activeEffect.getAmplifier() == amplifier && activeEffect.getDuration() < 40;
   }

   private static void applyCurrentEnchantEffect(Player player, CustomEnchantment enchantment, PotionEffectType effectType, int level) {
      if (enchantment instanceof DrunkEnchant && effectType.equals(PotionEffectType.INCREASE_DAMAGE)) {
         player.addPotionEffect(
            new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, getEnchantAmplifier(enchantment, effectType, level)), true
         );
      } else if (enchantment instanceof PlatemailEnchant && effectType.equals(PotionEffectType.DAMAGE_RESISTANCE)) {
         player.addPotionEffect(
            new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, getEnchantAmplifier(enchantment, effectType, level)), true
         );
      } else {
         enchantment.applyEquipEffect(player, level);
      }
   }

   private static int getEnchantAmplifier(CustomEnchantment enchantment, PotionEffectType effectType, int level) {
      if (effectType.equals(PotionEffectType.SPEED) && enchantment instanceof SpeedEnchant) {
         return level - 1;
      }

      if (effectType.equals(PotionEffectType.JUMP) && enchantment instanceof JumpEnchant) {
         return level - 1;
      }

      if (effectType.equals(PotionEffectType.JUMP) && enchantment instanceof AntiGravityEnchant) {
         if (level == 2) {
            return 4;
         } else {
            return level == 3 ? 5 : 3;
         }
      } else if (effectType.equals(PotionEffectType.INCREASE_DAMAGE) && enchantment instanceof DrunkEnchant) {
         return level == 1 ? 0 : 1;
      } else if (!effectType.equals(PotionEffectType.DAMAGE_RESISTANCE) || !(enchantment instanceof PlatemailEnchant)) {
         return -1;
      } else {
         return level == 3 ? 1 : 0;
      }
   }

   public enum AlchemyItem {
      RESISTANCE_FIGHTER(
         "Resistance Fighter",
         Material.IRON_INGOT,
         2000,
         30,
         PotionEffectType.DAMAGE_RESISTANCE,
         200,
         2,
         "&fProvide allies with Resistance III for",
         "&f10 seconds."
      ),
      HEART_HEALER("Heart Healer", Material.SPECKLED_MELON, 2000, 40, null, 0, 0, "&fProvide allies with a heal of 6 &c❤&f."),
      STRENGTH_PROVIDER(
         "Strength Provider",
         Material.BLAZE_POWDER,
         2000,
         40,
         PotionEffectType.INCREASE_DAMAGE,
         200,
         2,
         "&fProvide allies with Strength III for",
         "&f10 seconds."
      ),
      LIFTING_PAL("Lifting Pal", Material.FEATHER, 1000, 20, PotionEffectType.JUMP, 200, 4, "&fProvide allies with Jump V for 10", "&fseconds."),
      SWIFT_SUPPORT("Swift Support", Material.SUGAR, 2000, 30, PotionEffectType.SPEED, 200, 3, "&fProvide allies with Speed IV for", "&f10 seconds.");

      private final String plainName;
      private final Material material;
      private final int xpCost;
      private final int energyCost;
      private final PotionEffectType effectType;
      private final int duration;
      private final int amplifier;
      private final String[] description;

      AlchemyItem(
         String plainName, Material material, int xpCost, int energyCost, PotionEffectType effectType, int duration, int amplifier, String... description
      ) {
         this.plainName = plainName;
         this.material = material;
         this.xpCost = xpCost;
         this.energyCost = energyCost;
         this.effectType = effectType;
         this.duration = duration;
         this.amplifier = amplifier;
         this.description = description;
      }

      public ItemStack buildShopItem() {
         ItemBuilder builder = new ItemBuilder(this.material).name("&e&l" + this.plainName).lore(" ");

         for (String line : this.description) {
            builder.lore(line);
         }

         builder.lore(new String[]{" ", "&6&lCOST &f" + this.xpCost + " XP", " "});
         return builder.flag(ItemFlag.HIDE_ATTRIBUTES);
      }

      public ItemStack buildUsableItem() {
         ItemBuilder builder = new ItemBuilder(this.material).name("&e&l" + this.plainName).lore(" ");

         for (String line : this.description) {
            builder.lore(line);
         }

         builder.lore(new String[]{" ", "&6&lCOST &f" + this.energyCost + " Alchemist Energy", " "});
         NBTItem item = new NBTItem(builder.flag(ItemFlag.HIDE_ATTRIBUTES));
         item.setString("alchemyItem", this.name());
         return item.getItem();
      }

      public void apply(Player player) {
         if (this == HEART_HEALER) {
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 12.0));
         } else if (this.canApplyTo(player)) {
            player.addPotionEffect(new PotionEffect(this.effectType, this.duration, this.amplifier), true);
            AlchemistSet.scheduleEnchantEffectResync(player, this.effectType, this.duration, this.amplifier);
         }
      }

      public boolean canApplyTo(Player player) {
         return this.effectType == null || ArmorSetPlugin.getInstance().getPotionEffectAPI().canApplyPotionEffect(player, this.effectType);
      }

      public static AlchemistSet.AlchemyItem fromItem(ItemStack itemStack) {
         if (itemStack != null && itemStack.getType() != Material.AIR) {
            NBTItem item = new NBTItem(itemStack);
            String value = item.getString("alchemyItem");
            if (value != null && !value.isEmpty()) {
               try {
                  return valueOf(value);
               } catch (IllegalArgumentException ignored) {
                  return null;
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      }

      public String getPlainName() {
         return this.plainName;
      }

      public int getXpCost() {
         return this.xpCost;
      }

      public int getEnergyCost() {
         return this.energyCost;
      }
   }
}
