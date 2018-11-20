package org.l2j.gameserver.handler.admincommands.impl;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.l2j.gameserver.Config;
import org.l2j.gameserver.ThreadPoolManager;
import org.l2j.gameserver.handler.admincommands.IAdminCommandHandler;
import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.scripts.Scripts;
import org.apache.commons.lang3.ClassUtils;

public class AdminScripts implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_run_script,
		admin_runs,
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanReload)
			return false;

		switch(command)
		{
			case admin_run_script:
			case admin_runs:
				if(wordList.length < 2)
					return false;

				String param = wordList[1];
				if (!run(param))
					activeChar.sendMessage("Can't run script.");
				else
					activeChar.sendMessage("Running script...");
				break;
		}
		return true;
	}

	private boolean run(String target)
	{
		File file = new File(Config.DATAPACK_ROOT, "data/scripts/" + target.replace(".", "/") + ".java");
		if(!file.exists())
			return false;

		List<Class<?>> classes = Scripts.getInstance().load(file);
		for(Class<?> clazz : classes)
		{
			if(!ClassUtils.isAssignable(clazz, Runnable.class))
				return false;

			Runnable r;
			try
			{
				r = (Runnable) clazz.newInstance();
			}
			catch(Exception e)
			{
				return false;
			}

			ThreadPoolManager.getInstance().execute(r);
			return true;
		}

		return false;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}