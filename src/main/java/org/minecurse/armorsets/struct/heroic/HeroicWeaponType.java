package org.minecurse.armorsets.struct.heroic;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;

public enum HeroicWeaponType {
   AXE("Axe", Material.GOLD_AXE, 5.0, Arrays.asList(Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE), 1561),
   SWORD("Sword", Material.GOLD_SWORD, 5.0, Arrays.asList(Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD), 1561),
   HOE("Hoe", Material.GOLD_HOE, 5.0, Arrays.asList(Material.WOOD_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.DIAMOND_HOE), 1561);

   private final String name;
   private final Material type;
   private final double damageChance;
   private final List<Material> possibleTypes;
   private final int maxDurability;

   public String getName() {
      return this.name;
   }

   public Material getType() {
      return this.type;
   }

   public double getDamageChance() {
      return this.damageChance;
   }

   public List<Material> getPossibleTypes() {
      return this.possibleTypes;
   }

   public int getMaxDurability() {
      return this.maxDurability;
   }

   HeroicWeaponType(String name, Material type, double damageChance, List<Material> possibleTypes, int maxDurability) {
      this.name = name;
      this.type = type;
      this.damageChance = damageChance;
      this.possibleTypes = possibleTypes;
      this.maxDurability = maxDurability;
   }

   public static HeroicWeaponType fromType(Material material) {
      return Arrays.stream(values()).filter(heroicWeaponType -> heroicWeaponType.type == material).findFirst().orElse(null);
   }

   public static HeroicWeaponType fromPossibleType(Material material) {
      return Arrays.stream(values()).filter(heroicWeaponType -> heroicWeaponType.possibleTypes.contains(material)).findFirst().orElse(null);
   }
}
