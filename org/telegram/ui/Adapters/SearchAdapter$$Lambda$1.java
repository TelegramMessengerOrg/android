package org.telegram.ui.Adapters;

import java.util.ArrayList;

final /* synthetic */ class SearchAdapter$$Lambda$1 implements Runnable {
    private final SearchAdapter arg$1;
    private final ArrayList arg$2;
    private final ArrayList arg$3;

    SearchAdapter$$Lambda$1(SearchAdapter searchAdapter, ArrayList arrayList, ArrayList arrayList2) {
        this.arg$1 = searchAdapter;
        this.arg$2 = arrayList;
        this.arg$3 = arrayList2;
    }

    public void run() {
        this.arg$1.lambda$updateSearchResults$2$SearchAdapter(this.arg$2, this.arg$3);
    }
}
