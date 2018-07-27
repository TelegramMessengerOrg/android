package com.google.android.gms.internal.measurement;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.util.Clock;
import com.google.android.gms.common.util.ProcessUtils;
import com.google.android.gms.common.wrappers.Wrappers;
import com.google.android.gms.internal.measurement.zzey.zza;
import java.lang.reflect.InvocationTargetException;

public final class zzeg extends zzhh {
    private zzei zzaeu = zzeh.zzaev;
    private Boolean zzxz;

    zzeg(zzgm com_google_android_gms_internal_measurement_zzgm) {
        super(com_google_android_gms_internal_measurement_zzgm);
    }

    static String zzhi() {
        return (String) zzey.zzagp.get();
    }

    public final /* bridge */ /* synthetic */ Context getContext() {
        return super.getContext();
    }

    public final long zza(String str, zza<Long> com_google_android_gms_internal_measurement_zzey_zza_java_lang_Long) {
        if (str == null) {
            return ((Long) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Long.get()).longValue();
        }
        Object zze = this.zzaeu.zze(str, com_google_android_gms_internal_measurement_zzey_zza_java_lang_Long.getKey());
        if (TextUtils.isEmpty(zze)) {
            return ((Long) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Long.get()).longValue();
        }
        try {
            return ((Long) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Long.get(Long.valueOf(Long.parseLong(zze)))).longValue();
        } catch (NumberFormatException e) {
            return ((Long) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Long.get()).longValue();
        }
    }

    public final /* bridge */ /* synthetic */ void zzab() {
        super.zzab();
    }

    final Boolean zzar(String str) {
        Boolean bool = null;
        Preconditions.checkNotEmpty(str);
        try {
            if (getContext().getPackageManager() == null) {
                zzgf().zzis().log("Failed to load metadata: PackageManager is null");
            } else {
                ApplicationInfo applicationInfo = Wrappers.packageManager(getContext()).getApplicationInfo(getContext().getPackageName(), 128);
                if (applicationInfo == null) {
                    zzgf().zzis().log("Failed to load metadata: ApplicationInfo is null");
                } else if (applicationInfo.metaData == null) {
                    zzgf().zzis().log("Failed to load metadata: Metadata bundle is null");
                } else if (applicationInfo.metaData.containsKey(str)) {
                    bool = Boolean.valueOf(applicationInfo.metaData.getBoolean(str));
                }
            }
        } catch (NameNotFoundException e) {
            zzgf().zzis().zzg("Failed to load metadata: Package name not found", e);
        }
        return bool;
    }

    final boolean zzav(String str) {
        return zzd(str, zzey.zzaid);
    }

    final boolean zzax(String str) {
        return zzd(str, zzey.zzaif);
    }

    final boolean zzay(String str) {
        return zzd(str, zzey.zzaig);
    }

    final boolean zzaz(String str) {
        return zzd(str, zzey.zzaij);
    }

    public final int zzb(String str, zza<Integer> com_google_android_gms_internal_measurement_zzey_zza_java_lang_Integer) {
        if (str == null) {
            return ((Integer) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Integer.get()).intValue();
        }
        Object zze = this.zzaeu.zze(str, com_google_android_gms_internal_measurement_zzey_zza_java_lang_Integer.getKey());
        if (TextUtils.isEmpty(zze)) {
            return ((Integer) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Integer.get()).intValue();
        }
        try {
            return ((Integer) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Integer.get(Integer.valueOf(Integer.parseInt(zze)))).intValue();
        } catch (NumberFormatException e) {
            return ((Integer) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Integer.get()).intValue();
        }
    }

    public final /* bridge */ /* synthetic */ Clock zzbt() {
        return super.zzbt();
    }

    public final double zzc(String str, zza<Double> com_google_android_gms_internal_measurement_zzey_zza_java_lang_Double) {
        if (str == null) {
            return ((Double) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Double.get()).doubleValue();
        }
        Object zze = this.zzaeu.zze(str, com_google_android_gms_internal_measurement_zzey_zza_java_lang_Double.getKey());
        if (TextUtils.isEmpty(zze)) {
            return ((Double) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Double.get()).doubleValue();
        }
        try {
            return ((Double) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Double.get(Double.valueOf(Double.parseDouble(zze)))).doubleValue();
        } catch (NumberFormatException e) {
            return ((Double) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Double.get()).doubleValue();
        }
    }

