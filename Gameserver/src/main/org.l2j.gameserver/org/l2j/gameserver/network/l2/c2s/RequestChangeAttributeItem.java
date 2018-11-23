package org.l2j.gameserver.network.l2.c2s;

import org.l2j.commons.dao.JdbcEntityState;
import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.model.base.Element;
import org.l2j.gameserver.model.items.ItemInstance;
import org.l2j.gameserver.network.l2.components.SystemMsg;
import org.l2j.gameserver.network.l2.s2c.ExChangeAttributeFail;
import org.l2j.gameserver.network.l2.s2c.ExChangeAttributeOk;
import org.l2j.gameserver.network.l2.s2c.InventoryUpdatePacket;
import org.l2j.gameserver.network.l2.s2c.SystemMessagePacket;

/**
 * @author Bonux
 */
public class RequestChangeAttributeItem extends L2GameClientPacket
{
	public int _consumeItemId;
	public int _itemObjId;
	public int _newElementId;

	@Override
	protected void readImpl()
	{
		_consumeItemId = readInt(); //Change Attribute Crystall ID
		_itemObjId = readInt(); //Item for Change ObjId
		_newElementId = readInt(); //Element
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(SystemMsg.YOU_CAN_NOT_CHANGE_THE_ATTRIBUTE_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			activeChar.sendPacket(ExChangeAttributeFail.STATIC);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);
		if(item == null || !item.isWeapon())
		{
			activeChar.sendPacket(SystemMsg.UNABLE_TO_CHANCE_THE_ATTRIBUTE);
			activeChar.sendPacket(ExChangeAttributeFail.STATIC);
			return;
		}

		if(!activeChar.getInventory().destroyItemByItemId(_consumeItemId, 1L))
		{
			activeChar.sendActionFailed();
			return;
		}

		boolean equipped = false;
		if(equipped = item.isEquipped())
		{
			activeChar.getInventory().isRefresh = true;
			activeChar.getInventory().unEquipItem(item);
		}

		Element oldElement = item.getAttackElement();
		int elementVal = item.getAttributeElementValue(oldElement, false);
		item.setAttributeElement(oldElement, 0);

		Element newElement = Element.VALUES[_newElementId];
		item.setAttributeElement(newElement, item.getAttributeElementValue(newElement, false) + elementVal);

		item.setJdbcState(JdbcEntityState.UPDATED);
		item.update();

		if(equipped)
		{
			activeChar.getInventory().equipItem(item);
			activeChar.getInventory().isRefresh = false;
		}

		SystemMessagePacket msg = new SystemMessagePacket(SystemMsg.IN_THE_ITEM_S1_ATTRIBUTE_S2_SUCCESSFULLY_CHANGED_TO_S3);
		msg.addName(item);
		msg.addElementName(oldElement);
		msg.addElementName(newElement);
		activeChar.sendPacket(msg);
		activeChar.sendPacket(new InventoryUpdatePacket().addModifiedItem(activeChar, item));
		activeChar.sendPacket(ExChangeAttributeOk.STATIC);
		activeChar.updateStats();
	}
}
