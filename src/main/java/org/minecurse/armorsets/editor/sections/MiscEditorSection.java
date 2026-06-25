package org.minecurse.armorsets.editor.sections;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.editor.EditorMenuManager;
import org.minecurse.armorsets.editor.EditorSection;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;

public class MiscEditorSection implements EditorSection {
   private final ArmorSetPlugin plugin;

   public MiscEditorSection(ArmorSetPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public String getDisplayName() {
      return "&c&lHeroic & Weapons";
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Material.GOLD_SWORD);
   }

   @Override
   public void open(Player player) {
      this.openMiscMenu(player);
   }

   private void openMiscMenu(Player player) {
      DefaultConfig config = this.plugin.getDefaultConfig();
      Gui gui = Gui.gui().title(EditorMenuManager.legacyColor("&8Heroic & Weapon Settings")).rows(4).disableAllInteractions().create();
      GuiItem bg = ItemBuilder.from(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15)).name(EditorMenuManager.legacyColor(" ")).asGuiItem();
      gui.getFiller().fill(bg);
      gui.setItem(
         10,
         this.buildValueItem(
            Material.IRON_CHESTPLATE,
            "&e&lHeroic Armor Per Piece",
            config.getHeroicArmorPerPiece(),
            "&7Damage reduction % per equipped",
            "&7heroic armor piece.",
            player,
            "heroic-armor-per-piece"
         )
      );
      gui.setItem(
         12,
         this.buildValueItem(
            Material.DIAMOND_CHESTPLATE,
            "&d&lRainbow Armor Per Piece",
            config.getRainbowArmorPerPiece(),
            "&7Damage reduction % per equipped",
            "&7rainbow armor piece.",
            player,
            "rainbow-armor-per-piece"
         )
      );
      gui.setItem(
         14,
         this.buildValueItem(
            Material.GOLD_SWORD,
            "&6&lHeroic Weapon Bonus",
            config.getHeroicWeaponBonus(),
            "&7Extra damage % dealt with",
            "&7heroic weapons.",
            player,
            "heroic-weapon-bonus"
         )
      );
      gui.setItem(
         19,
         this.buildValueItem(
            Material.DIAMOND_HOE,
            "&b&lDiamond Hook Bonus",
            config.getDiamondHookMultiplier(),
            "&7Extra damage % dealt with",
            "&7diamond hooks.",
            player,
            "diamond-hook-bonus"
         )
      );
      gui.setItem(
         21,
         this.buildValueItem(
            Material.GOLD_HOE,
            "&4&lHeroic Hook Bonus",
            config.getHeroicHookMultiplier(),
            "&7Extra damage % dealt with",
            "&7heroic (upgraded) hooks.",
            player,
            "heroic-hook-bonus"
         )
      );
      gui.setItem(
         31,
         ItemBuilder.from(Material.ARROW)
            .name(EditorMenuManager.legacyColor("&c&l← Back to Editor"))
            .lore(EditorMenuManager.legacyColor("&7Return to the main editor menu."))
            .asGuiItem(event -> {
               event.setCancelled(true);
               this.plugin.getEditorMenuManager().openMainMenu(player);
            })
      );
      gui.open(player);
   }

   private GuiItem buildValueItem(Material material, String name, double currentValue, String descLine1, String descLine2, Player player, String configKey) {
      return ItemBuilder.from(material)
         .name(EditorMenuManager.legacyColor(name))
         .lore(
            EditorMenuManager.legacyColor(""),
            EditorMenuManager.legacyColor("&7Current Value: &a" + formatValue(currentValue) + "%"),
            EditorMenuManager.legacyColor(""),
            EditorMenuManager.legacyColor(descLine1),
            EditorMenuManager.legacyColor(descLine2),
            EditorMenuManager.legacyColor(""),
            EditorMenuManager.legacyColor("&eClick to modify.")
         )
         .asGuiItem(event -> {
            event.setCancelled(true);
            this.openValueEditor(player, configKey, name, currentValue);
         });
   }

   private void openValueEditor(Player player, String configKey, String displayName, double currentValue) {
      DefaultConfig config = this.plugin.getDefaultConfig();
      double value = this.getMiscValue(config, configKey);
      Gui gui = Gui.gui().title(EditorMenuManager.legacyColor("&8" + stripColorCodes(displayName))).rows(3).disableAllInteractions().create();
      GuiItem bg = ItemBuilder.from(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7)).name(EditorMenuManager.legacyColor(" ")).asGuiItem();
      gui.getFiller().fill(bg);
      gui.setItem(9, this.createAdjustButton((short)14, "-10.0", player, configKey, displayName, -10.0));
      gui.setItem(10, this.createAdjustButton((short)14, "-5.0", player, configKey, displayName, -5.0));
      gui.setItem(11, this.createAdjustButton((short)14, "-1.0", player, configKey, displayName, -1.0));
      gui.setItem(12, this.createAdjustButton((short)14, "-0.5", player, configKey, displayName, -0.5));
      gui.setItem(
         13,
         ItemBuilder.from(Material.EMERALD_BLOCK)
            .name(EditorMenuManager.legacyColor("&a&l" + formatValue(value) + "%"))
            .lore(
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&7Setting: &e" + stripColorCodes(displayName)),
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&8Use the buttons on the left/right"),
               EditorMenuManager.legacyColor("&8to adjust this value."),
               EditorMenuManager.legacyColor("")
            )
            .asGuiItem()
      );
      gui.setItem(14, this.createAdjustButton((short)5, "+0.5", player, configKey, displayName, 0.5));
      gui.setItem(15, this.createAdjustButton((short)5, "+1.0", player, configKey, displayName, 1.0));
      gui.setItem(16, this.createAdjustButton((short)5, "+5.0", player, configKey, displayName, 5.0));
      gui.setItem(17, this.createAdjustButton((short)5, "+10.0", player, configKey, displayName, 10.0));
      gui.setItem(
         4,
         ItemBuilder.from(Material.TNT)
            .name(EditorMenuManager.legacyColor("&4&lReset to 0%"))
            .lore(EditorMenuManager.legacyColor("&7Click to reset this value to 0."), EditorMenuManager.legacyColor("&cThis action is immediate."))
            .asGuiItem(event -> {
               event.setCancelled(true);
               config.setMiscValue(configKey, 0.0);
               player.sendMessage(ArmorSetPlugin.getPrefix("&7Set &e" + stripColorCodes(displayName) + " &7to &a0%&7.", false));
               this.openValueEditor(player, configKey, displayName, 0.0);
            })
      );
      gui.setItem(
         22,
         ItemBuilder.from(Material.ARROW)
            .name(EditorMenuManager.legacyColor("&c&l← Back to Misc Settings"))
            .lore(EditorMenuManager.legacyColor("&7Return to the settings overview."))
            .asGuiItem(event -> {
               event.setCancelled(true);
               this.openMiscMenu(player);
            })
      );
      gui.open(player);
   }

   private GuiItem createAdjustButton(short damage, String label, Player player, String configKey, String displayName, double delta) {
      String color = delta < 0.0 ? "&c" : "&a";
      return ItemBuilder.from(new ItemStack(Material.STAINED_GLASS_PANE, 1, damage))
         .name(EditorMenuManager.legacyColor(color + "&l" + label))
         .lore(
            EditorMenuManager.legacyColor(""),
            EditorMenuManager.legacyColor("&7Click to adjust by " + color + label + "&7."),
            EditorMenuManager.legacyColor("")
         )
         .asGuiItem(event -> {
            event.setCancelled(true);
            DefaultConfig config = this.plugin.getDefaultConfig();
            double current = this.getMiscValue(config, configKey);
            double newValue = Math.max(0.0, current + delta);
            newValue = Math.round(newValue * 100.0) / 100.0;
            config.setMiscValue(configKey, newValue);
            player.sendMessage(ArmorSetPlugin.getPrefix("&7Set &e" + stripColorCodes(displayName) + " &7to &a" + formatValue(newValue) + "%&7.", false));
            this.openValueEditor(player, configKey, displayName, newValue);
         });
   }

   private double getMiscValue(DefaultConfig config, String configKey) {
      switch (configKey) {
         case "heroic-armor-per-piece":
            return config.getHeroicArmorPerPiece();
         case "rainbow-armor-per-piece":
            return config.getRainbowArmorPerPiece();
         case "heroic-weapon-bonus":
            return config.getHeroicWeaponBonus();
         case "diamond-hook-bonus":
            return config.getDiamondHookMultiplier();
         case "heroic-hook-bonus":
            return config.getHeroicHookMultiplier();
         default:
            return 0.0;
      }
   }

   private static String formatValue(double value) {
      return value == (long)value ? String.valueOf((long)value) : String.valueOf(value);
   }

   private static String stripColorCodes(String text) {
      return text.replaceAll("&[0-9a-fk-or]", "");
   }
}
