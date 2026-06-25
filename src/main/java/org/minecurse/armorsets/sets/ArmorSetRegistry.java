package org.minecurse.armorsets.sets;

import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBTItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.RandomUtil;
import org.minecurse.commons.utils.StringUtil;

public class ArmorSetRegistry {
   private final HashMap<UUID, ArmorSet> activePlayerSet;
   private final HashMap<UUID, List<ArmorSet>> activePlayerCrystals;
   private final List<ArmorSet> registeredSets = new ArrayList<>();
   private final ArmorSetPlugin armorPlugin;

   public HashMap<UUID, ArmorSet> getActivePlayerSet() {
      return this.activePlayerSet;
   }

   public HashMap<UUID, List<ArmorSet>> getActivePlayerCrystals() {
      return this.activePlayerCrystals;
   }

   public List<ArmorSet> getRegisteredSets() {
      return this.registeredSets;
   }

   public ArmorSetPlugin getArmorPlugin() {
      return this.armorPlugin;
   }

   public ArmorSetRegistry(ArmorSetPlugin armorPlugin) {
      this.armorPlugin = armorPlugin;
      this.activePlayerCrystals = new HashMap<>();
      this.activePlayerSet = new HashMap<>();
   }

   public void registerArmorSet(ArmorSet armorSet) {
      this.registeredSets.add(armorSet);
      Bukkit.getConsoleSender().sendMessage(StringUtil.color("&2[CurseArmor] &f" + armorSet.getInternalName() + " &7has been &aEnabled&7!"));
   }

   public ArmorSet getByName(String name) {
      return this.registeredSets.stream().filter(armorSet -> armorSet.getInternalName().equalsIgnoreCase(name)).findFirst().orElse(null);
   }

   public ArmorSet getActiveSet(Player player) {
      return this.activePlayerSet.getOrDefault(player.getUniqueId(), null);
   }

   public boolean hasActiveSet(Player player) {
      return this.activePlayerSet.containsKey(player.getUniqueId());
   }

   public boolean isArmorSet(String name) {
      return this.getByName(name) != null;
   }

   public ArmorSet getByItem(ItemStack item) {
      return this.registeredSets.stream().filter(armorSet -> armorSet.isWearingPiece(item)).findFirst().orElse(null);
   }

   public void setActiveSet(Player player, ArmorSet armorSet) {
      if (armorSet == null) {
         this.activePlayerSet.remove(player.getUniqueId());
      } else {
         this.activePlayerSet.put(player.getUniqueId(), armorSet);
         PlayerUtils.playSound(player, Sound.HORSE_ARMOR, 1.75F);
         player.sendMessage(armorSet.armorEquipMessage());
         armorSet.onEquip(player);
      }
   }

   public List<ArmorSet> getCrystals(Player player) {
      List<ArmorSet> crystals = Lists.newArrayList();

      for (ItemStack itemStack : player.getInventory().getArmorContents()) {
         if (itemStack != null && !itemStack.getType().equals(Material.AIR) && this.hasCrystal(itemStack)) {
            crystals.addAll(this.getCrystals(itemStack));
         }
      }

      return crystals;
   }

   public boolean isCrystal(ItemStack item) {
      if (item.getType() != Material.NETHER_STAR) {
         return false;
      }

      NBTItem nbtItem = new NBTItem(item);
      return nbtItem.hasKey("crystalType");
   }

   public boolean hasCrystal(ItemStack item) {
      if (item != null && !item.getType().equals(Material.AIR)) {
         NBTItem nbtItem = new NBTItem(item);
         return nbtItem.hasKey("crystalType");
      } else {
         return false;
      }
   }

   public int getPercent(ItemStack item) {
      if (item != null && item.getType().equals(Material.NETHER_STAR)) {
         NBTItem nbtItem = new NBTItem(item);
         return !nbtItem.hasKey("crystalPercent") ? -1 : nbtItem.getInteger("crystalPercent");
      } else {
         return -1;
      }
   }

   public boolean hasArmorSetCrystal(ArmorSet set, Player player) {
      return this.getCrystals(player) != null && !this.getCrystals(player).isEmpty() && this.getCrystals(player).contains(set);
   }

   public Map<ArmorSet, Double> getCrystalAbilityChances(Player player) {
      HashMap<ArmorSet, Double> levels = new HashMap<>();

      for (ArmorSet set : this.getCrystals(player)) {
         double chances = 0.0;
         if (set.getCrystal().abilityChance() > 0.0) {
            chances += set.getCrystal().abilityChance();
         }

         levels.put(set, chances);
      }

      return levels;
   }

   public List<ArmorSet> getCrystals(ItemStack item) {
      if (!this.hasCrystal(item)) {
         return Lists.newArrayList();
      }

      List<ArmorSet> c = new ArrayList<>();
      NBTItem nbtItem = new NBTItem(item);
      String joinedCrystals = nbtItem.getString("crystalType");
      List<String> stringList = Arrays.asList(joinedCrystals.split(","));
      stringList.forEach(s -> c.add(this.getByName(s)));
      return c;
   }

   public ItemStack applyCrystals(ItemStack item, List<ArmorSet> crystals) {
      if (item != null && !item.getType().equals(Material.AIR)) {
         ItemBuilder builder = new ItemBuilder(item);
         builder.lore(
            StringUtil.colorFormat(
               "&f&lARMOR CRYSTAL: &7({0}&7)", new Object[]{crystals.stream().map(ArmorSet::getDisplayName).collect(Collectors.joining(ChatColor.GRAY + ", "))}
            )
         );
         NBTItem nbtItem = new NBTItem(builder);
         List<String> nbtList = new ArrayList<>();
         crystals.forEach(armorSet -> nbtList.add(armorSet.getInternalName()));
         String joinedCrystals = String.join(",", nbtList);
         nbtItem.setString("crystalType", joinedCrystals);
         return nbtItem.getItem();
      } else {
         return null;
      }
   }

   public void removeActiveSet(Player player, ArmorSet armorSet) {
      if (this.activePlayerSet.containsKey(player.getUniqueId())) {
         this.activePlayerSet.remove(player.getUniqueId());
         player.sendMessage(armorSet.armorUnequipMessage());
         armorSet.onUnEquip(player);
      }
   }

   public ArmorSet getRandomAbility() {
      Collection<ArmorSet> allSets = this.getRegisteredSets();
      List<ArmorSet> eligibleSets = allSets.stream()
         .filter(
            set -> !set.getInternalName().equalsIgnoreCase("Lucky")
               && !set.getInternalName().equalsIgnoreCase("Citadel")
               && !set.getInternalName().equalsIgnoreCase("Fortune")
               && !set.isHidden()
         )
         .collect(Collectors.toList());
      if (eligibleSets.isEmpty()) {
         return null;
      }

      int randomIndex = RandomUtil.getRandInt(0, eligibleSets.size() - 1);
      return eligibleSets.get(randomIndex);
   }
}
