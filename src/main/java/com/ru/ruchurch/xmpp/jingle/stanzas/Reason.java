package com.ru.ruchurch.xmpp.jingle.stanzas;

import com.ru.ruchurch.xml.Element;

public class Reason extends Element {
	private Reason(String name) {
		super(name);
	}

	public Reason() {
		super("reason");
	}
}
