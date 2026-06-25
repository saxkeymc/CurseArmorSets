package org.minecurse.armorsets.sets.types;

import com.rit.sucy.EnchantmentAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.runnable.RunnableBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.EntityUtil;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;
import org.minecurse.commons.utils.StringUtil;

@ArmorCrystal(name = "Inferno", lore = "", outgoing = 5.0, incoming = 0.0, abilityChance = 0.35)
public class InfernoSet extends ArmorSet {
   private static final List<Location> webs = new ArrayList<>();
   private final Map<UUID, Cooldown> heatwaveCd = new HashMap<>();

   public static List<Location> getWebs() {
      return webs;
   }

   public Map<UUID, Cooldown> getHeatwaveCd() {
      return this.heatwaveCd;
   }

   public InfernoSet(DefaultConfig defaultConfig) {
      super(
         "Inferno",
         "&6&lInferno",
         ChatColor.GOLD,
         new ItemBuilder(Material.BLAZE_ROD),
         defaultConfig.getArmorOutgoing("inferno"),
         defaultConfig.getArmorIncoming("inferno"),
         5.0,
         5.0
      );
   }

   public static List<Vector> getHollowCube(Location corner1, Location corner2) {
      List<Vector> result = new ArrayList<>();
      int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
      int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
      int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
      int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
      int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
      int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

      for (int i = minX; i <= maxX; i++) {
         for (int j = minZ; j <= maxZ; j++) {
            result.add(new Vector(i, minY, j));
            result.add(new Vector(i, maxY, j));
         }
      }

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            result.add(new Vector(x, y, minZ));
            result.add(new Vector(x, y, maxZ));
         }
      }

      for (int z = minZ; z <= maxZ; z++) {
         for (int y = minY; y <= maxY; y++) {
            result.add(new Vector(minX, y, z));
            result.add(new Vector(maxX, y, z));
         }
      }

      return result;
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.SWORD || armorPiece == ArmorPiece.BOW) {
         return null;
      } else if (armorPiece == ArmorPiece.AXE) {
         ItemBuilder builder = new ItemBuilder(armorPiece.getDefaultMaterial())
            .enchantment(Enchantment.DAMAGE_ALL, 5)
            .enchantment(Enchantment.DURABILITY, 3)
            .name(this.getPieceName(armorPiece))
            .lore(new String[]{
               "&7Lifesteal V",
               "&7Silence IV",
               " ",
               "&6&lEffects:",
               "&f • Enjoy a 5% damage reduction from enemies.",
               "&f • Deal an extra 5% damage to all enemies.",
               ""
            });
         return this.addNBT(builder, this.getInternalName());
      } else {
         return this.addNBT(
            new ItemBuilder(armorPiece.getDefaultMaterial())
               .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
               .enchantment(Enchantment.DURABILITY, 3)
               .name(StringUtil.color(this.getPieceName(armorPiece)))
               .lore(
                  new String[]{
                     " ",
                     "&7&oOne of the most sacred pieces of armor",
                     "&7&ohanded down to those who are worthy",
                     "&7&oof yielding the power of the sun!",
                     "",
                     "&6&lEffects:",
                     "&f • Permanent Fire Resistance I effect.",
                     "&f • Deal an extra 20% damage to all enemies.",
                     "&f • Amaterasu ability.",
                     "&f • Heatwave ability.",
                     "",
                     "&6&lAbilities:",
                     "&fAmaterasu ability &7&o(Summons cob webs and bleed effect)",
                     "&fHeatwave ability &7&o(Pushes back opponents and damages them)",
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
            return "&8» &6&lCap of Ignition &8«";
         case CHESTPLATE:
            return "&8» &6&lVest of Combustion &8«";
         case LEGGINGS:
            return "&8» &6&lScorched Leggings &8«";
         case BOOTS:
            return "&8» &6&lPyromaniac Boots &8«";
         case AXE:
            return "&8» &6&lSuper Hot Fire &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0), true);
   }

   @Override
   public void onUnEquip(Player player) {
      player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
   }

   @Override
   public void onAttack(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      if (RandomUtil.getChance(5.0)) {
         Cooldown cooldown = this.getAbilityCooldowns().get(armorHolder.getUniqueId());
         if (cooldown == null || cooldown.isOver()) {
            Optional<Player> hasKingslayer = armorHolder.getNearbyEntities(32.0, 32.0, 32.0)
               .stream()
               .filter(Player.class::isInstance)
               .map(Player.class::cast)
               .filter(player -> LocUtil.canPvp(player, player.getLocation()))
               .filter(player -> !FactionUtil.isAlly(player, armorHolder))
               .filter(player -> player.hasMetadata("flameCloakStarItem"))
               .filter(player -> player.hasMetadata("kingslayer"))
               .findAny();
            if (!FounderShard.hasFounderShardEquipped(armorHolder) && hasKingslayer.isPresent()) {
               this.sendAbilityMessage(
                  armorHolder,
                  "&6&lInferno Amaterasu",
                  "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.",
                  armorHolder.getName(),
                  hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(120, 140)));
            } else {
               this.sendAbilityMessage(armorHolder, "&6&lInferno Amaterasu", "&7{0}", armorHolder.getName());
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(120, 140)));
               armorHolder.getNearbyEntities(32.0, 32.0, 32.0)
                  .stream()
                  .filter(Player.class::isInstance)
                  .map(Player.class::cast)
                  .filter(player -> LocUtil.canPvp(player, player.getLocation()))
                  .filter(player -> !FactionUtil.isAlly(armorHolder, player))
                  .forEach(this::trapPlayer);
            }
         }
      }
   }

   @Override
   public void onDamaged(Player armorHolder, LivingEntity damager, EntityDamageByEntityEvent event) {
      if (RandomUtil.getChance(5.0)) {
         Cooldown cooldown = this.heatwaveCd.get(armorHolder.getUniqueId());
         if (cooldown == null || cooldown.isOver()) {
            Optional<Player> hasKingslayer = armorHolder.getNearbyEntities(32.0, 32.0, 32.0)
               .stream()
               .filter(Player.class::isInstance)
               .map(Player.class::cast)
               .filter(player -> LocUtil.canPvp(player, player.getLocation()))
               .filter(player -> !FactionUtil.isAlly(player, armorHolder))
               .filter(player -> player.hasMetadata("flameCloakStarItem"))
               .filter(player -> player.hasMetadata("kingslayer"))
               .findAny();
            if (!FounderShard.hasFounderShardEquipped(armorHolder) && hasKingslayer.isPresent()) {
               this.sendAbilityMessage(
                  armorHolder, "&6&lInferno Heatwave", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
               );
               this.heatwaveCd.put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(45, 80)));
            } else {
               this.sendAbilityMessage(armorHolder, "&6&lInferno Heatwave", "{0}", armorHolder.getName());
               this.heatwaveCd.put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(45, 80)));
               armorHolder.getNearbyEntities(32.0, 32.0, 32.0)
                  .stream()
                  .filter(Player.class::isInstance)
                  .map(Player.class::cast)
                  .filter(player -> LocUtil.canPvp(player, player.getLocation()))
                  .filter(player -> !FactionUtil.isAlly(armorHolder, player))
                  .forEach(player -> {
                     EntityUtil.pushAwayEntity(armorHolder, player, 2.5);
                     player.damage(RandomUtil.getRandInt(10, 16));
                  });
            }
         }
      }
   }

   public void trapPlayer(final Player player) {
      final Location location = player.getLocation().add(0.0, 0.5, 0.0);
      location.setX(location.getBlockX() + 0.5);
      location.setZ(location.getBlockZ() + 0.5);
      if (location.getBlock().getType() == Material.AIR) {
         location.getBlock().setType(Material.WEB);
         webs.add(location.getBlock().getLocation());
         player.teleport(location, TeleportCause.PLUGIN);
         int radius = 2;
         final Location cornerOne = new Location(location.getWorld(), location.getX() - radius, location.getY() - radius, location.getZ() - radius);
         final Location cornerTwo = new Location(location.getWorld(), location.getX() + radius, location.getY() + radius, location.getZ() + radius);
         (new BukkitRunnable() {
               private int ticks;

               public void run() {
                  if (this.ticks++ < 6
                     && !player.isDead()
                     && player.isValid()
                     && player.getWorld().equals(location.getWorld())
                     && LocUtil.canPvp(player, player.getLocation())) {
                     for (Vector vector : InfernoSet.getHollowCube(cornerOne, cornerTwo)) {
                        RunnableBuilder.bind(() -> player.damage(6.0)).runSync();
                     }
                  } else {
                     RunnableBuilder.forPlugin(ArmorSetPlugin.getInstance()).with(() -> location.getBlock().setType(Material.AIR)).runSync();
                     this.cancel();
                  }
               }
            })
            .runTaskTimerAsynchronously(ArmorSetPlugin.getInstance(), 0L, 20L);
      }
   }
}
