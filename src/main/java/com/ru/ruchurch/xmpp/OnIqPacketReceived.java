package com.ru.ruchurch.xmpp;

import com.ru.ruchurch.entities.Account;
import com.ru.ruchurch.xmpp.stanzas.IqPacket;

public interface OnIqPacketReceived extends PacketReceived {
	void onIqPacketReceived(Account account, IqPacket packet);
}
