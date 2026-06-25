package org.minecurse.armorsets.sets.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.EffectUtil;
import org.minecurse.commons.utils.Pair;
import org.minecurse.commons.utils.StringUtil;

@ArmorCrystal(name = "Archer", incoming = 0.0, outgoing = 0.0, abilityChance = 0.0)
public class MarksManSet extends ArmorSet implements Listener {
   private static final Map<String, Long> markedPlayers = new ConcurrentHashMap<>();
   private static final Map<String, Set<Pair<String, Long>>> markedBy = new HashMap<>();
   private final String display = StringUtil.color(this.getDisplayName());

   public static Map<String, Long> getMarkedPlayers() {
      return markedPlayers;
   }

   public static Map<String, Set<Pair<String, Long>>> getMarkedBy() {
      return markedBy;
   }

   public MarksManSet(DefaultConfig defaultConfig) {
      super(
         "Archer",
         "&a&lArcher",
         ChatColor.GREEN,
         new ItemBuilder(Material.BOW),
         defaultConfig.getArmorOutgoing("marksman"),
         defaultConfig.getArmorIncoming("marksman"),
         0.0,
         0.0
      );
   }

   public static boolean isMarked(Player player) {
      return getMarkedPlayers().containsKey(player.getName()) && getMarkedPlayers().get(player.getName()) > System.currentTimeMillis();
   }

   @Override
   public void onUnEquip(Player player) {
      player.removePotionEffect(PotionEffectType.SPEED);
   }

   @Override
   public void onEquip(Player player) {
      if (ArmorSetPlugin.getInstance().getPotionEffectAPI().canApplyPotionEffect(player, PotionEffectType.SPEED)) {
         EffectUtil.applyEffect(player, new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
      }
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.SWORD || armorPiece == ArmorPiece.AXE) {
         return null;
      } else {
         return (ItemStack)(armorPiece == ArmorPiece.BOW
            ? new ItemBuilder(Material.BOW)
               .name(this.getPieceName(armorPiece))
               .enchantment(Enchantment.ARROW_DAMAGE, 5)
               .enchantment(Enchantment.DURABILITY, 3)
               .enchantment(Enchantment.ARROW_FIRE, 2)
               .enchantment(Enchantment.ARROW_INFINITE, 1)
               .lore(
                  new String[]{
                     " ",
                     "&a&lEffects:",
                     "&f • Permanent Speed III effect.",
                     "&f • Archer Marks deal 25% extra damage.",
                     "&f • Enemy bows deal 25% less damage.",
                     " ",
                     "&aRight Click to view this armor set"
                  }
               )
            : this.addNBT(
               new ItemBuilder(armorPiece.getDefaultMaterial())
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(this.getPieceName(armorPiece))
                  .lore(
                     new String[]{
                        " ",
                        "&7&oHave an eye of an eagle? Become a",
                        "&7&omarksman with this armorset",
                        "",
                        "&a&lEffects:",
                        "&f • Permanent Speed III effect.",
                        "&f • Archer Marks deal 25% extra damage.",
                        "&f • Enemy bows deal 25% less damage.",
                        ""
                     }
                  ),
               this.getInternalName()
            ));
      }
   }

   @Override
   public String getPieceName(ArmorPiece armorPiece) {
      switch (armorPiece) {
         case HELMET:
            return "§8» §a§lArcher Helmet §8«";
         case CHESTPLATE:
            return "§8» §a§lArcher Chestplate §8«";
         case LEGGINGS:
            return "§8» §a§lArcher Leggings §8«";
         case BOOTS:
            return "§8» §a§lArcher Boots §8«";
         case BOW:
            return "§8» §a§lArcher Bow §8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onProjectile(Player armorHolder, LivingEntity shot, EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
         Arrow arrow = (Arrow)event.getDamager();
         Player victim = (Player)event.getEntity();
         if (!(arrow.getShooter() instanceof Player)) {
            return;
         }

         Player shooter = (Player)arrow.getShooter();
         if (!arrow.hasMetadata("Pullback") || !arrow.hasMetadata("ShotFromDistance")) {
            return;
         }

         float pullback = ((MetadataValue)arrow.getMetadata("Pullback").get(0)).asFloat();
         Location shotFrom = (Location)((MetadataValue)arrow.getMetadata("ShotFromDistance").get(0)).value();
      double distance = shotFrom.distance(victim.getLocation());
         if (pullback >= 0.5F && !victim.hasMetadata("immune_archer")) {
            shooter.sendMessage(
               ChatColor.YELLOW
                  + "["
                  + ChatColor.BLUE
                  + "Arrow Range"
                  + ChatColor.YELLOW
                  + " ("
                  + ChatColor.RED
                  + (int)distance
                  + ChatColor.YELLOW
                  + ")] "
                  + ChatColor.GOLD
                  + "Marked player for "
                  + '\u0007'
                  + " seconds."
            );
            if (!isMarked(victim)) {
               victim.sendMessage(
                  ChatColor.RED.toString()
                     + ChatColor.BOLD
                     + "Marked! "
                     + ChatColor.YELLOW
                     + "An archer has shot you and marked you (+20% damage) for "
                     + '\u0007'
                     + " seconds."
               );
            }

            getMarkedPlayers().put(victim.getName(), System.currentTimeMillis() + 7000L);
            getMarkedBy().putIfAbsent(shooter.getName(), new HashSet<>());
            getMarkedBy().get(shooter.getName()).add(new Pair(victim.getName(), System.currentTimeMillis() + 7000L));
         } else {
            shooter.sendMessage(
               ChatColor.YELLOW
                  + "["
                  + ChatColor.BLUE
                  + "Arrow Range"
                  + ChatColor.YELLOW
                  + " ("
                  + ChatColor.RED
                  + (int)distance
                  + ChatColor.YELLOW
                  + ")] "
                  + ChatColor.RED
                  + "Bow wasn't fully drawn back. "
                  + ChatColor.BLUE
                  + ChatColor.BOLD
                  + "(0.5 hearts)"
            );
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (isMarked(player)) {
            Player damager = null;
            if (event.getDamager() instanceof Player) {
               damager = (Player)event.getDamager();
            } else if (event.getDamager() instanceof Projectile && ((Projectile)event.getDamager()).getShooter() instanceof Player) {
               damager = (Player)((Projectile)event.getDamager()).getShooter();
            }

            if (damager != null && !this.canUseMark(damager, player)) {
               return;
            }

            event.setDamage(event.getDamage() * 1.2);
         }
      }
   }

   @EventHandler
   public void onEntityShootBow(EntityShootBowEvent event) {
      event.getProjectile().setMetadata("ShotFromDistance", new FixedMetadataValue(ArmorSetPlugin.getInstance(), event.getProjectile().getLocation()));
      event.getProjectile().setMetadata("Pullback", new FixedMetadataValue(ArmorSetPlugin.getInstance(), event.getForce()));
   }

   private boolean canUseMark(Player player, Player victim) {
      if (markedBy.containsKey(player.getName())) {
         for (Pair<String, Long> pair : markedBy.get(player.getName())) {
            if (victim.getName().equals(pair.getKey()) && (Long)pair.getValue() > System.currentTimeMillis()) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public void onDamaged(Player armorHolder, LivingEntity damager, EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Projectile) {
         event.setDamage(Math.max(0.0, event.getDamage() - event.getDamage() * 0.25));
      }
   }
}
