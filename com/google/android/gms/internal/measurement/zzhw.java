package com.google.android.gms.internal.measurement;

import java.util.concurrent.atomic.AtomicReference;

final class zzhw implements Runnable {
    private final /* synthetic */ AtomicReference zzaof;
    private final /* synthetic */ zzhl zzaog;

    zzhw(zzhl com_google_android_gms_internal_measurement_zzhl, AtomicReference atomicReference) {
        this.zzaog = com_google_android_gms_internal_measurement_zzhl;
        this.zzaof = atomicReference;
    }

    public final void run() {
        synchronized (this.zzaof) {
            try {
                this.zzaof.set(this.zzaog.zzgh().zzhq());
                this.zzaof.notify();
            } catch (Throwable th) {
                this.zzaof.notify();
            }
        }
    }
}
