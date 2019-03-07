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
package org.l2j.gameserver.mobius.gameserver.model.skills;

import com.l2jmobius.gameserver.model.actor.L2Character;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author UnAfraid
 */
public final class SkillChannelized
{
	private final Map<Integer, Map<Integer, L2Character>> _channelizers = new ConcurrentHashMap<>();
	
	public void addChannelizer(int skillId, L2Character channelizer)
	{
		_channelizers.computeIfAbsent(skillId, k -> new ConcurrentHashMap<>()).put(channelizer.getObjectId(), channelizer);
	}
	
	public void removeChannelizer(int skillId, L2Character channelizer)
	{
		getChannelizers(skillId).remove(channelizer.getObjectId());
	}
	
	public int getChannerlizersSize(int skillId)
	{
		return getChannelizers(skillId).size();
	}
	
	public Map<Integer, L2Character> getChannelizers(int skillId)
	{
		return _channelizers.getOrDefault(skillId, Collections.emptyMap());
	}
	
	public void abortChannelization()
	{
		for (Map<Integer, L2Character> map : _channelizers.values())
		{
			for (L2Character channelizer : map.values())
			{
				channelizer.abortCast();
			}
		}
		_channelizers.clear();
	}
	
	public boolean isChannelized()
	{
		for (Map<Integer, L2Character> map : _channelizers.values())
		{
			if (!map.isEmpty())
			{
				return true;
			}
		}
		return false;
	}
}
