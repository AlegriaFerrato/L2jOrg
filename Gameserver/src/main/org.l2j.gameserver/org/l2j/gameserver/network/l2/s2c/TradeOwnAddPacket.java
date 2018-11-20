package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.model.items.ItemInfo;

public class TradeOwnAddPacket extends L2GameServerPacket
{
	private ItemInfo _item;
	private long _amount;

	public TradeOwnAddPacket(ItemInfo item, long amount)
	{
		_item = item;
		_amount = amount;
	}

	@Override
	protected final void writeImpl()
	{
		writeH(1); // item count
		writeItemInfo(_item, _amount);
	}
}