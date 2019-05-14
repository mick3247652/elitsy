package com.ru.ruchurch.xmpp.jingle;

import com.ru.ruchurch.entities.DownloadableFile;

public interface OnFileTransmissionStatusChanged {
	void onFileTransmitted(DownloadableFile file);

	void onFileTransferAborted();
}
