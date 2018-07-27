package com.google.android.gms.internal.measurement;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.internal.BaseGmsClient.BaseConnectionCallbacks;
import com.google.android.gms.common.internal.BaseGmsClient.BaseOnConnectionFailedListener;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.stats.ConnectionTracker;
import org.telegram.messenger.exoplayer2.extractor.ts.TsExtractor;

public final class zzix implements ServiceConnection, BaseConnectionCallbacks, BaseOnConnectionFailedListener {
    final /* synthetic */ zzij zzapn;
    private volatile boolean zzapt;
    private volatile zzfg zzapu;

    protected zzix(zzij com_google_android_gms_internal_measurement_zzij) {
        this.zzapn = com_google_android_gms_internal_measurement_zzij;
    }

    public final void onConnected(Bundle bundle) {
        Preconditions.checkMainThread("MeasurementServiceConnection.onConnected");
        synchronized (this) {
            try {
                zzez com_google_android_gms_internal_measurement_zzez = (zzez) this.zzapu.getService();
                this.zzapu = null;
                this.zzapn.zzge().zzc(new zzja(this, com_google_android_gms_internal_measurement_zzez));
            } catch (DeadObjectException e) {
                this.zzapu = null;
                this.zzapt = false;
            } catch (IllegalStateException e2) {
                this.zzapu = null;
                this.zzapt = false;
            }
        }
    }

    public final void onConnectionFailed(ConnectionResult connectionResult) {
        Preconditions.checkMainThread("MeasurementServiceConnection.onConnectionFailed");
        zzfh zzjv = this.zzapn.zzacw.zzjv();
        if (zzjv != null) {
            zzjv.zziv().zzg("Service connection failed", connectionResult);
        }
        synchronized (this) {
            this.zzapt = false;
            this.zzapu = null;
        }
        this.zzapn.zzge().zzc(new zzjc(this));
    }

    public final void onConnectionSuspended(int i) {
        Preconditions.checkMainThread("MeasurementServiceConnection.onConnectionSuspended");
        this.zzapn.zzgf().zziy().log("Service connection suspended");
        this.zzapn.zzge().zzc(new zzjb(this));
    }

    public final void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        zzez com_google_android_gms_internal_measurement_zzez;
        Preconditions.checkMainThread("MeasurementServiceConnection.onServiceConnected");
        synchronized (this) {
            if (iBinder == null) {
                this.zzapt = false;
                this.zzapn.zzgf().zzis().log("Service connected with null binder");
                return;
            }
            try {
                String interfaceDescriptor = iBinder.getInterfaceDescriptor();
                if ("com.google.android.gms.measurement.internal.IMeasurementService".equals(interfaceDescriptor)) {
                    if (iBinder == null) {
                        com_google_android_gms_internal_measurement_zzez = null;
                    } else {
                        IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.measurement.internal.IMeasurementService");
                        com_google_android_gms_internal_measurement_zzez = queryLocalInterface instanceof zzez ? (zzez) queryLocalInterface : new zzfb(iBinder);
                    }
                    try {
                        this.zzapn.zzgf().zziz().log("Bound to IMeasurementService interface");
                    } catch (RemoteException e) {
                        this.zzapn.zzgf().zzis().log("Service connect failed to get IMeasurementService");
                        if (com_google_android_gms_internal_measurement_zzez != null) {
                            this.zzapn.zzge().zzc(new zziy(this, com_google_android_gms_internal_measurement_zzez));
                        } else {
                            this.zzapt = false;
                            try {
                                ConnectionTracker.getInstance().unbindService(this.zzapn.getContext(), this.zzapn.zzapg);
                            } catch (IllegalArgumentException e2) {
                            }
                        }
                    }
                    if (com_google_android_gms_internal_measurement_zzez != null) {
                        this.zzapt = false;
                        ConnectionTracker.getInstance().unbindService(this.zzapn.getContext(), this.zzapn.zzapg);
                    } else {
                        this.zzapn.zzge().zzc(new zziy(this, com_google_android_gms_internal_measurement_zzez));
                    }
                }
                this.zzapn.zzgf().zzis().zzg("Got binder with a wrong descriptor", interfaceDescriptor);
                com_google_android_gms_internal_measurement_zzez = null;
                if (com_google_android_gms_internal_measurement_zzez != null) {
                    this.zzapn.zzge().zzc(new zziy(this, com_google_android_gms_internal_measurement_zzez));
                } else {
                    this.zzapt = false;
                    ConnectionTracker.getInstance().unbindService(this.zzapn.getContext(), this.zzapn.zzapg);
                }
            } catch (RemoteException e3) {
                com_google_android_gms_internal_measurement_zzez = null;
                this.zzapn.zzgf().zzis().log("Service connect failed to get IMeasurementService");
                if (com_google_android_gms_internal_measurement_zzez != null) {
                    this.zzapt = false;
                    ConnectionTracker.getInstance().unbindService(this.zzapn.getContext(), this.zzapn.zzapg);
                } else {
                    this.zzapn.zzge().zzc(new zziy(this, com_google_android_gms_internal_measurement_zzez));
                }
            }
        }
    }

    public final void onServiceDisconnected(ComponentName componentName) {
        Preconditions.checkMainThread("MeasurementServiceConnection.onServiceDisconnected");
        this.zzapn.zzgf().zziy().log("Service disconnected");
        this.zzapn.zzge().zzc(new zziz(this, componentName));
    }

    public final void zzc(Intent intent) {
        this.zzapn.zzab();
        Context context = this.zzapn.getContext();
        ConnectionTracker instance = ConnectionTracker.getInstance();
        synchronized (this) {
            if (this.zzapt) {
                this.zzapn.zzgf().zziz().log("Connection attempt already in progress");
                return;
            }
            this.zzapn.zzgf().zziz().log("Using local app measurement service");
            this.zzapt = true;
            instance.bindService(context, intent, this.zzapn.zzapg, TsExtractor.TS_STREAM_TYPE_AC3);
        }
    }

    public final void zzkq() {
        this.zzapn.zzab();
        Context context = this.zzapn.getContext();
        synchronized (this) {
            if (this.zzapt) {
                this.zzapn.zzgf().zziz().log("Connection attempt already in progress");
            } else if (this.zzapu != null) {
                this.zzapn.zzgf().zziz().log("Already awaiting connection attempt");
            } else {
                this.zzapu = new zzfg(context, Looper.getMainLooper(), this, this);
                this.zzapn.zzgf().zziz().log("Connecting to remote service");
                this.zzapt = true;
                this.zzapu.checkAvailabilityAndConnect();
            }
        }
    }
}
