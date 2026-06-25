package org.minecurse.armorsets.sets.types.nextmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.minecurse.armorsets.ArmorSetPlugin;
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
import org.minecurse.commons.utils.StringUtil;

@ArmorCrystal(name = "Enforcer", lore = "", outgoing = 2.5, incoming = 5.0)
public class EnforcerSet extends ArmorSet {
   private final Map<UUID, Double> originalDamages = new HashMap<>();
   private final Map<UUID, Double> playerDamageIncrease = new HashMap<>();

   public EnforcerSet() {
      super("Enforcer", "&2&lEnforcer", ChatColor.DARK_GREEN, new ItemBuilder(Material.DIAMOND_AXE), 10.0, 15.0, 5.0, 0.0);
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
                  .lore(new String[]{"", "&2&lEffects:", "&f • Deal an extra 5% damage to all enemies.", ""}),
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
                        "&7&oTired of lawlessness in the arena?",
                        "&7&oWant to take control and dominate your foes?",
                        "&7&oThe Enforcer set empowers you with",
                        "&7&orelentless authority and unstoppable might.",
                        "",
                        "&2&lEffects:",
                        "&f • Deal an extra 20% damage to all enemies.",
                        "&f • Enjoy a 15% damage reduction from all enemies.",
                        "&f • Enforcer's Domination ability.",
                        "",
                        "&2&lAbility:",
                        "&fUnleash a powerful aura that debuffs enemies' damage",
                        "&fand boosts your power for 10 seconds.",
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
            return "&8» &2&lEnforcer Visor &8«";
         case CHESTPLATE:
            return "&8» &2&lEnforcer Plate &8«";
         case LEGGINGS:
            return "&8» &2&lEnforcer Guards &8«";
         case BOOTS:
            return "&8» &2&lEnforcer Treads &8«";
         case AXE:
            return "&8» &2&lEnforcer Cleaver &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      player.setMetadata("enforcerSet", new FixedMetadataValue(ArmorSetPlugin.getInstance(), true));
   }

   @Override
   public void onUnEquip(Player player) {
      player.removeMetadata("enforcerSet", ArmorSetPlugin.getInstance());
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
               armorHolder,
               "&2&lEnforcer's Domination",
               "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.",
               armorHolder.getName(),
               hasKingslayer.get().getName()
            );
            this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(120, 160)));
         } else {
            double PLAYER_DAMAGE_INCREASE = 0.3;
            this.playerDamageIncrease.put(armorHolder.getUniqueId(), PLAYER_DAMAGE_INCREASE);
            int AURA_RADIUS = 10;

            for (Entity entity : armorHolder.getNearbyEntities(AURA_RADIUS, AURA_RADIUS, AURA_RADIUS)) {
               if (entity instanceof Player && entity != armorHolder) {
                  Player enemy = (Player)entity;
                  double ENEMY_DAMAGE_REDUCTION = 0.1;
                  this.originalDamages.put(enemy.getUniqueId(), ENEMY_DAMAGE_REDUCTION);
                  enemy.sendMessage(
                     ArmorSetPlugin.getPrefix(
                        StringUtil.format("&2&l * Enforcer's Domination &7You will deal &c10% less&7 damage for 10 seconds!", new Object[0]), false
                     )
                  );
               }
            }

            int DURATION = 10;
            Bukkit.getScheduler()
               .scheduleSyncDelayedTask(
                  ArmorSetPlugin.getInstance(),
                  () -> {
                     armorHolder.sendMessage(
                        ArmorSetPlugin.getPrefix(
                           StringUtil.format("&2&l * Enforcer's Domination &7This has worn off, your rage has settled!", new Object[0]), false
                        )
                     );

                     for (UUID uuid : this.originalDamages.keySet()) {
                        Player enemyx = Bukkit.getPlayer(uuid);
                        if (enemyx != null) {
                           enemyx.sendMessage(
                              ArmorSetPlugin.getPrefix(
                                 StringUtil.format("&2&l * Enforcer's Domination &7This has worn off, {0} has calmed!", new Object[]{armorHolder.getName()}),
                                 false
                              )
                           );
                        }
                     }

                     this.originalDamages.clear();
                     this.playerDamageIncrease.clear();
                  },
                  DURATION * 20L
               );
            this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(120, 160)));
         }
      }
   }

   @EventHandler
   public void onPlayerDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
         Player damager = (Player)event.getDamager();
         Player target = (Player)event.getEntity();
         if (this.playerDamageIncrease.containsKey(damager.getUniqueId())) {
            double increase = this.playerDamageIncrease.get(damager.getUniqueId());
            event.setDamage(event.getDamage() * (1.0 + increase));
         }

         if (this.originalDamages.containsKey(target.getUniqueId())) {
            double reduction = this.originalDamages.get(target.getUniqueId());
            event.setDamage(event.getDamage() * (1.0 - reduction));
         }
      }
   }
}
