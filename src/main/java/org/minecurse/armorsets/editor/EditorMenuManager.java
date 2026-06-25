package org.minecurse.armorsets.editor;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.editor.sections.ArmorSetEditorSection;
import org.minecurse.armorsets.editor.sections.MiscEditorSection;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;

public class EditorMenuManager {
   private final ArmorSetPlugin plugin;
   private final List<EditorSection> sections = new ArrayList<>();

   public EditorMenuManager(ArmorSetPlugin plugin) {
      this.plugin = plugin;
      this.registerSections();
   }

   private void registerSections() {
      this.sections.add(new ArmorSetEditorSection(this.plugin));
      this.sections.add(new MiscEditorSection(this.plugin));
   }

   public void openMainMenu(Player player) {
      int rows = Math.max(1, (int)Math.ceil((this.sections.size() + 1) / 9.0));
      rows = Math.min(rows, 6);
      rows = Math.max(rows, 3);
      Gui gui = Gui.gui().title(legacyColor("&8ArmorSet Editor")).rows(rows).disableAllInteractions().create();
      GuiItem borderItem = ItemBuilder.from(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15)).name(legacyColor(" ")).asGuiItem();
      gui.getFiller().fillBorder(borderItem);
      int slot = 10;

      for (EditorSection section : this.sections) {
         EditorSection currentSection = section;
         GuiItem sectionItem = ItemBuilder.from(section.getDisplayItem())
            .name(legacyColor(section.getDisplayName()))
            .lore(legacyColor(""), legacyColor("&7Click to open this editor section."), legacyColor(""))
            .asGuiItem(event -> {
               event.setCancelled(true);
               currentSection.open(player);
            });
         gui.setItem(slot, sectionItem);
         if (++slot % 9 == 8) {
            slot += 2;
         }
      }

      GuiItem closeItem = ItemBuilder.from(Material.BARRIER)
         .name(legacyColor("&c&lClose"))
         .lore(legacyColor("&7Click to close the editor."))
         .asGuiItem(event -> {
            event.setCancelled(true);
            player.closeInventory();
         });
      gui.setItem(rows * 9 - 5, closeItem);
      gui.open(player);
   }

   public static Component legacyColor(String text) {
      return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
   }

   public List<EditorSection> getSections() {
      return this.sections;
   }
}
