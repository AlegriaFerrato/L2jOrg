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
package org.l2j.gameserver.mobius.gameserver.model;

import com.l2jmobius.gameserver.model.holders.RestorationItemHolder;

import java.util.List;

/**
 * @author Zoey76
 */
public class L2ExtractableProductItem
{
	private final List<RestorationItemHolder> _items;
	private final double _chance;
	
	public L2ExtractableProductItem(List<RestorationItemHolder> items, double chance)
	{
		_items = items;
		_chance = chance;
	}
	
	/**
	 * @return the the production list.
	 */
	public List<RestorationItemHolder> getItems()
	{
		return _items;
	}
	
	/**
	 * @return the chance of the production list.
	 */
	public double getChance()
	{
		return _chance;
	}
}
