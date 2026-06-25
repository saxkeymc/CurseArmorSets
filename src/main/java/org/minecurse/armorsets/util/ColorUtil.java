package org.minecurse.armorsets.util;

import org.bukkit.ChatColor;

public class ColorUtil {
   public static String translate(String text) {
      return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
   }
}
