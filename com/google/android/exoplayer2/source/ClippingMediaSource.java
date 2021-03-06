package com.google.android.exoplayer2.source;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.Timeline.Period;
import com.google.android.exoplayer2.Timeline.Window;
import com.google.android.exoplayer2.source.MediaSource.MediaPeriodId;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public final class ClippingMediaSource extends CompositeMediaSource<Void> {
    private final boolean allowDynamicClippingUpdates;
    private IllegalClippingException clippingError;
    private ClippingTimeline clippingTimeline;
    private final boolean enableInitialDiscontinuity;
    private final long endUs;
    private Object manifest;
    private final ArrayList<ClippingMediaPeriod> mediaPeriods;
    private final MediaSource mediaSource;
    private long periodEndUs;
    private long periodStartUs;
    private final boolean relativeToDefaultPosition;
    private final long startUs;
    private final Window window;

    public static final class IllegalClippingException extends IOException {
        public static final int REASON_INVALID_PERIOD_COUNT = 0;
        public static final int REASON_NOT_SEEKABLE_TO_START = 1;
        public static final int REASON_START_EXCEEDS_END = 2;
        public final int reason;

        @Retention(RetentionPolicy.SOURCE)
        public @interface Reason {
        }

        public IllegalClippingException(int reason) {
            super("Illegal clipping: " + getReasonDescription(reason));
            this.reason = reason;
        }

        private static String getReasonDescription(int reason) {
            switch (reason) {
                case 0:
                    return "invalid period count";
                case 1:
                    return "not seekable to start";
                case 2:
                    return "start exceeds end";
                default:
                    return "unknown";
            }
        }
    }

    private static final class ClippingTimeline extends ForwardingTimeline {
        private final long durationUs;
        private final long endUs;
        private final boolean isDynamic;
        private final long startUs;

        public ClippingTimeline(Timeline timeline, long startUs, long endUs) throws IllegalClippingException {
            super(timeline);
            if (timeline.getPeriodCount() != 1) {
                throw new IllegalClippingException(0);
            }
            Window window = timeline.getWindow(0, new Window(), false);
            startUs = Math.max(0, startUs);
            long resolvedEndUs = endUs == Long.MIN_VALUE ? window.durationUs : Math.max(0, endUs);
            if (window.durationUs != C.TIME_UNSET) {
                if (resolvedEndUs > window.durationUs) {
                    resolvedEndUs = window.durationUs;
                }
                if (startUs != 0 && !window.isSeekable) {
                    throw new IllegalClippingException(1);
                } else if (startUs > resolvedEndUs) {
                    throw new IllegalClippingException(2);
                }
            }
            this.startUs = startUs;
            this.endUs = resolvedEndUs;
            this.durationUs = resolvedEndUs == C.TIME_UNSET ? C.TIME_UNSET : resolvedEndUs - startUs;
            boolean z = window.isDynamic && (resolvedEndUs == C.TIME_UNSET || (window.durationUs != C.TIME_UNSET && resolvedEndUs == window.durationUs));
            this.isDynamic = z;
        }

        public Window getWindow(int windowIndex, Window window, boolean setTag, long defaultPositionProjectionUs) {
            this.timeline.getWindow(0, window, setTag, 0);
            window.positionInFirstPeriodUs += this.startUs;
            window.durationUs = this.durationUs;
            window.isDynamic = this.isDynamic;
            if (window.defaultPositionUs != C.TIME_UNSET) {
                long j;
                window.defaultPositionUs = Math.max(window.defaultPositionUs, this.startUs);
                if (this.endUs == C.TIME_UNSET) {
                    j = window.defaultPositionUs;
                } else {
                    j = Math.min(window.defaultPositionUs, this.endUs);
                }
                window.defaultPositionUs = j;
                window.defaultPositionUs -= this.startUs;
            }
            long startMs = C.usToMs(this.startUs);
            if (window.presentationStartTimeMs != C.TIME_UNSET) {
                window.presentationStartTimeMs += startMs;
            }
            if (window.windowStartTimeMs != C.TIME_UNSET) {
                window.windowStartTimeMs += startMs;
            }
            return window;
        }

        public Period getPeriod(int periodIndex, Period period, boolean setIds) {
            long periodDurationUs = C.TIME_UNSET;
            this.timeline.getPeriod(0, period, setIds);
            long positionInClippedWindowUs = period.getPositionInWindowUs() - this.startUs;
            if (this.durationUs != C.TIME_UNSET) {
                periodDurationUs = this.durationUs - positionInClippedWindowUs;
            }
            return period.set(period.id, period.uid, 0, periodDurationUs, positionInClippedWindowUs);
        }
    }

    public ClippingMediaSource(MediaSource mediaSource, long startPositionUs, long endPositionUs) {
        this(mediaSource, startPositionUs, endPositionUs, true, false, false);
    }

    @Deprecated
    public ClippingMediaSource(MediaSource mediaSource, long startPositionUs, long endPositionUs, boolean enableInitialDiscontinuity) {
        this(mediaSource, startPositionUs, endPositionUs, enableInitialDiscontinuity, false, false);
    }

    public ClippingMediaSource(MediaSource mediaSource, long durationUs) {
        this(mediaSource, 0, durationUs, true, false, true);
    }

    public ClippingMediaSource(MediaSource mediaSource, long startPositionUs, long endPositionUs, boolean enableInitialDiscontinuity, boolean allowDynamicClippingUpdates, boolean relativeToDefaultPosition) {
        Assertions.checkArgument(startPositionUs >= 0);
        this.mediaSource = (MediaSource) Assertions.checkNotNull(mediaSource);
        this.startUs = startPositionUs;
        this.endUs = endPositionUs;
        this.enableInitialDiscontinuity = enableInitialDiscontinuity;
        this.allowDynamicClippingUpdates = allowDynamicClippingUpdates;
        this.relativeToDefaultPosition = relativeToDefaultPosition;
        this.mediaPeriods = new ArrayList();
        this.window = new Window();
    }

    public void prepareSourceInternal(ExoPlayer player, boolean isTopLevelSource, TransferListener mediaTransferListener) {
        super.prepareSourceInternal(player, isTopLevelSource, mediaTransferListener);
        prepareChildSource(null, this.mediaSource);
    }

    public void maybeThrowSourceInfoRefreshError() throws IOException {
        if (this.clippingError != null) {
            throw this.clippingError;
        }
        super.maybeThrowSourceInfoRefreshError();
    }

    public MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator) {
        ClippingMediaPeriod mediaPeriod = new ClippingMediaPeriod(this.mediaSource.createPeriod(id, allocator), this.enableInitialDiscontinuity, this.periodStartUs, this.periodEndUs);
        this.mediaPeriods.add(mediaPeriod);
        return mediaPeriod;
    }

    public void releasePeriod(MediaPeriod mediaPeriod) {
        Assertions.checkState(this.mediaPeriods.remove(mediaPeriod));
        this.mediaSource.releasePeriod(((ClippingMediaPeriod) mediaPeriod).mediaPeriod);
        if (this.mediaPeriods.isEmpty() && !this.allowDynamicClippingUpdates) {
            refreshClippedTimeline(this.clippingTimeline.timeline);
        }
    }

    public void releaseSourceInternal() {
        super.releaseSourceInternal();
        this.clippingError = null;
        this.clippingTimeline = null;
    }

    protected void onChildSourceInfoRefreshed(Void id, MediaSource mediaSource, Timeline timeline, Object manifest) {
        if (this.clippingError == null) {
            this.manifest = manifest;
            refreshClippedTimeline(timeline);
        }
    }

    private void refreshClippedTimeline(Timeline timeline) {
        long windowStartUs;
        long windowEndUs;
        timeline.getWindow(0, this.window);
        long windowPositionInPeriodUs = this.window.getPositionInFirstPeriodUs();
        if (this.clippingTimeline == null || this.mediaPeriods.isEmpty() || this.allowDynamicClippingUpdates) {
            windowStartUs = this.startUs;
            windowEndUs = this.endUs;
            if (this.relativeToDefaultPosition) {
                long windowDefaultPositionUs = this.window.getDefaultPositionUs();
                windowStartUs += windowDefaultPositionUs;
                windowEndUs += windowDefaultPositionUs;
            }
            this.periodStartUs = windowPositionInPeriodUs + windowStartUs;
            this.periodEndUs = this.endUs == Long.MIN_VALUE ? Long.MIN_VALUE : windowPositionInPeriodUs + windowEndUs;
            int count = this.mediaPeriods.size();
            for (int i = 0; i < count; i++) {
                ((ClippingMediaPeriod) this.mediaPeriods.get(i)).updateClipping(this.periodStartUs, this.periodEndUs);
            }
        } else {
            windowStartUs = this.periodStartUs - windowPositionInPeriodUs;
            windowEndUs = this.endUs == Long.MIN_VALUE ? Long.MIN_VALUE : this.periodEndUs - windowPositionInPeriodUs;
        }
        try {
            this.clippingTimeline = new ClippingTimeline(timeline, windowStartUs, windowEndUs);
            refreshSourceInfo(this.clippingTimeline, this.manifest);
        } catch (IllegalClippingException e) {
            this.clippingError = e;
        }
    }

    protected long getMediaTimeForChildMediaTime(Void id, long mediaTimeMs) {
        if (mediaTimeMs == C.TIME_UNSET) {
            return C.TIME_UNSET;
        }
        long startMs = C.usToMs(this.startUs);
        long clippedTimeMs = Math.max(0, mediaTimeMs - startMs);
        if (this.endUs != Long.MIN_VALUE) {
            return Math.min(C.usToMs(this.endUs) - startMs, clippedTimeMs);
        }
        return clippedTimeMs;
    }
}
