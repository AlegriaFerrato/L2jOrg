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
package org.l2j.gameserver.mobius.gameserver.data.xml.impl;

import com.l2jmobius.commons.util.IGameXmlReader;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.residences.ResidenceFunctionTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * The residence functions data
 * @author UnAfraid
 */
public final class ResidenceFunctionsData implements IGameXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ResidenceFunctionsData.class.getName());
	private final Map<Integer, List<ResidenceFunctionTemplate>> _functions = new HashMap<>();
	
	protected ResidenceFunctionsData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_functions.clear();
		parseDatapackFile("data/ResidenceFunctions.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded: " + _functions.size() + " functions.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", list -> forEach(list, "function", func ->
		{
			final NamedNodeMap attrs = func.getAttributes();
			final StatsSet set = new StatsSet(HashMap::new);
			for (int i = 0; i < attrs.getLength(); i++)
			{
				final Node node = attrs.item(i);
				set.set(node.getNodeName(), node.getNodeValue());
			}
			forEach(func, "function", levelNode ->
			{
				final NamedNodeMap levelAttrs = levelNode.getAttributes();
				final StatsSet levelSet = new StatsSet(HashMap::new);
				levelSet.merge(set);
				for (int i = 0; i < levelAttrs.getLength(); i++)
				{
					final Node node = levelAttrs.item(i);
					levelSet.set(node.getNodeName(), node.getNodeValue());
				}
				final ResidenceFunctionTemplate template = new ResidenceFunctionTemplate(levelSet);
				_functions.computeIfAbsent(template.getId(), key -> new ArrayList<>()).add(template);
			});
		}));
	}
	
	/**
	 * @param id
	 * @param level
	 * @return function template by id and level, null if not available
	 */
	public ResidenceFunctionTemplate getFunction(int id, int level)
	{
		return _functions.getOrDefault(id, Collections.emptyList()).stream().filter(template -> template.getLevel() == level).findAny().orElse(null);
	}
	
	/**
	 * @param id
	 * @return function template by id, null if not available
	 */
	public List<ResidenceFunctionTemplate> getFunctions(int id)
	{
		return _functions.get(id);
	}
	
	public static ResidenceFunctionsData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ResidenceFunctionsData INSTANCE = new ResidenceFunctionsData();
	}
}
