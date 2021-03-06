package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.actor.Npc;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerPacketId;

/**
 * @author KenM
 */
public class ExSpawnEmitter extends ServerPacket {
    private final int _playerObjectId;
    private final int _npcObjectId;

    public ExSpawnEmitter(int playerObjectId, int npcObjectId) {
        _playerObjectId = playerObjectId;
        _npcObjectId = npcObjectId;
    }

    public ExSpawnEmitter(Player player, Npc npc) {
        this(player.getObjectId(), npc.getObjectId());
    }

    @Override
    public void writeImpl(GameClient client) {
        writeId(ServerPacketId.EX_SPAWN_EMITTER);

        writeInt(_npcObjectId);
        writeInt(_playerObjectId);
        writeInt(0x00); // ?
    }

}
