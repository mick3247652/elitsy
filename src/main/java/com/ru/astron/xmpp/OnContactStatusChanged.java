package com.ru.astron.xmpp;

import com.ru.astron.entities.Contact;

public interface OnContactStatusChanged {
	public void onContactStatusChanged(final Contact contact, final boolean online);
}
