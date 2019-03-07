package org.l2j.gameserver.mobius.gameserver;

import io.github.joealisson.mmocore.ConnectionBuilder;
import io.github.joealisson.mmocore.ConnectionHandler;
import org.l2j.commons.database.DatabaseAccess;
import org.l2j.commons.util.DeadLockDetector;
import org.l2j.gameserver.ThreadPoolManager;
import org.l2j.gameserver.mobius.gameserver.cache.HtmCache;
import org.l2j.gameserver.mobius.gameserver.data.sql.impl.*;
import org.l2j.gameserver.mobius.gameserver.data.xml.impl.*;
import org.l2j.gameserver.mobius.gameserver.datatables.BotReportTable;
import org.l2j.gameserver.mobius.gameserver.datatables.EventDroplist;
import org.l2j.gameserver.mobius.gameserver.datatables.ItemTable;
import org.l2j.gameserver.mobius.gameserver.datatables.SchemeBufferTable;
import org.l2j.gameserver.mobius.gameserver.geoengine.GeoEngine;
import org.l2j.gameserver.mobius.gameserver.handler.ConditionHandler;
import org.l2j.gameserver.mobius.gameserver.handler.DailyMissionHandler;
import org.l2j.gameserver.mobius.gameserver.handler.EffectHandler;
import org.l2j.gameserver.mobius.gameserver.handler.SkillConditionHandler;
import org.l2j.gameserver.mobius.gameserver.idfactory.IdFactory;
import org.l2j.gameserver.mobius.gameserver.instancemanager.*;
import org.l2j.gameserver.mobius.gameserver.model.L2World;
import org.l2j.gameserver.mobius.gameserver.model.entity.Hero;
import org.l2j.gameserver.mobius.gameserver.model.events.EventDispatcher;
import org.l2j.gameserver.mobius.gameserver.model.olympiad.Olympiad;
import org.l2j.gameserver.mobius.gameserver.model.votereward.VoteSystem;
import org.l2j.gameserver.mobius.gameserver.network.telnet.TelnetServer;
import org.l2j.gameserver.mobius.gameserver.scripting.ScriptEngineManager;
import org.l2j.gameserver.mobius.gameserver.taskmanager.TaskManager;
import org.l2j.gameserver.mobius.gameserver.util.Broadcast;
import org.l2j.gameserver.network.authcomm.AuthServerCommunication;
import org.l2j.gameserver.network.l2.GameClient;
import org.l2j.gameserver.network.l2.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Properties;

import static java.util.Objects.nonNull;
import static org.l2j.commons.util.Util.isNullOrEmpty;

public class GameServer {

    private static Logger LOGGER;
    private static final String LOG4J_CONFIGURATION_FILE = "log4j.configurationFile";

    public static final String UPDATE_NAME = "Classic: Saviors (Seven Signs)";
    private static String version;

    private final DeadLockDetector _deadDetectThread;
    private static GameServer INSTANCE;

    private final ConnectionHandler<GameClient> connectionHandler;

    public long getUsedMemoryMB()
    {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
    }

