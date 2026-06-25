package org.minecurse.armorsets.struct.heroic;

import java.util.Map.Entry;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class RainbowArmorInfo {
   private final Player player;
   private final HeroicArmorType armorType;
   private final ItemStack originalItemStack;
   private ItemStack leatherCopy;

   public Player getPlayer() {
      return this.player;
   }

   public HeroicArmorType getArmorType() {
      return this.armorType;
   }

   public ItemStack getOriginalItemStack() {
      return this.originalItemStack;
   }

   public ItemStack getLeatherCopy() {
      return this.leatherCopy;
   }

   public RainbowArmorInfo(Player player, HeroicArmorType armorType, ItemStack originalItemStack) {
      this.player = player;
      this.armorType = armorType;
      this.originalItemStack = originalItemStack;
      this.leatherCopy = new ItemStack(armorType.getLeatherType());
      ItemMeta originalMeta = originalItemStack.getItemMeta();
      LeatherArmorMeta leatherMeta = (LeatherArmorMeta)this.leatherCopy.getItemMeta();
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
      this.leatherCopy.setItemMeta(leatherMeta);

      for (Entry<Enchantment, Integer> entry : originalItemStack.getEnchantments().entrySet()) {
         this.leatherCopy.addUnsafeEnchantment(entry.getKey(), entry.getValue());
      }
      
      de.tr7zw.nbtapi.NBTItem oldNbt = new de.tr7zw.nbtapi.NBTItem(originalItemStack);
      if (oldNbt.hasKey("starType")) {
         de.tr7zw.nbtapi.NBTItem newNbt = new de.tr7zw.nbtapi.NBTItem(this.leatherCopy);
         newNbt.setString("starType", oldNbt.getString("starType"));
         this.leatherCopy = newNbt.getItem();
      }
   }

   public void update(Color color) {
      LeatherArmorMeta meta = (LeatherArmorMeta)this.leatherCopy.getItemMeta();
      meta.setColor(color);
      this.leatherCopy.setItemMeta(meta);
   }
}
