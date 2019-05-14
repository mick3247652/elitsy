package com.ru.ruchurch.xmpp;

import com.ru.ruchurch.crypto.axolotl.AxolotlService;

public interface OnKeyStatusUpdated {
	public void onKeyStatusUpdated(AxolotlService.FetchStatus report);
}