    public GameServer() throws Exception {

        final long serverLoadStart = System.currentTimeMillis();

        printSection("IdFactory");
        if (!IdFactory.getInstance().isInitialized()) {
            LOGGER.error("Could not read object IDs from database. Please check your configuration.");
            throw new Exception("Could not initialize the ID factory!");
        }

        // load script engines
        printSection("Scripting Engine");
        EventDispatcher.getInstance();
        ScriptEngineManager.getInstance();

        printSection("Telnet");
        TelnetServer.getInstance();

        printSection("World");
        // start game time control early
        GameTimeController.init();
        L2World.getInstance();
        MapRegionManager.getInstance();
        ZoneManager.getInstance();
        DoorData.getInstance();
        FenceData.getInstance();
        AnnouncementsTable.getInstance();
        GlobalVariablesManager.getInstance();

        printSection("Data");
        ActionData.getInstance();
        CategoryData.getInstance();
        SecondaryAuthData.getInstance();
        CombinationItemsData.getInstance();
        SayuneData.getInstance();
        ClanRewardData.getInstance();
        DailyMissionHandler.getInstance().executeScript();
        DailyMissionData.getInstance();

        printSection("Skills");
        SkillConditionHandler.getInstance().executeScript();
        EffectHandler.getInstance().executeScript();
        EnchantSkillGroupsData.getInstance();
        SkillTreesData.getInstance();
        SkillData.getInstance();
        PetSkillData.getInstance();

        printSection("Items");
        ConditionHandler.getInstance().executeScript();
        ItemTable.getInstance();
        EnchantItemGroupsData.getInstance();
        EnchantItemData.getInstance();
        EnchantItemOptionsData.getInstance();
        ItemCrystallizationData.getInstance();
        OptionData.getInstance();
        VariationData.getInstance();
        EnsoulData.getInstance();
        EnchantItemHPBonusData.getInstance();
        BuyListData.getInstance();
        MultisellData.getInstance();
        RecipeData.getInstance();
        ArmorSetsData.getInstance();
        FishingData.getInstance();
        HennaData.getInstance();
        PrimeShopData.getInstance();
        PcCafePointsManager.getInstance();
        AppearanceItemData.getInstance();
        CommissionManager.getInstance();
        LuckyGameData.getInstance();
        AttendanceRewardData.getInstance();

        printSection("Characters");
        ClassListData.getInstance();
        InitialEquipmentData.getInstance();
        InitialShortcutData.getInstance();
        ExperienceData.getInstance();
        PlayerXpPercentLostData.getInstance();
        KarmaData.getInstance();
        HitConditionBonusData.getInstance();
        PlayerTemplateData.getInstance();
        CharNameTable.getInstance();
        AdminData.getInstance();
        PetDataTable.getInstance();
        CubicData.getInstance();
        CharSummonTable.getInstance().init();
        BeautyShopData.getInstance();
        MentorManager.getInstance();

        if (Config.FACTION_SYSTEM_ENABLED)
        {
            FactionManager.getInstance();
        }

        if (Config.PREMIUM_SYSTEM_ENABLED)
        {
            LOGGER.info("PremiumManager: Premium system is enabled.");
            PremiumManager.getInstance();
        }

        printSection("Clans");
        ClanTable.getInstance();
        ResidenceFunctionsData.getInstance();
        ClanHallData.getInstance();
        ClanHallAuctionManager.getInstance();
        ClanEntryManager.getInstance();

        printSection("Geodata");
        long geodataMemory = getUsedMemoryMB();
        GeoEngine.getInstance();
        geodataMemory = getUsedMemoryMB() - geodataMemory;
        if (geodataMemory < 0)
        {
            geodataMemory = 0;
        }

        printSection("NPCs");
        NpcData.getInstance();
        FakePlayerData.getInstance();
        FakePlayerChatManager.getInstance();
        ExtendDropData.getInstance();
        SpawnsData.getInstance();
        WalkingManager.getInstance();
        StaticObjectData.getInstance();
        ItemAuctionManager.getInstance();
        CastleManager.getInstance().loadInstances();
        SchemeBufferTable.getInstance();
        GrandBossManager.getInstance();
        EventDroplist.getInstance();

        printSection("Instance");
        InstanceManager.getInstance();

        printSection("Olympiad");
        Olympiad.getInstance();
        Hero.getInstance();

        // Call to load caches
        printSection("Cache");
        HtmCache.getInstance();
        CrestTable.getInstance();
        TeleportersData.getInstance();
        MatchingRoomManager.getInstance();
        PetitionManager.getInstance();
        CursedWeaponsManager.getInstance();
        TransformData.getInstance();
        BotReportTable.getInstance();
        if (Config.SELLBUFF_ENABLED)
        {
            SellBuffsManager.getInstance();
        }

        printSection("Scripts");
        QuestManager.getInstance();
        BoatManager.getInstance();
        AirShipManager.getInstance();
        ShuttleData.getInstance();
        GraciaSeedsManager.getInstance();

        try {
            LOGGER.info(getClass().getSimpleName() + ": Loading server scripts:");
            ScriptEngineManager.getInstance().executeMasterHandler();
            ScriptEngineManager.getInstance().executeScriptList();
        } catch (Exception e) {
            LOGGER.warn("Failed to execute script list!", e);
        }

        SpawnsData.getInstance().init();
        DBSpawnManager.getInstance();

        printSection("Event Engine");
        EventEngineData.getInstance();
        VoteSystem.initialize();

        printSection("Siege");
        SiegeManager.getInstance().getSieges();
        CastleManager.getInstance().activateInstances();
        // No fortresses
        // FortManager.getInstance().loadInstances();
        // FortManager.getInstance().activateInstances();
        // FortSiegeManager.getInstance();
        SiegeScheduleData.getInstance();

        CastleManorManager.getInstance();
        SiegeGuardManager.getInstance();
        QuestManager.getInstance().report();

        if (Config.SAVE_DROPPED_ITEM)
        {
            ItemsOnGroundManager.getInstance();
        }

        if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
        {
            ItemsAutoDestroy.getInstance();
        }

        TaskManager.getInstance();

        AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);

