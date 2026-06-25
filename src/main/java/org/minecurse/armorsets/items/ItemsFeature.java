package org.minecurse.armorsets.items;

import org.minecurse.features.FeatureManager;
import org.minecurse.features.types.clickitems.ClickItemFeature;

public class ItemsFeature {
   public void setup() {
      ((ClickItemFeature)FeatureManager.getInstance().getByClass(ClickItemFeature.class)).registerItem(new MysteryArmorSet());
      ((ClickItemFeature)FeatureManager.getInstance().getByClass(ClickItemFeature.class)).registerItem(new MysteryHeroicUpgradeItem());
      ((ClickItemFeature)FeatureManager.getInstance().getByClass(ClickItemFeature.class)).registerItem(new MysteryHookItem());
      ((ClickItemFeature)FeatureManager.getInstance().getByClass(ClickItemFeature.class)).registerItem(new MysteryHookExtraDamageItem());
      ((ClickItemFeature)FeatureManager.getInstance().getByClass(ClickItemFeature.class)).registerItem(new MysteryHookExtraSharpnessItem());
   }
}
