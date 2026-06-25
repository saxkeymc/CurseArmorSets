package org.minecurse.armorsets.struct.heroic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Color;

public class Rainbow {
   private List<Color> colors = new ArrayList<>();
   private int index;
   private int count;
   private int size;
   private Color disabledColor;

   public void setColors(List<Color> colors) {
      this.colors = colors;
   }

   public void setIndex(int index) {
      this.index = index;
   }

   public void setCount(int count) {
      this.count = count;
   }

   public void setSize(int size) {
      this.size = size;
   }

   public void setDisabledColor(Color disabledColor) {
      this.disabledColor = disabledColor;
   }

   public List<Color> getColors() {
      return this.colors;
   }

   public int getIndex() {
      return this.index;
   }

   public int getCount() {
      return this.count;
   }

   public int getSize() {
      return this.size;
   }

   public Color getDisabledColor() {
      return this.disabledColor;
   }

   public Rainbow() {
      for (int k = 0; k < 100; k++) {
         this.colors.add(Color.fromRGB(k * 2, 230, 55));
      }

      for (int j = 100; j > 0; j--) {
         this.colors.add(Color.fromRGB(200, j * 2 + 30, 55));
      }

      for (int i = 0; i < 100; i++) {
         this.colors.add(Color.fromRGB(200, 30, i * 2 + 55));
      }

      for (int r = 100; r > 0; r--) {
         this.colors.add(Color.fromRGB(r * 2, 30, 255));
      }

      for (int g = 0; g < 100; g++) {
         this.colors.add(Color.fromRGB(0, g * 2 + 30, 255));
      }

      for (int b = 100; b > 0; b--) {
         this.colors.add(Color.fromRGB(0, 230, b * 2 + 55));
      }

      List<Color> newColors = new ArrayList<>();
      int addAmt = 15;

      for (Color color : this.colors) {
         newColors.add(Color.fromRGB(Math.min(color.getRed() + addAmt, 255), Math.min(color.getGreen() + addAmt, 255), Math.min(color.getBlue() + addAmt, 255)));
      }

      this.colors = newColors;
      Collections.reverse(this.colors);
      this.size = this.colors.size();
   }

   private static float lerp(float from, float to, float frac) {
      return from + (to - from) * frac;
   }

   public void update(int delay) {
      this.disabledColor = null;
      if (++this.count >= delay) {
         this.count = 0;
         this.index = (this.index + 1) % this.colors.size();
      }
   }

   public Color getColorAtPercentDecimal(float percent) {
      if (this.disabledColor != null) {
         return this.disabledColor;
      }

      int i = (int)lerp(this.index, this.index + this.colors.size(), percent) % this.colors.size();
      return this.colors.get(i);
   }
}
