package com.google.android.gms.internal.measurement;

import android.os.RemoteException;

final class zzio implements Runnable {
    private final /* synthetic */ zzif zzapf;
    private final /* synthetic */ zzij zzapn;

    zzio(zzij com_google_android_gms_internal_measurement_zzij, zzif com_google_android_gms_internal_measurement_zzif) {
        this.zzapn = com_google_android_gms_internal_measurement_zzij;
        this.zzapf = com_google_android_gms_internal_measurement_zzif;
    }

    public final void run() {
        zzez zzd = this.zzapn.zzaph;
        if (zzd == null) {
            this.zzapn.zzgf().zzis().log("Failed to send current screen to service");
            return;
        }
        try {
            if (this.zzapf == null) {
                zzd.zza(0, null, null, this.zzapn.getContext().getPackageName());
            } else {
                zzd.zza(this.zzapf.zzaot, this.zzapf.zzul, this.zzapf.zzaos, this.zzapn.getContext().getPackageName());
            }
            this.zzapn.zzcu();
        } catch (RemoteException e) {
            this.zzapn.zzgf().zzis().zzg("Failed to send current screen to the service", e);
        }
    }
}
