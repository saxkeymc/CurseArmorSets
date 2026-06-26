package org.minecurse.armorsets.sets.types;

import de.tr7zw.nbtapi.NBTItem;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;
import org.minecurse.commons.utils.StringUtil;
import org.minecurse.enchants.Enchants;
import org.minecurse.enchants.event.HolyWhiteScrollProtectEvent;
import org.minecurse.features.FeatureManager;
import org.minecurse.features.types.staritems.StarItemFeature;

@ArmorCrystal(name = "Phantom", lore = "", outgoing = 5.0, incoming = 0.0)
public class PirateSet extends ArmorSet {
   public PirateSet(DefaultConfig defaultConfig) {
      super(
         "Phantom",
         "&c&lPhantom",
         ChatColor.RED,
         new ItemBuilder(Material.FLINT_AND_STEEL),
         defaultConfig.getArmorOutgoing("phantom"),
         defaultConfig.getArmorIncoming("phantom"),
         0.0,
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
                  .lore(new String[]{"", "&c&lEffects:", "&f • Have a 15% chance to negate Holy White Scrolls.", ""}),
               this.getInternalName()
            )
            : this.addNBT(
               new ItemBuilder(armorPiece.getDefaultMaterial())
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(StringUtil.color(this.getPieceName(armorPiece)))
                  .lore(
                     new String[]{
                        " ",
                        "&7&oOne of the most deadly set for",
                        "&7&othose who are aggressive damage",
                        "&7&odealers!",
                        "",
                        "&c&lEffects:",
                        "&f • Deal an extra 20% damage to all enemies.",
                        "&f • Look cool with a neat fire effect.",
                        "",
                        "&c&lAbility:",
                        "&fWhile wearing this set, you can become",
                        "&fa Phoenix and go up to full health!",
                        ""
                     }
                  ),
               this.getInternalName()
            );
      }
   }

   @Override
   public void onEquip(Player player) {
      player.setMetadata("piratePhoenix", new FixedMetadataValue(ArmorSetPlugin.getInstance(), true));
   }

   @Override
   public void onUnEquip(Player player) {
      player.removeMetadata("piratePhoenix", ArmorSetPlugin.getInstance());
   }

   @Override
   public String getPieceName(ArmorPiece armorPiece) {
      switch (armorPiece) {
         case HELMET:
            return "§8» §c§lPhantom Cap §8«";
         case CHESTPLATE:
            return "§8» §c§lPhantom Coat §8«";
         case LEGGINGS:
            return "§8» §c§lPhantom Leggings §8«";
         case BOOTS:
            return "§8» §c§lPhantom Boots §8«";
         case SWORD:
            return "§8» §c§lPhantom Scythe §8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
   public void onHolyWhiteScrollProtect(HolyWhiteScrollProtectEvent event) {
      Player killer = event.getKiller();
      if (killer != null) {
         if (ArmorSetPlugin.getInstance().getArmorSetRegistry().getActiveSet(killer) == this) {
            if (this.hasWeapon(killer)) {
               if (RandomUtil.getChance(15.0)) {
                  event.setCancelled(true);
                  event.setReason("Phantom Scythe");
                  event.setVictimMessage(
                     Enchants.prefix(
                        "&c{0} &7negated one of your &6&lHoly White Scrolls &7with their &8» &c&lPhantom Scythe &8«&7.", new Object[]{killer.getName()}
                     )
                  );
                  event.setKillerMessage(
                     Enchants.prefix(
                        "You negated one of &c{0} &6&lHoly White Scrolls &7with your &8» &c&lPhantom Scythe &8«&7.", new Object[]{event.getVictim().getName()}
                     )
                  );
               }
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.NORMAL)
   public void onPreDeath(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         boolean phoenix = player.hasMetadata("piratePhoenix") && RandomUtil.getChance(50.0);
         if (phoenix) {
            Cooldown cooldown = this.getAbilityCooldowns().get(player.getUniqueId());
            if (cooldown == null || cooldown.isOver()) {
               double finalDamage = player.getHealth() - event.getFinalDamage();
               if (finalDamage <= 0.0) {
                  Optional<Player> hasKingslayer = player.getNearbyEntities(32.0, 32.0, 32.0)
                     .stream()
                     .filter(Player.class::isInstance)
                     .map(Player.class::cast)
                     .filter(player1 -> LocUtil.canPvp(player1, player1.getLocation()))
                     .filter(player1 -> !FactionUtil.isAlly(player1, player))
                     .filter(player1 -> player1.hasMetadata("kingslayer"))
                     .findAny();
                  this.getAbilityCooldowns().put(player.getUniqueId(), new Cooldown(RandomUtil.getRandInt(180, 210)));
                  if (!FounderShard.hasFounderShardEquipped(player) && hasKingslayer.isPresent()) {
                     this.sendAbilityMessage(
                        player, "&c&lPhantom Phoenix", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", player.getName(), hasKingslayer.get().getName()
                     );
                     return;
                  }

                  EntityDamageEvent lastDamageCause = player.getLastDamageCause();
                  if (lastDamageCause instanceof EntityDamageByEntityEvent) {
                     EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent)lastDamageCause;
                     if (entityDamageEvent.getDamager() instanceof Player) {
                        Player damager = (Player)entityDamageEvent.getDamager();
                        ItemStack itemStack = damager.getEquipment().getItemInHand();
                        boolean crimsonMask = damager.hasMetadata("crimson_phoenix") && RandomUtil.getChance(25.0);
                        boolean roverSword = itemStack != null
                           && itemStack.getType() != Material.AIR
                           && new NBTItem(itemStack).getString("armorSet").equalsIgnoreCase("rover")
                           && damager.hasMetadata("rover_set");
                        boolean amethystSet = damager.hasMetadata("soulReaper");
                        boolean gravityBalloon = damager.hasMetadata("gravityBalloon") && RandomUtil.getChance(25.0);
                        if (gravityBalloon) {
                           player.sendMessage(
                              ((StarItemFeature)FeatureManager.getInstance().getByClass(StarItemFeature.class))
                                 .prefix("&cDue to {0}'s &9&lGravity &7Balloon&c, Your phoenix was blocked.", new Object[]{damager.getName()})
                           );
                           return;
                        }

                        if (crimsonMask) {
                           player.sendMessage(
                              ((StarItemFeature)FeatureManager.getInstance().getByClass(StarItemFeature.class))
                                 .prefix("&cDue to {0}'s &d&lGalaxy Mask&c, Your phoenix was blocked.", new Object[]{damager.getName()})
                           );
                           return;
                        }

                        if (roverSword) {
                           player.sendMessage(
                              ((StarItemFeature)FeatureManager.getInstance().getByClass(StarItemFeature.class))
                                 .prefix("&cDue to {0}'s &f&lRover Weapon&c, Your phoenix was blocked.", new Object[]{damager.getName()})
                           );
                           return;
                        }

                        if (amethystSet) {
                           player.sendMessage(
                              ((StarItemFeature)FeatureManager.getInstance().getByClass(StarItemFeature.class))
                                 .prefix("&cDue to {0}'s &8&lSoul Reaper&c, Your phoenix was blocked.", new Object[]{damager.getName()})
                           );
                           return;
                        }
                     }
                  }

                  event.setCancelled(true);
                  player.setHealth(player.getMaxHealth());
                  this.sendAbilityMessage(player, "&c&lPhantom Phoenix", "{0}", player.getName());
               }
            }
         }
      }
   }
}
