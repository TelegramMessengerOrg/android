package com.google.firebase.iid;

public final class zzaj extends Exception {
    private final int errorCode;

    public zzaj(int i, String str) {
        super(str);
        this.errorCode = i;
    }

    public final int getErrorCode() {
        return this.errorCode;
    }
}
