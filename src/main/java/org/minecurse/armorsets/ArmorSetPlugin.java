package org.minecurse.armorsets;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.minecurse.armorsets.abilities.AbilityManager;
import org.minecurse.armorsets.abilities.loversability.LoversListener;
import org.minecurse.armorsets.abilities.rotability.RotMoveListener;
import org.minecurse.armorsets.api.ArmorSetPotionEffectAPI;
import org.minecurse.armorsets.commands.AlchemistCommand;
import org.minecurse.armorsets.commands.ArmorSetCommand;
import org.minecurse.armorsets.commands.ArmorSetsCommand;
import org.minecurse.armorsets.commands.CrystalCommand;
import org.minecurse.armorsets.commands.EditorCommand;
import org.minecurse.armorsets.commands.FixKothCommand;
import org.minecurse.armorsets.commands.HeroicArmorCommand;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.configuration.PlayerData;
import org.minecurse.armorsets.editor.EditorMenuManager;
import org.minecurse.armorsets.items.ItemsFeature;
import org.minecurse.armorsets.listeners.ArmorSetListener;
import org.minecurse.armorsets.listeners.CrystalListener;
import org.minecurse.armorsets.listeners.WorldBorderPatchListener;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.ArmorSetRegistry;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.sets.types.AlchemistSet;
import org.minecurse.armorsets.sets.types.BlizzardSet;
import org.minecurse.armorsets.sets.types.ColossalSet;
import org.minecurse.armorsets.sets.types.DiabloSet;
import org.minecurse.armorsets.sets.types.FortuneSet;
import org.minecurse.armorsets.sets.types.GoliathSet;
import org.minecurse.armorsets.sets.types.HavenSet;
import org.minecurse.armorsets.sets.types.InfernoSet;
import org.minecurse.armorsets.sets.types.KothSet;
import org.minecurse.armorsets.sets.types.LeviathanSet;
import org.minecurse.armorsets.sets.types.LoverSet;
import org.minecurse.armorsets.sets.types.LuckySet;
import org.minecurse.armorsets.sets.types.MagmaSet;
import org.minecurse.armorsets.sets.types.MarksManSet;
import org.minecurse.armorsets.sets.types.NovaSet;
import org.minecurse.armorsets.sets.types.OrcSet;
import org.minecurse.armorsets.sets.types.PirateSet;
import org.minecurse.armorsets.sets.types.RogueSet;
import org.minecurse.armorsets.sets.types.RoverSet;
import org.minecurse.armorsets.sets.types.WardenSet;
import org.minecurse.armorsets.sets.types.WraithSet;
import org.minecurse.armorsets.sets.types.nextmap.DemonicSet;
import org.minecurse.armorsets.tasks.ParticleTask;
import org.minecurse.armorsets.util.CageUtil;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.features.commands.FounderItemsCommand;

public class ArmorSetPlugin extends JavaPlugin {
   private ArmorSetRegistry armorSetRegistry;
   private HeroicArmorAPI api;
   private ArmorSetPotionEffectAPI potionEffectAPI;
   private PaperCommandManager paperCommandManager;
   private DefaultConfig defaultConfig;
   private PlayerData playerData;
   private AbilityManager abilityManager;
   private EditorMenuManager editorMenuManager;

   public ArmorSetRegistry getArmorSetRegistry() {
      return this.armorSetRegistry;
   }

   public HeroicArmorAPI getApi() {
      return this.api;
   }

   public ArmorSetPotionEffectAPI getPotionEffectAPI() {
      return this.potionEffectAPI;
   }

   public PaperCommandManager getPaperCommandManager() {
      return this.paperCommandManager;
   }

   public static ArmorSetPlugin getInstance() {
      return (ArmorSetPlugin)getPlugin(ArmorSetPlugin.class);
   }

   public PlayerData getPlayerData() {
      return this.playerData;
   }

   public AbilityManager getAbilityManager() {
      return this.abilityManager;
   }

   public EditorMenuManager getEditorMenuManager() {
      return this.editorMenuManager;
   }

