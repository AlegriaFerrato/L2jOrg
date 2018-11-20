package org.l2j.gameserver.data;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.lang.reflect.Constructor;

import org.l2j.commons.data.xml.AbstractHolder;
import org.l2j.gameserver.data.xml.holder.ShuttleTemplateHolder;
import org.l2j.gameserver.idfactory.IdFactory;
import org.l2j.gameserver.model.entity.boat.Boat;
import org.l2j.gameserver.model.entity.boat.Shuttle;
import org.l2j.gameserver.templates.CreatureTemplate;
import org.l2j.gameserver.templates.ShuttleTemplate;

/**
 * @author VISTALL
 * @date 9:20/27.12.2010
 */
public final class BoatHolder extends AbstractHolder
{
	public static final CreatureTemplate TEMPLATE = new CreatureTemplate(CreatureTemplate.getEmptyStatsSet());

	private static BoatHolder _instance = new BoatHolder();
	private final TIntObjectHashMap<Boat> _boats = new TIntObjectHashMap<Boat>();

	public static BoatHolder getInstance()
	{
		return _instance;
	}

	public void spawnAll()
	{
		log();
		for(TIntObjectIterator<Boat> iterator = _boats.iterator(); iterator.hasNext();)
		{
			iterator.advance();
			iterator.value().spawnMe();
			info("Spawning: " + iterator.value().getName());
		}
	}

	public Boat initBoat(String name, String clazz)
	{
		try
		{
			Class<?> cl = Class.forName("org.l2j.gameserver.model.entity.boat." + clazz);
			Constructor<?> constructor = cl.getConstructor(Integer.TYPE, CreatureTemplate.class);

			Boat boat = (Boat) constructor.newInstance(IdFactory.getInstance().getNextId(), TEMPLATE);
			boat.setName(name);
			addBoat(boat);
			return boat;
		}
		catch(Exception e)
		{
			error("Fail to init boat: " + clazz, e);
		}

		return null;
	}

	public Shuttle initShuttle(String name, int shuttleId)
	{
		try
		{
			ShuttleTemplate template = ShuttleTemplateHolder.getInstance().getTemplate(shuttleId);
			Shuttle shuttle = new Shuttle(IdFactory.getInstance().getNextId(), template);
			shuttle.setName(name);
			addBoat(shuttle);
			return shuttle;
		}
		catch(Exception e)
		{
			error("Fail to init shuttle id: " + shuttleId, e);
		}

		return null;
	}

	public Boat getBoat(String name)
	{
		for(TIntObjectIterator<Boat> iterator = _boats.iterator(); iterator.hasNext();)
		{
			iterator.advance();
			if(iterator.value().getName().equals(name))
				return iterator.value();
		}

		return null;
	}

	public Boat getBoat(int boatId)
	{
		return _boats.get(boatId);
	}

	public void addBoat(Boat boat)
	{
		_boats.put(boat.getBoatId(), boat);
	}

	public void removeBoat(Boat boat)
	{
		_boats.remove(boat.getBoatId());
	}

	@Override
	public int size()
	{
		return _boats.size();
	}

	@Override
	public void clear()
	{
		_boats.clear();
	}
}