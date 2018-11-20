package org.l2j.gameserver.network.l2.components;

import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author VISTALL
 * @date  13:28/01.12.2010
 */
public interface IBroadcastPacket
{
    L2GameServerPacket packet(Player player);
}