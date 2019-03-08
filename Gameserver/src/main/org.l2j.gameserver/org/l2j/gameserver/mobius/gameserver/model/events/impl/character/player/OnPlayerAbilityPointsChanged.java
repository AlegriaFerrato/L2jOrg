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
package org.l2j.gameserver.mobius.gameserver.model.events.impl.character.player;

import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.events.EventType;
import org.l2j.gameserver.mobius.gameserver.model.events.impl.IBaseEvent;

/**
 * @author St3eT
 */
public class OnPlayerAbilityPointsChanged implements IBaseEvent
{
	private final L2PcInstance _activeChar;
	private final int _newAbilityPoints;
	private final int _oldAbilityPoints;
	
	public OnPlayerAbilityPointsChanged(L2PcInstance activeChar, int newAbilityPoints, int oldAbilityPoints)
	{
		_activeChar = activeChar;
		_newAbilityPoints = newAbilityPoints;
		_oldAbilityPoints = oldAbilityPoints;
	}
	
	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}
	
	public long getNewAbilityPoints()
	{
		return _newAbilityPoints;
	}
	
	public long getOldAbilityPoints()
	{
		return _oldAbilityPoints;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_ABILITY_POINTS_CHANGED;
	}
}
