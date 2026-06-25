package org.minecurse.armorsets.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.minecurse.armorsets.ArmorSetPlugin;

public class DefaultConfig {
   private final ArmorSetPlugin plugin;
   private int loversActivationChance;
   private double loversMaxHearts;
   private double loversHeartsPerProc;
   private int loversHeartProcChance;
   private int loversAbilityTime;
   private final Map<String, DefaultConfig.CachedArmorSetValues> armorSetCache = new HashMap<>();
   private double heroicArmorPerPiece;
   private double rainbowArmorPerPiece;
   private double heroicWeaponBonus;
   private double diamondHookMultiplier;
   private double heroicHookMultiplier;

   public int getLoversActivationChance() {
      return this.loversActivationChance;
   }

   public double getLoversMaxHearts() {
      return this.loversMaxHearts;
   }

   public double getLoversHeartsPerProc() {
      return this.loversHeartsPerProc;
   }

   public int getLoversHeartProcChance() {
      return this.loversHeartProcChance;
   }

   public int getLoversAbilityTime() {
      return this.loversAbilityTime;
   }

   public DefaultConfig(ArmorSetPlugin plugin) {
      this.plugin = plugin;
      this.loadAll();
   }

   private void loadAll() {
      FileConfiguration config = this.plugin.getConfig();
      this.loversActivationChance = config.getInt("ability-settings.lovers-ability.lovers-activation-chance");
      this.loversMaxHearts = config.getDouble("ability-settings.lovers-ability.max-extra-hearts");
      this.loversHeartsPerProc = config.getDouble("ability-settings.lovers-ability.hearts-per-proc");
      this.loversHeartProcChance = config.getInt("ability-settings.lovers-ability.proc-chance");
      this.loversAbilityTime = config.getInt("ability-settings.lovers-ability.lovers-time");
      this.armorSetCache.clear();
      ConfigurationSection section = config.getConfigurationSection("armorset-settings");
      if (section != null) {
         for (String key : section.getKeys(false)) {
            String lower = key.toLowerCase();
            double outgoing = section.getDouble(key + ".outgoing", 0.0);
            double incoming = section.getDouble(key + ".incoming", 0.0);
            boolean hidden = section.getBoolean(key + ".hidden", false);
            this.armorSetCache.put(lower, new DefaultConfig.CachedArmorSetValues(outgoing, incoming, hidden));
         }
      }

      this.heroicArmorPerPiece = config.getDouble("misc-settings.heroic-armor-per-piece", 2.5);
      this.rainbowArmorPerPiece = config.getDouble("misc-settings.rainbow-armor-per-piece", 3.75);
      this.heroicWeaponBonus = config.getDouble("misc-settings.heroic-weapon-bonus", 27.5);
      this.diamondHookMultiplier = config.getDouble("misc-settings.diamond-hook-bonus", 50.0);
      this.heroicHookMultiplier = config.getDouble("misc-settings.heroic-hook-bonus", 70.0);
   }

   public void reload() {
      this.plugin.reloadConfig();
      this.loadAll();
   }

   public double getHeroicArmorPerPiece() {
      return this.heroicArmorPerPiece;
   }

   public double getRainbowArmorPerPiece() {
      return this.rainbowArmorPerPiece;
   }

   public double getHeroicWeaponBonus() {
      return this.heroicWeaponBonus;
   }

   public double getDiamondHookMultiplier() {
      return this.diamondHookMultiplier;
   }

   public double getHeroicHookMultiplier() {
      return this.heroicHookMultiplier;
   }

   public void setMiscValue(String configKey, double value) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("misc-settings." + configKey, value);
      this.plugin.saveConfig();
      this.heroicArmorPerPiece = config.getDouble("misc-settings.heroic-armor-per-piece", 2.5);
      this.rainbowArmorPerPiece = config.getDouble("misc-settings.rainbow-armor-per-piece", 3.75);
      this.heroicWeaponBonus = config.getDouble("misc-settings.heroic-weapon-bonus", 27.5);
      this.diamondHookMultiplier = config.getDouble("misc-settings.diamond-hook-bonus", 50.0);
      this.heroicHookMultiplier = config.getDouble("misc-settings.heroic-hook-bonus", 70.0);
   }

   public double getArmorOutgoing(String armorName) {
      DefaultConfig.CachedArmorSetValues values = this.armorSetCache.get(armorName.toLowerCase());
      return values != null ? values.outgoing : 0.0;
   }

   public double getArmorIncoming(String armorName) {
      DefaultConfig.CachedArmorSetValues values = this.armorSetCache.get(armorName.toLowerCase());
      return values != null ? values.incoming : 0.0;
   }

   public void setArmorValues(String armorName, double incoming, double outgoing) {
      String lower = armorName.toLowerCase();
      DefaultConfig.CachedArmorSetValues existing = this.armorSetCache.get(lower);
      boolean hidden = existing != null && existing.isHidden();
      this.armorSetCache.put(lower, new DefaultConfig.CachedArmorSetValues(outgoing, incoming, hidden));
      FileConfiguration config = this.plugin.getConfig();
      config.set("armorset-settings." + lower + ".incoming", incoming);
      config.set("armorset-settings." + lower + ".outgoing", outgoing);
      this.plugin.saveConfig();
   }

   public boolean isHidden(String armorName) {
      DefaultConfig.CachedArmorSetValues values = this.armorSetCache.get(armorName.toLowerCase());
      return values != null && values.isHidden();
   }

   public void setHidden(String armorName, boolean hidden) {
      String lower = armorName.toLowerCase();
      DefaultConfig.CachedArmorSetValues existing = this.armorSetCache.get(lower);
      if (existing != null) {
         this.armorSetCache.put(lower, new DefaultConfig.CachedArmorSetValues(existing.getOutgoing(), existing.getIncoming(), hidden));
         FileConfiguration config = this.plugin.getConfig();
         if (hidden) {
            config.set("armorset-settings." + lower + ".hidden", true);
         } else {
            config.set("armorset-settings." + lower + ".hidden", null);
         }

         this.plugin.saveConfig();
      }
   }

   public Set<String> getArmorSetNames() {
      return Collections.unmodifiableSet(this.armorSetCache.keySet());
   }

   public Map<String, DefaultConfig.CachedArmorSetValues> getCachedValues() {
      return Collections.unmodifiableMap(this.armorSetCache);
   }

   public static class CachedArmorSetValues {
      private final double outgoing;
      private final double incoming;
      private final boolean hidden;

      public CachedArmorSetValues(double outgoing, double incoming, boolean hidden) {
         this.outgoing = outgoing;
         this.incoming = incoming;
         this.hidden = hidden;
      }

      public double getOutgoing() {
         return this.outgoing;
      }

      public double getIncoming() {
         return this.incoming;
      }

      public boolean isHidden() {
         return this.hidden;
      }
   }
}
