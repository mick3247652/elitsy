package com.ru.astron.xmpp;

import com.ru.astron.entities.Account;

public interface OnMessageAcknowledged {
	boolean onMessageAcknowledged(Account account, String id);
}
