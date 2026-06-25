package org.minecurse.armorsets.abilities.rotability;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class RotBlock {
   private final Block block;
   private final byte blockData;
   private final Set<UUID> blockOwners = new HashSet<>();

   public RotBlock(Block block) {
      this.block = block;
      this.blockData = block.getData();
   }

   public void updatePlayer(UUID uuid, List<Block> eligibleRotBlocks) {
      if (!this.isValidLocation(eligibleRotBlocks)) {
         this.removeOwner(uuid);
      }
   }

   public void removeOwner(UUID uuid) {
      if (this.isOwner(uuid) || this.blockOwners.isEmpty()) {
         this.blockOwners.remove(uuid);
         Player player = Bukkit.getPlayer(uuid);
         Material blockChange = this.blockOwners.isEmpty() ? this.block.getType() : Material.WOOL;
         byte newBlockData = this.blockOwners.isEmpty() ? this.blockData : 10;
         if (player != null && player.isOnline()) {
            player.sendBlockChange(this.block.getLocation(), blockChange, newBlockData);
         }

         this.block.getLocation().getWorld().getPlayers().forEach(loopedPlayer -> {
            if (loopedPlayer != player) {
               loopedPlayer.sendBlockChange(this.block.getLocation(), blockChange, newBlockData);
            }
         });
      }
   }

   public RotBlock addOwner(UUID uuid) {
      this.blockOwners.add(uuid);
      return this;
   }

   public boolean isOwner(UUID uuid) {
      return this.blockOwners.contains(uuid);
   }

   public Set<UUID> getBlockOwners() {
      return this.blockOwners;
   }

   public Block getBlock() {
      return this.block;
   }

   private boolean isValidLocation(List<Block> eligibleRotBlocks) {
      return eligibleRotBlocks.contains(this.block);
   }
}
