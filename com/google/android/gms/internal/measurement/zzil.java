package com.google.android.gms.internal.measurement;

import android.os.RemoteException;

final class zzil implements Runnable {
    private final /* synthetic */ zzdz zzano;
    private final /* synthetic */ zzij zzapn;

    zzil(zzij com_google_android_gms_internal_measurement_zzij, zzdz com_google_android_gms_internal_measurement_zzdz) {
        this.zzapn = com_google_android_gms_internal_measurement_zzij;
        this.zzano = com_google_android_gms_internal_measurement_zzdz;
    }

    public final void run() {
        zzez zzd = this.zzapn.zzaph;
        if (zzd == null) {
            this.zzapn.zzgf().zzis().log("Failed to reset data on the service; null service");
            return;
        }
        try {
            zzd.zzd(this.zzano);
        } catch (RemoteException e) {
            this.zzapn.zzgf().zzis().zzg("Failed to reset data on the service", e);
        }
        this.zzapn.zzcu();
    }
}
