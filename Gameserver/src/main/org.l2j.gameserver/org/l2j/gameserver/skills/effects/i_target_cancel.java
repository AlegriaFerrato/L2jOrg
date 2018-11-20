package org.l2j.gameserver.skills.effects;

import org.l2j.gameserver.ai.CtrlIntention;
import org.l2j.gameserver.ai.DefaultAI;
import org.l2j.gameserver.model.Skill;
import org.l2j.gameserver.model.Skill.SkillType;
import org.l2j.gameserver.model.actor.instances.creature.Abnormal;
import org.l2j.gameserver.stats.Env;
import org.l2j.gameserver.templates.skill.EffectTemplate;

public final class i_target_cancel extends i_abstract_effect
{
	private final boolean _stopTarget;

	public i_target_cancel(Abnormal abnormal, Env env, EffectTemplate template)
	{
		super(abnormal, env, template);
		_stopTarget = template.getParam().getBool("stop_target", false);
	}

	@Override
	public void instantUse()
	{
		if(getEffected().getAI() instanceof DefaultAI)
			((DefaultAI) getEffected().getAI()).setGlobalAggro(System.currentTimeMillis() + 3000L);

		getEffected().setTarget(null);

		if(_stopTarget)
			getEffected().stopMove();

		getEffected().abortAttack(true, true);

		Skill castingSkill = getEffected().getCastingSkill();
		if(castingSkill == null || castingSkill.getSkillType() != SkillType.TAKECASTLE)
			getEffected().abortCast(true, true);

		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, getEffector());
	}
}