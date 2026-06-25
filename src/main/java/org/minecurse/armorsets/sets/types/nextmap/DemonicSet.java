package org.minecurse.armorsets.sets.types.nextmap;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.util.ColorUtil;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.EffectUtil;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.LocationUtil;
import org.minecurse.commons.utils.PlayerUtils;
import org.minecurse.commons.utils.RandomUtil;
import org.minecurse.commons.utils.StringUtil;

@ArmorCrystal(name = "Demonic", lore = "", outgoing = 2.5, incoming = 5.0)
public class DemonicSet extends ArmorSet {
   private static final List<DemonicSet.ActiveHellTrap> trapList = Lists.newArrayList();

   public DemonicSet(DefaultConfig defaultConfig) {
      super(
         "Demonic",
         "&c&lDemonic",
         ChatColor.RED,
         new ItemBuilder(Material.BLAZE_POWDER),
         defaultConfig.getArmorOutgoing("demonic"),
         defaultConfig.getArmorIncoming("demonic"),
         5.0,
         0.0
      );
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.SWORD || armorPiece == ArmorPiece.BOW) {
         return null;
      } else {
         return armorPiece == ArmorPiece.AXE
            ? this.addNBT(
               new ItemBuilder(armorPiece.getDefaultMaterial())
                  .enchantment(Enchantment.DAMAGE_ALL, 5)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(this.getPieceName(armorPiece))
                  .lore(new String[]{"", "&c&lEffects:", "&f • Deal an extra 5% damage to all enemies.", ""}),
               this.getInternalName()
            )
            : this.addNBT(
               new ItemBuilder(armorPiece.getDefaultMaterial())
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(ColorUtil.translate(this.getPieceName(armorPiece)))
                  .lore(
                     new String[]{
                        "",
                        "&7&oUnleash the power of the underworld upon your enemies.",
                        "&7&oCrush your foes with the raw fury of the abyss.",
                        "&7&oThe Demonic set grants you dominion over darkness and flame.",
                        "",
                        "&c&lEffects:",
                        "&f • Deal an extra 15% damage to all enemies.",
                        "&f • Enjoy a 20% damage reduction from all enemies.",
                        "&f • Eternal Flame ability.",
                        "",
                        "&c&lAbility:",
                        "&fSummon a powerful array of hellish blocks which will",
                        "&fboost your damage to 1.3x to all enemies on the blocks.",
                        ""
                     }
                  ),
               this.getInternalName()
            );
      }
   }

   @Override
   public String getPieceName(ArmorPiece armorPiece) {
      switch (armorPiece) {
         case HELMET:
            return "&8» &c&lInfernal Helm &8«";
         case CHESTPLATE:
            return "&8» &c&lFiery Emblem &8«";
         case LEGGINGS:
            return "&8» &c&lDoom Leggings &8«";
         case BOOTS:
            return "&8» &c&lBlazing Boots &8«";
         case AXE:
            return "&8» &c&lEmber Rod &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      player.setMetadata("demonicSet", new FixedMetadataValue(ArmorSetPlugin.getInstance(), true));
   }

   @Override
   public void onUnEquip(Player player) {
      player.removeMetadata("demonicSet", ArmorSetPlugin.getInstance());
   }

   @Override
   public void onAttack(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      Cooldown cooldown = this.getAbilityCooldowns().get(armorHolder.getUniqueId());
      if (cooldown == null || cooldown.isOver()) {
         Optional<Player> hasKingslayer = armorHolder.getNearbyEntities(32.0, 32.0, 32.0)
            .stream()
            .filter(Player.class::isInstance)
            .map(Player.class::cast)
            .filter(player -> LocUtil.canPvp(player, player.getLocation()))
            .filter(player -> !FactionUtil.isAlly(player, armorHolder))
            .filter(player -> player.hasMetadata("kingslayer"))
            .findAny();
         if (!FounderShard.hasFounderShardEquipped(armorHolder) && hasKingslayer.isPresent()) {
            this.sendAbilityMessage(
               armorHolder, "&c&lEternal Flame", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
            );
            this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(120, 160)));
         } else {
            new DemonicSet.ActiveHellTrap(armorHolder);
            this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(120, 160)));
         }
      }
   }

   public boolean isPlayerInTrap(Player player) {
      return trapList.stream().anyMatch(trap -> trap.isInTrap(player));
   }

   public static class ActiveHellTrap {
      private final List<Material> hellBlockTypes = Lists.newArrayList(new Material[]{Material.NETHERRACK, Material.NETHER_BRICK, Material.QUARTZ_ORE});
      private Player player;
      private int minX;
      private int minY;
      private int minZ;
      private int maxX;
      private int maxY;
      private int maxZ;

      public ActiveHellTrap(Player player) {
         this.player = player;
         this.setBlocks();
         DemonicSet.trapList.add(this);
      }

      public boolean isInTrap(Player player) {
         Location loc = player.getLocation();
         int x = loc.getBlockX();
         int z = loc.getBlockZ();
         return x <= this.maxX && x >= this.minX && z <= this.maxZ && z >= this.minZ;
      }

      public void setBlocks() {
         Location loc = this.player.getLocation();
         this.minX = loc.getBlockX() - 5;
         this.minY = loc.getBlockY() - 2;
         this.minZ = loc.getBlockZ() - 5;
         this.maxX = loc.getBlockX() + 5;
         this.maxY = loc.getBlockY() + 1;
         this.maxZ = loc.getBlockZ() + 5;
         final List<Player> playerList = LocationUtil.getNearbyPlayers(loc, 30.0);
         final List<Block> blockList = Lists.newArrayList();

         for (int x = this.minX; x <= this.maxX; x++) {
            for (int y = this.minY; y < this.maxY; y++) {
               for (int z = this.minZ; z <= this.maxZ; z++) {
                  Block block = loc.getWorld().getBlockAt(x, y, z);
                  if (block.getType().isSolid()
                     && block.getType() != Material.AIR
                     && block.getLocation().clone().add(0.0, 1.0, 0.0).getBlock().getType() == Material.AIR) {
                     blockList.add(block);
                  }
               }
            }
         }

         for (Player player : playerList) {
            if (this.isInTrap(player)) {
               EffectUtil.applyEffect(player, new PotionEffect(PotionEffectType.WITHER, 40, 1));
            }

            blockList.forEach(blockx -> {
               Material randMat = this.getRandomType();
               player.sendBlockChange(blockx.getLocation(), randMat, (byte)0);
               if (randMat == Material.NETHERRACK) {
                  player.sendBlockChange(blockx.getRelative(BlockFace.UP).getLocation(), Material.FIRE, (byte)0);
               }
            });
            PlayerUtils.playSound(player, Sound.ENDERDRAGON_GROWL, 2.0F);
            player.sendMessage(ArmorSetPlugin.getPrefix(StringUtil.format("&c&l* Eternal Flame &7" + this.player.getName(), new Object[0]), false));
         }

         (new BukkitRunnable() {
            public void run() {
               blockList.forEach(block -> {
                  block.getState().update();
                  block.getRelative(BlockFace.UP).getState().update();
               });
               playerList.clear();
               blockList.clear();
               ActiveHellTrap.this.delete();
            }
         }).runTaskLater(ArmorSetPlugin.getInstance(), 160L);
      }

      public void delete() {
         this.player = null;
         DemonicSet.trapList.remove(this);
      }

      public Material getRandomType() {
         return this.hellBlockTypes.get(new Random().nextInt(this.hellBlockTypes.size()));
      }
   }
}
