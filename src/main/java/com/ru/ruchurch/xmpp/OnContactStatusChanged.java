package com.ru.ruchurch.xmpp;

import com.ru.ruchurch.entities.Contact;

public interface OnContactStatusChanged {
	public void onContactStatusChanged(final Contact contact, final boolean online);
}
