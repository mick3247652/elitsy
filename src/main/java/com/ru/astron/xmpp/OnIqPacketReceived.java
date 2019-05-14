package com.ru.astron.xmpp;

import com.ru.astron.entities.Account;
import com.ru.astron.xmpp.stanzas.IqPacket;

public interface OnIqPacketReceived extends PacketReceived {
	void onIqPacketReceived(Account account, IqPacket packet);
}
