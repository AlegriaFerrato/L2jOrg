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

import java.util.HashSet;
import java.util.Set;

import org.l2j.gameserver.mobius.gameserver.datatables.BotReportTable;
import org.l2j.gameserver.mobius.gameserver.instancemanager.PunishmentManager;
import org.l2j.gameserver.mobius.gameserver.model.StatsSet;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Character;
import org.l2j.gameserver.mobius.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.mobius.gameserver.model.punishment.PunishmentAffect;
import org.l2j.gameserver.mobius.gameserver.model.punishment.PunishmentTask;
import org.l2j.gameserver.mobius.gameserver.model.punishment.PunishmentType;
import org.l2j.gameserver.mobius.gameserver.model.skills.Skill;

/**
 * Block Action effect implementation.
 * @author BiggBoss
 */
public final class BlockAction extends AbstractEffect
{
	private final Set<Integer> _blockedActions = new HashSet<>();
	
	public BlockAction(StatsSet params)
	{
		final String[] actions = params.getString("blockedActions").split(",");
		for (String action : actions)
		{
			_blockedActions.add(Integer.parseInt(action));
		}
	}
	
	@Override
	public boolean canStart(L2Character effector, L2Character effected, Skill skill)
	{
		return (effected != null) && effected.isPlayer();
	}
	
	@Override
	public boolean checkCondition(Object id)
	{
		return !_blockedActions.contains(id);
	}
	
	@Override
	public void onStart(L2Character effector, L2Character effected, Skill skill)
	{
		if (_blockedActions.contains(BotReportTable.PARTY_ACTION_BLOCK_ID))
		{
			PunishmentManager.getInstance().startPunishment(new PunishmentTask(0, effected.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.PARTY_BAN, 0, "block action debuff", "system", true));
		}
		
		if (_blockedActions.contains(BotReportTable.CHAT_BLOCK_ID))
		{
			PunishmentManager.getInstance().startPunishment(new PunishmentTask(0, effected.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN, 0, "block action debuff", "system", true));
		}
	}
	
	@Override
	public void onExit(L2Character effector, L2Character effected, Skill skill)
	{
		if (_blockedActions.contains(BotReportTable.PARTY_ACTION_BLOCK_ID))
		{
			PunishmentManager.getInstance().stopPunishment(effected.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.PARTY_BAN);
		}
		if (_blockedActions.contains(BotReportTable.CHAT_BLOCK_ID))
		{
			PunishmentManager.getInstance().stopPunishment(effected.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN);
		}
	}
}
