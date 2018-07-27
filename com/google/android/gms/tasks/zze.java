package com.google.android.gms.tasks;

import java.util.concurrent.Executor;

final class zze<TResult, TContinuationResult> implements OnCanceledListener, OnFailureListener, OnSuccessListener<TContinuationResult>, zzq<TResult> {
    private final Executor zzafk;
    private final Continuation<TResult, Task<TContinuationResult>> zzafl;
    private final zzu<TContinuationResult> zzafm;

    public zze(Executor executor, Continuation<TResult, Task<TContinuationResult>> continuation, zzu<TContinuationResult> com_google_android_gms_tasks_zzu_TContinuationResult) {
        this.zzafk = executor;
        this.zzafl = continuation;
        this.zzafm = com_google_android_gms_tasks_zzu_TContinuationResult;
    }

    public final void onCanceled() {
        this.zzafm.zzdp();
    }

    public final void onComplete(Task<TResult> task) {
        this.zzafk.execute(new zzf(this, task));
    }

    public final void onFailure(Exception exception) {
        this.zzafm.setException(exception);
    }

    public final void onSuccess(TContinuationResult tContinuationResult) {
        this.zzafm.setResult(tContinuationResult);
    }
}
