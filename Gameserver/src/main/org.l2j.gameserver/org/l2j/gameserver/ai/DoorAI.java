package org.l2j.gameserver.ai;

import org.l2j.commons.util.Rnd;
import org.l2j.gameserver.model.Creature;
import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.model.Skill;
import org.l2j.gameserver.model.entity.events.impl.SiegeEvent;
import org.l2j.gameserver.model.instances.DoorInstance;
import org.l2j.gameserver.model.instances.NpcInstance;

public class DoorAI extends CharacterAI
{
	public DoorAI(DoorInstance actor)
	{
		super(actor);
	}

	public void onEvtTwiceClick(Player player)
	{
		//
	}

	public void onEvtOpen(Player player)
	{
		//
	}

	public void onEvtClose(Player player)
	{
		//
	}

	@Override
	public DoorInstance getActor()
	{
		return (DoorInstance) super.getActor();
	}

	//TODO [VISTALL] унести в SiegeDoor
	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		Creature actor;
		if(attacker == null || (actor = getActor()) == null)
			return;

		Player player = attacker.getPlayer();
		if(player == null)
			return;

		SiegeEvent<?, ?> siegeEvent1 = player.getEvent(SiegeEvent.class);
		SiegeEvent<?, ?> siegeEvent2 = actor.getEvent(SiegeEvent.class);

		if(siegeEvent1 == null || siegeEvent1 == siegeEvent2 && siegeEvent1.getSiegeClan(SiegeEvent.ATTACKERS, player.getClan()) != null)
			for(NpcInstance npc : actor.getAroundNpc(900, 200))
			{
				if(!npc.isSiegeGuard())
					continue;

				if(Rnd.chance(20))
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 10000);
				else
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2000);
			}
	}
}