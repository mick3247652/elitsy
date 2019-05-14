package com.ru.ruchurch.xmpp.jingle;

public interface OnPrimaryCandidateFound {
	void onPrimaryCandidateFound(boolean success, JingleCandidate canditate);
}
