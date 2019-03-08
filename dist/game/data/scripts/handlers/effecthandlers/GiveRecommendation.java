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
package handlers.effecthandlers;

import org.l2j.gameserver.mobius.gameserver.model.StatsSet;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Character;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.mobius.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.mobius.gameserver.model.skills.Skill;
import org.l2j.gameserver.mobius.gameserver.network.SystemMessageId;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ExVoteSystemInfo;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.SystemMessage;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.UserInfo;

/**
 * Give Recommendation effect implementation.
 * @author NosBit
 */
public final class GiveRecommendation extends AbstractEffect
{
	private final int _amount;
	
	public GiveRecommendation(StatsSet params)
	{
		_amount = params.getInt("amount", 0);
		if (_amount == 0)
		{
			throw new IllegalArgumentException("amount parameter is missing or set to 0.");
		}
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		final L2PcInstance target = (effected != null) && effected.isPlayer() ? (L2PcInstance) effected : null;
		if (target != null)
		{
			int recommendationsGiven = _amount;
			if ((target.getRecomHave() + _amount) >= 255)
			{
				recommendationsGiven = 255 - target.getRecomHave();
			}
			
			if (recommendationsGiven > 0)
			{
				target.setRecomHave(target.getRecomHave() + recommendationsGiven);
				
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATION_S);
				sm.addInt(recommendationsGiven);
				target.sendPacket(sm);
				target.sendPacket(new UserInfo(target));
				target.sendPacket(new ExVoteSystemInfo(target));
			}
			else
			{
				final L2PcInstance player = (effector != null) && effector.isPlayer() ? (L2PcInstance) effector : null;
				if (player != null)
				{
					player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
			}
		}
	}
}
