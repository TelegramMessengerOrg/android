package org.telegram.messenger;

import android.util.SparseArray;
import org.telegram.tgnet.TLRPC.updates_Difference;

final /* synthetic */ class MessagesController$$Lambda$169 implements Runnable {
    private final MessagesController arg$1;
    private final updates_Difference arg$2;
    private final SparseArray arg$3;
    private final SparseArray arg$4;

    MessagesController$$Lambda$169(MessagesController messagesController, updates_Difference org_telegram_tgnet_TLRPC_updates_Difference, SparseArray sparseArray, SparseArray sparseArray2) {
        this.arg$1 = messagesController;
        this.arg$2 = org_telegram_tgnet_TLRPC_updates_Difference;
        this.arg$3 = sparseArray;
        this.arg$4 = sparseArray2;
    }

    public void run() {
        this.arg$1.lambda$null$194$MessagesController(this.arg$2, this.arg$3, this.arg$4);
    }
}
