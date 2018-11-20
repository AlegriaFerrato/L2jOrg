package org.l2j.gameserver.network.l2.c2s;

import org.l2j.gameserver.network.l2.s2c.QuestListPacket;

public class RequestQuestList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		sendPacket(new QuestListPacket(getClient().getActiveChar()));
	}
}