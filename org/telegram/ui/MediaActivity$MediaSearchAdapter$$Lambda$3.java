package org.telegram.ui;

import java.util.ArrayList;
import org.telegram.ui.MediaActivity.MediaSearchAdapter;

final /* synthetic */ class MediaActivity$MediaSearchAdapter$$Lambda$3 implements Runnable {
    private final MediaSearchAdapter arg$1;
    private final String arg$2;
    private final ArrayList arg$3;

    MediaActivity$MediaSearchAdapter$$Lambda$3(MediaSearchAdapter mediaSearchAdapter, String str, ArrayList arrayList) {
        this.arg$1 = mediaSearchAdapter;
        this.arg$2 = str;
        this.arg$3 = arrayList;
    }

    public void run() {
        this.arg$1.lambda$null$2$MediaActivity$MediaSearchAdapter(this.arg$2, this.arg$3);
    }
}
