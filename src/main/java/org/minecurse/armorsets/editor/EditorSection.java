package org.minecurse.armorsets.editor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface EditorSection {
   String getDisplayName();

   ItemStack getDisplayItem();

   void open(Player var1);
}
