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
package handlers.itemhandlers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2j.gameserver.mobius.gameserver.enums.PlayerAction;
import org.l2j.gameserver.mobius.gameserver.handler.IItemHandler;
import org.l2j.gameserver.mobius.gameserver.instancemanager.CastleManager;
import org.l2j.gameserver.mobius.gameserver.instancemanager.SiegeGuardManager;
import org.l2j.gameserver.mobius.gameserver.model.ClanPrivilege;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Playable;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.entity.Castle;
import org.l2j.gameserver.mobius.gameserver.model.events.EventType;
import org.l2j.gameserver.mobius.gameserver.model.events.ListenerRegisterType;
import org.l2j.gameserver.mobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2j.gameserver.mobius.gameserver.model.events.annotations.RegisterType;
import org.l2j.gameserver.mobius.gameserver.model.events.impl.character.player.OnPlayerDlgAnswer;
import org.l2j.gameserver.mobius.gameserver.model.holders.SiegeGuardHolder;
import org.l2j.gameserver.mobius.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.mobius.gameserver.network.SystemMessageId;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ConfirmDlg;

import ai.AbstractNpcAI;

/**
 * Mercenary Ticket Item Handler.
 * @author St3eT
 */
public final class MercTicket extends AbstractNpcAI implements IItemHandler
{
	private final Map<Integer, L2ItemInstance> _items = new ConcurrentHashMap<>();
	
	public MercTicket()
	{
	}
	
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		final Castle castle = CastleManager.getInstance().getCastle(activeChar);
		if ((castle == null) || (activeChar.getClan() == null) || (castle.getOwnerId() != activeChar.getClanId()) || !activeChar.hasClanPrivilege(ClanPrivilege.CS_MERCENARIES))
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_POSITION_MERCENARIES);
			return false;
		}
		
		final int castleId = castle.getResidenceId();
		final SiegeGuardHolder holder = SiegeGuardManager.getInstance().getSiegeGuardByItem(castleId, item.getId());
		if ((holder == null) || (castleId != holder.getCastleId()))
		{
			activeChar.sendPacket(SystemMessageId.MERCENARIES_CANNOT_BE_POSITIONED_HERE);
			return false;
		}
		else if (castle.getSiege().isInProgress())
		{
			activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
			return false;
		}
		else if (SiegeGuardManager.getInstance().isTooCloseToAnotherTicket(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT);
			return false;
		}
		else if (SiegeGuardManager.getInstance().isAtNpcLimit(castleId, item.getId()))
		{
			activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
			return false;
		}
		
		_items.put(activeChar.getObjectId(), item);
		final ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.PLACE_S1_IN_THE_CURRENT_LOCATION_AND_DIRECTION_DO_YOU_WISH_TO_CONTINUE);
		dlg.addTime(15000);
		dlg.addNpcName(holder.getNpcId());
		activeChar.sendPacket(dlg);
		activeChar.addAction(PlayerAction.MERCENARY_CONFIRM);
		return true;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_DLG_ANSWER)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerDlgAnswer(OnPlayerDlgAnswer event)
	{
		final L2PcInstance activeChar = event.getActiveChar();
		if (activeChar.removeAction(PlayerAction.MERCENARY_CONFIRM) && _items.containsKey(activeChar.getObjectId()))
		{
			if (SiegeGuardManager.getInstance().isTooCloseToAnotherTicket(activeChar))
			{
				activeChar.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT);
				return;
			}
			
			if (event.getAnswer() == 1)
			{
				final L2ItemInstance item = _items.get(activeChar.getObjectId());
				SiegeGuardManager.getInstance().addTicket(item.getId(), activeChar);
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false); // Remove item from char's inventory
			}
			_items.remove(activeChar.getObjectId());
		}
	}
}