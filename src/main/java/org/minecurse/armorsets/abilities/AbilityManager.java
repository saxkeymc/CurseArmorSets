package org.minecurse.armorsets.abilities;

import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.abilities.loversability.LoversManager;
import org.minecurse.armorsets.abilities.rotability.RotManager;

public class AbilityManager {
   private final LoversManager loversManager;
   private final RotManager rotManager;

   public LoversManager getLoversManager() {
      return this.loversManager;
   }

   public RotManager getRotManager() {
      return this.rotManager;
   }

   public AbilityManager(ArmorSetPlugin plugin) {
      this.loversManager = new LoversManager(plugin);
      this.rotManager = new RotManager(plugin);
   }
}
