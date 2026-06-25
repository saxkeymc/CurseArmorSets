package org.minecurse.armorsets.struct.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.minecurse.commons.utils.LocUtil;

public class DiabloAbilityTask extends BukkitRunnable {
   private final List<ArmorStand> swords;
   private final Player user;
   private final Player target;
   int teleportTicks;
   private int currentTick;
   private int swordRotationTick;
   private Iterator<ArmorStand> iter;
   private ArmorStand current;

   public List<ArmorStand> getSwords() {
      return this.swords;
   }

   public Player getUser() {
      return this.user;
   }

   public Player getTarget() {
      return this.target;
   }

   public int getTeleportTicks() {
      return this.teleportTicks;
   }

   public int getCurrentTick() {
      return this.currentTick;
   }

   public int getSwordRotationTick() {
      return this.swordRotationTick;
   }

   public Iterator<ArmorStand> getIter() {
      return this.iter;
   }

   public ArmorStand getCurrent() {
      return this.current;
   }

   public DiabloAbilityTask(Player user, Player target) {
      this.user = user;
      this.target = target;
      this.currentTick = 0;
      this.swordRotationTick = 0;
      this.teleportTicks = 0;
      this.iter = null;
      this.swords = new ArrayList<>();
      this.current = null;
   }

   public void run() {
      if (this.currentTick <= 100) {
         for (int i = 0; i < 6; i++) {
            double radius = 3.25;
            double angle = (Math.PI * 2) * i / 6.0;
            double x = this.target.getLocation().getX() + radius * Math.cos(angle);
            double y = this.target.getLocation().getY() + 2.5;
            double z = this.target.getLocation().getZ() + radius * Math.sin(angle);
            Location location = new Location(this.target.getWorld(), x, y, z);
            if (this.swords.size() != 6) {
               ArmorStand armorStand1 = (ArmorStand)location.getWorld().spawn(location, ArmorStand.class);
               armorStand1.setVisible(false);
               armorStand1.setGravity(false);
               armorStand1.setArms(true);
               armorStand1.setBasePlate(true);
               armorStand1.setItemInHand(new ItemStack(Material.IRON_SWORD));
               armorStand1.setRightArmPose(new EulerAngle(-Math.PI / 2, 0.0, 0.0));
               this.swords.add(armorStand1);
            }

            ArmorStand armorStand = this.swords.get(i);
            double rotationAngle;
            if (this.currentTick > 75) {
               rotationAngle = this.swordRotationTick += 4;
            } else {
               rotationAngle = this.swordRotationTick++;
            }

            location.setYaw((float)rotationAngle);
            armorStand.teleport(location);
         }
      }

      if (this.currentTick > 100 && this.currentTick < 120) {
         for (ArmorStand armorStand : this.swords) {
            if (this.target == null || !this.target.isOnline() || this.target.isDead() || this.target.getWorld() != armorStand.getWorld()) {
               this.swords.forEach(Entity::remove);
               this.swords.clear();
               this.cancel();
               return;
            }

            Vector directionToCenter = this.target.getEyeLocation().clone().subtract(armorStand.getLocation()).toVector();
            armorStand.setRightArmPose(armorStand.getRightArmPose().add(0.11, 0.0, 0.0));
            Location targetLocation = armorStand.getLocation().setDirection(directionToCenter);
            armorStand.teleport(targetLocation);
         }
      }

      if (this.currentTick == 120) {
         this.iter = this.swords.iterator();
      }

      if (this.currentTick > 120 && this.iter != null) {
         if (this.current == null) {
            if (!this.iter.hasNext()) {
               this.swords.forEach(Entity::remove);
               this.swords.clear();
               this.cancel();
               return;
            }

            this.current = this.iter.next();
         }

         if (this.target == null || !this.target.isOnline() || this.target.getWorld() != this.current.getWorld() || this.target.isDead()) {
            if (this.current != null) {
               this.current.remove();
            }

            this.iter.remove();
            this.swords.forEach(Entity::remove);
            this.swords.clear();
            this.cancel();
            return;
         }

         double distance = this.target.getEyeLocation().distance(this.current.getLocation());
         if (distance <= 1.5) {
            this.current.remove();
            this.iter.remove();
            this.current = null;
            if (LocUtil.canPvp(this.target, this.target.getLocation())) {
               ((CraftPlayer)this.user).getHandle().attack(((CraftEntity)this.target).getHandle());
            }
         } else if (this.teleportTicks > 9) {
            Location nextLocation = this.target.getEyeLocation();
            this.current.teleport(nextLocation);
            this.teleportTicks = 0;
         } else {
            this.teleportTicks++;
         }
      }

      this.currentTick++;
      if (this.swordRotationTick >= 360) {
         this.swordRotationTick = 0;
      }
   }
}
