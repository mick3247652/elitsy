package com.ru.ruchurch.xmpp;

import com.ru.ruchurch.entities.Account;

public interface OnMessageAcknowledged {
	boolean onMessageAcknowledged(Account account, String id);
}
