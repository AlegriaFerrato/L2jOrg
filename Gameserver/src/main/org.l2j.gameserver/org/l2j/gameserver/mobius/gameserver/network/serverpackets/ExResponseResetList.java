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
package org.l2j.gameserver.mobius.gameserver.network.serverpackets;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Sdw
 */
public class ExResponseResetList implements IClientOutgoingPacket
{
	private final L2PcInstance _activeChar;
	
	public ExResponseResetList(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_RESPONSE_RESET_LIST.writeId(packet);
		
		packet.writeQ(_activeChar.getAdena());
		packet.writeQ(_activeChar.getBeautyTickets());
		
		packet.writeD(_activeChar.getAppearance().getHairStyle());
		packet.writeD(_activeChar.getAppearance().getHairColor());
		packet.writeD(_activeChar.getAppearance().getFace());
		return true;
	}
}
