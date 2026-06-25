package org.minecurse.armorsets.listeners;

import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.ArmorSetRegistry;
import org.minecurse.armorsets.sets.types.ColossalSet;
import org.minecurse.armorsets.sets.types.InfernoSet;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.MaterialUtil;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.RandomUtil;

public class CrystalListener implements Listener {
   private final ArmorSetRegistry registry;

   public CrystalListener(ArmorSetRegistry registry) {
      this.registry = registry;
   }

   @EventHandler
   public void onDura(PlayerItemDamageEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onApply(InventoryClickEvent event) {
      ItemStack cursor = event.getCursor();
      ItemStack current = event.getCurrentItem();
      Player player = (Player)event.getWhoClicked();
      if (event.getClickedInventory() == player.getInventory()) {
         if (cursor != null && current != null && !current.getType().equals(Material.AIR)) {
            if (MaterialUtil.isArmor(current)) {
               if (cursor.getType() == Material.NETHER_STAR && this.registry.isCrystal(cursor)) {
                  event.setCancelled(true);
                  if (this.registry.hasCrystal(current)) {
                     player.sendMessage(ArmorSetPlugin.getPrefix("&cThere is already an armor crystal attached to this item.", false));
                     PlayerUtils.playSound(player, Sound.ITEM_BREAK, 0.75F);
                  } else if (this.registry.getByItem(current) != null) {
                     player.sendMessage(ArmorSetPlugin.getPrefix("&cYou cannot apply crystals to armor sets.", false));
                     PlayerUtils.playSound(player, Sound.ITEM_BREAK, 0.75F);
                  } else {
                     if (cursor.getAmount() > 1) {
                        cursor.setAmount(cursor.getAmount() - 1);
                     } else {
                        event.setCursor(null);
                     }

                     if (RandomUtil.getChance(this.registry.getPercent(current))) {
                        player.sendMessage(ArmorSetPlugin.getPrefix("&cYour crystal has failed to apply!", false));
                        PlayerUtils.playSound(player, Sound.GLASS, 1.5F);
                        PlayerUtils.playSound(player, Sound.FIRE_IGNITE, 5.0F);
                     } else {
                        List<ArmorSet> crystal = this.registry.getCrystals(cursor);
                        event.setCurrentItem(this.registry.applyCrystals(current, crystal));
                        player.sendMessage(ArmorSetPlugin.getPrefix("&7You have applied an armor crystal to your item.", false));
                        PlayerUtils.playSound(player, Sound.LEVEL_UP, 1.25F);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onIncomingDamage(EntityDamageByEntityEvent event) {
      if (!event.isCancelled()) {
         if (event.getDamager() instanceof Player) {
            if (event.getEntity() instanceof Player) {
               if (LocUtil.canPvp((Player)event.getEntity(), event.getEntity().getLocation())) {
                  double reduction = 0.0;
                  Player player = (Player)event.getDamager();
                  if (LocUtil.canPvp(player, player.getLocation()) && LocUtil.canPvp((Player)event.getEntity(), event.getEntity().getLocation())) {
                     ArmorSetRegistry registry = ArmorSetPlugin.getInstance().getArmorSetRegistry();
                     List<ArmorSet> crystals = registry.getCrystals(player);
                     if (crystals != null && !crystals.isEmpty()) {
                        for (ArmorSet set : crystals) {
                           reduction += set.getCrystal() != null ? set.getCrystal().incoming() : 0.0;
                           if (set.getCrystal() != null && set.getCrystal().abilityChance() > 0.0 && set instanceof InfernoSet) {
                              InfernoSet inferno = (InfernoSet)set;
                              Map<ArmorSet, Double> chances = registry.getCrystalAbilityChances(player);
                              if (!RandomUtil.getChance(chances.get(inferno))) {
                              }
                           }
                        }

                        event.setDamage(event.getDamage() - event.getDamage() * 0.01 * reduction);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onOutgoingDamage(EntityDamageByEntityEvent event) {
      if (!event.isCancelled()) {
         if (event.getDamager() instanceof Player) {
            if (event.getEntity() instanceof Player) {
               if (LocUtil.canPvp((Player)event.getDamager(), event.getDamager().getLocation())) {
                  double extra = 0.0;
                  Player player = (Player)event.getDamager();
                  ArmorSetRegistry registry = ArmorSetPlugin.getInstance().getArmorSetRegistry();
                  List<ArmorSet> crystals = registry.getCrystals(player);
                  if (crystals != null && !crystals.isEmpty()) {
                     for (ArmorSet set : crystals) {
                        extra += set.getCrystal() != null ? set.getCrystal().outgoing() : 0.0;
                        if (set.getCrystal() != null && set.getCrystal().abilityChance() > 0.0) {
                           if (set instanceof InfernoSet) {
                              InfernoSet inferno = (InfernoSet)set;
                              Map<ArmorSet, Double> chances = registry.getCrystalAbilityChances(player);
                              if (!RandomUtil.getChance(chances.get(inferno))) {
                                 continue;
                              }
                           }

                           if (set instanceof ColossalSet) {
                              ColossalSet colossal = (ColossalSet)set;
                              Map<ArmorSet, Double> chances = registry.getCrystalAbilityChances(player);
                              if (RandomUtil.getChance(chances.get(colossal))) {
                                 Cooldown cooldown = colossal.getCooldowns().get(player.getUniqueId());
                                 if (cooldown == null || cooldown.isOver()) {
                                    colossal.procAbility(player, (LivingEntity)event.getEntity());
                                    colossal.getCooldowns().put(player.getUniqueId(), new Cooldown(120L));
                                 }
                              }
                           }
                        }
                     }

                     event.setDamage(event.getDamage() + event.getDamage() * 0.01 * extra);
                  }
               }
            }
         }
      }
   }
}
