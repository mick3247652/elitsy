package com.ru.astron.xmpp.jingle;

import com.ru.astron.entities.Account;
import com.ru.astron.xmpp.PacketReceived;
import com.ru.astron.xmpp.jingle.stanzas.JinglePacket;

public interface OnJinglePacketReceived extends PacketReceived {
	void onJinglePacketReceived(Account account, JinglePacket packet);
}
