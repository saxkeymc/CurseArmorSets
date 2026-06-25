package org.minecurse.armorsets.abilities.rotability;

import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NovaRotDamagePlayerEvent extends Event implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   private final Set<UUID> rotOwners;
   private final Player damagedPlayer;
   private double damageAmount;
   private boolean isCancelled = false;

   public NovaRotDamagePlayerEvent(Player damagedPlayer, double damageAmount, Set<UUID> rotOwners) {
      this.damagedPlayer = damagedPlayer;
      this.damageAmount = damageAmount;
      this.rotOwners = rotOwners;
   }

   public Player getDamagedPlayer() {
      return this.damagedPlayer;
   }

   public double getDamageAmount() {
      return this.damageAmount;
   }

   public void setDamageAmount(double damageAmount) {
      this.damageAmount = damageAmount;
   }

   public Set<UUID> getRotOwners() {
      return this.rotOwners;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean cancel) {
      this.isCancelled = cancel;
   }

   public HandlerList getHandlers() {
      return handlerList;
   }

   public static HandlerList getHandlerList() {
      return handlerList;
   }
}
