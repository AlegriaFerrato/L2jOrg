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
package ai.areas.DungeonOfAbyss.SoulTracker;

import java.util.HashMap;
import java.util.Map;

import org.l2j.gameserver.mobius.gameserver.model.Location;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Npc;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.quest.QuestState;

import ai.AbstractNpcAI;

/**
 * @author QuangNguyen
 */
public class Iris extends AbstractNpcAI
{
	// NPC
	private static final int SOUL_TRACKER_IRIS = 31776;
	// Item
	private static final int KEY_OF_EAST_WING = 90011;
	// Locations
	private static final Map<String, Location> LOCATIONS = new HashMap<>();
	static
	{
		LOCATIONS.put("1", new Location(-110038, -180560, -6754)); // Join Room from Iris
		LOCATIONS.put("2", new Location(-109234, -177737, -6751)); // Move to East Wing 2nd
		LOCATIONS.put("3", new Location(-112648, -181517, -6751)); // Go to the Condemned of Abyss Prison
		LOCATIONS.put("4", new Location(146945, 26764, -2200)); // Return to Aden
	}
	
	private Iris()
	{
		addStartNpc(SOUL_TRACKER_IRIS);
		addTalkId(SOUL_TRACKER_IRIS);
		addFirstTalkId(SOUL_TRACKER_IRIS);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc.getId() == SOUL_TRACKER_IRIS)
		{
			QuestState qs = player.getQuestState("Q00935_ExploringTheEastWingOfTheDungeonOfAbyss");
			switch (event)
			{
				case "1":
				{
					if ((qs != null) && qs.isStarted())
					{
						player.teleToLocation(LOCATIONS.get(event), false); // Join Room form Iris
					}
					else
					{
						return "no_enter.htm";
					}
					break;
				}
				case "2":
				{
					player.teleToLocation(LOCATIONS.get(event), false); // Move to East Wing 2nd
					break;
				}
				case "3":
				{
					if (!hasQuestItems(player, KEY_OF_EAST_WING))
					{
						return "no_key.htm";
					}
					player.teleToLocation(LOCATIONS.get(event), false); // Go to the Condemned of Abyss Prison
					break;
				}
				case "4":
				{
					player.teleToLocation(LOCATIONS.get(event), false); // Return to Aden
					break;
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public static void main(String[] args)
	{
		new Iris();
	}
}