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
package handlers.skillconditionhandlers;

import java.util.List;

import org.l2j.gameserver.mobius.gameserver.model.L2Object;
import org.l2j.gameserver.mobius.gameserver.model.StatsSet;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Character;
import org.l2j.gameserver.mobius.gameserver.model.items.L2Item;
import org.l2j.gameserver.mobius.gameserver.model.items.type.WeaponType;
import org.l2j.gameserver.mobius.gameserver.model.skills.ISkillCondition;
import org.l2j.gameserver.mobius.gameserver.model.skills.Skill;

/**
 * @author Sdw
 */
public class EquipWeaponSkillCondition implements ISkillCondition
{
	private int _weaponTypesMask = 0;
	
	public EquipWeaponSkillCondition(StatsSet params)
	{
		final List<WeaponType> weaponTypes = params.getEnumList("weaponType", WeaponType.class);
		if (weaponTypes != null)
		{
			for (WeaponType weaponType : weaponTypes)
			{
				_weaponTypesMask |= weaponType.mask();
			}
		}
	}
	
	@Override
	public boolean canUse(L2Character caster, Skill skill, L2Object target)
	{
		final L2Item weapon = caster.getActiveWeaponItem();
		return (weapon != null) && ((weapon.getItemMask() & _weaponTypesMask) != 0);
	}
}
