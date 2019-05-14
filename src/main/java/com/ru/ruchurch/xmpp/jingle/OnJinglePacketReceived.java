package com.ru.ruchurch.xmpp.jingle;

import com.ru.ruchurch.entities.Account;
import com.ru.ruchurch.xmpp.PacketReceived;
import com.ru.ruchurch.xmpp.jingle.stanzas.JinglePacket;

public interface OnJinglePacketReceived extends PacketReceived {
	void onJinglePacketReceived(Account account, JinglePacket packet);
}
