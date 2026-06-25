package org.minecurse.armorsets.listeners;

import com.asset.curseexpansions.damagetracker.DamageTracker;
import com.asset.curseexpansions.damagetracker.EntryRole;
import com.golfing8.winespigot.armorequip.ArmorEquipEvent;
import de.tr7zw.nbtapi.NBTItem;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.DiamondHook;
import org.minecurse.armorsets.struct.heroic.HeroicArmorColor;
import org.minecurse.armorsets.struct.heroic.HeroicArmorType;
import org.minecurse.armorsets.struct.heroic.HeroicWeaponType;
import org.minecurse.commons.runnable.RunnableBuilder;
import org.minecurse.commons.utils.MaterialList;
import org.minecurse.commons.utils.RandomUtil;

public class HeroicArmorListener implements Listener {
   private final ChatColor[] RAINBOW_COLORS = new ChatColor[]{
      ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.BLUE, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE
   };

   @EventHandler(priority = EventPriority.HIGH)
   public void onArmorChange(ArmorEquipEvent event) {
      Player player = event.getPlayer();
      ItemStack oldPiece = event.getOldArmorPiece();
      ItemStack newPiece = event.getNewArmorPiece();
      boolean hadArmor = oldPiece != null && oldPiece.getType() != Material.AIR;
      boolean hasArmor = newPiece != null && newPiece.getType() != Material.AIR;
      if (hadArmor && ArmorSetPlugin.getInstance().getApi().isHeroicArmor(oldPiece) && ArmorSetPlugin.getInstance().getApi().isRainbowArmor(oldPiece)) {
         ArmorSetPlugin.getInstance().getApi().removeRainbowInfo(player, oldPiece);
      }

      if (hasArmor && ArmorSetPlugin.getInstance().getApi().isHeroicArmor(newPiece) && ArmorSetPlugin.getInstance().getApi().isRainbowArmor(newPiece)) {
         ArmorSetPlugin.getInstance().getApi().addRainbowInfo(player, newPiece);
      }
   }

   @EventHandler(priority = EventPriority.LOW)
   public void onQuit(PlayerQuitEvent event) {
      this.handleRemoval(event.getPlayer());
   }

