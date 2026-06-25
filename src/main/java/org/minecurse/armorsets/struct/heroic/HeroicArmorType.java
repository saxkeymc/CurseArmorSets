package org.minecurse.armorsets.struct.heroic;

import java.util.Arrays;
import java.util.List;
import net.minecraft.server.v1_8_R3.ItemArmor.EnumArmorMaterial;
import org.bukkit.Material;

public enum HeroicArmorType {
   HELMET(
      "Helmet",
      Material.LEATHER_HELMET,
      0.165,
      EnumArmorMaterial.DIAMOND.b(0),
      Arrays.asList(Material.GOLD_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET),
      363,
      0.225F
   ),
   CHESTPLATE(
      "Chestplate",
      Material.LEATHER_CHESTPLATE,
      0.165,
      EnumArmorMaterial.DIAMOND.b(1),
      Arrays.asList(Material.GOLD_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE),
      528,
      0.225F
   ),
   LEGGINGS(
      "Leggings",
      Material.LEATHER_LEGGINGS,
      0.165,
      EnumArmorMaterial.DIAMOND.b(2),
      Arrays.asList(Material.GOLD_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS),
      495,
      0.225F
   ),
   BOOTS(
      "Boots",
      Material.LEATHER_BOOTS,
      0.165,
      EnumArmorMaterial.DIAMOND.b(3),
      Arrays.asList(Material.GOLD_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS),
      429,
      0.225F
   );

   private final String name;
   private final Material leatherType;
   private final double damageReduction;
   private final int armorModifier;
   private final List<Material> possibleTypes;
   private final int maxDurability;
   private final float rainbowPercent;

   public String getName() {
      return this.name;
   }

   public Material getLeatherType() {
      return this.leatherType;
   }

   public double getDamageReduction() {
      return this.damageReduction;
   }

   public int getArmorModifier() {
      return this.armorModifier;
   }

   public List<Material> getPossibleTypes() {
      return this.possibleTypes;
   }

   public int getMaxDurability() {
      return this.maxDurability;
   }

   public float getRainbowPercent() {
      return this.rainbowPercent;
   }

   HeroicArmorType(
      String name, Material leatherType, double damageReduction, int armorModifier, List<Material> possibleTypes, int maxDurability, float rainbowPercent
   ) {
      this.name = name;
      this.leatherType = leatherType;
      this.damageReduction = damageReduction;
      this.armorModifier = armorModifier;
      this.possibleTypes = possibleTypes;
      this.maxDurability = maxDurability;
      this.rainbowPercent = rainbowPercent;
   }

   public static HeroicArmorType fromType(Material material) {
      return Arrays.stream(values()).filter(heroicArmorType -> heroicArmorType.leatherType == material).findFirst().orElse(null);
   }

   public static HeroicArmorType fromPossibleType(Material material) {
      return Arrays.stream(values()).filter(heroicArmorType -> heroicArmorType.possibleTypes.contains(material)).findFirst().orElse(null);
   }
}
