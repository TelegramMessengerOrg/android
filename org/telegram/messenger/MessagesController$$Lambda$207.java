package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.messages_Dialogs;

final /* synthetic */ class MessagesController$$Lambda$207 implements Runnable {
    private final MessagesController arg$1;
    private final messages_Dialogs arg$2;
    private final int arg$3;

    MessagesController$$Lambda$207(MessagesController messagesController, messages_Dialogs org_telegram_tgnet_TLRPC_messages_Dialogs, int i) {
        this.arg$1 = messagesController;
        this.arg$2 = org_telegram_tgnet_TLRPC_messages_Dialogs;
        this.arg$3 = i;
    }

    public void run() {
        this.arg$1.lambda$null$103$MessagesController(this.arg$2, this.arg$3);
    }
}