   @EventHandler(priority = EventPriority.LOW)
   public void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      org.bukkit.Bukkit.getScheduler().runTaskLater(ArmorSetPlugin.getInstance(), () -> {
         if (player.isOnline()) {
            for (ItemStack item : player.getInventory().getArmorContents()) {
               if (item != null && item.getType() != Material.AIR) {
                  if (ArmorSetPlugin.getInstance().getApi().isHeroicArmor(item) && ArmorSetPlugin.getInstance().getApi().isRainbowArmor(item)) {
                     ArmorSetPlugin.getInstance().getApi().addRainbowInfo(player, item);
                  }
               }
            }
         }
      }, 20L);
   }

   @EventHandler(priority = EventPriority.LOW)
   public void onKick(PlayerKickEvent event) {
      this.handleRemoval(event.getPlayer());
   }

   @EventHandler(priority = EventPriority.LOW)
   public void onDeath(PlayerDeathEvent event) {
      this.handleRemoval(event.getEntity());
   }

   private void handleRemoval(Player player) {
      ArmorSetPlugin.getInstance().getApi().getRainbowInfo().remove(player);
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         ItemStack[] armor = player.getInventory().getArmorContents();
         double reductionPercent = 0.0;
         double rainbowPerPiece = ArmorSetPlugin.getInstance().getDefaultConfig().getRainbowArmorPerPiece();
         double heroicPerPiece = ArmorSetPlugin.getInstance().getDefaultConfig().getHeroicArmorPerPiece();
         int rainbowCount = 0;
         int heroicCount = 0;

         for (ItemStack itemStack : armor) {
            if (itemStack != null) {
               if (ArmorSetPlugin.getInstance().getApi().isRainbowArmor(itemStack)) {
                  reductionPercent += rainbowPerPiece;
                  rainbowCount++;
               } else if (ArmorSetPlugin.getInstance().getApi().isHeroicArmor(itemStack)) {
                  reductionPercent += heroicPerPiece;
                  heroicCount++;
               }
            }
         }

         if (reductionPercent > 0.0) {
            event.setDamage(event.getDamage() * (1.0 - reductionPercent / 100.0));
            if (DamageTracker.isEnabled()) {
               String msg;
               if (rainbowCount > 0 && heroicCount > 0) {
                  msg = "Rainbow(" + rainbowCount + ") + Heroic(" + heroicCount + ") -" + reductionPercent + "%";
               } else if (rainbowCount > 0) {
                  msg = "Rainbow armor -" + reductionPercent + "% (" + rainbowCount + "pc)";
               } else {
                  msg = "Heroic armor -" + reductionPercent + "% (" + heroicCount + "pc)";
               }

               DamageTracker.addOrUpdatePermanent(player.getUniqueId(), msg, "heroic:armor-reduction", EntryRole.DEFENSIVE);
            }
         }
      }

      if (event.getDamager() instanceof Player) {
         Player player2 = (Player)event.getDamager();
         ItemStack itemStack2 = player2.getItemInHand();
         if (itemStack2 == null) {
            return;
         }

         if (ArmorSetPlugin.getInstance().getApi().isHeroicHook(itemStack2)) {
            double heroicHookBonus = ArmorSetPlugin.getInstance().getDefaultConfig().getHeroicHookMultiplier();
            event.setDamage(event.getDamage() * (1.0 + heroicHookBonus / 100.0));
            if (DamageTracker.isEnabled()) {
               DamageTracker.addOrUpdatePermanent(player2.getUniqueId(), "Heroic hook +" + heroicHookBonus + "%", "heroic:hook-bonus", EntryRole.OFFENSIVE);
            }

            return;
         }

         if (DiamondHook.isHook(itemStack2)) {
            double diamondHookBonus = ArmorSetPlugin.getInstance().getDefaultConfig().getDiamondHookMultiplier();
            event.setDamage(event.getDamage() * (1.0 + diamondHookBonus / 100.0));
            if (DamageTracker.isEnabled()) {
               DamageTracker.addOrUpdatePermanent(player2.getUniqueId(), "Diamond hook +" + diamondHookBonus + "%", "diamond:hook-bonus", EntryRole.OFFENSIVE);
            }

            return;
         }

         if (!ArmorSetPlugin.getInstance().getApi().isHeroicWeapon(itemStack2)) {
            return;
         }

         if (event.getEntity() instanceof Player && event.getEntity().hasMetadata("midas")) {
            event.setDamage(event.getDamage() - event.getDamage() * 0.05);
            if (DamageTracker.isEnabled()) {
               DamageTracker.addOrUpdatePermanent(event.getEntity().getUniqueId(), "Midas -5% heroic dmg", "heroic:midas-penalty", EntryRole.DEFENSIVE);
            }
         }

         double heroicWeaponBonus = ArmorSetPlugin.getInstance().getDefaultConfig().getHeroicWeaponBonus();
         event.setDamage(event.getDamage() + event.getDamage() * (heroicWeaponBonus / 100.0));
         if (DamageTracker.isEnabled()) {
            DamageTracker.addOrUpdatePermanent(player2.getUniqueId(), "Heroic weapon +" + heroicWeaponBonus + "%", "heroic:weapon-bonus", EntryRole.OFFENSIVE);
         }
      }
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
   public void onItemDamage(PlayerItemDamageEvent event) {
      ItemStack itemStack = event.getItem();
      if (itemStack != null) {
         if (ArmorSetPlugin.getInstance().getApi().isHeroicArmor(itemStack)
            || ArmorSetPlugin.getInstance().getApi().isHeroicWeapon(itemStack)
            || ArmorSetPlugin.getInstance().getApi().isHeroicHook(itemStack)) {
            event.setCancelled(true);
            event.setDamage(0);
            RunnableBuilder.bind(event.getPlayer()::updateInventory).runAsync();
         }
      }
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
   public void onClickArmor(InventoryClickEvent event) {
      ItemStack itemStack = event.getCurrentItem();
      if (itemStack != null && MaterialList.ARMOR.hasMaterial(itemStack.getType())) {
         HeroicArmorType armorType = HeroicArmorType.fromPossibleType(itemStack.getType());
         if (armorType != null) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && ArmorSetPlugin.getInstance().getApi().isUpgrade(cursor)) {
               Player player = (Player)event.getWhoClicked();
               NBTItem test = new NBTItem(cursor);
               if (!test.hasKey("armorUpgrade") && !ArmorSetPlugin.getInstance().getApi().isRainbowUpgrade(cursor)) {
                  player.sendMessage(ArmorSetPlugin.getPrefix("&cThis upgrade can only be applied to weapons.", true));
               } else {
                  ArmorSet armorSet = ArmorSetPlugin.getInstance().getArmorSetRegistry().getByItem(itemStack);
                  if (armorSet == null
                     || ArmorSetPlugin.getInstance().getApi().isRainbowUpgrade(cursor)
                     || ArmorSetPlugin.getInstance().getApi().isArmorSetUpgrade(cursor)
                        && ArmorSetPlugin.getInstance().getArmorSetRegistry().getByItem(cursor) == armorSet) {
                     boolean cancel = false;
                     if (RandomUtil.getChance(ArmorSetPlugin.getInstance().getApi().getChance(cursor))) {
                        if (ArmorSetPlugin.getInstance().getApi().isRainbowUpgrade(cursor)) {
                           cancel = true;
                           ItemMeta itemMeta = itemStack.getItemMeta();
                           List<String> lore = itemMeta.getLore();
                           String line = "This armor is stronger than diamond.";
                           StringBuilder sb = new StringBuilder();
                           int currentIndex = 0;

                           for (char c : line.toCharArray()) {
                              sb.append(this.RAINBOW_COLORS[currentIndex++].toString()).append(c);
                              if (currentIndex >= this.RAINBOW_COLORS.length) {
                                 currentIndex = 0;
                              }
                           }

                           lore.add(sb.toString());
                           itemMeta.setLore(lore);
                           itemStack.setItemMeta(itemMeta);
                           NBTItem item = new NBTItem(itemStack);
                           item.setBoolean("rainbow", true);
                           item.setBoolean("heroic", true);
                           itemStack = item.getItem();
                           player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.7F, 1.0F);
                        } else {
                           cancel = true;
                           HeroicArmorColor armorColor = ArmorSetPlugin.getInstance().getApi().getArmorColor(itemStack);
                           if (armorColor == null) {
                              armorColor = HeroicArmorColor.DARK_RED;
                           }

                           ItemMeta itemMeta = itemStack.getItemMeta();
                           List<String> lore = itemMeta.getLore();
                           lore.add(armorColor.getChatColor() + "This armor is stronger than diamond.");
                           itemMeta.setLore(lore);
                           itemStack.setItemMeta(itemMeta);
                           player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.7F, 1.0F);
                        }
                     } else {
                        event.setCancelled(true);
                        if (cursor.getAmount() > 1) {
                           cursor.setAmount(cursor.getAmount() - 1);
                           player.setItemOnCursor(cursor);
                        } else {
                           player.setItemOnCursor(null);
                        }

                        player.updateInventory();
                        player.sendMessage(
                           ArmorSetPlugin.getPrefix("&cThis &6&lHeroic Upgrade &7crystal was destroyed while attempting to upgrade your armor.", true)
                        );
                        player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
                     }

                     if (cancel) {
                        event.setCancelled(true);
                        NBTItem item = new NBTItem(itemStack);
                        item.setBoolean("heroic", true);
                        event.getClickedInventory().setItem(event.getSlot(), item.getItem());
                        if (cursor.getAmount() > 1) {
                           cursor.setAmount(cursor.getAmount() - 1);
                           player.setItemOnCursor(cursor);
                        } else {
                           player.setItemOnCursor(null);
                        }

                        player.updateInventory();
                     }
                  } else {
                     player.sendMessage(ArmorSetPlugin.getPrefix("&cYou cannot apply this upgrade on this armor piece.", true));
                  }
               }
            }
         }
      }
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
   public void onClickWeapon(InventoryClickEvent event) {
      ItemStack itemStack = event.getCurrentItem();
      if (itemStack != null && MaterialList.MELEE_WEAPONS.hasMaterial(itemStack.getType())) {
         if (!itemStack.getType().name().endsWith("_HOE")) {
            HeroicWeaponType weaponType = HeroicWeaponType.fromPossibleType(itemStack.getType());
            if (weaponType != null) {
               ItemStack cursor = event.getCursor();
               if (cursor != null && ArmorSetPlugin.getInstance().getApi().isUpgrade(cursor)) {
                  Player player = (Player)event.getWhoClicked();
                  NBTItem test = new NBTItem(cursor);
                  if (!ArmorSetPlugin.getInstance().getApi().isRainbowUpgrade(cursor) && !test.hasKey("armorUpgrade")) {
                     ArmorSet armorSet = ArmorSetPlugin.getInstance().getArmorSetRegistry().getByItem(itemStack);
                     ArmorSet upgradingSet = null;
                     if (armorSet == null
                        || ArmorSetPlugin.getInstance().getApi().isArmorSetUpgrade(cursor)
                           && (upgradingSet = ArmorSetPlugin.getInstance().getArmorSetRegistry().getByItem(cursor)) == armorSet) {
                        boolean cancel = false;
                        if (RandomUtil.getChance(ArmorSetPlugin.getInstance().getApi().getChance(cursor))) {
                           if (upgradingSet != null && ArmorSetPlugin.getInstance().getApi().isArmorSetUpgrade(cursor)) {
                              cancel = true;
                              HeroicArmorColor armorColor = ArmorSetPlugin.getInstance().getApi().getArmorColor(itemStack);
                              ItemMeta itemMeta = itemStack.getItemMeta();
                              List<String> lore = itemMeta.getLore();
                              lore.add(armorColor.getChatColor() + "This weapon is stronger than diamond.");
                              itemMeta.setLore(lore);
                              itemStack.setItemMeta(itemMeta);
                              player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.7F, 1.0F);
                           } else if (ArmorSetPlugin.getInstance().getApi().isNormalUpgrade(cursor)) {
                              cancel = true;
                              HeroicArmorColor armorColor = ArmorSetPlugin.getInstance().getApi().getArmorColor(itemStack);
                              ItemMeta itemMeta = itemStack.getItemMeta();
                              List<String> lore = itemMeta.getLore();
                              lore.add(armorColor.getChatColor() + "This weapon is stronger than diamond.");
                              itemMeta.setLore(lore);
                              itemStack.setItemMeta(itemMeta);
                              player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.7F, 1.0F);
                           }
                        } else {
                           event.setCancelled(true);
                           if (cursor.getAmount() > 1) {
                              cursor.setAmount(cursor.getAmount() - 1);
                              player.setItemOnCursor(cursor);
                           } else {
                              player.setItemOnCursor(null);
                           }

                           player.updateInventory();
                           player.sendMessage(
                              ArmorSetPlugin.getPrefix("&cThis &6&lHeroic Upgrade &7crystal was destroyed while attempting to upgrade your armor.", true)
                           );
                           player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
                        }

                        if (cancel) {
                           event.setCancelled(true);
                           NBTItem item = new NBTItem(itemStack);
                           item.setBoolean("heroic", true);
                           event.getClickedInventory().setItem(event.getSlot(), item.getItem());
                           if (cursor.getAmount() > 1) {
                              cursor.setAmount(cursor.getAmount() - 1);
                              player.setItemOnCursor(cursor);
                           } else {
                              player.setItemOnCursor(null);
                           }

                           player.updateInventory();
                        }
                     } else {
                        player.sendMessage(ArmorSetPlugin.getPrefix("&cYou cannot apply this upgrade on this weapon.", true));
                     }
                  } else {
                     player.sendMessage(ArmorSetPlugin.getPrefix("&cYou cannot apply this upgrade on this weapon.", true));
                  }
               }
            }
         }
      }
   }

   @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
   public void onClickHook(InventoryClickEvent event) {
      ItemStack itemStack = event.getCurrentItem();
      if (itemStack != null && itemStack.getType() == Material.DIAMOND_HOE) {
         HeroicWeaponType weaponType = HeroicWeaponType.fromPossibleType(itemStack.getType());
         if (weaponType != null) {
            if (!ArmorSetPlugin.getInstance().getApi().isHeroicHook(itemStack)) {
               ItemStack cursor = event.getCursor();
               if (cursor != null && ArmorSetPlugin.getInstance().getApi().isHookUpgrade(cursor)) {
                  Player player = (Player)event.getWhoClicked();
                  NBTItem test = new NBTItem(cursor);
                  if (!ArmorSetPlugin.getInstance().getApi().isRainbowUpgrade(cursor) && !test.hasKey("armorUpgrade")) {
                     ArmorSet armorSet = ArmorSetPlugin.getInstance().getArmorSetRegistry().getByItem(itemStack);
                     if (armorSet == null
                        || ArmorSetPlugin.getInstance().getApi().isArmorSetUpgrade(cursor)
                           && ArmorSetPlugin.getInstance().getArmorSetRegistry().getByItem(cursor) == armorSet) {
                        boolean cancel = false;
                        if (RandomUtil.getChance(ArmorSetPlugin.getInstance().getApi().getChance(cursor))) {
                           if (ArmorSetPlugin.getInstance().getApi().isHookUpgrade(cursor)) {
                              cancel = true;
                              player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.7F, 1.0F);
                           }
                        } else {
                           event.setCancelled(true);
                           if (cursor.getAmount() > 1) {
                              cursor.setAmount(cursor.getAmount() - 1);
                              player.setItemOnCursor(cursor);
                           } else {
                              player.setItemOnCursor(null);
                           }

                           player.updateInventory();
                           player.sendMessage(
                              ArmorSetPlugin.getPrefix("&cThis &6&lHeroic Upgrade &7crystal was destroyed while attempting to upgrade your armor.", true)
                           );
                           player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
                        }

                        if (cancel) {
                           event.setCancelled(true);
                           event.getClickedInventory().setItem(event.getSlot(), ArmorSetPlugin.getInstance().getApi().makeHookHeroic(itemStack));
                           if (cursor.getAmount() > 1) {
                              cursor.setAmount(cursor.getAmount() - 1);
                              player.setItemOnCursor(cursor);
                           } else {
                              player.setItemOnCursor(null);
                           }

                           player.updateInventory();
                        }
                     } else {
                        player.sendMessage(ArmorSetPlugin.getPrefix("&cYou cannot apply this upgrade on this weapon.", true));
                     }
                  } else {
                     player.sendMessage(ArmorSetPlugin.getPrefix("&cYou cannot apply this upgrade on this weapon.", true));
                  }
               }
            }
         }
      }
   }
}
