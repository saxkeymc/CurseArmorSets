package org.minecurse.armorsets.util;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.perms.Relation;
import org.bukkit.entity.Player;

public class FactionsSupport {
   public static boolean isRelated(Player player1, Player player2) {
      if (player1 != null && player2 != null) {
         FPlayer fPlayer1 = FPlayers.getInstance().getByPlayer(player1);
         FPlayer fPlayer2 = FPlayers.getInstance().getByPlayer(player2);
         Relation relation = fPlayer1.getRelationTo(fPlayer2);
         return relation.isAlly() || relation.isMember() || relation.isTruce();
      } else {
         return false;
      }
   }
}
