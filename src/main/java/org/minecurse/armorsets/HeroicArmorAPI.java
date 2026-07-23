package org.minecurse.armorsets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.tr7zw.nbtapi.NBTItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutSetSlot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.minecurse.armorsets.listeners.HeroicArmorListener;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.struct.heroic.HeroicArmorColor;
import org.minecurse.armorsets.struct.heroic.HeroicArmorType;
import org.minecurse.armorsets.struct.heroic.HeroicWeaponType;
import org.minecurse.armorsets.struct.heroic.Rainbow;
import org.minecurse.armorsets.struct.heroic.RainbowArmorInfo;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.MaterialList;
import org.minecurse.commons.utils.MaterialUtil;
import org.minecurse.commons.utils.PlayerUtils;

public class HeroicArmorAPI {
   private final Rainbow rainbow;
   private final Map<Integer, Player> playersById;
   private final Map<Player, List<RainbowArmorInfo>> rainbowInfo;
   private final ArmorSetPlugin plugin;

   public HeroicArmorAPI(ArmorSetPlugin plugin) {
      this.plugin = plugin;
      this.rainbow = new Rainbow();
      this.playersById = new ConcurrentHashMap<>();
      this.rainbowInfo = new ConcurrentHashMap<>();
      Bukkit.getPluginManager().registerEvents(new HeroicArmorListener(), plugin);
      ProtocolLibrary.getProtocolManager()
         .addPacketListener(
            new PacketAdapter(ArmorSetPlugin.getInstance(), ListenerPriority.LOW, Server.ENTITY_EQUIPMENT, Server.SET_SLOT, Server.WINDOW_ITEMS, Server.ENTITY_METADATA) {
               public void onPacketSending(PacketEvent event) {
                  try {
                     Player sender = event.getPlayer();
                     PacketContainer packet = event.getPacket();
                     PacketType packetType = event.getPacketType();
                     if (packetType == Server.ENTITY_EQUIPMENT) {
                        this.handleEntityEquipment(packet, sender);
                     } else if (packetType == Server.SET_SLOT) {
                        this.handleSetSlot(packet, sender);
                     } else if (packetType == Server.WINDOW_ITEMS) {
                        this.handleWindowItems(packet, sender);
                     } else if (packetType == Server.ENTITY_METADATA) {
                        this.handleEntityMetadata(packet, sender);
                     }
                  } catch (Throwable $ex) {
                     throw $ex;
                  }
               }

               private void handleEntityMetadata(PacketContainer packet, Player sender) {
                  java.util.List<com.comphenix.protocol.wrappers.WrappedWatchableObject> watchables = packet.getWatchableCollectionModifier().read(0);
                  if (watchables != null) {
                     boolean modified = false;
                     for (com.comphenix.protocol.wrappers.WrappedWatchableObject watchable : watchables) {
                        if (watchable.getIndex() == 10) {
                           Object val = watchable.getValue();
                           ItemStack itemStack = null;
                           if (val instanceof ItemStack) {
                              itemStack = (ItemStack) val;
                           } else if (val != null && val.getClass().getName().contains("ItemStack")) {
                              itemStack = org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asBukkitCopy((net.minecraft.server.v1_8_R3.ItemStack) val);
                           }

                           if (itemStack != null && itemStack.getType() != Material.AIR) {
                              ItemStack copy = itemStack.clone();
                              boolean changed = false;

                              if (HeroicArmorAPI.this.isHeroicHook(copy, true) || HeroicArmorAPI.this.isHeroicWeapon(copy, true)) {
                                 HeroicWeaponType weaponType = HeroicWeaponType.fromPossibleType(copy.getType());
                                 if (weaponType != null) {
                                    copy.setType(weaponType.getType());
                                    copy.setDurability((short)0);
                                    changed = true;
                                 }
                              } else if (HeroicArmorAPI.this.isHeroicArmor(copy)) {
                                 HeroicArmorType armorType = HeroicArmorType.fromPossibleType(copy.getType());
                                 if (armorType != null) {
                                    Color color;
                                    if (HeroicArmorAPI.this.isRainbowArmor(copy)) {
                                       color = HeroicArmorAPI.this.rainbow.getColorAtPercentDecimal(armorType.getRainbowPercent());
                                    } else {
                                       HeroicArmorColor ac = HeroicArmorAPI.this.getArmorColor(copy);
                                       color = ac != null ? ac.getArmorColor() : HeroicArmorColor.DARK_RED.getArmorColor();
                                    }
                                    copy = HeroicArmorAPI.this.createLeatherCopy(copy, armorType, color);
                                    changed = true;
                                 }
                              }

                              if (changed) {
                                 if (val instanceof ItemStack) {
                                    watchable.setValue(copy);
                                 } else {
                                    watchable.setValue(org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(copy));
                                 }
                                 modified = true;
                              }
                           }
                        }
                     }
                     if (modified) {
                        packet.getWatchableCollectionModifier().write(0, watchables);
                     }
                  }
               }

               private void handleEntityEquipment(PacketContainer packet, Player sender) {
                  int slot = (Integer)packet.getIntegers().read(1);
                  int entityId = (Integer)packet.getIntegers().read(0);
                  if (entityId != sender.getEntityId() || sender.getGameMode() == GameMode.SURVIVAL) {
                     ItemStack itemStack = (ItemStack)packet.getItemModifier().read(0);
                     if (itemStack != null && itemStack.getType() != Material.AIR) {
                        if (slot == 0) {
                           if (!HeroicArmorAPI.this.isHeroicHook(itemStack, true) && !HeroicArmorAPI.this.isHeroicWeapon(itemStack, true)) {
                              return;
                           }

                           HeroicWeaponType weaponType = HeroicWeaponType.fromPossibleType(itemStack.getType());
                           if (weaponType == null) {
                              return;
                           }

                           ItemStack weaponCopy = new ItemBuilder(itemStack).enchantments(itemStack.getEnchantments()).type(weaponType.getType());
                           weaponCopy.setDurability((short)0);
                           weaponCopy.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                           ItemMeta weaponMeta = weaponCopy.getItemMeta();
                           weaponMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
                           weaponCopy.setItemMeta(weaponMeta);
                           NBTItem weaponNbt = new NBTItem(weaponCopy);
                           weaponNbt.setByte("Unbreakable", (byte)1);
                           packet.getItemModifier().write(0, weaponNbt.getItem());
                        } else {
                            if (!HeroicArmorAPI.this.isHeroicArmor(itemStack)) {
                               return;
                            }

                            if (HeroicArmorAPI.this.hasLeatherStarItem(itemStack)) {
                               return;
                            }

                           NBTItem nbt = new NBTItem(itemStack);

                           HeroicArmorType armorType = HeroicArmorType.fromPossibleType(itemStack.getType());
                           if (armorType == null) {
                              return;
                           }

                           Color color;
                           if (HeroicArmorAPI.this.isRainbowArmor(itemStack)) {
                              color = HeroicArmorAPI.this.rainbow.getColorAtPercentDecimal(armorType.getRainbowPercent());
                           } else {
                              HeroicArmorColor ac = HeroicArmorAPI.this.getArmorColor(itemStack);
                              color = ac != null ? ac.getArmorColor() : HeroicArmorColor.DARK_RED.getArmorColor();
                           }

                           packet.getItemModifier().write(0, HeroicArmorAPI.this.createLeatherCopy(itemStack, armorType, color));
                        }
                     }
                  }
               }

               private void handleSetSlot(PacketContainer packet, Player sender) {
                  if (sender.getGameMode() == GameMode.SURVIVAL) {
                     ItemStack itemStack = (ItemStack)packet.getItemModifier().read(0);
                     if (itemStack != null && itemStack.getType() != Material.AIR) {
                        if (HeroicArmorAPI.this.isHeroicHook(itemStack, true) || HeroicArmorAPI.this.isHeroicWeapon(itemStack, true)) {
                           HeroicWeaponType weaponType = HeroicWeaponType.fromPossibleType(itemStack.getType());
                           if (weaponType != null) {
                              Material type = weaponType.getType();
                              Material oldType = itemStack.getType();
                              itemStack.setType(type);
                              itemStack.setDurability((short)0);
                              itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                              ItemMeta itemMeta = itemStack.getItemMeta();
                              itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
                              itemStack.setItemMeta(itemMeta);
                              NBTItem setSlotNbt = new NBTItem(itemStack);
                              setSlotNbt.setByte("Unbreakable", (byte)1);
                              packet.getItemModifier().write(0, setSlotNbt.getItem());
                              return;
                           }
                        }

                        if (HeroicArmorAPI.this.isHeroicArmor(itemStack)) {
                           int slot = (Integer)packet.getIntegers().read(1);
                           if (slot >= 5 && slot <= 8 && HeroicArmorAPI.this.hasLeatherStarItem(itemStack)) {
                              return;
                           }

                           NBTItem nbt = new NBTItem(itemStack);

                           HeroicArmorType armorType = HeroicArmorType.fromPossibleType(itemStack.getType());
                           if (armorType == null) {
                              return;
                           }

                           Color color;
                           if (HeroicArmorAPI.this.isRainbowArmor(itemStack)) {
                              color = HeroicArmorAPI.this.rainbow.getColorAtPercentDecimal(armorType.getRainbowPercent());
                           } else {
                              HeroicArmorColor ac = HeroicArmorAPI.this.getArmorColor(itemStack);
                              color = ac != null ? ac.getArmorColor() : HeroicArmorColor.DARK_RED.getArmorColor();
                           }

                           packet.getItemModifier().write(0, HeroicArmorAPI.this.createLeatherCopy(itemStack, armorType, color));
                        }
                     }
                  }
               }

               private void handleWindowItems(PacketContainer packet, Player sender) {
                  if (sender.getGameMode() == GameMode.SURVIVAL) {
                     ItemStack[] itemStacks = (ItemStack[])packet.getItemArrayModifier().read(0);
                     if (itemStacks != null) {
                        boolean modified = false;

                        for (int i = 0; i < itemStacks.length; i++) {
                           ItemStack itemStack = itemStacks[i];
                           if (itemStack != null && itemStack.getType() != Material.AIR) {
                              if (HeroicArmorAPI.this.isHeroicHook(itemStack, true) || HeroicArmorAPI.this.isHeroicWeapon(itemStack, true)) {
                                 HeroicWeaponType weaponType = HeroicWeaponType.fromPossibleType(itemStack.getType());
                                 if (weaponType != null) {
                                    Material type = weaponType.getType();
                                    Material oldType = itemStack.getType();
                                    itemStack.setType(type);
                                    itemStack.setDurability((short)0);
                                    itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                                    ItemMeta itemMeta = itemStack.getItemMeta();
                                    itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
                                    itemStack.setItemMeta(itemMeta);
                                    NBTItem windowNbt = new NBTItem(itemStack);
                                    windowNbt.setByte("Unbreakable", (byte)1);
                                    itemStacks[i] = windowNbt.getItem();
                                    modified = true;
                                    continue;
                                 }
                              }

                              if (HeroicArmorAPI.this.isHeroicArmor(itemStack)) {
                                 boolean skipRainbow = (i >= 5 && i <= 8) && HeroicArmorAPI.this.hasLeatherStarItem(itemStack);
                                 if (!skipRainbow) {
                                    HeroicArmorType armorType = HeroicArmorType.fromPossibleType(itemStack.getType());
                                    if (armorType != null) {
                                       Color color;
                                       if (HeroicArmorAPI.this.isRainbowArmor(itemStack)) {
                                          color = HeroicArmorAPI.this.rainbow.getColorAtPercentDecimal(armorType.getRainbowPercent());
                                       } else {
                                          HeroicArmorColor ac = HeroicArmorAPI.this.getArmorColor(itemStack);
                                          color = ac != null ? ac.getArmorColor() : HeroicArmorColor.DARK_RED.getArmorColor();
                                       }

                                       itemStacks[i] = HeroicArmorAPI.this.createLeatherCopy(itemStack, armorType, color);
                                       modified = true;
                                    }
                                 }
                              }
                           }
                        }

                        if (modified) {
                           packet.getItemArrayModifier().write(0, itemStacks);
                        }
                     }
                  }
               }
            }
         );
      (new BukkitRunnable() {
         public void run() {
            if (!HeroicArmorAPI.this.rainbowInfo.isEmpty()) {
               HeroicArmorAPI.this.rainbow.update(1);
               Map<HeroicArmorType, Color> colors = new HashMap<>();

               for (HeroicArmorType heroicArmorType : HeroicArmorType.values()) {
                  colors.put(heroicArmorType, HeroicArmorAPI.this.rainbow.getColorAtPercentDecimal(heroicArmorType.getRainbowPercent()));
               }

               synchronized (HeroicArmorAPI.this.rainbowInfo) {
                  Iterator<Entry<Player, List<RainbowArmorInfo>>> it = HeroicArmorAPI.this.rainbowInfo.entrySet().iterator();

                  while (it.hasNext()) {
                     Entry<Player, List<RainbowArmorInfo>> entry = it.next();
                     Player player = entry.getKey();
                     if (player == null || !player.isOnline() || player.isDead()) {
                        it.remove();
                     } else if (!player.hasMetadata("invis") && !player.hasMetadata("vanish")) {
                        for (RainbowArmorInfo rainbowArmorInfo : entry.getValue()) {
                           Color color = colors.get(rainbowArmorInfo.getArmorType());
                           if (color != null) {
                              rainbowArmorInfo.update(color);
                              ItemStack leatherCopy = rainbowArmorInfo.getLeatherCopy();
                               boolean starItemEquipped = false;
                               org.minecurse.features.types.staritems.data.StarItem starItem = null;
                               de.tr7zw.nbtapi.NBTItem nbt = new de.tr7zw.nbtapi.NBTItem(rainbowArmorInfo.getOriginalItemStack());
                               if (nbt.hasKey("starType")) {
                                  starItem = org.minecurse.features.FeaturesPlugin.getInstance().getFeatureManager().getByClass(org.minecurse.features.types.staritems.StarItemFeature.class).getManager().getStarItemEquippedToItemStack(rainbowArmorInfo.getOriginalItemStack());
                                  if (starItem != null && (starItem.isViewable() || starItem.isUpdate())) {
                                     starItemEquipped = true;
                                  }
                               }
                               
                               if (starItemEquipped) {
                                  ItemStack displayMat = starItem.getDisplayMaterial();
                                  boolean isLeatherDisplay = displayMat != null && (
                                     displayMat.getType() == Material.LEATHER_HELMET ||
                                     displayMat.getType() == Material.LEATHER_CHESTPLATE ||
                                     displayMat.getType() == Material.LEATHER_LEGGINGS ||
                                     displayMat.getType() == Material.LEATHER_BOOTS
                                  );
                                  
                                  if (isLeatherDisplay) {
                                     org.bukkit.Bukkit.getLogger().fine("[CurseArmorSets] Skipping rainbow for leather star item: " + starItem.getName() + " on " + player.getName());
                                  } else {
                                     int armorSlot = HeroicArmorAPI.this.getArmorSlotOfType(rainbowArmorInfo.getArmorType());
                                     if (armorSlot != -1) {
                                        HeroicArmorAPI.this.updateForPlayer(player, armorSlot, leatherCopy);
                                     }
                                  }
                               } else {
                                  HeroicArmorAPI.this.sendArmorUpdate(player, leatherCopy, rainbowArmorInfo.getArmorType());
                               }
                           }
                        }
                     }
                  }
               }
            }
         }
      }).runTaskTimerAsynchronously(ArmorSetPlugin.getInstance(), 200L, 5L);
   }

