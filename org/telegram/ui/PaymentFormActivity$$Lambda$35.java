package org.telegram.ui;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

final /* synthetic */ class PaymentFormActivity$$Lambda$35 implements OnClickListener {
    private final PaymentFormActivity arg$1;
    private final String arg$2;

    PaymentFormActivity$$Lambda$35(PaymentFormActivity paymentFormActivity, String str) {
        this.arg$1 = paymentFormActivity;
        this.arg$2 = str;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.arg$1.lambda$null$25$PaymentFormActivity(this.arg$2, dialogInterface, i);
    }
}
