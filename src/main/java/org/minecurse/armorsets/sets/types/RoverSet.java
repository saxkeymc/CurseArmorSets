package org.minecurse.armorsets.sets.types;

import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.util.ColorUtil;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;

@ArmorCrystal(name = "Frost", lore = "", outgoing = 0.0, incoming = 5.0)
public class RoverSet extends ArmorSet {
   public RoverSet(DefaultConfig defaultConfig) {
      super(
         "Frost",
         "&f&lFrost",
         ChatColor.WHITE,
         new ItemBuilder(Material.QUARTZ),
         defaultConfig.getArmorOutgoing("rover"),
         defaultConfig.getArmorIncoming("rover"),
         10.0,
         0.0
      );
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.AXE || armorPiece == ArmorPiece.BOW) {
         return null;
      } else {
         return armorPiece == ArmorPiece.SWORD
            ? this.addNBT(
               new ItemBuilder(armorPiece.getDefaultMaterial())
                  .enchantment(Enchantment.DAMAGE_ALL, 5)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(this.getPieceName(armorPiece))
                  .lore(new String[]{"", "&f&lEffects:", "&f • Deal 10% more damage towards enemies.", "&f • 100% chance to block Phoenix", ""}),
               this.getInternalName()
            )
            : this.addNBT(
               new ItemBuilder(armorPiece.getDefaultMaterial())
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(ColorUtil.translate(this.getPieceName(armorPiece)))
                  .lore(
                     new String[]{
                        " ",
                        "&7&oAn armorset for those who prefer",
                        "&7&oto play passively while making you",
                        "&7&olook confident.",
                        "",
                        "&f&lEffects:",
                        "&f • Enjoy a 20% damage reduction from enemies.",
                        "&f • Walk around with confidence.",
                        "&f • Spiritual Damage ability.",
                        "",
                        "&f&lAbility:",
                        "&fSpiritual Damage ability &7&o(Summons a heat seeking lantern)",
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
            return "§8» §f§lFrost Helmet §8«";
         case CHESTPLATE:
            return "§8» §f§lFrosty Sweater §8«";
         case LEGGINGS:
            return "§8» §f§lFrosty Snowpants §8«";
         case BOOTS:
            return "§8» §f§lFrosty Boots §8«";
         case SWORD:
            return "§8» §f§lFrost Blade §8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      player.setMetadata("rover_set", new FixedMetadataValue(ArmorSetPlugin.getInstance(), true));
   }

   @Override
   public void onUnEquip(Player player) {
      player.removeMetadata("rover_set", ArmorSetPlugin.getInstance());
   }

   @Override
   public void onAttack(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      if (!(attacked instanceof Player)) {
         return;
      }

      if (RandomUtil.getChance(5.0)) {
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
                  armorHolder,
                  "&f&lSpiritual Damager",
                  "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.",
                  armorHolder.getName(),
                  hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(120, 160)));
            } else {
               this.procAbility(armorHolder, attacked);
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(120, 160)));
            }
         }
      }
   }

   public void procAbility(Player player, LivingEntity targ) {
      if (!(targ instanceof Player)) {
         return;
      }

      this.sendAbilityMessage(player, "&f&lSpiritual Damager", "{0}", player.getName());
      final ArmorStand stand = (ArmorStand)player.getWorld().spawn(player.getLocation().clone().add(0.5, 2.0, 0.5), ArmorStand.class);
      stand.setSmall(true);
      stand.setGravity(false);
      stand.setVisible(false);
      stand.setHelmet(new ItemStack(Material.SEA_LANTERN));
      final Player target = (Player)targ;
      (new BukkitRunnable() {
         private final double teleportDistance = 0.85;

         public void run() {
            if (!target.isDead() && target.isOnline() && target.isValid() && target.getWorld() == stand.getWorld()) {
               Location targetLocation = target.getEyeLocation();
               double distance = targetLocation.distance(stand.getLocation());
               if (distance > 25.0) {
                  stand.remove();
                  this.cancel();
               } else if (!(distance <= 0.5)) {
                  Location standLocation = stand.getLocation();
                  Vector direction = targetLocation.subtract(standLocation).toVector().normalize().multiply(0.85);
                  Location newLocation = standLocation.add(direction);
                  stand.teleport(newLocation);
               } else if (!LocUtil.canPvp(target, target.getLocation())) {
                  stand.remove();
                  this.cancel();
               } else {
                  stand.remove();
                  target.damage(RandomUtil.getRandInt(25, 30));
                  target.playSound(target.getLocation(), Sound.ANVIL_BREAK, 1.0F, 1.5F);
                  this.cancel();
               }
            } else {
               stand.remove();
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(ArmorSetPlugin.getInstance(), 0L, 2L);
   }
}