        if (Config.ALLOW_MAIL)
        {
            MailManager.getInstance();
        }

        PunishmentManager.getInstance();

        Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

        LOGGER.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

        if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
        {
            OfflineTradersTable.getInstance().restoreOfflineTraders();
        }

        if (Config.SERVER_RESTART_SCHEDULE_ENABLED)
        {
            ServerRestartManager.getInstance();
        }

        if (Config.DEADLOCK_DETECTOR)
        {
            _deadDetectThread = new DeadLockDetector(Duration.ofSeconds(Config.DEADLOCK_CHECK_INTERVAL), () ->
            {
                if (Config.RESTART_ON_DEADLOCK)
                {
                    Broadcast.toAllOnlinePlayers("Server has stability issues - restarting now.");
                    Shutdown.getInstance().startShutdown(null, 60, true);
                }
            });
            _deadDetectThread.setDaemon(true);
            _deadDetectThread.start();
        }
        else
        {
            _deadDetectThread = null;
        }
        System.gc();
        final long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
        LOGGER.info(getClass().getSimpleName() + ": Started, using " + getUsedMemoryMB() + " of " + totalMem + " MB total memory.");
        LOGGER.info(getClass().getSimpleName() + ": Geodata use " + geodataMemory + " MB of memory.");
        LOGGER.info(getClass().getSimpleName() + ": Maximum number of connected players is " + Config.MAXIMUM_ONLINE_USERS + ".");
        LOGGER.info(getClass().getSimpleName() + ": Server loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");

        final GamePacketHandler gph = new GamePacketHandler();
        connectionHandler = ConnectionBuilder.create(new InetSocketAddress(Config.PORT_GAME), gph, gph, gph).bufferLargeSize(24 * 1024).build();
        connectionHandler.start();


        Toolkit.getDefaultToolkit().beep();
    }

    public long getStartedTime()
    {
        return ManagementFactory.getRuntimeMXBean().getStartTime();
    }

    public String getUptime()
    {
        final long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        final long hours = uptime / 3600;
        final long mins = (uptime - (hours * 3600)) / 60;
        final long secs = ((uptime - (hours * 3600)) - (mins * 60));
        if (hours > 0)
        {
            return hours + "hrs " + mins + "mins " + secs + "secs";
        }
        return mins + "mins " + secs + "secs";
    }

    public static void main(String[] args) throws Exception {
        configureLogger();
        logVersionInfo();
        configureDatabase();
        Config.load();
        INSTANCE = new GameServer();
        ThreadPoolManager.getInstance().execute(AuthServerCommunication.getInstance());
    }

    private static void configureLogger() {
        var logConfigurationFile = System.getProperty(LOG4J_CONFIGURATION_FILE);
        if (isNullOrEmpty(logConfigurationFile)) {
            System.setProperty(LOG4J_CONFIGURATION_FILE, "log4j.xml");
        }
        LOGGER = LoggerFactory.getLogger(org.l2j.gameserver.GameServer.class);
    }

    private static void configureDatabase() throws Exception {
        System.setProperty("hikaricp.configurationFile", "config/database.properties");
        if(!DatabaseAccess.initialize()) {
            throw new Exception("Database Access could not be initialized");
        }
    }

    private static void logVersionInfo() {
        try {
            var versionProperties = new Properties();
            var versionFile = ClassLoader.getSystemResourceAsStream("version.properties");
            if(nonNull(versionFile)) {
                versionProperties.load(versionFile);
                version = versionProperties.getProperty("version");
                LOGGER.info("======================================================================");
                LOGGER.info("Build Version: ........... {}", version);
                LOGGER.info("Build Revision: .......... {}", versionProperties.getProperty("revision"));
                LOGGER.info("Update: .................. {}", UPDATE_NAME);
                LOGGER.info("Build date: .............. {}", versionProperties.getProperty("buildDate"));
                LOGGER.info("Compiler JDK version: .... {}", versionProperties.getProperty("compilerVersion"));
                LOGGER.info("======================================================================");
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    private static void printSection(String s)
    {
        s = "=[ " + s + " ]";
        while (s.length() < 61)
        {
            s = "-" + s;
        }
        LOGGER.info(s);
    }

    public static GameServer getInstance()
    {
        return INSTANCE;
    }
}
