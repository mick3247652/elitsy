package com.ru.astron.xmpp;

import com.ru.astron.entities.Account;

public interface OnStatusChanged {
	public void onStatusChanged(Account account);
}
