package org.minecurse.armorsets.util;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.minecurse.commons.utils.StringUtil;

public final class ActionBarUtil {
   private ActionBarUtil() {
   }

   public static void send(Player player, String message) {
      IChatBaseComponent component = ChatSerializer.a("{\"text\":\"" + StringUtil.color(message) + "\"}");
      PacketPlayOutChat packet = new PacketPlayOutChat(component, (byte)2);
      ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
   }
}
