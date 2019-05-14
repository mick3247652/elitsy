package com.ru.ruchurch.xmpp.stanzas.streammgmt;

import com.ru.ruchurch.xmpp.stanzas.AbstractStanza;

public class RequestPacket extends AbstractStanza {

	public RequestPacket(int smVersion) {
		super("r");
		this.setAttribute("xmlns", "urn:xmpp:sm:" + smVersion);
	}

}