   public ItemStack createLeatherCopy(ItemStack diamondItem, HeroicArmorType armorType, Color color) {
      ItemStack leatherCopy = new ItemStack(armorType.getLeatherType());
      ItemMeta originalMeta = diamondItem.getItemMeta();
      LeatherArmorMeta leatherMeta = (LeatherArmorMeta)leatherCopy.getItemMeta();
      leatherMeta.setColor(color);
      if (originalMeta != null) {
         if (originalMeta.hasDisplayName()) {
            leatherMeta.setDisplayName(originalMeta.getDisplayName());
         }

         if (originalMeta.hasLore()) {
            leatherMeta.setLore(originalMeta.getLore());
         }

         for (ItemFlag flag : originalMeta.getItemFlags()) {
            leatherMeta.addItemFlags(new ItemFlag[]{flag});
         }
      }

      leatherMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      leatherCopy.setItemMeta(leatherMeta);

      for (java.util.Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : diamondItem.getEnchantments().entrySet()) {
         leatherCopy.addUnsafeEnchantment(entry.getKey(), entry.getValue());
      }

      de.tr7zw.nbtapi.NBTItem oldNbt = new de.tr7zw.nbtapi.NBTItem(diamondItem);
      if (oldNbt.hasKey("starType")) {
         de.tr7zw.nbtapi.NBTItem newNbt = new de.tr7zw.nbtapi.NBTItem(leatherCopy);
         newNbt.setString("starType", oldNbt.getString("starType"));
         leatherCopy = newNbt.getItem();
      }

      return leatherCopy;
   }

