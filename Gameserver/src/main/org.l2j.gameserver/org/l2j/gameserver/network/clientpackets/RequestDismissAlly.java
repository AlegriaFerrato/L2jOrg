package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.network.SystemMessageId;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestDismissAlly extends ClientPacket {
    @Override
    public void readImpl() {
    }

    @Override
    public void runImpl() {
        final Player activeChar = client.getPlayer();
        if (activeChar == null) {
            return;
        }
        if (!activeChar.isClanLeader()) {
            client.sendPacket(SystemMessageId.THIS_FEATURE_IS_ONLY_AVAILABLE_TO_ALLIANCE_LEADERS);
            return;
        }
        activeChar.getClan().dissolveAlly(activeChar);
    }
}
