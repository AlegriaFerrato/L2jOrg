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
package handlers.effecthandlers;

import org.l2j.gameserver.ai.CtrlIntention;
import org.l2j.gameserver.engine.geo.GeoEngine;
import org.l2j.gameserver.model.Location;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.Creature;
import org.l2j.gameserver.model.actor.Summon;
import org.l2j.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.model.effects.EffectType;
import org.l2j.gameserver.model.items.instance.Item;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.network.serverpackets.FlyToLocation;
import org.l2j.gameserver.network.serverpackets.FlyToLocation.FlyType;
import org.l2j.gameserver.network.serverpackets.ValidateLocation;
import org.l2j.gameserver.util.MathUtil;

import static org.l2j.gameserver.util.MathUtil.convertHeadingToDegree;

/**
 * Teleport To Target effect implementation.
 * @author Didldak, Adry_85
 */
public final class TeleportToSummon extends AbstractEffect
{
	private final double _maxDistance;
	
	public TeleportToSummon(StatsSet params)
	{
		_maxDistance = params.getDouble("distance", -1);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.TELEPORT_TO_TARGET;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean canStart(Creature effector, Creature effected, Skill skill)
	{
		return effected.hasServitors();
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		final Summon summon = effected.getActingPlayer().getFirstServitor();
		
		if (_maxDistance > 0 && !MathUtil.isInsideRadius2D(effector, summon, (int) _maxDistance))
		{
			return;
		}
		
		final int px = summon.getX();
		final int py = summon.getY();
		double ph = convertHeadingToDegree(summon.getHeading());
		
		ph += 180;
		if (ph > 360)
		{
			ph -= 360;
		}
		
		ph = (Math.PI * ph) / 180;
		final int x = (int) (px + (25 * Math.cos(ph)));
		final int y = (int) (py + (25 * Math.sin(ph)));
		final int z = summon.getZ();
		
		final Location loc = GeoEngine.getInstance().canMoveToTargetLoc(effector.getX(), effector.getY(), effector.getZ(), x, y, z, effector.getInstanceWorld());
		
		effector.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		effector.broadcastPacket(new FlyToLocation(effector, loc.getX(), loc.getY(), loc.getZ(), FlyType.DUMMY));
		effector.abortAttack();
		effector.abortCast();
		effector.setXYZ(loc);
		effector.broadcastPacket(new ValidateLocation(effector));
		effected.revalidateZone(true);
	}
}
