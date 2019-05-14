package com.ru.ruchurch.xmpp.stanzas.csi;

import com.ru.ruchurch.xmpp.stanzas.AbstractStanza;

public class ActivePacket extends AbstractStanza {
	public ActivePacket() {
		super("active");
		setAttribute("xmlns", "urn:xmpp:csi:0");
	}
}
