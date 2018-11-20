package org.l2j.gameserver.network.l2.s2c;

import java.util.HashMap;
import java.util.Map;

import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.utils.Location;

public class PartyMemberPositionPacket extends L2GameServerPacket
{
	private final Map<Integer, Location> positions = new HashMap<Integer, Location>();

	public PartyMemberPositionPacket add(Player actor)
	{
		positions.put(actor.getObjectId(), actor.getLoc());
		return this;
	}

	public int size()
	{
		return positions.size();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(positions.size());
		for(Map.Entry<Integer, Location> e : positions.entrySet())
		{
			writeD(e.getKey());
			writeD(e.getValue().x);
			writeD(e.getValue().y);
			writeD(e.getValue().z);
		}
	}
}