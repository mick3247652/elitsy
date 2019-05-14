package com.ru.ruchurch.xmpp.stanzas;

public class PresencePacket extends AbstractAcknowledgeableStanza {

	public PresencePacket() {
		super("presence");
	}
}
