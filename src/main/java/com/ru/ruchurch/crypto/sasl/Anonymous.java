package com.ru.ruchurch.crypto.sasl;

import java.security.SecureRandom;

import com.ru.ruchurch.entities.Account;
import com.ru.ruchurch.xml.TagWriter;

public class Anonymous extends SaslMechanism {

	public Anonymous(TagWriter tagWriter, Account account, SecureRandom rng) {
		super(tagWriter, account, rng);
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String getMechanism() {
		return "ANONYMOUS";
	}

	@Override
	public String getClientFirstMessage() {
		return "";
	}
}
