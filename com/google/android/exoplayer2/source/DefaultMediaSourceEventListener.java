package com.google.android.exoplayer2.source;

import com.google.android.exoplayer2.source.MediaSource.MediaPeriodId;
import com.google.android.exoplayer2.source.MediaSourceEventListener.LoadEventInfo;
import com.google.android.exoplayer2.source.MediaSourceEventListener.MediaLoadData;
import java.io.IOException;

public abstract class DefaultMediaSourceEventListener implements MediaSourceEventListener {
    public void onMediaPeriodCreated(int windowIndex, MediaPeriodId mediaPeriodId) {
    }

    public void onMediaPeriodReleased(int windowIndex, MediaPeriodId mediaPeriodId) {
    }

    public void onLoadStarted(int windowIndex, MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
    }

    public void onLoadCompleted(int windowIndex, MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
    }

    public void onLoadCanceled(int windowIndex, MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
    }

    public void onLoadError(int windowIndex, MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
    }

    public void onReadingStarted(int windowIndex, MediaPeriodId mediaPeriodId) {
    }

    public void onUpstreamDiscarded(int windowIndex, MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {
    }

    public void onDownstreamFormatChanged(int windowIndex, MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {
    }
}
