package org.minecurse.armorsets.configuration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.minecurse.armorsets.ArmorSetPlugin;

public class PlayerData {
   private final ArmorSetPlugin plugin;

   public PlayerData(ArmorSetPlugin plugin) {
      this.plugin = plugin;
   }

   public FileConfiguration getPlayerConfig(UUID uuid) {
      return YamlConfiguration.loadConfiguration(this.getPlayerFile(uuid));
   }

   public File getPlayerFile(UUID uuid) {
      File folder = new File(this.plugin.getDataFolder(), "PlayerData");
      if (!folder.exists()) {
         folder.mkdirs();
      }

      return new File(folder, uuid.toString() + ".yml");
   }

   public void saveConfig(UUID uuid, FileConfiguration config) {
      try {
         config.save(this.getPlayerFile(uuid));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
