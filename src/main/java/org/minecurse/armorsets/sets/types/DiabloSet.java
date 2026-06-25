package org.minecurse.armorsets.sets.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.struct.task.DiabloAbilityTask;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;

@ArmorCrystal
public class DiabloSet extends ArmorSet {
   public static List<DiabloAbilityTask> tasks;

   public static List<DiabloAbilityTask> getTasks() {
      return tasks;
   }

   public DiabloSet(DefaultConfig defaultConfig) {
      super(
         "Diablo",
         "&4&lDiablo",
         ChatColor.DARK_RED,
         new ItemBuilder(Material.FIREBALL),
         defaultConfig.getArmorOutgoing("diablo"),
         defaultConfig.getArmorIncoming("diablo"),
         0.0,
         5.0
      );
      tasks = new ArrayList<>();
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
                  .lore(new String[]{"", "&4&lEffects:", "&f • Enjoy a 5% damage reduction.", "&f • Deal 5% more damage in lava.", ""}),
               this.getInternalName()
            )
            : this.addNBT(
               new ItemBuilder(armorPiece.getDefaultMaterial())
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(this.getPieceName(armorPiece))
                  .lore(
                     new String[]{
                        " ",
                        "&7&oObtained from the depths of hell, the",
                        "&7&ofull loadout of the devil himself!",
                        "&7&oStolen by a demon, this set holds the",
                        "&7&opower of corruption and illusions.",
                        "",
                        "&4&lEffects:",
                        "&f • Immune to Silence.",
                        "&f • Deal an extra 25% more damage.",
                        "&f • Soul Devour ability",
                        "",
                        "&4&lAbility:",
                        "&fSoul Devour ability &7&o(Summons bloody swords)",
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
            return "&8» &4&lDevil Horns &8«";
         case CHESTPLATE:
            return "&8» &4&lChest of Fury &8«";
         case LEGGINGS:
            return "&8» &4&lFiery Pants of Hell &8«";
         case BOOTS:
            return "&8» &4&lDemon Drip &8«";
         case SWORD:
            return "&8» &4&lNightblood &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onAttack(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      if (!(attacked instanceof Player)) {
         return;
      }

      Player target = (Player)attacked;
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
                  armorHolder, "&4&lSoul Devour", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(160, 200)));
            } else {
               this.sendAbilityMessage(armorHolder, "&4&lSoul Devour", "{0}", armorHolder.getName());
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(160, 200)));
               new DiabloAbilityTask(armorHolder, target).runTaskTimer(ArmorSetPlugin.getInstance(), 0L, 1L);
            }
         }
      }
   }

   @Override
   public void onDefenseWithWeapon(Player armorHolder, LivingEntity damager, EntityDamageByEntityEvent event) {
      if (armorHolder.getLocation().getBlock().getType().equals(Material.LAVA)
         || armorHolder.getLocation().getBlock().getType().equals(Material.STATIONARY_LAVA)) {
         event.setDamage(event.getDamage() + event.getDamage() * 0.08);
      }
   }
}
