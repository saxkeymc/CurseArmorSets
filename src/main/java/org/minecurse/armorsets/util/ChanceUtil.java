package org.minecurse.armorsets.util;

import java.util.Random;

public class ChanceUtil {
   public static boolean doChance(int chance) {
      Random random = new Random();
      int randomInt = random.nextInt(101);
      return chance >= randomInt;
   }
}
