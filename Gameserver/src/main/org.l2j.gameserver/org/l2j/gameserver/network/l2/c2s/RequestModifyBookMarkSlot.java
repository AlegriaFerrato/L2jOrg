package org.l2j.gameserver.network.l2.c2s;

import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.model.actor.instances.player.BookMark;
import org.l2j.gameserver.network.l2.s2c.ExGetBookMarkInfoPacket;

/**
 * dSdS
 */
public class RequestModifyBookMarkSlot extends L2GameClientPacket
{
	private String name, acronym;
	private int icon, slot;

	@Override
	protected void readImpl()
	{
		slot = readInt();
		name = readS(32);
		icon = readInt();
		acronym = readS(4);
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			final BookMark mark = activeChar.getBookMarkList().get(slot);
			if(mark != null)
			{
				mark.setName(name);
				mark.setIcon(icon);
				mark.setAcronym(acronym);
				activeChar.sendPacket(new ExGetBookMarkInfoPacket(activeChar));
			}
		}
	}
}