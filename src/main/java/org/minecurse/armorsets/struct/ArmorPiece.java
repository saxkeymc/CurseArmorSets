package org.minecurse.armorsets.struct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;

public enum ArmorPiece {
   HELMET(Material.DIAMOND_HELMET),
   CHESTPLATE(Material.DIAMOND_CHESTPLATE),
   LEGGINGS(Material.DIAMOND_LEGGINGS),
   BOOTS(Material.DIAMOND_BOOTS),
   SWORD(Material.DIAMOND_SWORD),
   AXE(Material.DIAMOND_AXE),
   BOW(Material.BOW);

   private final Material defaultMaterial;

   public Material getDefaultMaterial() {
      return this.defaultMaterial;
   }

   ArmorPiece(Material defaultMaterial) {
      this.defaultMaterial = defaultMaterial;
   }

   public static ArmorPiece fromMaterial(Material material) {
      switch (material) {
         case PUMPKIN:
         case SKULL_ITEM:
         case GOLD_HELMET:
         case LEATHER_HELMET:
         case CHAINMAIL_HELMET:
         case IRON_HELMET:
         case DIAMOND_HELMET:
            return HELMET;
         case GOLD_CHESTPLATE:
         case LEATHER_CHESTPLATE:
         case CHAINMAIL_CHESTPLATE:
         case IRON_CHESTPLATE:
         case DIAMOND_CHESTPLATE:
            return CHESTPLATE;
         case GOLD_BOOTS:
         case LEATHER_BOOTS:
         case CHAINMAIL_BOOTS:
         case IRON_BOOTS:
         case DIAMOND_BOOTS:
            return BOOTS;
         case GOLD_LEGGINGS:
         case LEATHER_LEGGINGS:
         case CHAINMAIL_LEGGINGS:
         case IRON_LEGGINGS:
         case DIAMOND_LEGGINGS:
            return LEGGINGS;
         case WOOD_SWORD:
         case STONE_SWORD:
         case GOLD_SWORD:
         case IRON_SWORD:
         case DIAMOND_SWORD:
            return SWORD;
         case WOOD_AXE:
         case STONE_AXE:
         case GOLD_AXE:
         case IRON_AXE:
         case DIAMOND_AXE:
            return AXE;
         case BOW:
            return BOW;
         default:
            return null;
      }
   }

   public static ArmorPiece random() {
      List<ArmorPiece> armorPieces = new ArrayList<>(Arrays.asList(values()));
      armorPieces.remove(BOW);
      return armorPieces.get(ThreadLocalRandom.current().nextInt(0, armorPieces.size() - 1));
   }

   public boolean isArmor() {
      switch (this) {
         case HELMET:
         case CHESTPLATE:
         case LEGGINGS:
         case BOOTS:
            return true;
         default:
            return false;
      }
   }

   public boolean isWeapon() {
      switch (this) {
         case SWORD:
         case AXE:
         case BOW:
            return true;
         default:
            return false;
      }
   }
}
