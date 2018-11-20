package org.l2j.gameserver.skills.effects;

import org.l2j.gameserver.model.actor.instances.creature.Abnormal;
import org.l2j.gameserver.network.l2.components.SystemMsg;
import org.l2j.gameserver.stats.Env;
import org.l2j.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 */
public final class i_fishing_shot extends i_abstract_effect
{
	private final double _power;

	public i_fishing_shot(Abnormal abnormal, Env env, EffectTemplate template)
	{
		super(abnormal, env, template);
		_power = template.getParam().getDouble("power", 100.);
	}

	@Override
	public boolean checkCondition()
	{
		if(!getEffected().isPlayer())
			return false;

		return super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		getEffected().sendPacket(SystemMsg.YOUR_SPIRITSHOT_HAS_BEEN_ENABLED);
		getEffected().getPlayer().setChargedFishshotPower(_power);
	}
}