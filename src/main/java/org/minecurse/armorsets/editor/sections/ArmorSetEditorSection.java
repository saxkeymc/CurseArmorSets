package org.minecurse.armorsets.editor.sections;

import java.util.List;
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
import dev.triumphteam.gui.guis.PaginatedGui;
import org.minecurse.armorsets.sets.ArmorSet;

public class ArmorSetEditorSection implements EditorSection {
   private final ArmorSetPlugin plugin;

   public ArmorSetEditorSection(ArmorSetPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public String getDisplayName() {
      return "&6&lArmor Sets";
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Material.DIAMOND_CHESTPLATE);
   }

   @Override
   public void open(Player player) {
      this.openSetListMenu(player);
   }

   private void openSetListMenu(Player player) {
      PaginatedGui gui = Gui.paginated().title(EditorMenuManager.legacyColor("&8Select an Armor Set")).rows(6).pageSize(28).disableAllInteractions().create();
      GuiItem borderItem = ItemBuilder.from(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15)).name(EditorMenuManager.legacyColor(" ")).asGuiItem();
      gui.getFiller().fillBorder(borderItem);
      List<ArmorSet> sets = this.plugin.getArmorSetRegistry().getRegisteredSets();
      DefaultConfig config = this.plugin.getDefaultConfig();

      for (ArmorSet set : sets) {
         double incoming = config.getArmorIncoming(set.getInternalName());
         double outgoing = config.getArmorOutgoing(set.getInternalName());
         boolean hidden = config.isHidden(set.getInternalName());
         if (hidden) {
            continue;
         }
         String hiddenTag = hidden ? "&c&l[HIDDEN] " : "";
         GuiItem setItem = ItemBuilder.from(set.getDisplayMaterial().clone())
            .name(EditorMenuManager.legacyColor(hiddenTag + "&e&l" + set.getInternalName()))
            .lore(
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&7Incoming &8(Damage Reduction)&7: &a" + formatValue(incoming) + "%"),
               EditorMenuManager.legacyColor("&7Outgoing &8(Damage Increase)&7: &c" + formatValue(outgoing) + "%"),
               EditorMenuManager.legacyColor("&7Hidden: " + (hidden ? "&c&lYes" : "&a&lNo")),
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&eClick to edit values.")
            )
            .asGuiItem(event -> {
               event.setCancelled(true);
               this.openSetDetailMenu(player, set);
            });
         gui.addItem(setItem);
      }

