package com.google.android.gms.internal.measurement;

import java.util.concurrent.atomic.AtomicReference;

final class zzho implements Runnable {
    private final /* synthetic */ AtomicReference zzaof;
    private final /* synthetic */ zzhl zzaog;
    private final /* synthetic */ boolean zzaoj;

    zzho(zzhl com_google_android_gms_internal_measurement_zzhl, AtomicReference atomicReference, boolean z) {
        this.zzaog = com_google_android_gms_internal_measurement_zzhl;
        this.zzaof = atomicReference;
        this.zzaoj = z;
    }

    public final void run() {
        this.zzaog.zzfy().zza(this.zzaof, this.zzaoj);
    }
}
