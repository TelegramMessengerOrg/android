package com.google.android.gms.internal.measurement;

import java.util.concurrent.atomic.AtomicReference;

final class zzhv implements Runnable {
    private final /* synthetic */ String zzanr;
    private final /* synthetic */ String zzans;
    private final /* synthetic */ String zzant;
    private final /* synthetic */ AtomicReference zzaof;
    private final /* synthetic */ zzhl zzaog;
    private final /* synthetic */ boolean zzaoj;

    zzhv(zzhl com_google_android_gms_internal_measurement_zzhl, AtomicReference atomicReference, String str, String str2, String str3, boolean z) {
        this.zzaog = com_google_android_gms_internal_measurement_zzhl;
        this.zzaof = atomicReference;
        this.zzant = str;
        this.zzanr = str2;
        this.zzans = str3;
        this.zzaoj = z;
    }

    public final void run() {
        this.zzaog.zzacw.zzfy().zza(this.zzaof, this.zzant, this.zzanr, this.zzans, this.zzaoj);
    }
}
