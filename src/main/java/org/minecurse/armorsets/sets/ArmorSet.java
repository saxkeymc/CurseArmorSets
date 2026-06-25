package org.minecurse.armorsets.sets;

import de.tr7zw.nbtapi.NBTItem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.StringUtil;

@ArmorCrystal
public abstract class ArmorSet implements Listener {
   private final String internalName;
   private final double defaultOutgoing;
   private final double defaultIncoming;
   private final double weaponBonus;
   private final double weaponIncoming;
   private final ChatColor setColor;
   private final ArmorCrystal crystal;
   private final ItemStack material;
   private final Map<UUID, Cooldown> abilityCooldowns;
   protected String displayName;

   public String getInternalName() {
      return this.internalName;
   }

   public double getOutgoing() {
      try {
         return ArmorSetPlugin.getInstance().getDefaultConfig().getArmorOutgoing(this.internalName);
      } catch (Exception e) {
         return this.defaultOutgoing;
      }
   }

   public double getIncoming() {
      try {
         return ArmorSetPlugin.getInstance().getDefaultConfig().getArmorIncoming(this.internalName);
      } catch (Exception e) {
         return this.defaultIncoming;
      }
   }

   public boolean isHidden() {
      try {
         return ArmorSetPlugin.getInstance().getDefaultConfig().isHidden(this.internalName);
      } catch (Exception e) {
         return false;
      }
   }

   public double getWeaponBonus() {
      return this.weaponBonus;
   }

   public double getWeaponIncoming() {
      return this.weaponIncoming;
   }

   public ChatColor getSetColor() {
      return this.setColor;
   }

   public ArmorCrystal getCrystal() {
      return this.crystal;
   }

   public ItemStack getMaterial() {
      return this.material;
   }

