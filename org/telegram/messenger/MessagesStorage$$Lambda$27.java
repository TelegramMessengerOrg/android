package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.photos_Photos;

final /* synthetic */ class MessagesStorage$$Lambda$27 implements Runnable {
    private final MessagesStorage arg$1;
    private final int arg$2;
    private final photos_Photos arg$3;

    MessagesStorage$$Lambda$27(MessagesStorage messagesStorage, int i, photos_Photos org_telegram_tgnet_TLRPC_photos_Photos) {
        this.arg$1 = messagesStorage;
        this.arg$2 = i;
        this.arg$3 = org_telegram_tgnet_TLRPC_photos_Photos;
    }

    public void run() {
        this.arg$1.lambda$putDialogPhotos$43$MessagesStorage(this.arg$2, this.arg$3);
    }
}