    public final boolean zzd(String str, zza<Boolean> com_google_android_gms_internal_measurement_zzey_zza_java_lang_Boolean) {
        if (str == null) {
            return ((Boolean) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Boolean.get()).booleanValue();
        }
        Object zze = this.zzaeu.zze(str, com_google_android_gms_internal_measurement_zzey_zza_java_lang_Boolean.getKey());
        return TextUtils.isEmpty(zze) ? ((Boolean) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Boolean.get()).booleanValue() : ((Boolean) com_google_android_gms_internal_measurement_zzey_zza_java_lang_Boolean.get(Boolean.valueOf(Boolean.parseBoolean(zze)))).booleanValue();
    }

    public final boolean zzds() {
        if (this.zzxz == null) {
            synchronized (this) {
                if (this.zzxz == null) {
                    ApplicationInfo applicationInfo = getContext().getApplicationInfo();
                    String myProcessName = ProcessUtils.getMyProcessName();
                    if (applicationInfo != null) {
                        String str = applicationInfo.processName;
                        boolean z = str != null && str.equals(myProcessName);
                        this.zzxz = Boolean.valueOf(z);
                    }
                    if (this.zzxz == null) {
                        this.zzxz = Boolean.TRUE;
                        zzgf().zzis().log("My process not in the list of running processes");
                    }
                }
            }
        }
        return this.zzxz.booleanValue();
    }

    public final /* bridge */ /* synthetic */ void zzfr() {
        super.zzfr();
    }

    public final /* bridge */ /* synthetic */ void zzfs() {
        super.zzfs();
    }

    public final /* bridge */ /* synthetic */ zzdu zzfu() {
        return super.zzfu();
    }

    public final /* bridge */ /* synthetic */ zzhl zzfv() {
        return super.zzfv();
    }

    public final /* bridge */ /* synthetic */ zzfc zzfw() {
        return super.zzfw();
    }

    public final /* bridge */ /* synthetic */ zzeq zzfx() {
        return super.zzfx();
    }

    public final /* bridge */ /* synthetic */ zzij zzfy() {
        return super.zzfy();
    }

    public final /* bridge */ /* synthetic */ zzig zzfz() {
        return super.zzfz();
    }

    public final /* bridge */ /* synthetic */ zzfd zzga() {
        return super.zzga();
    }

    public final /* bridge */ /* synthetic */ zzff zzgb() {
        return super.zzgb();
    }

    public final /* bridge */ /* synthetic */ zzkc zzgc() {
        return super.zzgc();
    }

    public final /* bridge */ /* synthetic */ zzji zzgd() {
        return super.zzgd();
    }

    public final /* bridge */ /* synthetic */ zzgh zzge() {
        return super.zzge();
    }

    public final /* bridge */ /* synthetic */ zzfh zzgf() {
        return super.zzgf();
    }

    public final /* bridge */ /* synthetic */ zzfs zzgg() {
        return super.zzgg();
    }

    public final /* bridge */ /* synthetic */ zzeg zzgh() {
        return super.zzgh();
    }

    public final /* bridge */ /* synthetic */ zzec zzgi() {
        return super.zzgi();
    }

    public final boolean zzhj() {
        zzgi();
        Boolean zzar = zzar("firebase_analytics_collection_deactivated");
        return zzar != null && zzar.booleanValue();
    }

    public final Boolean zzhk() {
        zzgi();
        return zzar("firebase_analytics_collection_enabled");
    }

    public final String zzhn() {
        try {
            return (String) Class.forName("android.os.SystemProperties").getMethod("get", new Class[]{String.class, String.class}).invoke(null, new Object[]{"debug.firebase.analytics.app", TtmlNode.ANONYMOUS_REGION_ID});
        } catch (ClassNotFoundException e) {
            zzgf().zzis().zzg("Could not find SystemProperties class", e);
        } catch (NoSuchMethodException e2) {
            zzgf().zzis().zzg("Could not find SystemProperties.get() method", e2);
        } catch (IllegalAccessException e3) {
            zzgf().zzis().zzg("Could not access SystemProperties.get()", e3);
        } catch (InvocationTargetException e4) {
            zzgf().zzis().zzg("SystemProperties.get() threw an exception", e4);
        }
        return TtmlNode.ANONYMOUS_REGION_ID;
    }

    final boolean zzhp() {
        return zzd(zzfw().zzah(), zzey.zzahw);
    }

    final String zzhq() {
        String zzah = zzfw().zzah();
        zza com_google_android_gms_internal_measurement_zzey_zza = zzey.zzahx;
        return zzah == null ? (String) com_google_android_gms_internal_measurement_zzey_zza.get() : (String) com_google_android_gms_internal_measurement_zzey_zza.get(this.zzaeu.zze(zzah, com_google_android_gms_internal_measurement_zzey_zza.getKey()));
    }
}
