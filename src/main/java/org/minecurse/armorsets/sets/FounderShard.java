package org.minecurse.armorsets.sets;

import com.golfing8.winespigot.armorequip.ArmorEquipEvent;
import de.tr7zw.nbtapi.NBTItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.types.FortuneSet;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.item.ItemNames;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.MaterialUtil;

public class FounderShard implements Listener {
   private static final ArmorSetRegistry registry = ArmorSetPlugin.getInstance().getArmorSetRegistry();
   public static final List<ArmorSet> valid = new ArrayList<>(registry.getRegisteredSets());
   private static final List<UUID> uuidsEquipped = new ArrayList<>();
   private final Random random = new Random();

   public Random getRandom() {
      return this.random;
   }

   public FounderShard() {
      valid.removeIf(FortuneSet.class::isInstance);
   }

   public static boolean isItem(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() == Material.PRISMARINE_SHARD) {
         NBTItem item = new NBTItem(itemStack);
         return item.hasKey("founder_shard");
      } else {
         return false;
      }
   }

   public static ItemStack buildItem() {
      ItemBuilder builder = new ItemBuilder(Material.PRISMARINE_SHARD);
      builder.name("&4&lFounder Shard");
      builder.lore("&fDo the fusion dance and obtain the");
      builder.lore("&fabilities from ALL armor sets!");
      builder.lore("");
      builder.lore("&c&lAbilities: ");
      valid.forEach(set -> builder.lore("&f • " + set.getDisplayName() + " &fArmor Set Ability"));
      builder.lore("");
      builder.lore("&7&oAttach this item to any armor");
      builder.lore("&7&opiece to receive effects!");
      builder.lore("");
      builder.lore("&7To attach, place this item on any armor.");
      builder.lore("&7Once attached, it cannot be removed!");
      NBTItem item = new NBTItem(builder);
      item.setBoolean("founder_shard", true);
      return item.getItem();
   }

   public static boolean hasFounderShardEquipped(Player player) {
      return uuidsEquipped.contains(player.getUniqueId());
   }

   public static boolean hasItem(ItemStack itemStack) {
      if (itemStack != null && MaterialUtil.isArmor(itemStack.getType())) {
         NBTItem item = new NBTItem(itemStack);
         return item.hasKey("founder_shard");
      } else {
         return false;
      }
   }

   public ItemStack applyToItem(ItemStack item) {
      ItemBuilder builder = new ItemBuilder(item);
      builder.lore("&f&lSTAR ITEM: &4&lFounder Shard");
      NBTItem nbt = new NBTItem(builder);
      nbt.setBoolean("founder_shard", true);
      return nbt.getItem();
   }

   @EventHandler(priority = EventPriority.HIGH)
   public void onArmorChange(ArmorEquipEvent event) {
      Player player = event.getPlayer();
      ItemStack oldPiece = event.getOldArmorPiece();
      ItemStack newPiece = event.getNewArmorPiece();
      boolean hadArmor = oldPiece != null && oldPiece.getType() != Material.AIR;
      boolean hasArmor = newPiece != null && newPiece.getType() != Material.AIR;
      if (hadArmor && hasItem(oldPiece)) {
         player.sendMessage(ArmorSetPlugin.getPrefix("&7You have un-equipped a &4&lFounder Shard&7.", false));
         uuidsEquipped.remove(player.getUniqueId());
         valid.forEach(set -> set.onUnEquip(player));
      }

      if (hasArmor && hasItem(newPiece)) {
         player.sendMessage(ArmorSetPlugin.getPrefix("&7You have equipped a &4&lFounder Shard&7.", false));
         uuidsEquipped.add(player.getUniqueId());
         valid.forEach(set -> set.onEquip(player));
      }
   }

   @EventHandler
   public void onApplyToItem(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      ItemStack item = event.getCursor();
      if (item != null && item.getType() != Material.AIR) {
         ItemStack attemptingToApplyTo = event.getCurrentItem();
         if (isItem(item)) {
            if (attemptingToApplyTo != null && attemptingToApplyTo.getType() != Material.AIR) {
               if (hasItem(attemptingToApplyTo)) {
                  player.sendMessage(ArmorSetPlugin.getPrefix("&cThis item already has a &4&lFounder Shard&c!", false));
               } else if (!MaterialUtil.isArmor(attemptingToApplyTo)) {
                  player.sendMessage(ArmorSetPlugin.getPrefix("&cYou may only apply &4&lFounder Shards&c on armor pieces!", false));
               } else {
                  event.setCancelled(true);
                  if (item.getAmount() > 1) {
                     item.setAmount(item.getAmount() - 1);
                  } else {
                     player.setItemOnCursor(null);
                  }

                  ItemStack newItem = this.applyToItem(attemptingToApplyTo);
                  event.setCurrentItem(newItem);
                  player.sendMessage(
                     ArmorSetPlugin.getPrefix("&7You have applied a &4&lFounder Shard &7on your " + ItemNames.lookup(attemptingToApplyTo), false)
                  );
                  player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
                  player.updateInventory();
               }
            }
         }
      }
   }

   @EventHandler
   public void onDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
         Player attacker = (Player)event.getDamager();
         Player hit = (Player)event.getEntity();
         if (LocUtil.canPvp(attacker, attacker.getLocation()) && LocUtil.canPvp(hit, hit.getLocation())) {
            if (!FactionUtil.isAlly(attacker, hit)) {
               if (uuidsEquipped.contains(attacker.getUniqueId()) && !valid.isEmpty()) {
                  ArmorSet randomSet = this.getRandomSet();
                  randomSet.onAttack(attacker, hit, event);
               }

               if (uuidsEquipped.contains(hit.getUniqueId()) && !valid.isEmpty()) {
                  ArmorSet randomSet = this.getRandomSet();
                  randomSet.onDamaged(hit, attacker, event);
               }
            }
         }
      }
   }

   private ArmorSet getRandomSet() {
      int randomIndex = this.random.nextInt(valid.size());
      return valid.get(randomIndex);
   }
}
