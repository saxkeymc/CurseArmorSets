package org.minecurse.armorsets.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class ArmorSetPotionEffectAPI {
   private final Map<UUID, Set<PotionEffectType>> disabledEffects = new ConcurrentHashMap<>();

   public boolean disablePotionEffect(Player player, PotionEffectType potionEffectType) {
      return this.disablePotionEffect(player, potionEffectType, false);
   }

   public boolean disablePotionEffect(Player player, PotionEffectType potionEffectType, boolean removeCurrentEffect) {
      if (player == null) {
         return false;
      }

      boolean changed = this.disablePotionEffect(player.getUniqueId(), potionEffectType);
      if (changed && removeCurrentEffect) {
         player.removePotionEffect(potionEffectType);
      }

      return changed;
   }

   public boolean disablePotionEffect(UUID uuid, PotionEffectType potionEffectType) {
      return uuid != null && this.isSupportedEffect(potionEffectType)
         ? this.disabledEffects.computeIfAbsent(uuid, ignored -> ConcurrentHashMap.newKeySet()).add(potionEffectType)
         : false;
   }

   public boolean enablePotionEffect(Player player, PotionEffectType potionEffectType) {
      return player == null ? false : this.enablePotionEffect(player.getUniqueId(), potionEffectType);
   }

   public boolean enablePotionEffect(UUID uuid, PotionEffectType potionEffectType) {
      if (uuid != null && this.isSupportedEffect(potionEffectType)) {
         Set<PotionEffectType> effects = this.disabledEffects.get(uuid);
         if (effects == null) {
            return false;
         }

         boolean changed = effects.remove(potionEffectType);
         if (effects.isEmpty()) {
            this.disabledEffects.remove(uuid);
         }

         return changed;
      } else {
         return false;
      }
   }

   public void disableMovementPotionEffects(Player player) {
      this.disableMovementPotionEffects(player, false);
   }

   public void disableMovementPotionEffects(Player player, boolean removeCurrentEffects) {
      this.disablePotionEffect(player, PotionEffectType.SPEED, removeCurrentEffects);
      this.disablePotionEffect(player, PotionEffectType.JUMP, removeCurrentEffects);
   }

   public void enableMovementPotionEffects(Player player) {
      if (player != null) {
         this.enablePotionEffect(player, PotionEffectType.SPEED);
         this.enablePotionEffect(player, PotionEffectType.JUMP);
      }
   }

   public boolean isPotionEffectDisabled(Player player, PotionEffectType potionEffectType) {
      return player == null ? false : this.isPotionEffectDisabled(player.getUniqueId(), potionEffectType);
   }

   public boolean isPotionEffectDisabled(UUID uuid, PotionEffectType potionEffectType) {
      if (uuid != null && this.isSupportedEffect(potionEffectType)) {
         Set<PotionEffectType> effects = this.disabledEffects.get(uuid);
         return effects != null && effects.contains(potionEffectType);
      } else {
         return false;
      }
   }

   public boolean canApplyPotionEffect(Player player, PotionEffectType potionEffectType) {
      return !this.isPotionEffectDisabled(player, potionEffectType);
   }

   public boolean hasDisabledPotionEffects(Player player) {
      return player != null && this.hasDisabledPotionEffects(player.getUniqueId());
   }

   public boolean hasDisabledPotionEffects(UUID uuid) {
      Set<PotionEffectType> effects = this.disabledEffects.get(uuid);
      return effects != null && !effects.isEmpty();
   }

   public Set<PotionEffectType> getDisabledPotionEffects(Player player) {
      return player == null ? Collections.emptySet() : this.getDisabledPotionEffects(player.getUniqueId());
   }

   public Set<PotionEffectType> getDisabledPotionEffects(UUID uuid) {
      Set<PotionEffectType> effects = this.disabledEffects.get(uuid);
      return effects != null && !effects.isEmpty() ? Collections.unmodifiableSet(new HashSet<>(effects)) : Collections.emptySet();
   }

   public void clearDisabledPotionEffects(Player player) {
      if (player != null) {
         this.clearDisabledPotionEffects(player.getUniqueId());
      }
   }

   public void clearDisabledPotionEffects(UUID uuid) {
      if (uuid != null) {
         this.disabledEffects.remove(uuid);
      }
   }

   public void clearAllDisabledPotionEffects() {
      this.disabledEffects.clear();
   }

   public boolean isSupportedEffect(PotionEffectType potionEffectType) {
      return PotionEffectType.SPEED.equals(potionEffectType) || PotionEffectType.JUMP.equals(potionEffectType);
   }
}
