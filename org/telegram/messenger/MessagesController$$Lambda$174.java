package org.telegram.messenger;

import android.util.SparseArray;
import java.util.ArrayList;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.updates_ChannelDifference;

final /* synthetic */ class MessagesController$$Lambda$174 implements Runnable {
    private final MessagesController arg$1;
    private final ArrayList arg$2;
    private final int arg$3;
    private final updates_ChannelDifference arg$4;
    private final Chat arg$5;
    private final SparseArray arg$6;
    private final int arg$7;
    private final long arg$8;

    MessagesController$$Lambda$174(MessagesController messagesController, ArrayList arrayList, int i, updates_ChannelDifference org_telegram_tgnet_TLRPC_updates_ChannelDifference, Chat chat, SparseArray sparseArray, int i2, long j) {
        this.arg$1 = messagesController;
        this.arg$2 = arrayList;
        this.arg$3 = i;
        this.arg$4 = org_telegram_tgnet_TLRPC_updates_ChannelDifference;
        this.arg$5 = chat;
        this.arg$6 = sparseArray;
        this.arg$7 = i2;
        this.arg$8 = j;
    }

    public void run() {
        this.arg$1.lambda$null$185$MessagesController(this.arg$2, this.arg$3, this.arg$4, this.arg$5, this.arg$6, this.arg$7, this.arg$8);
    }
}
