package org.l2j.gameserver.network.l2.s2c;

import java.util.Set;

import org.l2j.gameserver.Config;
import org.l2j.gameserver.model.Creature;
import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.model.actor.instances.player.Cubic;
import org.l2j.gameserver.model.base.TeamType;
import org.l2j.gameserver.model.instances.DecoyInstance;
import org.l2j.gameserver.model.items.Inventory;
import org.l2j.gameserver.model.items.PcInventory;
import org.l2j.gameserver.model.matching.MatchingRoom;
import org.l2j.gameserver.model.pledge.Alliance;
import org.l2j.gameserver.model.pledge.Clan;
import org.l2j.gameserver.skills.AbnormalEffect;
import org.l2j.gameserver.utils.Location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CIPacket extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CIPacket.class);

	private int[][] _inv;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private Location _loc, _fishLoc;
	private String _name, _title;
	private int _objId, _race, _sex, base_class, pvp_flag, karma, rec_have;
	private double speed_move, speed_atack, col_radius, col_height;
	private int hair_style, hair_color, face;
	private int clan_id, clan_crest_id, large_clan_crest_id, ally_id, ally_crest_id, class_id;
	private int _sit, _run, _combat, _dead, private_store, _enchant;
	private int _hero, _fishing, mount_type;
	private int plg_class, pledge_type, clan_rep_score, cw_level, mount_id;
	private int _nameColor, _title_color, _transform, _agathion;
	private Cubic[] cubics;
	private boolean _isPartyRoomLeader, _isFlying;
	private int _curHp, _maxHp, _curMp, _maxMp, _curCp;
	private TeamType _team;
	private Set<AbnormalEffect> _abnormalEffects;
	private final Player _receiver;
	private boolean _showHeadAccessories;
	private int _armorSetEnchant;

	public CIPacket(Player cha, Player receiver)
	{
		this((Creature) cha, receiver);
	}

	public CIPacket(DecoyInstance cha, Player receiver)
	{
		this((Creature) cha, receiver);
	}

	public CIPacket(Creature cha, Player receiver)
	{
		_receiver = receiver;

		if(cha == null)
		{
			System.out.println("CIPacket: cha is null!");
			Thread.dumpStack();
			return;
		}

		if(_receiver == null)
			return;

		if(cha.isInvisible(receiver))
			return;

		if(cha.isDeleted())
			return;

		_objId = cha.getObjectId();
		if(_objId == 0)
			return;

		if(_receiver.getObjectId() == _objId)
		{
			_log.error("You cant send CIPacket about his character to active user!!!");
			return;
		}

		Player player = cha.getPlayer();
		if(player == null)
			return;

		if(player.isInBoat())
			_loc = player.getInBoatPosition();

		if(_loc == null)
			_loc = cha.getLoc();

		if(_loc == null)
			return;

		_name = player.getVisibleName(_receiver);
		_nameColor = player.getVisibleNameColor(_receiver);

		if(player.isConnected() || player.isInOfflineMode() || player.isFakePlayer())
		{
			_title = player.getVisibleTitle(_receiver);
			_title_color = player.getVisibleTitleColor(_receiver);
		}
		else
		{
			_title = "NO CARRIER";
			_title_color = 255;
		}

		if(player.isPledgeVisible(_receiver))
		{
			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			//
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			//
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		}

		if(player.isMounted())
		{
			_enchant = 0;
			mount_id = player.getMountNpcId() + 1000000;
			mount_type = player.getMountType().ordinal();
		}
		else
		{
			_enchant = player.getEnchantEffect();
			mount_id = 0;
			mount_type = 0;
		}

		_inv = new int[PcInventory.PAPERDOLL_MAX][4];
		for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
		{
			_inv[PAPERDOLL_ID][0] = player.getInventory().getPaperdollItemId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][1] = player.getInventory().getPaperdollVariation1Id(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][2] = player.getInventory().getPaperdollVariation2Id(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][3] = player.getInventory().getPaperdollVisualId(PAPERDOLL_ID);
		}

		_mAtkSpd = player.getMAtkSpd();
		_pAtkSpd = player.getPAtkSpd();
		speed_move = player.getMovementSpeedMultiplier();
		_runSpd = (int) (player.getRunSpeed() / speed_move);
		_walkSpd = (int) (player.getWalkSpeed() / speed_move);

		_flRunSpd = 0; // TODO
		_flWalkSpd = 0; // TODO

		if(player.isFlying())
		{
			_flyRunSpd = _runSpd;
			_flyWalkSpd = _walkSpd;
		}
		else
		{
			_flyRunSpd = 0;
			_flyWalkSpd = 0;
		}

		_swimRunSpd = player.getSwimRunSpeed();
		_swimWalkSpd = player.getSwimWalkSpeed();
		_race = player.getRace().ordinal();
		_sex = player.getSex().ordinal();
		base_class = player.getBaseClassId();
		pvp_flag = player.getPvpFlag();
		karma = player.getKarma();

		speed_atack = player.getAttackSpeedMultiplier();
		col_radius = player.getCollisionRadius();
		col_height = player.getCollisionHeight();
		hair_style = player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) > 0 ? _sex : (player.getBeautyHairStyle() > 0 ? player.getBeautyHairStyle() : player.getHairStyle());
		hair_color = player.getBeautyHairColor() > 0 ? player.getBeautyHairColor() : player.getHairColor();
		face = player.getBeautyFace() > 0 ? player.getBeautyFace() : player.getFace();
		if(clan_id > 0 && player.getClan() != null)
			clan_rep_score = player.getClan().getReputationScore();
		else
			clan_rep_score = 0;
		_sit = player.isSitting() ? 0 : 1; // standing = 1 sitting = 0
		_run = player.isRunning() ? 1 : 0; // running = 1 walking = 0
		_combat = player.isInCombat() ? 1 : 0;
		_dead = player.isAlikeDead() ? 1 : 0;
		private_store = player.isInObserverMode() ? Player.STORE_OBSERVING_GAMES : (player.isInBuffStore() ? Player.STORE_PRIVATE_NONE : player.getPrivateStoreType());
		cubics = player.getCubics().toArray(new Cubic[player.getCubics().size()]);
		_abnormalEffects = player.getAbnormalEffects();
		rec_have = player.isGM() ? 0 : player.getRecomHave();
		class_id = player.getClassId().getId();
		_team = player.getTeam();
		_hero = player.isHero() || player.isGM() && Config.GM_HERO_AURA ? 1 : 0; // 0x01: Hero Aura
		_fishing = player.getFishing().isInProcess() ? 1 : 0;
		_fishLoc = player.getFishing().getHookLocation();
		plg_class = player.getPledgeRank().ordinal();
		pledge_type = player.getPledgeType();
		_transform = player.getVisualTransformId();
		_agathion = player.getAgathionId();
		_isPartyRoomLeader = player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player;
		_isFlying = player.isInFlyingTransform();
		_curHp = (int) player.getCurrentHp();
		_maxHp = player.getMaxHp();
		_curMp = (int) player.getCurrentMp();
		_maxMp = player.getMaxMp();
		_curCp = (int) player.getCurrentCp();

		_showHeadAccessories = !player.hideHeadAccessories();

		_armorSetEnchant = player.getArmorSetEnchant();
	}

	@Override
	protected final void writeImpl()
	{
		writeByte(0x00);
		writeInt(_loc.x);
		writeInt(_loc.y);
		writeInt(_loc.z + Config.CLIENT_Z_SHIFT);
		writeInt(0x00);
		writeInt(_objId);
		writeString(_name);
		writeShort(_race);
		writeByte(_sex);
		writeInt(base_class);

		for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
			writeInt(_inv[PAPERDOLL_ID][0]);

		writeInt(_inv[Inventory.PAPERDOLL_RHAND][1]);
		writeInt(_inv[Inventory.PAPERDOLL_RHAND][2]);

		writeInt(_inv[Inventory.PAPERDOLL_LHAND][1]);
		writeInt(_inv[Inventory.PAPERDOLL_LHAND][2]);

		writeInt(_inv[Inventory.PAPERDOLL_LRHAND][1]);
		writeInt(_inv[Inventory.PAPERDOLL_LRHAND][2]);

		writeByte(_armorSetEnchant);	// Armor Enchant Effect

		writeInt(_inv[Inventory.PAPERDOLL_RHAND][3]);
		writeInt(_inv[Inventory.PAPERDOLL_LHAND][3]);
		writeInt(_inv[Inventory.PAPERDOLL_LRHAND][3]);
		writeInt(_inv[Inventory.PAPERDOLL_GLOVES][3]);
		writeInt(_inv[Inventory.PAPERDOLL_CHEST][3]);
		writeInt(_inv[Inventory.PAPERDOLL_LEGS][3]);
		writeInt(_inv[Inventory.PAPERDOLL_FEET][3]);
		writeInt(_inv[Inventory.PAPERDOLL_HAIR][3]);
		writeInt(_inv[Inventory.PAPERDOLL_DHAIR][3]);

		writeByte(pvp_flag);
		writeInt(karma);

		writeInt(_mAtkSpd);
		writeInt(_pAtkSpd);

		writeShort(_runSpd);
		writeShort(_walkSpd);
		writeShort(_swimRunSpd);
		writeShort(_swimWalkSpd);
		writeShort(_flRunSpd);
		writeShort(_flWalkSpd);
		writeShort(_flyRunSpd);
		writeShort(_flyWalkSpd);

		writeDouble(speed_move); // _cha.getProperMultiplier()
		writeDouble(speed_atack); // _cha.getAttackSpeedMultiplier()
		writeDouble(col_radius);
		writeDouble(col_height);
		writeInt(hair_style);
		writeInt(hair_color);
		writeInt(face);
		writeString(_title);
		writeInt(clan_id);
		writeInt(clan_crest_id);
		writeInt(ally_id);
		writeInt(ally_crest_id);

		writeByte(_sit);
		writeByte(_run);
		writeByte(_combat);
		writeByte(_dead);
		writeByte(0x00);
		writeByte(mount_type); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeByte(private_store);
		writeShort(cubics.length);
		for(Cubic cubic : cubics)
			writeShort(cubic == null ? 0 : cubic.getId());
		writeByte(_isPartyRoomLeader ? 0x01 : 0x00); // find party members
		writeByte(_isFlying ? 0x02 : 0x00);
		writeShort(rec_have);
		writeInt(mount_id);
		writeInt(class_id);
		writeInt(0x00);
		writeByte(_enchant);

		writeByte(_team.ordinal()); // team circle around feet 1 = Blue, 2 = red

		writeInt(large_clan_crest_id);
		writeByte(0x00);
		writeByte(_hero);

		writeByte(_fishing);
		writeInt(_fishLoc.x);
		writeInt(_fishLoc.y);
		writeInt(_fishLoc.z);

		writeInt(_nameColor);
		writeInt(_loc.h);
		writeByte(plg_class);
		writeShort(pledge_type);
		writeInt(_title_color);
		writeByte(0x00);
		writeInt(clan_rep_score);
		writeInt(_transform);
		writeInt(_agathion);

		writeByte(0x01);	// UNK

		writeInt(_curCp);
		writeInt(_curHp);
		writeInt(_maxHp);
		writeInt(_curMp);
		writeInt(_maxMp);

		writeByte(0x00);	// UNK

		writeInt(_abnormalEffects.size());
		for(AbnormalEffect abnormal : _abnormalEffects)
			writeShort(abnormal.getId());

		writeByte(0x00);	// UNK
		writeByte(_showHeadAccessories);
		writeByte(0x00);
	}

	public static final int[] PAPERDOLL_ORDER =
	{
		Inventory.PAPERDOLL_PENDANT,
		Inventory.PAPERDOLL_HEAD,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_FEET,
		Inventory.PAPERDOLL_BACK,
		Inventory.PAPERDOLL_LRHAND,
		Inventory.PAPERDOLL_HAIR,
		Inventory.PAPERDOLL_DHAIR
	};
}