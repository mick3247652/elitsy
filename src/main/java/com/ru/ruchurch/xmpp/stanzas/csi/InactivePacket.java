package com.ru.ruchurch.xmpp.stanzas.csi;

import com.ru.ruchurch.xmpp.stanzas.AbstractStanza;

public class InactivePacket extends AbstractStanza {
	public InactivePacket() {
		super("inactive");
		setAttribute("xmlns", "urn:xmpp:csi:0");
	}
}
