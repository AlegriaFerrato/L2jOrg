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

import com.l2jmobius.Config;
import org.l2j.commons.network.PacketReader;
import org.l2j.gameserver.mobius.gameserver.instancemanager.CastleManager;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.entity.Castle;
import org.l2j.gameserver.mobius.gameserver.network.L2GameClient;
import org.l2j.gameserver.mobius.gameserver.network.SystemMessageId;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.SiegeInfo;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.SystemMessage;
import org.l2j.gameserver.mobius.gameserver.util.Broadcast;

import java.util.Calendar;
import java.util.Date;

/**
 * @author UnAfraid
 */
public class RequestSetCastleSiegeTime extends IClientIncomingPacket
{
	private int _castleId;
	private long _time;
	
	@Override
	public void readImpl(ByteBuffer packet)
	{
		_castleId = packet.getInt();
		_time = packet.getInt();
		_time *= 1000;
		return true;
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = client.getActiveChar();
		final Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if ((activeChar == null) || (castle == null))
		{
			LOGGER.warning(getClass().getSimpleName() + ": activeChar: " + activeChar + " castle: " + castle + " castleId: " + _castleId);
			return;
		}
		if ((castle.getOwnerId() > 0) && (castle.getOwnerId() != activeChar.getClanId()))
		{
			LOGGER.warning(getClass().getSimpleName() + ": activeChar: " + activeChar + " castle: " + castle + " castleId: " + _castleId + " is trying to change siege date of not his own castle!");
			return;
		}
		else if (!activeChar.isClanLeader())
		{
			LOGGER.warning(getClass().getSimpleName() + ": activeChar: " + activeChar + " castle: " + castle + " castleId: " + _castleId + " is trying to change siege date but is not clan leader!");
			return;
		}
		else if (!castle.getIsTimeRegistrationOver())
		{
			if (isSiegeTimeValid(castle.getSiegeDate().getTimeInMillis(), _time))
			{
				castle.getSiegeDate().setTimeInMillis(_time);
				castle.setIsTimeRegistrationOver(true);
				castle.getSiege().saveSiegeDate();
				final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ANNOUNCED_THE_NEXT_CASTLE_SIEGE_TIME);
				msg.addCastleId(_castleId);
				Broadcast.toAllOnlinePlayers(msg);
				activeChar.sendPacket(new SiegeInfo(castle, activeChar));
			}
			else
			{
				LOGGER.warning(getClass().getSimpleName() + ": activeChar: " + activeChar + " castle: " + castle + " castleId: " + _castleId + " is trying to an invalid time (" + new Date(_time) + " !");
			}
		}
		else
		{
			LOGGER.warning(getClass().getSimpleName() + ": activeChar: " + activeChar + " castle: " + castle + " castleId: " + _castleId + " is trying to change siege date but currently not possible!");
		}
	}
	
	private static boolean isSiegeTimeValid(long siegeDate, long choosenDate)
	{
		final Calendar cal1 = Calendar.getInstance();
		cal1.setTimeInMillis(siegeDate);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		
		final Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(choosenDate);
		
		for (int hour : Config.SIEGE_HOUR_LIST)
		{
			cal1.set(Calendar.HOUR_OF_DAY, hour);
			if (isEqual(cal1, cal2, Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND))
			{
				return true;
			}
		}
		return false;
	}
	
	private static boolean isEqual(Calendar cal1, Calendar cal2, int... fields)
	{
		for (int field : fields)
		{
			if (cal1.get(field) != cal2.get(field))
			{
				return false;
			}
		}
		return true;
	}
}
