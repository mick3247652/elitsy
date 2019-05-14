package com.ru.astron.xmpp.jingle.stanzas;

import com.ru.astron.xml.Element;

public class Reason extends Element {
	private Reason(String name) {
		super(name);
	}

	public Reason() {
		super("reason");
	}
}
