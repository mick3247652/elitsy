package com.ru.ruchurch.xmpp;

import com.ru.ruchurch.entities.Account;
import com.ru.ruchurch.xmpp.stanzas.PresencePacket;

public interface OnPresencePacketReceived extends PacketReceived {
	public void onPresencePacketReceived(Account account, PresencePacket packet);
}
