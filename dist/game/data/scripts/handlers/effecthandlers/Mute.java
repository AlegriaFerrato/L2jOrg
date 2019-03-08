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

import org.l2j.gameserver.mobius.gameserver.ai.CtrlEvent;
import org.l2j.gameserver.mobius.gameserver.model.StatsSet;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Character;
import org.l2j.gameserver.mobius.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.mobius.gameserver.model.effects.EffectFlag;
import org.l2j.gameserver.mobius.gameserver.model.effects.L2EffectType;
import org.l2j.gameserver.mobius.gameserver.model.skills.Skill;

/**
 * Mute effect implementation.
 */
public final class Mute extends AbstractEffect
{
	public Mute(StatsSet params)
	{
	}
	
	@Override
	public long getEffectFlags()
	{
		return EffectFlag.MUTED.getMask();
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MUTE;
	}
	
	@Override
	public void onStart(L2Character effector, L2Character effected, Skill skill)
	{
		effected.abortCast();
		effected.getAI().notifyEvent(CtrlEvent.EVT_MUTED);
	}
}
