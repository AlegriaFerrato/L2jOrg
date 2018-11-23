package org.l2j.gameserver.network.l2.c2s;

import org.l2j.gameserver.network.l2.GameClient;
import org.l2j.gameserver.network.l2.s2c.CharacterSelectionInfoPacket;

public class CharacterRestore extends L2GameClientPacket
{
	// cd
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readInt();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		try
		{
			client.markRestoredChar(_charSlot);
		}
		catch(Exception e)
		{}
		CharacterSelectionInfoPacket cl = new CharacterSelectionInfoPacket(client);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}