   private void sendArmorUpdate(Player player, ItemStack itemStack, HeroicArmorType armorType) {
      int armorSlot = this.getArmorSlotOfType(armorType);
      if (armorSlot != -1) {
         this.updateForPlayer(player, armorSlot, itemStack);

         for (Player nearbyPlayer : PlayerUtils.getNearbyPlayers(player.getLocation(), 24.0, 24.0, 24.0)) {
            if (player != nearbyPlayer) {
               this.updateForOnline(nearbyPlayer, player, armorSlot, itemStack);
            }
         }
      }
   }

   private void updateForOnline(Player observer, Player other, int slot, ItemStack content) {
      PacketPlayOutEntityEquipment entityEquipment = new PacketPlayOutEntityEquipment(other.getEntityId(), slot, CraftItemStack.asNMSCopy(content));
      ((CraftPlayer)observer).getHandle().playerConnection.sendPacket(entityEquipment);
   }

   private void updateForPlayer(Player player, int armorSlot, ItemStack content) {
      EntityPlayer craft = ((CraftPlayer)player).getHandle();
      int inventorySlot = this.convertToInventorySlot(armorSlot);
      PacketPlayOutSetSlot slotPacket = new PacketPlayOutSetSlot(craft.defaultContainer.windowId, inventorySlot, CraftItemStack.asNMSCopy(content));
      craft.playerConnection.sendPacket(slotPacket);
   }

