package com.ru.ruchurch.xmpp;

import com.ru.ruchurch.entities.Account;
import com.ru.ruchurch.xmpp.stanzas.MessagePacket;

public interface OnMessagePacketReceived extends PacketReceived {
	public void onMessagePacketReceived(Account account, MessagePacket packet);
}
