package org.telegram.messenger;

import java.util.ArrayList;
import org.telegram.tgnet.TLRPC.updates_Difference;

final /* synthetic */ class MessagesController$$Lambda$171 implements Runnable {
    private final MessagesController arg$1;
    private final ArrayList arg$2;
    private final updates_Difference arg$3;

    MessagesController$$Lambda$171(MessagesController messagesController, ArrayList arrayList, updates_Difference org_telegram_tgnet_TLRPC_updates_Difference) {
        this.arg$1 = messagesController;
        this.arg$2 = arrayList;
        this.arg$3 = org_telegram_tgnet_TLRPC_updates_Difference;
    }

    public void run() {
        this.arg$1.lambda$null$193$MessagesController(this.arg$2, this.arg$3);
    }
}