   private int getArmorSlotOfType(HeroicArmorType armorType) {
      switch (armorType) {
         case HELMET:
            return 4;
         case CHESTPLATE:
            return 3;
         case LEGGINGS:
            return 2;
         case BOOTS:
            return 1;
         default:
            return -1;
      }
   }

   private int convertToInventorySlot(int entityEquipmentSlot) {
      switch (entityEquipmentSlot) {
         case 1:
            return 8;
         case 2:
            return 7;
         case 3:
            return 6;
         case 4:
            return 5;
         default:
            return -1;
      }
   }

   public boolean isHeroicArmor(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         return MaterialList.ARMOR.hasMaterial(item.getItem().getType()) && item.getBoolean("heroic");
      } else {
         return false;
      }
   }

   public boolean isHeroicWeapon(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         return MaterialList.WEAPONS.hasMaterial(item.getItem().getType()) && item.getBoolean("heroic");
      } else {
         return false;
      }
   }

   public boolean isHeroicWeapon(ItemStack itemStack, boolean starItem) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         return MaterialList.WEAPONS.hasMaterial(item.getItem().getType()) && item.getBoolean("heroic") && !item.hasKey("starType");
      } else {
         return false;
      }
   }

   public boolean isHeroicHook(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         return item.hasKey("heroicHook");
      } else {
         return false;
      }
   }

   public boolean isHeroicHook(ItemStack itemStack, boolean starItem) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         return item.hasKey("heroicHook") && !item.hasKey("starType");
      } else {
         return false;
      }
   }

   public boolean isRainbowArmor(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         return MaterialList.ARMOR.hasMaterial(itemStack.getType()) && item.getBoolean("rainbow");
      } else {
         return false;
      }
   }

   public boolean hasLeatherStarItem(ItemStack itemStack) {
      if (itemStack == null || itemStack.getType() == Material.AIR) return false;
      NBTItem nbt = new NBTItem(itemStack);
      if (!nbt.hasKey("starType")) return false;
      try {
         org.minecurse.features.types.staritems.data.StarItem starItem =
            org.minecurse.features.FeaturesPlugin.getInstance().getFeatureManager()
               .getByClass(org.minecurse.features.types.staritems.StarItemFeature.class)
               .getManager().getStarItemEquippedToItemStack(itemStack);
         if (starItem != null && (starItem.isViewable() || starItem.isUpdate())) {
            ItemStack displayMat = starItem.getDisplayMaterial();
            if (displayMat != null) {
               Material t = displayMat.getType();
               return t == Material.LEATHER_HELMET || t == Material.LEATHER_CHESTPLATE
                     || t == Material.LEATHER_LEGGINGS || t == Material.LEATHER_BOOTS;
            }
         }
      } catch (Exception ignored) {}
      return false;
   }

   public boolean isUpgrade(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         return item.getItem().getType() != Material.AIR && item.hasKey("heroicUpgrade");
      } else {
         return false;
      }
   }

   public boolean isNormalUpgrade(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         return item.getItem().getType() != Material.AIR && item.hasKey("heroicUpgrade") && !item.hasKey("armorSet");
      } else {
         return false;
      }
   }

   public boolean isRainbowUpgrade(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         return item.getItem().getType() != Material.AIR && item.hasKey("heroicUpgrade") && item.getBoolean("rainbowUpgrade");
      } else {
         return false;
      }
   }

   public boolean isArmorSetUpgrade(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         return item.getItem().getType() != Material.AIR && item.hasKey("heroicUpgrade") && item.hasKey("armorSet");
      } else {
         return false;
      }
   }

   public boolean isHookUpgrade(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         return item.getItem().getType() != Material.AIR && item.hasKey("hookUpgrade");
      } else {
         return false;
      }
   }

   public int getChance(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0) {
         NBTItem item = new NBTItem(itemStack);
         if (this.isNormalUpgrade(itemStack)) {
            return item.getInteger("chance");
         } else if (this.isArmorSetUpgrade(itemStack) || this.isRainbowUpgrade(itemStack)) {
            return item.getInteger("chance");
         } else {
            return this.isHookUpgrade(itemStack) ? item.getInteger("chance") : 100;
         }
      } else {
         return 0;
      }
   }

   public ItemStack getNormalUpgrade() {
      return this.getNormalUpgrade(100, false);
   }

   public ItemStack getNormalUpgrade(int chance, boolean forArmor) {
      NBTItem item = new NBTItem(
         new ItemBuilder(Material.INK_SACK, DyeColor.RED.getDyeData())
            .name("&6&lHeroic Upgrade (&e" + chance + "%&6&l)")
            .lore(new String[]{" ", "&7Apply this to any non-specialty", "&7" + (forArmor ? "Armor Piece" : "Weapon") + " to imbue it with Heroic power.", " "})
            .glow()
      );
      item.setBoolean("heroicUpgrade", true);
      item.setInteger("chance", chance);
      if (forArmor) {
         item.setString("armorUpgrade", "yessir");
      }

      return item.getItem();
   }

   public ItemStack getHookUpgrade(int chance) {
      NBTItem item = new NBTItem(
         new ItemBuilder(Material.FIREBALL)
            .name("&6&lHeroic (&b&lDiamond Hook&6&l) Upgrade (&e" + chance + "%&6&l)")
            .lore(new String[]{"", "&7Apply this to &b&lDiamond Hook", "&7to imbue it with Heroic power.", ""})
      );
      item.setBoolean("hookUpgrade", true);
      item.setInteger("chance", chance);
      return item.getItem();
   }

   public ItemStack getHookUpgrade() {
      return this.getHookUpgrade(100);
   }

   public ItemStack getRainbowUpgrade() {
      return this.getRainbowUpgrade(100);
   }

   public ItemStack getRainbowUpgrade(int chance) {
      NBTItem item = new NBTItem(
         new ItemBuilder(Material.INK_SACK, DyeColor.RED.getDyeData())
            .name("&6&lHeroic (&c&lR&6&la&e&li&a&ln&b&lb&d&lo&5&lw&6&l) Upgrade")
            .lore(
               new String[]{
                  " ",
                  "&7Apply this to &7&nany&r&7 armor piece",
                  "&7for a &e" + chance + "% &7chance to imbue it with the",
                  "&7power of Heroic Armor and a",
                  "&7special Rainbow ability.",
                  " "
               }
            )
            .glow()
      );
      item.setBoolean("heroicUpgrade", true);
      item.setBoolean("rainbowUpgrade", true);
      item.setInteger("chance", chance);
      return item.getItem();
   }

   public ItemStack getArmorSetUpgrade(ArmorSet armorSet) {
      return this.getArmorSetUpgrade(armorSet, 100, false);
   }

   public ItemStack getArmorSetUpgrade(ArmorSet armorSet, int chance, boolean forArmor) {
      NBTItem item = new NBTItem(
         new ItemBuilder(Material.INK_SACK, DyeColor.RED.getDyeData())
            .name("&6&lHeroic (" + armorSet.getDisplayName() + "&6&l) Upgrade")
            .lore(
               new String[]{
                  " ",
                  "&7Apply this to any " + armorSet.getDisplayName() + " &7" + (forArmor ? "Armor Piece" : "Weapon"),
                  "&7for a &e" + chance + "% &7chance to imbue it",
                  "&7with the power of Heroic Armor.",
                  " "
               }
            )
            .glow()
      );
      item.setBoolean("heroicUpgrade", true);
      item.setString("armorSet", armorSet.getInternalName());
      item.setInteger("chance", chance);
      if (forArmor) {
         item.setString("armorUpgrade", "yessir");
      }

      return item.getItem();
   }

   public ItemStack makeHookHeroic(ItemStack item) {
      ItemBuilder builder = new ItemBuilder(item);
      builder.lore("&6This weapon is stronger than diamond.");
      NBTItem item2 = new NBTItem(item);
      item2.setBoolean("heroicHook", true);
      return item2.getItem();
   }

   public ItemStack makeHeroic(ItemStack itemStack) {
      if (MaterialUtil.isWeapon(itemStack.getType())) {
         HeroicWeaponType weaponType = HeroicWeaponType.fromPossibleType(itemStack.getType());
         if (weaponType == null) {
            return itemStack;
         }

         HeroicArmorColor armorColor = this.getArmorColor(itemStack);
         if (armorColor == null) {
            return itemStack;
         }

         ItemMeta itemMeta = itemStack.getItemMeta();
         List<String> lore = itemMeta.getLore();
         lore.add(armorColor.getChatColor() + "This weapon is stronger than diamond.");
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         NBTItem item = new NBTItem(itemStack);
         item.setBoolean("heroic", true);
         return item.getItem();
      } else if (MaterialUtil.isArmor(itemStack.getType())) {
         HeroicArmorType armorType = HeroicArmorType.fromPossibleType(itemStack.getType());
         if (armorType == null) {
            return itemStack;
         }

         HeroicArmorColor armorColor = this.getArmorColor(itemStack);
         if (armorColor == null) {
            return itemStack;
         }

         ItemMeta itemMeta = itemStack.getItemMeta();
         List<String> lore = itemMeta.getLore();
         lore.add(armorColor.getChatColor() + "This armor is stronger than diamond.");
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         NBTItem item = new NBTItem(itemStack);
         item.setBoolean("heroic", true);
         return item.getItem();
      } else {
         return itemStack;
      }
   }

   public HeroicArmorColor getArmorColor(ItemStack itemStack) {
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null && itemMeta.hasDisplayName()) {
         boolean darkGray = true;
         String displayName = itemStack.getItemMeta().getDisplayName().replace('§', '&');

         for (int index = 0; index < displayName.length(); index++) {
            char section = displayName.charAt(index);
            if (section == '&' && index < displayName.length() - 1) {
               char c = displayName.charAt(index + 1);
               ChatColor color = ChatColor.getByChar(c);
               if (color == ChatColor.DARK_GRAY) {
                  darkGray = true;
               } else if (color != null) {
                  return HeroicArmorColor.fromChatColor(color);
               }
            }
         }

         return darkGray ? HeroicArmorColor.DARK_GRAY : HeroicArmorColor.RED;
      } else {
         return HeroicArmorColor.DARK_RED;
      }
   }

   public void addRainbowInfo(Player player, ItemStack itemStack) {
      NBTItem item = new NBTItem(itemStack);
      List<RainbowArmorInfo> armorInfo = this.rainbowInfo.computeIfAbsent(player, p -> new ArrayList<>());
      armorInfo.add(new RainbowArmorInfo(player, HeroicArmorType.fromPossibleType(itemStack.getType()), itemStack));
   }

   public void removeRainbowInfo(Player player, ItemStack itemStack) {
      List<RainbowArmorInfo> armorInfo = this.rainbowInfo.computeIfAbsent(player, p -> new ArrayList<>());
      HeroicArmorType armorType = HeroicArmorType.fromPossibleType(itemStack.getType());
      armorInfo.removeIf(info -> info.getArmorType() == armorType);
      if (armorInfo.isEmpty()) {
         this.rainbowInfo.remove(player);
      }
   }

   public Rainbow getRainbow() {
      return this.rainbow;
   }

   public Map<Integer, Player> getPlayersById() {
      return this.playersById;
   }

   public Map<Player, List<RainbowArmorInfo>> getRainbowInfo() {
      return this.rainbowInfo;
   }
}
