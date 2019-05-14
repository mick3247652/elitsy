package com.ru.ruchurch.crypto.axolotl;

public interface OnMessageCreatedCallback {
	void run(XmppAxolotlMessage message);
}
