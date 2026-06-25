package org.minecurse.armorsets.sets.types;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.minecurse.armorsets.ArmorSetPlugin;
import org.minecurse.armorsets.configuration.DefaultConfig;
import org.minecurse.armorsets.sets.ArmorSet;
import org.minecurse.armorsets.struct.ArmorCrystal;
import org.minecurse.armorsets.struct.ArmorPiece;
import org.minecurse.commons.item.ItemBuilder;
import org.minecurse.commons.utils.Cooldown;
import org.minecurse.commons.utils.RandomUtil;
import org.minecurse.commons.utils.StringUtil;

@ArmorCrystal(name = "Rogue", lore = "", outgoing = 0.0, incoming = 0.0, abilityChance = 0.0)
public class RogueSet extends ArmorSet {
   private final String display;
   private final Map<UUID, Cooldown> cooldowns = new HashMap<>();

   public String getDisplay() {
      return this.display;
   }

   public Map<UUID, Cooldown> getCooldowns() {
      return this.cooldowns;
   }

   public RogueSet(DefaultConfig defaultConfig) {
      super(
         "Rogue",
         "&8&lRogue",
         ChatColor.DARK_GRAY,
         new ItemBuilder(Material.STONE_SWORD),
         defaultConfig.getArmorOutgoing("rogue"),
         defaultConfig.getArmorIncoming("rogue"),
         0.0,
         0.0
      );
      this.display = StringUtil.color("&8» &8&lRogue {0} &8«");
   }

   @Override
   public ItemStack buildArmor(ArmorPiece armorPiece) {
      if (armorPiece != ArmorPiece.AXE && armorPiece != ArmorPiece.BOW && armorPiece != ArmorPiece.SWORD) {
         Material material = Material.getMaterial("CHAINMAIL_" + armorPiece.name());
         return material == null
            ? null
            : this.addNBT(
               new ItemBuilder(material)
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                  .enchantment(Enchantment.DURABILITY, 3)
                  .name(StringUtil.format(this.display, new Object[]{this.getPieceName(armorPiece)}))
                  .lore(
                     new String[]{
                        "",
                        "&8&lEffects:",
                        " &f• Permanent Speed IV effect.",
                        " &f• Permanent Resistance I effect.",
                        " &f• Increased Execute proc chance.",
                        " &f• Backstab an enemy with a stone sword",
                        "    &fdeal major damage.",
                        "",
                        "&7This armor acts like diamond armor"
                     }
                  ),
               this.getInternalName()
            );
      } else {
         return null;
      }
   }

   @Override
   public String getPieceName(ArmorPiece armorPiece) {
      switch (armorPiece) {
         case HELMET:
            return "Hat";
         case CHESTPLATE:
            return "Robe";
         case LEGGINGS:
            return "Trousers";
         case BOOTS:
            return "Boots";
         default:
            return super.getPieceName(armorPiece);
      }
   }

   @Override
   public void onEquip(Player player) {
      player.setMetadata("rogueArmorSet", new FixedMetadataValue(ArmorSetPlugin.getInstance(), true));
      if (ArmorSetPlugin.getInstance().getPotionEffectAPI().canApplyPotionEffect(player, PotionEffectType.SPEED)) {
         player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3), true);
      }

      player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0), true);
   }

   @Override
   public void onUnEquip(Player player) {
      player.removePotionEffect(PotionEffectType.SPEED);
      player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
      player.removeMetadata("rogueArmorSet", ArmorSetPlugin.getInstance());
      this.cooldowns.remove(player.getUniqueId());
   }

   @Override
   public void onAttack(Player armorHolder, LivingEntity attacked, EntityDamageByEntityEvent event) {
      if (attacked instanceof Player) {
         ItemStack itemStack = armorHolder.getItemInHand();
         if (itemStack != null && itemStack.getType() == Material.STONE_SWORD) {
            Cooldown cooldown = this.getAbilityCooldowns().get(armorHolder.getUniqueId());
            if (cooldown == null || cooldown.isOver()) {
               Player damaged = (Player)attacked;
               Vector c = damaged.getEyeLocation().toVector().subtract(armorHolder.getEyeLocation().toVector());
               Vector d = damaged.getEyeLocation().getDirection();
               double delta = c.dot(d);
               if (delta > 0.0) {
                  this.cooldowns.put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(10, 15)));
                  this.getAbilityCooldowns().put(armorHolder.getUniqueId(), new Cooldown(RandomUtil.getRandInt(10, 15)));
                  armorHolder.setItemInHand(null);
                  armorHolder.updateInventory();
                  armorHolder.getWorld().playSound(armorHolder.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);
                  armorHolder.sendMessage(
                     ArmorSetPlugin.getPrefix(StringUtil.format("&7You have back-stabbed &c{0}&7!", new Object[]{damaged.getName()}), false)
                  );
                  damaged.sendMessage(
                     ArmorSetPlugin.getPrefix(StringUtil.format("&7You have been back-stabbed by &c{0}&7!", new Object[]{armorHolder.getName()}), false)
                  );
                  event.setCancelled(true);
                  damaged.damage(0.0);
                  damaged.setHealth(Math.max(2.0, damaged.getHealth() - RandomUtil.getRandDouble(10.0, 16.0)));
               }
            }
         }
      }
   }
}