      gui.setItem(48, ItemBuilder.from(Material.ARROW).name(EditorMenuManager.legacyColor("&a&l← Previous Page")).asGuiItem(event -> {
         event.setCancelled(true);
         gui.previous();
      }));
      gui.setItem(
         49,
         ItemBuilder.from(Material.BARRIER)
            .name(EditorMenuManager.legacyColor("&c&l← Back to Editor"))
            .lore(EditorMenuManager.legacyColor("&7Return to the main editor menu."))
            .asGuiItem(event -> {
               event.setCancelled(true);
               this.plugin.getEditorMenuManager().openMainMenu(player);
            })
      );
      gui.setItem(50, ItemBuilder.from(Material.ARROW).name(EditorMenuManager.legacyColor("&a&lNext Page →")).asGuiItem(event -> {
         event.setCancelled(true);
         gui.next();
      }));
      gui.open(player);
   }

   private void openSetDetailMenu(Player player, ArmorSet set) {
      DefaultConfig config = this.plugin.getDefaultConfig();
      double incoming = config.getArmorIncoming(set.getInternalName());
      double outgoing = config.getArmorOutgoing(set.getInternalName());
      boolean hidden = config.isHidden(set.getInternalName());
      Gui gui = Gui.gui().title(EditorMenuManager.legacyColor("&8Editing: " + set.getInternalName())).rows(4).disableAllInteractions().create();
      GuiItem borderItem = ItemBuilder.from(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15)).name(EditorMenuManager.legacyColor(" ")).asGuiItem();
      gui.getFiller().fill(borderItem);
      gui.setItem(
         4,
         ItemBuilder.from(set.getDisplayMaterial().clone())
            .name(EditorMenuManager.legacyColor("&6&l" + set.getInternalName() + " Set"))
            .lore(
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&7Currently editing this armor set's"),
               EditorMenuManager.legacyColor("&7combat damage values."),
               EditorMenuManager.legacyColor("")
            )
            .asGuiItem()
      );
      gui.setItem(
         11,
         ItemBuilder.from(new ItemStack(Material.INK_SACK, 1, (short)10))
            .name(EditorMenuManager.legacyColor("&a&lIncoming Damage Reduction"))
            .lore(
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&7Current Value: &a" + formatValue(incoming) + "%"),
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&7Reduces damage taken by this percentage"),
               EditorMenuManager.legacyColor("&7when wearing the full armor set."),
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&eClick to modify.")
            )
            .asGuiItem(event -> {
               event.setCancelled(true);
               this.openValueEditor(player, set, ArmorSetEditorSection.ValueType.INCOMING);
            })
      );
      Material toggleMat = hidden ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK;
      String toggleStatus = hidden ? "&c&lHIDDEN" : "&a&lVISIBLE";
      gui.setItem(
         13,
         ItemBuilder.from(toggleMat)
            .name(EditorMenuManager.legacyColor("&e&lVisibility: " + toggleStatus))
            .lore(
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&7When hidden, this set will not appear"),
               EditorMenuManager.legacyColor("&7in the player armor sets menu and"),
               EditorMenuManager.legacyColor("&7cannot be obtained from mystery items."),
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&7Players who already have this set can"),
               EditorMenuManager.legacyColor("&7still use it normally."),
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&eClick to toggle.")
            )
            .asGuiItem(event -> {
               event.setCancelled(true);
               config.setHidden(set.getInternalName(), !hidden);
               String newState = !hidden ? "&cHidden" : "&aVisible";
               player.sendMessage(ArmorSetPlugin.getPrefix("&7Set &e" + set.getInternalName() + " &7visibility to " + newState + "&7.", false));
               this.openSetDetailMenu(player, set);
            })
      );
      gui.setItem(
         15,
         ItemBuilder.from(new ItemStack(Material.INK_SACK, 1, (short)1))
            .name(EditorMenuManager.legacyColor("&c&lOutgoing Damage Increase"))
            .lore(
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&7Current Value: &c" + formatValue(outgoing) + "%"),
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&7Increases damage dealt by this percentage"),
               EditorMenuManager.legacyColor("&7when wearing the full armor set."),
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&eClick to modify.")
            )
            .asGuiItem(event -> {
               event.setCancelled(true);
               this.openValueEditor(player, set, ArmorSetEditorSection.ValueType.OUTGOING);
            })
      );
      gui.setItem(
         31,
         ItemBuilder.from(Material.ARROW)
            .name(EditorMenuManager.legacyColor("&c&l← Back to Set List"))
            .lore(EditorMenuManager.legacyColor("&7Return to the armor set list."))
            .asGuiItem(event -> {
               event.setCancelled(true);
               this.openSetListMenu(player);
            })
      );
      gui.open(player);
   }

   private void openValueEditor(Player player, ArmorSet set, ArmorSetEditorSection.ValueType type) {
      DefaultConfig config = this.plugin.getDefaultConfig();
      double currentIncoming = config.getArmorIncoming(set.getInternalName());
      double currentOutgoing = config.getArmorOutgoing(set.getInternalName());
      double currentValue = type == ArmorSetEditorSection.ValueType.INCOMING ? currentIncoming : currentOutgoing;
      String typeName = type == ArmorSetEditorSection.ValueType.INCOMING ? "&aIncoming (DR)" : "&cOutgoing (DMG)";
      String typeColor = type == ArmorSetEditorSection.ValueType.INCOMING ? "&a" : "&c";
      Gui gui = Gui.gui().title(EditorMenuManager.legacyColor("&8" + set.getInternalName() + " - " + typeName)).rows(3).disableAllInteractions().create();
      GuiItem borderItem = ItemBuilder.from(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7)).name(EditorMenuManager.legacyColor(" ")).asGuiItem();
      gui.getFiller().fill(borderItem);
      gui.setItem(9, this.createAdjustButton(Material.STAINED_GLASS_PANE, (short)14, "-10.0", player, set, type, -10.0));
      gui.setItem(10, this.createAdjustButton(Material.STAINED_GLASS_PANE, (short)14, "-5.0", player, set, type, -5.0));
      gui.setItem(11, this.createAdjustButton(Material.STAINED_GLASS_PANE, (short)14, "-1.0", player, set, type, -1.0));
      gui.setItem(12, this.createAdjustButton(Material.STAINED_GLASS_PANE, (short)14, "-0.5", player, set, type, -0.5));
      Material displayMat = type == ArmorSetEditorSection.ValueType.INCOMING ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
      gui.setItem(
         13,
         ItemBuilder.from(displayMat)
            .name(EditorMenuManager.legacyColor(typeColor + "&l" + formatValue(currentValue) + "%"))
            .lore(
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&7Set: &e" + set.getInternalName()),
               EditorMenuManager.legacyColor("&7Type: " + typeName),
               EditorMenuManager.legacyColor(""),
               EditorMenuManager.legacyColor("&8Use the buttons on the left/right"),
               EditorMenuManager.legacyColor("&8to adjust this value."),
               EditorMenuManager.legacyColor("")
            )
            .asGuiItem()
      );
      gui.setItem(14, this.createAdjustButton(Material.STAINED_GLASS_PANE, (short)5, "+0.5", player, set, type, 0.5));
      gui.setItem(15, this.createAdjustButton(Material.STAINED_GLASS_PANE, (short)5, "+1.0", player, set, type, 1.0));
      gui.setItem(16, this.createAdjustButton(Material.STAINED_GLASS_PANE, (short)5, "+5.0", player, set, type, 5.0));
      gui.setItem(17, this.createAdjustButton(Material.STAINED_GLASS_PANE, (short)5, "+10.0", player, set, type, 10.0));
      gui.setItem(
         4,
         ItemBuilder.from(Material.TNT)
            .name(EditorMenuManager.legacyColor("&4&lReset to 0%"))
            .lore(EditorMenuManager.legacyColor("&7Click to reset this value to 0."), EditorMenuManager.legacyColor("&cThis action is immediate."))
            .asGuiItem(event -> {
               event.setCancelled(true);
               this.applyValueChange(player, set, type, 0.0, true);
            })
      );
      gui.setItem(
         22,
         ItemBuilder.from(Material.ARROW)
            .name(EditorMenuManager.legacyColor("&c&l← Back to Set Details"))
            .lore(EditorMenuManager.legacyColor("&7Return to the set overview."))
            .asGuiItem(event -> {
               event.setCancelled(true);
               this.openSetDetailMenu(player, set);
            })
      );
      gui.open(player);
   }

   private GuiItem createAdjustButton(
      Material material, short damage, String label, Player player, ArmorSet set, ArmorSetEditorSection.ValueType type, double delta
   ) {
      String color = delta < 0.0 ? "&c" : "&a";
      return ItemBuilder.from(new ItemStack(material, 1, damage))
         .name(EditorMenuManager.legacyColor(color + "&l" + label))
         .lore(
            EditorMenuManager.legacyColor(""),
            EditorMenuManager.legacyColor("&7Click to adjust by " + color + label + "&7."),
            EditorMenuManager.legacyColor("")
         )
         .asGuiItem(event -> {
            event.setCancelled(true);
            this.applyValueChange(player, set, type, delta, false);
         });
   }

   private void applyValueChange(Player player, ArmorSet set, ArmorSetEditorSection.ValueType type, double value, boolean absolute) {
      DefaultConfig config = this.plugin.getDefaultConfig();
      double currentIncoming = config.getArmorIncoming(set.getInternalName());
      double currentOutgoing = config.getArmorOutgoing(set.getInternalName());
      double var16;
      if (type == ArmorSetEditorSection.ValueType.INCOMING) {
         double newValue = absolute ? value : currentIncoming + value;
         double var15 = Math.max(0.0, newValue);
         var16 = Math.round(var15 * 10.0) / 10.0;
         config.setArmorValues(set.getInternalName(), var16, currentOutgoing);
      } else {
         double newValue = absolute ? value : currentOutgoing + value;
         double var18 = Math.max(0.0, newValue);
         var16 = Math.round(var18 * 10.0) / 10.0;
         config.setArmorValues(set.getInternalName(), currentIncoming, var16);
      }

      String typeLabel = type == ArmorSetEditorSection.ValueType.INCOMING ? "Incoming" : "Outgoing";
      player.sendMessage(ArmorSetPlugin.getPrefix("&7Set &e" + set.getInternalName() + " &7" + typeLabel + " to &a" + formatValue(var16) + "%&7.", false));
      this.openValueEditor(player, set, type);
   }

   private static String formatValue(double value) {
      return value == (long)value ? String.valueOf((long)value) : String.valueOf(value);
   }

   private enum ValueType {
      INCOMING,
      OUTGOING;
   }
}
