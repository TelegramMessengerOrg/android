package org.telegram.messenger;

import android.util.LongSparseArray;
import org.telegram.tgnet.TLRPC.messages_Dialogs;

final /* synthetic */ class MessagesController$$Lambda$210 implements Runnable {
    private final MessagesController arg$1;
    private final messages_Dialogs arg$2;
    private final LongSparseArray arg$3;
    private final LongSparseArray arg$4;

    MessagesController$$Lambda$210(MessagesController messagesController, messages_Dialogs org_telegram_tgnet_TLRPC_messages_Dialogs, LongSparseArray longSparseArray, LongSparseArray longSparseArray2) {
        this.arg$1 = messagesController;
        this.arg$2 = org_telegram_tgnet_TLRPC_messages_Dialogs;
        this.arg$3 = longSparseArray;
        this.arg$4 = longSparseArray2;
    }

    public void run() {
        this.arg$1.lambda$null$100$MessagesController(this.arg$2, this.arg$3, this.arg$4);
    }
}