   public static String getPrefix(String s, boolean heroic) {
      return heroic ? StringUtil.color("&5&lHeroic Armor &8➼ " + s) : StringUtil.color("&2&lArmor Sets &8➼ " + s);
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.defaultConfig = new DefaultConfig(this);
      this.playerData = new PlayerData(this);
      this.abilityManager = new AbilityManager(this);
      Bukkit.getPluginManager().registerEvents(new LoversListener(this), this);
      Bukkit.getPluginManager().registerEvents(new RotMoveListener(this.abilityManager.getRotManager()), this);
      this.paperCommandManager = new PaperCommandManager(this);
      this.api = new HeroicArmorAPI(this);
      this.potionEffectAPI = new ArmorSetPotionEffectAPI();
      new ParticleTask().runTaskTimerAsynchronously(this, 0L, 30L);
      this.registerArmor();
      this.registerListeners();
      this.registerCommands();
      new ItemsFeature().setup();
      FounderItemsCommand.getFounders().add(FounderShard.buildItem());
      this.editorMenuManager = new EditorMenuManager(this);
   }

   private void registerCommands() {
      this.paperCommandManager.getCommandContexts().registerContext(ArmorSet.class, c -> {
         String arg = c.popFirstArg();
         ArmorSet armorSet = getInstance().getArmorSetRegistry().getByName(arg.toLowerCase());
         if (armorSet != null) {
            return armorSet;
         } else {
            throw new InvalidCommandArgument(getPrefix(StringUtil.format("&cThe armor set '{0}' does not exist.", new Object[]{arg}), false));
         }
      });
      this.paperCommandManager
         .getCommandCompletions()
         .registerCompletion(
            "armorsets", c -> getInstance().getArmorSetRegistry().getRegisteredSets().stream().map(ArmorSet::getInternalName).collect(Collectors.toList())
         );
      this.paperCommandManager.registerCommand(new ArmorSetsCommand());
      this.paperCommandManager.registerCommand(new HeroicArmorCommand());
      this.paperCommandManager.registerCommand(new ArmorSetCommand());
      this.paperCommandManager.registerCommand(new FixKothCommand());
      this.paperCommandManager.registerCommand(new CrystalCommand(this));
      this.paperCommandManager.registerCommand(new EditorCommand());
      this.paperCommandManager.registerCommand(new AlchemistCommand());
   }

   private void registerArmor() {
      this.armorSetRegistry = new ArmorSetRegistry(this);
      Arrays.asList(
            new AlchemistSet(this.getDefaultConfig()),
            new FortuneSet(this.getDefaultConfig()),
            new OrcSet(this.getDefaultConfig()),
            new PirateSet(this.getDefaultConfig()),
            new MarksManSet(this.getDefaultConfig()),
            new RogueSet(this.getDefaultConfig()),
            new RoverSet(this.getDefaultConfig()),
            new LeviathanSet(this.getDefaultConfig()),
            new ColossalSet(this.getDefaultConfig()),
            new KothSet(this.getDefaultConfig()),
            new InfernoSet(this.getDefaultConfig()),
            new MagmaSet(this.getDefaultConfig()),
            new WraithSet(this.getDefaultConfig()),
            new DiabloSet(this.getDefaultConfig()),
            new DemonicSet(this.getDefaultConfig()),
            new BlizzardSet(this.getDefaultConfig()),
            new LuckySet(this.getDefaultConfig()),
            new NovaSet(this.getDefaultConfig()),
            new WardenSet(this.getDefaultConfig()),
            new LoverSet(this.getDefaultConfig(), this),
            new GoliathSet(this.getDefaultConfig()),
            new HavenSet(this.getDefaultConfig())
         )
         .forEach(armorSet -> this.armorSetRegistry.registerArmorSet(armorSet));
   }

   private void registerListeners() {
      PluginManager manager = this.getServer().getPluginManager();
      manager.registerEvents(new ArmorSetListener(), this);
      manager.registerEvents(new CrystalListener(this.armorSetRegistry), this);
      manager.registerEvents(new FounderShard(), this);
      manager.registerEvents(new WorldBorderPatchListener(), this);
   }

   public void onDisable() {
      DiabloSet.getTasks().forEach(diabloAbilityTask -> diabloAbilityTask.getSwords().forEach(Entity::remove));
      InfernoSet.getWebs().forEach(location -> location.getBlock().setType(Material.AIR));
      CageUtil.cages.values().forEach(CageUtil::removeCage);
   }

   public void removePrimedTNT(String worldName) {
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
         this.getLogger().warning("World " + worldName + " does not exist!");
      } else {
         int removedCount = 0;

         for (Entity entity : world.getEntities()) {
            if (entity.getType() == EntityType.PRIMED_TNT) {
               entity.remove();
               removedCount++;
            }
         }

         this.getLogger().info("Removed " + removedCount + " primed TNT entities from world " + worldName + ".");
      }
   }

   public DefaultConfig getDefaultConfig() {
      return this.defaultConfig;
   }
}
