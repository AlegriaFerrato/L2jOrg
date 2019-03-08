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
package org.l2j.gameserver.mobius.gameserver.network.clientpackets;

import com.l2jmobius.Config;
import org.l2j.commons.network.PacketReader;
import org.l2j.gameserver.mobius.gameserver.data.xml.impl.SkillData;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Summon;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.skills.AbnormalType;
import org.l2j.gameserver.mobius.gameserver.model.skills.Skill;
import org.l2j.gameserver.mobius.gameserver.network.L2GameClient;

/**
 * @author KenM
 */
public class RequestDispel extends IClientIncomingPacket
{
	private int _objectId;
	private int _skillId;
	private int _skillLevel;
	private int _skillSubLevel;
	
	@Override
	public void readImpl(ByteBuffer packet)
	{
		_objectId = packet.getInt();
		_skillId = packet.getInt();
		_skillLevel = packet.getShort();
		_skillSubLevel = packet.getShort();
		return true;
	}
	
	@Override
	public void runImpl()
	{
		if ((_skillId <= 0) || (_skillLevel <= 0))
		{
			return;
		}
		final L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		final Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLevel, _skillSubLevel);
		if (skill == null)
		{
			return;
		}
		if (!skill.canBeDispelled() || skill.isStayAfterDeath() || skill.isDebuff())
		{
			return;
		}
		if (skill.getAbnormalType() == AbnormalType.TRANSFORM)
		{
			return;
		}
		if (skill.isDance() && !Config.DANCE_CANCEL_BUFF)
		{
			return;
		}
		if (activeChar.getObjectId() == _objectId)
		{
			activeChar.stopSkillEffects(true, _skillId);
		}
		else
		{
			final L2Summon pet = activeChar.getPet();
			if ((pet != null) && (pet.getObjectId() == _objectId))
			{
				pet.stopSkillEffects(true, _skillId);
			}
			
			final L2Summon servitor = activeChar.getServitor(_objectId);
			if (servitor != null)
			{
				servitor.stopSkillEffects(true, _skillId);
			}
		}
	}
}
