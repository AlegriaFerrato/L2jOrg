package org.l2j.gameserver.stats.funcs;

import org.l2j.gameserver.stats.Env;
import org.l2j.gameserver.stats.Stats;

public class FuncDiv extends Func
{
	public FuncDiv(Stats stat, int order, Object owner, double value)
	{
		super(stat, order, owner, value);
	}

	@Override
	public void calc(Env env)
	{
		env.value /= value;
	}
}