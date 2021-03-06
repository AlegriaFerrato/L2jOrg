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
package handlers.mission;

import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.engine.mission.MissionStatus;
import org.l2j.gameserver.enums.QuestType;
import org.l2j.gameserver.handler.AbstractMissionHandler;
import org.l2j.gameserver.engine.mission.MissionDataHolder;
import org.l2j.gameserver.data.database.data.MissionPlayerData;
import org.l2j.gameserver.model.events.Containers;
import org.l2j.gameserver.model.events.EventType;
import org.l2j.gameserver.model.events.impl.character.player.OnPlayerQuestComplete;
import org.l2j.gameserver.model.events.listeners.ConsumerEventListener;

/**
 * @author UnAfraid
 */
public class QuestMissionHandler extends AbstractMissionHandler
{
	public QuestMissionHandler(MissionDataHolder holder)
	{
		super(holder);
	}
	
	@Override
	public void init()
	{
		Containers.Players().addListener(new ConsumerEventListener(this, EventType.ON_PLAYER_QUEST_COMPLETE, (OnPlayerQuestComplete event) -> onQuestComplete(event), this));
	}

	
	private void onQuestComplete(OnPlayerQuestComplete event)
	{
		final Player player = event.getActiveChar();
		if (event.getQuestType() == QuestType.DAILY)
		{
			final MissionPlayerData entry = getPlayerEntry(player, true);
			if (entry.getStatus() == MissionStatus.NOT_AVAILABLE)
			{
				if (entry.increaseProgress() >= getRequiredCompletion())
				{
					entry.setStatus(MissionStatus.AVAILABLE);
					notifyAvailablesReward(player);
				}
				storePlayerEntry(entry);
			}
		}
	}
}
