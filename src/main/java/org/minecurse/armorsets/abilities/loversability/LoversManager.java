package org.minecurse.armorsets.abilities.loversability;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.configuration.PlayerData;

public class LoversManager {
   private final Map<UUID, BukkitTask> loversPlayersTask = new HashMap<>();
   private final ArmorSetPlugin plugin;

   public LoversManager(ArmorSetPlugin plugin) {
      this.plugin = plugin;
   }

   public void activateLovers(UUID uuid) {
      if (Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline()) {
         if (!this.hasLovers(uuid)) {
            Player player = Bukkit.getPlayer(uuid);
            player.sendMessage(this.color(this.plugin.getConfig().getString("ability-settings.lovers-ability.lovers-msg")));
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 10.0F, 7.0F);
            DefaultConfig defaultConfig = this.plugin.getDefaultConfig();
            PlayerData playerData = this.plugin.getPlayerData();
            FileConfiguration playerConfig = playerData.getPlayerConfig(uuid);
            playerConfig.set("lovers-ability", 0.0);
            playerData.saveConfig(uuid, playerConfig);
            this.addLoversHearts(uuid);
            this.loversPlayersTask
               .put(uuid, Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.removeLovers(uuid, false), 20L * defaultConfig.getLoversAbilityTime()));
         }
      }
   }

   protected void addLoversHearts(UUID uuid) {
      if (Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline()) {
         if (this.hasLovers(uuid)) {
            Player player = Bukkit.getPlayer(uuid);
            DefaultConfig defaultConfig = this.plugin.getDefaultConfig();
            PlayerData playerData = this.plugin.getPlayerData();
            FileConfiguration playerConfig = playerData.getPlayerConfig(uuid);
            double heartsPer = defaultConfig.getLoversHeartsPerProc();
            double maxHearts = defaultConfig.getLoversMaxHearts();
            double newHearts = Math.min(maxHearts, this.getLoversHearts(uuid) + heartsPer);
            if (this.getLoversHearts(uuid) != maxHearts) {
               PotionEffect potionEffect = null;
               double healthBeforePotion = player.getHealth();
               if (player.hasPotionEffect(PotionEffectType.HEALTH_BOOST)) {
                  potionEffect = player.getPotionEffect(PotionEffectType.HEALTH_BOOST);
                  player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
               }

               if (this.getLoversHearts(uuid) != 0.0) {
                  player.sendMessage(this.color(this.plugin.getConfig().getString("ability-settings.lovers-ability.lovers-proc-msg")));
                  player.playSound(player.getLocation(), Sound.LEVEL_UP, 10.0F, 7.0F);
               }

               if (newHearts == maxHearts) {
                  double finalHealth = maxHearts - this.getLoversHearts(uuid);
                  player.setMaxHealth(player.getMaxHealth() + Math.max(0.0, finalHealth));
               } else {
                  player.setMaxHealth(player.getMaxHealth() + heartsPer);
               }

               playerConfig.set("lovers-ability", newHearts);
               playerData.saveConfig(uuid, playerConfig);
               if (potionEffect != null) {
                  player.addPotionEffect(potionEffect);
                  double healthToAdd = healthBeforePotion - player.getHealth();
                  player.setHealth(Math.min(player.getMaxHealth(), Math.max(player.getHealth(), player.getHealth() + healthToAdd)));
               }
            }
         }
      }
   }

   public void removeLovers(UUID uuid, boolean playerDied) {
      if (this.hasLovers(uuid)) {
         OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
         PlayerData playerData = this.plugin.getPlayerData();
         FileConfiguration playerConfig = playerData.getPlayerConfig(uuid);
         if (this.hasLoversTask(uuid)) {
            this.loversPlayersTask.get(uuid).cancel();
            this.loversPlayersTask.remove(uuid);
         }

         if (playerDied) {
            playerConfig.set("lovers-ability", null);
            playerData.saveConfig(uuid, playerConfig);
         } else if (player.isOnline()) {
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            PotionEffect potionEffect = null;
            double healthBeforePotion = onlinePlayer.getHealth();
            if (onlinePlayer.hasPotionEffect(PotionEffectType.HEALTH_BOOST)) {
               potionEffect = onlinePlayer.getPotionEffect(PotionEffectType.HEALTH_BOOST);
               onlinePlayer.removePotionEffect(PotionEffectType.HEALTH_BOOST);
            }

            onlinePlayer.setMaxHealth(onlinePlayer.getMaxHealth() - this.getLoversHearts(uuid));
            playerConfig.set("lovers-ability", null);
            playerData.saveConfig(uuid, playerConfig);
            if (potionEffect != null) {
               onlinePlayer.addPotionEffect(potionEffect);
               double healthToAdd = healthBeforePotion - onlinePlayer.getHealth();
               onlinePlayer.setHealth(Math.min(onlinePlayer.getMaxHealth(), Math.max(onlinePlayer.getHealth(), onlinePlayer.getHealth() + healthToAdd)));
            }
         }
      }
   }

   public boolean hasLovers(UUID uuid) {
      if (Bukkit.getOfflinePlayer(uuid) == null) {
         return false;
      }

      PlayerData playerData = this.plugin.getPlayerData();
      FileConfiguration playerConfig = playerData.getPlayerConfig(uuid);
      return playerConfig.contains("lovers-ability");
   }

   private double getLoversHearts(UUID uuid) {
      if (Bukkit.getOfflinePlayer(uuid) == null) {
         return 0.0;
      }

      if (!this.hasLovers(uuid)) {
         return 0.0;
      }

      PlayerData playerData = this.plugin.getPlayerData();
      FileConfiguration playerConfig = playerData.getPlayerConfig(uuid);
      return playerConfig.getDouble("lovers-ability", 0.0);
   }

   protected boolean hasLoversTask(UUID uuid) {
      return this.loversPlayersTask.containsKey(uuid);
   }

   private String color(String string) {
      return ChatColor.translateAlternateColorCodes('&', string);
   }
}
