package org.l2j.gameserver.instancemanager;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.l2j.commons.database.L2DatabaseFactory;
import org.l2j.commons.dbutils.DbUtils;
import org.l2j.gameserver.Announcements;
import org.l2j.gameserver.Config;
import org.l2j.gameserver.data.xml.holder.NpcHolder;
import org.l2j.gameserver.model.Spawner;
import org.l2j.gameserver.model.instances.NpcInstance;
import org.l2j.gameserver.model.instances.ReflectionBossInstance;
import org.l2j.gameserver.tables.GmListTable;
import org.l2j.gameserver.templates.StatsSet;
import org.l2j.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class RaidBossSpawnManager
{
	private static final Logger _log = LoggerFactory.getLogger(RaidBossSpawnManager.class);

	private static RaidBossSpawnManager _instance;

	protected static final TIntObjectMap<Spawner> _spawntable = new TIntObjectHashMap<>();

	protected static TIntObjectMap<StatsSet> _storedInfo;
	private final TIntSet _aliveRaidBosses = new TIntHashSet();

	public enum Status
	{
		ALIVE,
		DEAD,
		UNDEFINED
	}

	private RaidBossSpawnManager()
	{
		_instance = this;
		if(!Config.DONTLOADSPAWN)
			reloadBosses();
	}

	public void reloadBosses()
	{
		loadStatus();
	}

	public void cleanUp()
	{
		updateAllStatusDb();

		_storedInfo.clear();
		_spawntable.clear();
	}

	public static RaidBossSpawnManager getInstance()
	{
		if(_instance == null)
			new RaidBossSpawnManager();
		return _instance;
	}

	private void loadStatus()
	{
		_storedInfo = new TIntObjectHashMap<>();

		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			rset = con.createStatement().executeQuery("SELECT * FROM `raidboss_status`");
			while(rset.next())
			{
				int id = rset.getInt("id");
				StatsSet info = new StatsSet();
				info.set("current_hp", rset.getDouble("current_hp"));
				info.set("current_mp", rset.getDouble("current_mp"));
				info.set("death_time", rset.getInt("death_time"));
				info.set("respawn_delay", rset.getInt("respawn_delay"));
				_storedInfo.put(id, info);
			}
		}
		catch(Exception e)
		{
			_log.warn("RaidBossSpawnManager: Couldnt load raidboss statuses");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		_log.info("RaidBossSpawnManager: Loaded " + _storedInfo.size() + " Statuses");
	}

	private void updateAllStatusDb()
	{
		for(int id : _storedInfo.keySet().toArray())
			updateStatusDb(id, 0);
	}

	private void updateStatusDb(int id, long deathTime)
	{
		Spawner spawner = _spawntable.get(id);
		if(spawner == null)
			return;

		NpcInstance raidboss = spawner.getFirstSpawned();
		if(raidboss instanceof ReflectionBossInstance)
			return;

		StatsSet info = _storedInfo.get(id);
		if(info == null)
			_storedInfo.put(id, info = new StatsSet());

		if(raidboss == null || deathTime > 0)
		{
			info.set("current_hp", 0);
			info.set("current_mp", 0);
			info.set("death_time", (int)(deathTime / 1000));
		}
		else
		{
			info.set("current_hp", raidboss.getCurrentHp());
			info.set("current_mp", raidboss.getCurrentMp());
			info.set("death_time", 0);
		}

		Connection con = null;
		PreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO `raidboss_status` (id, current_hp, current_mp, death_time, respawn_delay) VALUES (?,?,?,?,?)");
			statement.setInt(1, id);
			statement.setDouble(2, info.getDouble("current_hp"));
			statement.setDouble(3, info.getDouble("current_mp"));
			statement.setInt(4, info.getInteger("death_time", 0));
			statement.setInt(5, 0);
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("RaidBossSpawnManager: Couldnt update raidboss_status table");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void addNewSpawn(int npcId, Spawner spawnDat)
	{
		if(_spawntable.containsKey(npcId))
			return;

		_spawntable.put(npcId, spawnDat);

		StatsSet info = _storedInfo.get(npcId);
		if(info != null)
		{
			int respawnDelay = info.getInteger("respawn_delay", 0);
			if(respawnDelay == 0)
			{
				int deathTime = info.getInteger("death_time", 0);
				if(deathTime > 0)
				{
					NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
					if(template != null)
						respawnDelay = spawnDat.calcRespawnTime(deathTime * 1000L, template.isRaid);
				}
			}
			spawnDat.setRespawnTime(respawnDelay);
		}
	}

	public void deleteSpawn(int npcId)
	{
		_spawntable.remove(npcId);
	}

	public void onBossSpawned(NpcInstance npc)
	{
		int bossId = npc.getNpcId();
		if(!_spawntable.containsKey(bossId))
			return;

		StatsSet info = _storedInfo.get(bossId);
		if(info != null && info.getDouble("current_hp") > 1)
		{
			npc.setCurrentHp(info.getDouble("current_hp"), false);
			npc.setCurrentMp(info.getDouble("current_mp"));
		}

		if(npc.isRaid())
		{
			_aliveRaidBosses.add(npc.getNpcId());

			GmListTable.broadcastMessageToGMs("Spawning RaidBoss " + npc.getName());

			if(Config.ALT_ANNONCE_RAID_BOSSES_REVIVAL)
				Announcements.announceToAllFromStringHolder("org.l2j.gameserver.instancemanager.RaidBossSpawnManager." + (npc.isBoss() ? "onBossSpawned" : "onRaidBossSpawned"), npc.getName(), npc.getTitle());
		}
	}

	public void onBossDeath(NpcInstance npc)
	{
		_aliveRaidBosses.remove(npc.getNpcId());
		updateStatusDb(npc.getNpcId(), npc.getDeathTime());
	}

	public Status getRaidBossStatusId(int bossId)
	{
		Spawner spawner = _spawntable.get(bossId);
		if(spawner == null)
			return Status.UNDEFINED;

		NpcInstance npc = spawner.getFirstSpawned();
		return npc == null ? Status.DEAD : Status.ALIVE;
	}

	public boolean isDefined(int bossId)
	{
		return _spawntable.containsKey(bossId);
	}

	public TIntObjectMap<Spawner> getSpawnTable()
	{
		return _spawntable;
	}

	public int[] getAliveRaidBosees()
	{
		return _aliveRaidBosses.toArray();
	}
}