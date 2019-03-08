package org.l2j.gameserver.mobius.gameserver.network.clientpackets.friend;

import org.l2j.gameserver.mobius.gameserver.Config;
import org.l2j.gameserver.mobius.gameserver.model.L2World;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.network.SystemMessageId;
import org.l2j.gameserver.mobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.L2FriendSay;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Recieve Private (Friend) Message - 0xCC Format: c SS S: Message S: Receiving Player
 * @author Tempy
 */
public final class RequestSendFriendMsg extends IClientIncomingPacket
{
    private static Logger LOGGER_CHAT = Logger.getLogger("chat");

    private String _message;
    private String _reciever;

    @Override
    public void readImpl(ByteBuffer packet)
    {
        _message = readString(packet);
        _reciever = readString(packet);
    }

    @Override
    public void runImpl()
    {
        final L2PcInstance activeChar = client.getActiveChar();
        if (activeChar == null)
        {
            return;
        }

        if ((_message == null) || _message.isEmpty() || (_message.length() > 300))
        {
            return;
        }

        final L2PcInstance targetPlayer = L2World.getInstance().getPlayer(_reciever);
        if ((targetPlayer == null) || !targetPlayer.getFriendList().contains(activeChar.getObjectId()))
        {
            activeChar.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
            return;
        }

        if (Config.LOG_CHAT)
        {
            LOGGER_CHAT.info("PRIV_MSG [" + activeChar + " to " + targetPlayer + "] " + _message);
        }

        targetPlayer.sendPacket(new L2FriendSay(activeChar.getName(), _reciever, _message));
    }
}
