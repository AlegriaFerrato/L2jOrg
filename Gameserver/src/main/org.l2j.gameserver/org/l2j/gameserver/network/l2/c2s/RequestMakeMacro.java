package org.l2j.gameserver.network.l2.c2s;

import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.model.actor.instances.player.Macro;
import org.l2j.gameserver.model.actor.instances.player.Macro.L2MacroCmd;
import org.l2j.gameserver.network.l2.components.SystemMsg;

/**
 * packet type id 0xcd
 *
 * sample
 *
 * cd
 * d // id
 * S // macro name
 * S // unknown  desc
 * S // unknown  acronym
 * c // icon
 * c // count
 *
 * c // entry
 * c // type
 * d // skill id
 * c // shortcut id
 * S // command name
 *
 * format:		cdSSScc (ccdcS)
 */
public class RequestMakeMacro extends L2GameClientPacket
{
	private Macro _macro;

	@Override
	protected void readImpl()
	{
		int _id = readInt();
		String _name = readS(32);
		String _desc = readS(64);
		String _acronym = readS(4);
		int _icon = readInt();
		int _count = readByte();
		if(_count > 12)
			_count = 12;
		L2MacroCmd[] commands = new L2MacroCmd[_count];
		for(int i = 0; i < _count; i++)
		{
			int entry = readByte();
			int type = readByte(); // 1 = skill, 3 = action, 4 = shortcut
			int d1 = readInt(); // skill or page number for shortcuts
			int d2 = readByte();
			String command = readString().replace(";", "").replace(",", "");
			commands[i] = new L2MacroCmd(entry, type, d1, d2, command);
		}
		_macro = new Macro(_id, _icon, _name, _desc, _acronym, commands);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getMacroses().getAllMacroses().length > 48)
		{
			activeChar.sendPacket(SystemMsg.YOU_MAY_CREATE_UP_TO_48_MACROS);
			return;
		}

		if(_macro.name.length() == 0)
		{
			activeChar.sendPacket(SystemMsg.ENTER_THE_NAME_OF_THE_MACRO);
			return;
		}

		if(_macro.descr.length() > 32)
		{
			activeChar.sendPacket(SystemMsg.MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS);
			return;
		}

		activeChar.registerMacro(_macro);
	}
}