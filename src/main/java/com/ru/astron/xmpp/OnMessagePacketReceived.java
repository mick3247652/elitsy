package com.ru.astron.xmpp;

import com.ru.astron.entities.Account;
import com.ru.astron.xmpp.stanzas.MessagePacket;

public interface OnMessagePacketReceived extends PacketReceived {
	public void onMessagePacketReceived(Account account, MessagePacket packet);
}
