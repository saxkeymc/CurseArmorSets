package org.minecurse.armorsets.menus;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.menu.button.Button;
import org.minecurse.commons.menu.type.chest.ChestMenu;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.commons.utils.inventory.ClickableItem;
import org.minecurse.commons.utils.inventory.SmartInventory;
import org.minecurse.commons.utils.inventory.content.InventoryContents;
import org.minecurse.commons.utils.inventory.content.InventoryProvider;

public class ArmorSetMenu implements InventoryProvider {
   private static final List<ArmorSet> sets = new ArrayList<>(ArmorSetPlugin.getInstance().getArmorSetRegistry().getRegisteredSets());
   private static final SmartInventory inventory = SmartInventory.builder()
      .size(calculateTotalRows(), 9)
      .title(StringUtil.color("&8All Armor Sets"))
      .provider(new ArmorSetMenu())
      .build();

   public static SmartInventory getInventory() {
      return inventory;
   }

   private static int calculateTotalRows() {
      return 3;
   }

   public void init(Player player, InventoryContents contents) {
      for (ArmorSet set : sets) {
         if (!set.isHidden()) {
            ItemBuilder builder = new ItemBuilder(set.getDisplayMaterial())
               .name("&n" + set.getDisplayName())
               .lore(set.buildArmor(ArmorPiece.HELMET).getItemMeta().getLore())
               .lore("&7Click me to view this armor set.");
            contents.add(
               ClickableItem.of(
                  builder,
                  event -> {
                     if (event.isLeftClick() || event.isRightClick()) {
                        ChestMenu setMenu = new ChestMenu(set.getDisplayName() + " Set", 3);
                        setMenu.fillSides(Button.PLACEHOLDER);

                        for (ArmorPiece piece : ArmorPiece.values()) {
                           ItemStack i = set.buildArmor(piece);
                           if (i != null) {
                              setMenu.addButton(new Button(i));
                           }
                        }

                        setMenu.setButton(
                           22,
                           new Button(
                              new ItemBuilder(Material.ARROW).name("&c&lGo Back").lore("&7Click here to return"),
                              (player2, clickInformation) -> inventory.open(player2)
                           )
                        );
                        setMenu.buildInventory();
                        setMenu.show(player);
                     }
                  }
               )
            );
         }
      }
   }
}
