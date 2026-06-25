package org.minecurse.armorsets.listeners;

import com.asset.curseexpansions.damagetracker.DamageTracker;
import com.asset.curseexpansions.damagetracker.EntryRole;
import com.golfing8.winespigot.armorequip.ArmorEquipEvent;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.ArmorSetRegistry;
import org.minecurse.armorsets.sets.types.MagmaSet;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.commons.menu.button.Button;
import org.minecurse.commons.menu.type.chest.ChestMenu;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.PlayerUtils;

public class ArmorSetListener implements Listener {
   @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
   public void onArmorChange(ArmorEquipEvent event) {
      Player player = event.getPlayer();
      ArmorSetRegistry registry = ArmorSetPlugin.getInstance().getArmorSetRegistry();
      ItemStack oldPiece = event.getOldArmorPiece();
      ItemStack newPiece = event.getNewArmorPiece();
      
      if (oldPiece != null && oldPiece.getType() != Material.AIR) {
         de.tr7zw.nbtapi.NBTItem oldNbt = new de.tr7zw.nbtapi.NBTItem(oldPiece);
         if (oldNbt.hasKey("starType")) {
            DamageTracker.removeEntriesBySource(player.getUniqueId(), "staritem:" + event.getType().name());
         }
      }

      if (newPiece != null && newPiece.getType() != Material.AIR) {
         de.tr7zw.nbtapi.NBTItem newNbt = new de.tr7zw.nbtapi.NBTItem(newPiece);
         if (newNbt.hasKey("starType")) {
            String starType = newNbt.getString("starType");
            String displayType = org.apache.commons.lang.WordUtils.capitalizeFully(starType.replace("_", " "));
            String hudText = "&e★ &f" + displayType;
            
            if (DamageTracker.isEnabled()) {
               DamageTracker.addOrUpdatePermanent(
                  player.getUniqueId(), hudText, "staritem:" + event.getType().name(), EntryRole.NEUTRAL
               );
            }
            
            player.sendMessage(org.minecurse.commons.utils.StringUtil.color("&e&l★ &aYou equipped a " + displayType + " &aStar Item!"));
            player.playSound(player.getLocation(), org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.2f);
         }
      }

      boolean wasWearingArmor = oldPiece != null && oldPiece.getType() != Material.AIR;
      boolean isNowWearingArmor = newPiece != null && newPiece.getType() != Material.AIR;
      if (wasWearingArmor && !isNowWearingArmor) {
         ArmorSet armor = registry.getByItem(oldPiece);
         if (armor != null) {
            Bukkit.getScheduler().runTask(ArmorSetPlugin.getInstance(), () -> {
               if (!armor.hasFullArmor(player)) {
                  registry.removeActiveSet(player, armor);
               }
            });
         }
      }

      if (!wasWearingArmor && isNowWearingArmor) {
         ArmorSet armor = registry.getByItem(newPiece);
         if (armor == null) {
            return;
         }

         if (registry.hasActiveSet(player)) {
            return;
         }

         Bukkit.getScheduler().runTask(ArmorSetPlugin.getInstance(), () -> {
            if (armor.hasFullArmor(player)) {
               if (!registry.hasActiveSet(player)) {
                  registry.setActiveSet(player, armor);
               }
            }
         });
      }

      if (wasWearingArmor && isNowWearingArmor && !oldPiece.isSimilar(newPiece)) {
         ArmorSet oldArmor = registry.getByItem(oldPiece);
         ArmorSet newArmor = registry.getByItem(newPiece);
         Bukkit.getScheduler().runTask(ArmorSetPlugin.getInstance(), () -> {
            if (oldArmor != null && !oldArmor.hasFullArmor(player)) {
               registry.removeActiveSet(player, oldArmor);
            }

            if (newArmor != null && !registry.hasActiveSet(player) && newArmor.hasFullArmor(player)) {
               registry.setActiveSet(player, newArmor);
            }
         });
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onHit(EntityDamageByEntityEvent event) {
      if (!event.isCancelled()) {
         if (event.getEntity() instanceof Player) {
            Player target = (Player)event.getEntity();
            if (!LocUtil.canPvp(target, target.getLocation())) {
               return;
            }

            ArmorSet armorSet = ArmorSetPlugin.getInstance().getArmorSetRegistry().getActiveSet(target.getPlayer());
            if (armorSet != null) {
               double damageReduction = armorSet.getIncoming();
               if (armorSet instanceof MagmaSet && event.getDamager() instanceof Projectile) {
                  damageReduction -= 30.0;
               }

               if (armorSet.hasWeapon(target)) {
                  damageReduction += armorSet.getWeaponIncoming();
                  if (event.getDamager() instanceof LivingEntity) {
                     armorSet.onDefenseWithWeapon(target, (LivingEntity)event.getDamager(), event);
                  }
               }

               event.setDamage(event.getDamage(DamageModifier.BASE) - event.getDamage(DamageModifier.BASE) * (damageReduction * 0.01));
               if (DamageTracker.isEnabled()) {
                  DamageTracker.addOrUpdatePermanent(
                     target.getUniqueId(), armorSet.getInternalName() + " -" + damageReduction + "% incoming", "armorset:incoming", EntryRole.DEFENSIVE
                  );
               }

               if (event.getDamager() instanceof LivingEntity) {
                  armorSet.onDamaged(target, (LivingEntity)event.getDamager(), event);
               }
            }
         }

         if (event.getDamager() instanceof Player) {
            Player attacker = (Player)event.getDamager();
            ArmorSet armorSet = ArmorSetPlugin.getInstance().getArmorSetRegistry().getActiveSet(attacker.getPlayer());
            if (armorSet != null) {
               double toIncrease = armorSet.getOutgoing();
               if (armorSet.hasWeapon(attacker)) {
                  toIncrease += armorSet.getWeaponBonus();
                  if (event.getEntity() instanceof LivingEntity) {
                     armorSet.onAttackWithWeapon(attacker, (LivingEntity)event.getEntity(), event);
                  }
               }

               event.setDamage(event.getDamage(DamageModifier.BASE) + event.getDamage(DamageModifier.BASE) * (toIncrease * 0.01));
               if (DamageTracker.isEnabled()) {
                  DamageTracker.addOrUpdatePermanent(
                     attacker.getUniqueId(), armorSet.getInternalName() + " +" + toIncrease + "% outgoing", "armorset:outgoing", EntryRole.OFFENSIVE
                  );
               }

               if (event.getEntity() instanceof LivingEntity) {
                  armorSet.onAttack(attacker, (LivingEntity)event.getEntity(), event);
               }
            }
         }

         if (event.getDamager() instanceof Arrow) {
            if (event.getEntity() instanceof LivingEntity) {
               Arrow arrowEntity = (Arrow)event.getDamager();
               if (arrowEntity.getShooter() instanceof Player) {
                  Player attacker = (Player)((Arrow)event.getDamager()).getShooter();
                  LivingEntity defenderEntity = (LivingEntity)event.getEntity();
                  ArmorSet armorSet = ArmorSetPlugin.getInstance().getArmorSetRegistry().getActiveSet(attacker);
                  if (armorSet != null) {
                     double toIncrease = armorSet.getOutgoing();
                     if (armorSet.hasWeapon(attacker) && armorSet.getWeaponBonus() > 0.0) {
                        toIncrease += armorSet.getWeaponBonus();
                     }

                     event.setDamage(event.getDamage(DamageModifier.BASE) + event.getDamage(DamageModifier.BASE) * toIncrease * 0.01);
                     if (DamageTracker.isEnabled()) {
                        DamageTracker.addOrUpdatePermanent(
                           attacker.getUniqueId(), armorSet.getInternalName() + " +" + toIncrease + "% outgoing", "armorset:outgoing", EntryRole.OFFENSIVE
                        );
                     }

                     armorSet.onProjectile(attacker, defenderEntity, event);
                  }
               }
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      ArmorSetRegistry registry = ArmorSetPlugin.getInstance().getArmorSetRegistry();
      ArmorSet dead = registry.getActiveSet(player);
      if (dead != null) {
         registry.removeActiveSet(player, dead);
      }
   }

   @EventHandler
   public void onClick(PlayerInteractEvent event) {
      if (event.getItem() != null && event.getItem().getType() != Material.AIR) {
         NBTItem item = new NBTItem(event.getItem());
         if (item.hasKey("armorSet")) {
            if (event.getAction() != Action.PHYSICAL) {
               if (item.getBoolean("redeemItem") || item.getBoolean("randomItem")) {
                  event.setCancelled(true);
                  ArmorSet set = ArmorSetPlugin.getInstance().getArmorSetRegistry().getByName(item.getString("armorSet"));
                  if (set == null) {
                     return;
                  }

                  switch (event.getAction()) {
                     case LEFT_CLICK_AIR:
                     case LEFT_CLICK_BLOCK:
                        ChestMenu menu = new ChestMenu(set.getDisplayName() + " Set", 3);
                        menu.fillSides(Button.PLACEHOLDER);

                        for (ArmorPiece piece : ArmorPiece.values()) {
                           ItemStack stack = set.buildArmor(piece);
                           if (stack != null) {
                              menu.addButton(new Button(stack));
                           }
                        }

                        menu.buildInventory();
                        menu.show(event.getPlayer());
                        break;
                     case RIGHT_CLICK_AIR:
                     case RIGHT_CLICK_BLOCK:
                        if (!item.getBoolean("redeemItem")) {
                           if (item.getBoolean("randomItem")) {
                              ItemStack i = set.getRandomPiece();
                              if (i != null) {
                                 PlayerUtils.giveOrDropProtectedItem(event.getPlayer(), i, 5);
                                 event.getPlayer().updateInventory();
                                 event.getPlayer()
                                    .sendMessage(
                                       ArmorSetPlugin.getPrefix(
                                          "&7You've received the " + i.getItemMeta().getDisplayName() + " &7from the " + set.getDisplayName() + " &7armor set.",
                                          false
                                       )
                                    );
                              }
                           }
                        } else {
                           for (ArmorPiece piece : ArmorPiece.values()) {
                              ItemStack i = set.buildArmor(piece);
                              if (i != null) {
                                 PlayerUtils.giveOrDropProtectedItem(event.getPlayer(), i, 5);
                              }
                           }

                           event.getPlayer().updateInventory();
                           event.getPlayer()
                              .sendMessage(ArmorSetPlugin.getPrefix("&7You've received the " + set.getDisplayName() + " &7armor set items.", false));
                        }

                        if (event.getItem().getAmount() > 1) {
                           event.getItem().setAmount(event.getItem().getAmount() - 1);
                           event.getPlayer().setItemInHand(event.getItem());
                        } else {
                           event.getPlayer().setItemInHand(null);
                        }

                        event.getPlayer().updateInventory();
                  }
               }
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.NORMAL)
   public void onArmorQuickSwap(PlayerInteractEvent event) {
      if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
         ItemStack handItem = event.getItem();
         if (handItem != null && handItem.getType() != Material.AIR) {
            if (org.minecurse.commons.utils.MaterialList.ARMOR.hasMaterial(handItem.getType())) {
               Player player = event.getPlayer();
               com.golfing8.winespigot.armorequip.ArmorType type = com.golfing8.winespigot.armorequip.ArmorType.matchType(handItem);
               if (type != null) {
                  ItemStack equipped = null;
                  
                  if (type == com.golfing8.winespigot.armorequip.ArmorType.HELMET) {
                     equipped = player.getInventory().getHelmet();
                  } else if (type == com.golfing8.winespigot.armorequip.ArmorType.CHESTPLATE) {
                     equipped = player.getInventory().getChestplate();
                  } else if (type == com.golfing8.winespigot.armorequip.ArmorType.LEGGINGS) {
                     equipped = player.getInventory().getLeggings();
                  } else if (type == com.golfing8.winespigot.armorequip.ArmorType.BOOTS) {
                     equipped = player.getInventory().getBoots();
                  }
                  
                  if (equipped != null && equipped.getType() != Material.AIR) {
                     ArmorEquipEvent equipEvent = new ArmorEquipEvent(player, type, equipped, handItem);
                     Bukkit.getServer().getPluginManager().callEvent(equipEvent);
                     
                     event.setCancelled(true);
                     ItemStack toEquip = handItem.clone();
                     toEquip.setAmount(1);
                     
                     if (handItem.getAmount() > 1) {
                        handItem.setAmount(handItem.getAmount() - 1);
                        player.setItemInHand(handItem);
                        org.minecurse.commons.utils.PlayerUtils.giveOrDropProtectedItem(player, equipped, 5);
                     } else {
                        player.setItemInHand(equipped);
                     }
                     
                     if (type == com.golfing8.winespigot.armorequip.ArmorType.HELMET) {
                        player.getInventory().setHelmet(toEquip);
                     } else if (type == com.golfing8.winespigot.armorequip.ArmorType.CHESTPLATE) {
                        player.getInventory().setChestplate(toEquip);
                     } else if (type == com.golfing8.winespigot.armorequip.ArmorType.LEGGINGS) {
                        player.getInventory().setLeggings(toEquip);
                     } else if (type == com.golfing8.winespigot.armorequip.ArmorType.BOOTS) {
                        player.getInventory().setBoots(toEquip);
                     }
                     
                     player.updateInventory();
                     player.playSound(player.getLocation(), org.bukkit.Sound.HORSE_ARMOR, 1.0f, 1.0f);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      ArmorSetRegistry registry = ArmorSetPlugin.getInstance().getArmorSetRegistry();
      Bukkit.getScheduler().runTaskLater(ArmorSetPlugin.getInstance(), () -> {
         if (player.isOnline()) {
            ItemStack[] armor = player.getInventory().getArmorContents();
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().setArmorContents(armor);
            player.updateInventory();

            ArmorSet active = registry.getActiveSet(player);
            if (active != null) {
               registry.getActivePlayerSet().remove(player.getUniqueId());
            }

            for (ItemStack item : armor) {
               if (item != null && item.getType() != Material.AIR) {
                  ArmorSet set = registry.getByItem(item);
                  if (set != null && set.hasFullArmor(player)) {
                     registry.setActiveSet(player, set);
                     break;
                  }
               }
            }
         }
      }, 2L);
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      ArmorSetRegistry registry = ArmorSetPlugin.getInstance().getArmorSetRegistry();
      ArmorSet active = registry.getActiveSet(player);
      if (active != null) {
         registry.removeActiveSet(player, active);
      }
   }
}
