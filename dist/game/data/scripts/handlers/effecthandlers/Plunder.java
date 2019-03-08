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

import java.util.Collection;

import org.l2j.gameserver.mobius.gameserver.ai.CtrlEvent;
import org.l2j.gameserver.mobius.gameserver.model.L2Party;
import org.l2j.gameserver.mobius.gameserver.model.StatsSet;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Character;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2MonsterInstance;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.mobius.gameserver.model.holders.ItemHolder;
import org.l2j.gameserver.mobius.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.mobius.gameserver.model.skills.Skill;
import org.l2j.gameserver.mobius.gameserver.model.stats.Formulas;
import org.l2j.gameserver.mobius.gameserver.network.SystemMessageId;

/**
 * @author Sdw
 */
public final class Plunder extends AbstractEffect
{
	public Plunder(StatsSet params)
	{
	}
	
	@Override
	public boolean calcSuccess(L2Character effector, L2Character effected, Skill skill)
	{
		return Formulas.calcMagicSuccess(effector, effected, skill);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		if (!effector.isPlayer())
		{
			return;
		}
		else if (!effected.isMonster() || effected.isDead())
		{
			effector.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		final L2MonsterInstance monster = (L2MonsterInstance) effected;
		final L2PcInstance player = effector.getActingPlayer();
		
		if (monster.isSpoiled())
		{
			effector.sendPacket(SystemMessageId.PLUNDER_SKILL_HAS_BEEN_ALREADY_USED_ON_THIS_TARGET);
			return;
		}
		
		monster.setSpoilerObjectId(effector.getObjectId());
		if (monster.isSweepActive())
		{
			final Collection<ItemHolder> items = monster.takeSweep();
			if (items != null)
			{
				for (ItemHolder sweepedItem : items)
				{
					final L2Party party = effector.getParty();
					if (party != null)
					{
						party.distributeItem(player, sweepedItem, true, monster);
					}
					else
					{
						player.addItem("Sweeper", sweepedItem, effected, true);
					}
				}
			}
		}
		monster.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, effector);
	}
}
