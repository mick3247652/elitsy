package com.ru.astron.xmpp;

import com.ru.astron.entities.Account;
import com.ru.astron.xmpp.stanzas.PresencePacket;

public interface OnPresencePacketReceived extends PacketReceived {
	public void onPresencePacketReceived(Account account, PresencePacket packet);
}