   public Map<UUID, Cooldown> getAbilityCooldowns() {
      return this.abilityCooldowns;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public ArmorSet(
      String internalName,
      String displayName,
      ChatColor setColor,
      ItemStack displayItem,
      double outgoing,
      double incoming,
      double weaponBonus,
      double weaponIncoming
   ) {
      this.material = displayItem;
      this.internalName = internalName;
      this.displayName = displayName;
      this.defaultOutgoing = outgoing;
      this.defaultIncoming = incoming;
      this.weaponBonus = weaponBonus;
      this.weaponIncoming = weaponIncoming;
      this.setColor = setColor;
      this.crystal = this.getClass().getAnnotation(ArmorCrystal.class);
      Bukkit.getPluginManager().registerEvents(this, ArmorSetPlugin.getInstance());
      this.abilityCooldowns = new HashMap<>();
   }

   public String armorEquipMessage() {
      return ArmorSetPlugin.getPrefix("&7You have equipped the " + this.getDisplayName() + " &7armor set!", false);
   }

   public ItemStack getDisplayMaterial() {
      return this.material;
   }

   public String armorUnequipMessage() {
      return ArmorSetPlugin.getPrefix("&7You have un-equipped the " + this.getDisplayName() + " &7armor set!", false);
   }

   public ItemStack addNBT(ItemStack is, String internalName) {
      NBTItem item = new NBTItem(is);
      item.setString("armorSet", internalName.toLowerCase());
      return item.getItem();
   }

   public ItemStack makeHeroic(ArmorPiece piece) {
      ItemStack piece1 = this.buildArmor(piece);
      return piece1 == null ? piece1 : ArmorSetPlugin.getInstance().getApi().makeHeroic(piece1);
   }

   public boolean isWearingPiece(ItemStack item) {
      if (item != null && !item.getType().equals(Material.AIR)) {
         String set = new NBTItem(item).getString("armorSet");
         return set != null && set.equalsIgnoreCase(this.internalName);
      } else {
         return false;
      }
   }

   public boolean hasFullArmor(Player player) {
      for (ItemStack item : player.getInventory().getArmorContents()) {
         if (item == null || item.getType() == Material.AIR) {
            return false;
         }

         if (!this.isWearingPiece(item)) {
            return false;
         }
      }

      return true;
   }

   public String getPieceName(ArmorPiece armorPiece) {
      switch (armorPiece) {
         case HELMET:
            return "Helmet";
         case CHESTPLATE:
            return "Chestplate";
         case LEGGINGS:
            return "Leggings";
         case BOOTS:
            return "Boots";
         case SWORD:
            return "Sword";
         case AXE:
            return "Axe";
         case BOW:
            return "Bow";
         default:
            return "";
      }
   }

   public boolean hasWeapon(Player player) {
      ItemStack item = player.getItemInHand();
      if (item == null || item.getType() == Material.AIR) {
         return false;
      } else if (DiamondHook.isHook(item) && DiamondHook.getFromHook(item) != null) {
         ArmorSet armorSet = DiamondHook.getFromHook(item);
         return ArmorSetPlugin.getInstance().getArmorSetRegistry().getActiveSet(player) != null
            && ArmorSetPlugin.getInstance().getArmorSetRegistry().getActiveSet(player) == armorSet;
      } else {
         return this.isWearingPiece(item);
      }
   }

   public ItemStack getRedeemItem(boolean heroic) {
      ItemBuilder builder = new ItemBuilder(Material.CHEST)
         .name(this.getDisplayName() + " &7Crate")
         .lore(
            new String[]{
               "",
               "&7Left click this crate to",
               "&7preview this armor set.",
               "",
               "&7Right click this crate to",
               "&7receive the &a" + this.getInternalName() + " &7armor set."
            }
         );
      NBTItem item = new NBTItem(builder);
      item.setString("armorSet", this.getInternalName());
      item.setBoolean("redeemItem", true);
      if (heroic) {
         item.setBoolean("heroicRedeem", true);
      }

      return item.getItem();
   }

   public ItemStack getRandomItem(boolean heroic) {
      ItemBuilder builder = new ItemBuilder(Material.CHEST)
         .name(this.getDisplayName() + " &7Crate")
         .lore(
            new String[]{
               "",
               "&7Left click this crate to",
               "&7preview this armor set.",
               "",
               "&7Right click this crate to",
               "&7receive a random piece of the &a" + this.getInternalName() + " &7armor set."
            }
         );
      NBTItem item = new NBTItem(builder);
      item.setString("armorSet", this.getInternalName());
      item.setBoolean("randomItem", true);
      if (heroic) {
         item.setBoolean("heroicRandom", true);
      }

      return item.getItem();
   }

   public ItemStack getRandomPiece() {
      ItemStack itemStack = this.buildArmor(ArmorPiece.random());
      return itemStack != null ? itemStack : this.getRandomPiece();
   }

   public ItemStack getRandomWeapon() {
      ArmorPiece piece = ArmorPiece.SWORD;
      if (this.buildArmor(piece) == null) {
         piece = ArmorPiece.AXE;
         if (this.buildArmor(piece) == null) {
            piece = ArmorPiece.BOW;
         }
      }

      return this.buildArmor(piece);
   }

   public void sendAbilityMessage(LivingEntity entity, String ability, String message, Object... arguments) {
      String finalMessage = ArmorSetPlugin.getPrefix(StringUtil.colorFormat(this.getSetColor() + "&l* " + ability + " &7" + message, arguments), false);
      entity.getNearbyEntities(32.0, 32.0, 32.0)
         .stream()
         .filter(Player.class::isInstance)
         .map(Player.class::cast)
         .forEach(player -> player.sendMessage(finalMessage));
      entity.sendMessage(finalMessage);
   }

   public void onAttack(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
   }

   public void onAttackWithWeapon(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
   }

   public void onProjectile(Player armorHolder, LivingEntity shot, EntityDamageByEntityEvent event) {
   }

   public void onDamaged(Player armorHolder, LivingEntity damager, EntityDamageByEntityEvent event) {
   }

   public void onDefenseWithWeapon(Player armorHolder, LivingEntity damager, EntityDamageByEntityEvent event) {
   }

   public void onEquip(Player player) {
   }

   public void onUnEquip(Player player) {
   }

   public abstract ItemStack buildArmor(ArmorPiece var1);
}
