package org.telegram.messenger;

import android.util.SparseArray;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.updates_ChannelDifference;

final /* synthetic */ class MessagesController$$Lambda$177 implements Runnable {
    private final MessagesController arg$1;
    private final updates_ChannelDifference arg$2;
    private final int arg$3;
    private final Chat arg$4;
    private final SparseArray arg$5;
    private final int arg$6;
    private final long arg$7;

    MessagesController$$Lambda$177(MessagesController messagesController, updates_ChannelDifference org_telegram_tgnet_TLRPC_updates_ChannelDifference, int i, Chat chat, SparseArray sparseArray, int i2, long j) {
        this.arg$1 = messagesController;
        this.arg$2 = org_telegram_tgnet_TLRPC_updates_ChannelDifference;
        this.arg$3 = i;
        this.arg$4 = chat;
        this.arg$5 = sparseArray;
        this.arg$6 = i2;
        this.arg$7 = j;
    }

    public void run() {
        this.arg$1.lambda$null$184$MessagesController(this.arg$2, this.arg$3, this.arg$4, this.arg$5, this.arg$6, this.arg$7);
    }
}
