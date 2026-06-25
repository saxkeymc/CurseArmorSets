package org.minecurse.armorsets.sets;

import de.tr7zw.nbtapi.NBTItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.commons.item.ItemBuilder;

public class DiamondHook {
   public static boolean isHook(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() == Material.DIAMOND_HOE) {
         NBTItem item = new NBTItem(itemStack);
         return item.hasKey("diamond_hook");
      } else {
         return false;
      }
   }

   public static ArmorSet getFromHook(ItemStack stack) {
      if (!isHook(stack)) {
         return null;
      }

      NBTItem item = new NBTItem(stack);
      return !item.hasKey("diamond_hook") ? null : ArmorSetPlugin.getInstance().getArmorSetRegistry().getByName(item.getString("armorset"));
   }

   public static ItemStack buildItem(ArmorSet set, int damage, int sharpnessLevel) {
      try {
         ItemBuilder builder = new ItemBuilder(Material.DIAMOND_HOE);
         builder.name("&8» " + set.getSetColor() + "&lThe Diamond Hook &8«");
         builder.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpnessLevel);
         builder.lore("");
         builder.lore(set.getDisplayName() + " Effects:");

         for (String lore : getLore(set)) {
            builder.lore(lore);
         }

         builder.lore("");
         builder.lore("&7A powerful hoe that has the ability");
         builder.lore("&7to harness Axe & Sword enchants.");
         builder.lore("");
         builder.lore("&610 slots");
         builder.lore("&6This hoe acts like a diamond sword.");
         net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(builder);
         NBTTagCompound tagCompound = nmsStack.getTag();
         NBTTagList modifiers = new NBTTagList();
         NBTTagCompound damageTag = new NBTTagCompound();
         damageTag.set("AttributeName", new NBTTagString("generic.attackDamage"));
         damageTag.set("Name", new NBTTagString("generic.attackDamage"));
         damageTag.set("Amount", new NBTTagInt(damage));
         damageTag.set("Operation", new NBTTagInt(0));
         damageTag.set("UUIDLeast", new NBTTagInt(894654));
         damageTag.set("UUIDMost", new NBTTagInt(2872));
         modifiers.add(damageTag);
         tagCompound.set("AttributeModifiers", modifiers);
         nmsStack.setTag(tagCompound);
         NBTItem item = new NBTItem(CraftItemStack.asBukkitCopy(nmsStack));
         item.setInteger("diamond_hook", 1);
         item.setString("armorset", set.getInternalName());
         return item.getItem();
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   public static ItemStack buildItem(ArmorSet set) {
      return buildItem(set, 7, 5);
   }

   public static List<String> getLore(ArmorSet set) {
      ItemStack weapon = set.buildArmor(ArmorPiece.AXE);
      if (weapon == null) {
         weapon = set.buildArmor(ArmorPiece.SWORD);
      }

      if (weapon == null) {
         return new ArrayList<>();
      }

      List<String> lore = new ArrayList<>(weapon.getItemMeta().getLore());
      lore.removeIf(string -> !string.contains("•"));
      return lore;
   }
}
