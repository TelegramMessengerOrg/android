package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.messages_Messages;

final /* synthetic */ class MessagesStorage$$Lambda$89 implements Runnable {
    private final MessagesStorage arg$1;
    private final messages_Messages arg$2;
    private final int arg$3;
    private final long arg$4;
    private final int arg$5;
    private final boolean arg$6;

    MessagesStorage$$Lambda$89(MessagesStorage messagesStorage, messages_Messages org_telegram_tgnet_TLRPC_messages_Messages, int i, long j, int i2, boolean z) {
        this.arg$1 = messagesStorage;
        this.arg$2 = org_telegram_tgnet_TLRPC_messages_Messages;
        this.arg$3 = i;
        this.arg$4 = j;
        this.arg$5 = i2;
        this.arg$6 = z;
    }

    public void run() {
        this.arg$1.lambda$putMessages$117$MessagesStorage(this.arg$2, this.arg$3, this.arg$4, this.arg$5, this.arg$6);
    }
}
