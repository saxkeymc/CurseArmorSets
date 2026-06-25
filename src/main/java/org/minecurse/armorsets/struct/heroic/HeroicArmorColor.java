package org.minecurse.armorsets.struct.heroic;

import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum HeroicArmorColor {
   RED(ChatColor.RED, Color.fromRGB(255, 38, 38)),
   DARK_RED(ChatColor.DARK_RED, Color.fromRGB(252, 0, 0)),
   AQUA(ChatColor.AQUA, Color.fromRGB(0, 244, 252)),
   DARK_AQUA(ChatColor.DARK_AQUA, Color.fromRGB(0, 170, 170)),
   BLACK(ChatColor.BLACK, Color.fromRGB(25, 25, 25)),
   BLUE(ChatColor.BLUE, Color.fromRGB(58, 179, 218)),
   DARK_BLUE(ChatColor.DARK_BLUE, Color.fromRGB(60, 68, 170)),
   GRAY(ChatColor.GRAY, Color.fromRGB(153, 153, 153)),
   DARK_GRAY(ChatColor.DARK_GRAY, Color.fromRGB(102, 102, 102)),
   GREEN(ChatColor.GREEN, Color.fromRGB(98, 255, 0)),
   DARK_GREEN(ChatColor.DARK_GREEN, Color.fromRGB(0, 124, 0)),
   GOLD(ChatColor.GOLD, Color.fromRGB(216, 127, 51)),
   PINK(ChatColor.LIGHT_PURPLE, Color.fromRGB(255, 0, 217)),
   PURPLE(ChatColor.DARK_PURPLE, Color.fromRGB(191, 0, 255)),
   YELLOW(ChatColor.YELLOW, Color.fromRGB(229, 229, 51)),
   WHITE(ChatColor.WHITE, Color.fromRGB(255, 255, 255)),
   RESET(ChatColor.RESET, Color.fromRGB(255, 255, 255));

   private final ChatColor chatColor;
   private final Color armorColor;

   public ChatColor getChatColor() {
      return this.chatColor;
   }

   public Color getArmorColor() {
      return this.armorColor;
   }

   HeroicArmorColor(ChatColor chatColor, Color armorColor) {
      this.chatColor = chatColor;
      this.armorColor = armorColor;
   }

   public static HeroicArmorColor fromChatColor(ChatColor color) {
      return Arrays.stream(values()).filter(heroicArmorColor -> heroicArmorColor.chatColor == color).findFirst().orElse(RED);
   }
}
