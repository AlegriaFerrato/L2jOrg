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

import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.Creature;
import org.l2j.gameserver.model.actor.Npc;
import org.l2j.gameserver.model.actor.Playable;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.model.events.EventType;
import org.l2j.gameserver.model.events.impl.character.player.OnPlayableExpChanged;
import org.l2j.gameserver.model.events.listeners.ConsumerEventListener;
import org.l2j.gameserver.model.items.instance.Item;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.model.stats.Stats;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.ExSpawnEmitter;

import static org.l2j.gameserver.util.GameUtils.isNpc;
import static org.l2j.gameserver.util.GameUtils.isPlayer;

/**
 * Soul Eating effect implementation.
 * @author UnAfraid
 */
public final class SoulEating extends AbstractEffect
{
	private final int _expNeeded;
	private final int _maxSouls;
	
	public SoulEating(StatsSet params)
	{
		_expNeeded = params.getInt("expNeeded");
		_maxSouls = params.getInt("maxSouls");
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (isPlayer(effected))
		{
			effected.addListener(new ConsumerEventListener(effected, EventType.ON_PLAYABLE_EXP_CHANGED, (OnPlayableExpChanged event) -> onExperienceReceived(event.getActiveChar(), (event.getNewExp() - event.getOldExp())), this));
		}
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		if (isPlayer(effected))
		{
			effected.removeListenerIf(EventType.ON_PLAYABLE_EXP_CHANGED, listener -> listener.getOwner() == this);
		}
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		effected.getStat().mergeAdd(Stats.MAX_SOULS, _maxSouls);
	}
	
	private void onExperienceReceived(Playable playable, long exp)
	{
		// TODO: Verify logic.
		if (isPlayer(playable) && (exp >= _expNeeded))
		{
			final Player player = playable.getActingPlayer();
			final int maxSouls = (int) player.getStat().getValue(Stats.MAX_SOULS, 0);
			if (player.getChargedSouls() >= maxSouls)
			{
				playable.sendPacket(SystemMessageId.SOUL_CANNOT_BE_ABSORBED_ANYMORE);
				return;
			}
			
			player.increaseSouls(1);
			
			if (isNpc(player.getTarget()))
			{
				final Npc npc = (Npc) playable.getTarget();
				player.broadcastPacket(new ExSpawnEmitter(player, npc), 500);
			}
		}
	}
}
