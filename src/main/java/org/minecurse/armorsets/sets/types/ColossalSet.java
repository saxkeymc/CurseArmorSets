package org.minecurse.armorsets.sets.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

@ArmorCrystal(name = "Colossal", lore = "", outgoing = 3.75, incoming = 2.5, abilityChance = 0.35)
public class ColossalSet extends ArmorSet {
   private final Map<UUID, Cooldown> cooldowns = new HashMap<>();

   public Map<UUID, Cooldown> getCooldowns() {
      return this.cooldowns;
   }

   public ColossalSet(DefaultConfig defaultConfig) {
      super(
         "Colossal",
         "&a&lColossal",
         ChatColor.GREEN,
         new ItemBuilder(Material.SLIME_BLOCK),
         defaultConfig.getArmorOutgoing("colossal"),
         defaultConfig.getArmorIncoming("colossal"),
         5.0,
         5.0
      );
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece == ArmorPiece.AXE || armorPiece == ArmorPiece.BOW) {
         return null;
      } else if (armorPiece == ArmorPiece.SWORD) {
         ItemBuilder itemBuilder = new ItemBuilder(armorPiece.getDefaultMaterial())
            .enchantment(Enchantment.DAMAGE_ALL, 5)
            .enchantment(Enchantment.DURABILITY, 3)
            .name(this.getPieceName(armorPiece))
            .lore(new String[]{" ", "&a&lEffects:", "&f • Deal an extra 5% more damage to all enemies.", "&f • Enjoy a 5% damage reduction.", ""});
         return this.addNBT(itemBuilder, this.getInternalName());
      } else {
         return this.addNBT(
            new ItemBuilder(armorPiece.getDefaultMaterial())
               .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
               .enchantment(Enchantment.DURABILITY, 3)
               .name(ColorUtil.translate(this.getPieceName(armorPiece)))
               .lore(
                  new String[]{
                     "",
                     "&7&oFrom the planet of Namek in the outskirts of our",
                     "&7&osolar system, these monsters have been attacking our",
                     "&7&ostars and stealing their power for their own. Leaving",
                     "&7&ono trace of them once they've drained",
                     "&7&oit down to their core.",
                     "",
                     "&a&lEffects:",
                     "&f • Deal an extra 15% damage to all enemies.",
                     "&f • Enjoy a 10% damage reduction.",
                     "&f • Colossal Namekian's Shooter ability.",
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
            return "&8» &a&lMighty Helmet &8«";
         case CHESTPLATE:
            return "&8» &a&lShielding Chestplate &8«";
         case LEGGINGS:
            return "&8» &a&lReinforced Pants &8«";
         case BOOTS:
            return "&8» &a&lColossal Boots &8«";
         case SWORD:
            return "&8» &a&lSword of Namek &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onAttack(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      if (!(attacked instanceof Player)) {
         return;
      }

      if (RandomUtil.getChance(2.5)) {
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
                  armorHolder, "&a&lColossal Shooter", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(140, 180)));
            } else {
               this.procAbility(armorHolder, attacked);
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(140, 180)));
            }
         }
      }
   }

   public void procAbility(Player player, LivingEntity targ) {
      if (!(targ instanceof Player)) {
         return;
      }

      this.sendAbilityMessage(player, "&a&lColossal Shooter", "{0}", player.getName());
      final ArmorStand stand = (ArmorStand)player.getWorld().spawn(player.getLocation().clone().add(0.5, 2.0, 0.5), ArmorStand.class);
      stand.setSmall(true);
      stand.setGravity(false);
      stand.setVisible(false);
      stand.setHelmet(new ItemStack(Material.SLIME_BLOCK));
      final Player target = (Player)targ;
      (new BukkitRunnable() {
         private final double teleportDistance = 0.85;

         public void run() {
            if (!target.isDead() && target.isOnline() && target.isValid() && target.getWorld() == stand.getWorld()) {
               Location targetLocation = target.getEyeLocation();
               double distance = targetLocation.distance(stand.getLocation());
               if (distance > 15.0) {
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
                  target.playSound(target.getLocation(), Sound.SLIME_ATTACK, 1.0F, 1.5F);
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
