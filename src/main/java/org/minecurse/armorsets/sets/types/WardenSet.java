package org.minecurse.armorsets.sets.types;

import com.rit.sucy.EnchantmentAPI;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.sets.FounderShard;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.armorsets.util.CageUtil;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.runnable.RunnableBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.FactionUtil;
import org.minecurse.commons.utils.LocUtil;
import org.minecurse.commons.utils.RandomUtil;

@ArmorCrystal(name = "Warden", lore = "", incoming = 3.0, outgoing = 2.75, abilityChance = 0.25)
public class WardenSet extends ArmorSet {
   public WardenSet(DefaultConfig defaultConfig) {
      super(
         "Warden",
         "&e&lWarden",
         ChatColor.YELLOW,
         new ItemBuilder(Material.IRON_FENCE),
         defaultConfig.getArmorOutgoing("warden"),
         defaultConfig.getArmorIncoming("warden"),
         10.0,
         0.0
      );
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
               "&e&lEffects:",
               "&f • Deal 10% more damage to all enemies.",
               "&f • Disable your opponents' Grappler temporarily.",
               ""
            });
         return this.addNBT(builder, this.getInternalName());
      } else {
         return this.addNBT(
            new ItemBuilder(armorPiece.getDefaultMaterial())
               .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
               .enchantment(Enchantment.DURABILITY, 3)
               .name(this.getPieceName(armorPiece))
               .lore(
                  new String[]{
                     " ",
                     "&7&oA strict yet intense armor set witholding",
                     "&7&othe power of the entire facility.",
                     "",
                     "&e&lEffects:",
                     "&f • Deal an extra 10% damage to all enemies.",
                     "&f • Enjoy a 20% damage reduction.",
                     "&f • Immune to Dimensional Rift ability.",
                     "&f • Lockdown ability.",
                     "",
                     "&e&lAbility:",
                     "&fSpawn a 6x6 Cage to force close quarter",
                     "&fcombat against your foes and deal 1.5x more",
                     "&fdamage for a limited time",
                     ""
                  }
               ),
            this.getInternalName()
         );
      }
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
               .filter(player -> player.hasMetadata("kingslayer"))
               .findAny();
            if (!FounderShard.hasFounderShardEquipped(armorHolder) && hasKingslayer.isPresent()) {
               this.sendAbilityMessage(
                  armorHolder, "&e&lWarden Lockdown", "{0} &cFailed &7due to {1} &4&lKingslayer Mask&7.", armorHolder.getName(), hasKingslayer.get().getName()
               );
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(140, 180)));
            } else {
               this.sendAbilityMessage(armorHolder, "&e&lWarden Lockdown", "{0}", armorHolder.getName());
               armorHolder.setMetadata("wardenLockdown", new FixedMetadataValue(ArmorSetPlugin.getInstance(), true));
               int size = 9;
               int[] ints = new int[]{size, size == 3 ? size + 2 : size, size};
               CageUtil.cage(armorHolder, Material.OBSIDIAN, Material.IRON_FENCE, Material.OBSIDIAN, size, size, size, 5, ints);
               this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(140, 180)));
               RunnableBuilder.bind(() -> armorHolder.removeMetadata("wardenLockdown", ArmorSetPlugin.getInstance())).runSyncLater(80L);
            }
         }
      }
   }

   @Override
   public String getPieceName(ArmorPiece armorPiece) {
      switch (armorPiece) {
         case HELMET:
            return "&8» &e&lWarden Riot Helmet &8«";
         case CHESTPLATE:
            return "&8» &e&lWarden Tac Vest &8«";
         case LEGGINGS:
            return "&8» &e&lWarden Khakis &8«";
         case BOOTS:
            return "&8» &e&lWarden Boots &8«";
         case AXE:
            return "&8» &e&lWarden Baton &8«";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      player.setMetadata("warden", new FixedMetadataValue(ArmorSetPlugin.getInstance(), true));
   }

   @Override
   public void onUnEquip(Player player) {
      player.removeMetadata("warden", ArmorSetPlugin.getInstance());
   }

   @EventHandler
   public void onDamageEntity(EntityDamageByEntityEvent event) {
      if (event.getDamager().hasMetadata("wardenLockdown")) {
         event.setDamage(event.getDamage() * 1.25);
      }
   }
}
