package org.telegram.messenger;

import android.content.Context;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.AlertDialog;

final /* synthetic */ class SecretChatHelper$$Lambda$16 implements RequestDelegate {
    private final SecretChatHelper arg$1;
    private final Context arg$2;
    private final AlertDialog arg$3;
    private final byte[] arg$4;
    private final User arg$5;

    SecretChatHelper$$Lambda$16(SecretChatHelper secretChatHelper, Context context, AlertDialog alertDialog, byte[] bArr, User user) {
        this.arg$1 = secretChatHelper;
        this.arg$2 = context;
        this.arg$3 = alertDialog;
        this.arg$4 = bArr;
        this.arg$5 = user;
    }

    public void run(TLObject tLObject, TL_error tL_error) {
        this.arg$1.lambda$null$27$SecretChatHelper(this.arg$2, this.arg$3, this.arg$4, this.arg$5, tLObject, tL_error);
    }
}
