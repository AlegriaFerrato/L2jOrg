/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2j.gameserver.mobius.gameserver.network.clientpackets;

import org.l2j.commons.network.PacketReader;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.network.Disconnection;
import org.l2j.gameserver.mobius.gameserver.network.L2GameClient;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ActionFailed;
import org.l2j.gameserver.mobius.gameserver.util.OfflineTradeUtil;

import java.util.logging.Logger;

/**
 * This class ...
 * @version $Revision: 1.9.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class Logout extends IClientIncomingPacket
{
	protected static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");
	
	@Override
	public void readImpl(ByteBuffer packet)
	{
		return true;
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = client.getActiveChar();
		if (player == null)
		{
			client.closeNow();
			return;
		}
		
		if (!player.canLogout())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		LOGGER_ACCOUNTING.info("Logged out, " + client);
		
		if (!OfflineTradeUtil.enteredOfflineMode(player))
		{
			Disconnection.of(client, player).defaultSequence(false);
		}
	}
}
