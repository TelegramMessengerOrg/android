package com.google.android.exoplayer2;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import com.google.android.exoplayer2.DefaultMediaClock.PlaybackParameterListener;
import com.google.android.exoplayer2.PlayerMessage.Sender;
import com.google.android.exoplayer2.Timeline.Period;
import com.google.android.exoplayer2.Timeline.Window;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSource.MediaPeriodId;
import com.google.android.exoplayer2.source.MediaSource.SourceInfoRefreshListener;
import com.google.android.exoplayer2.source.SampleStream;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector.InvalidationListener;
import com.google.android.exoplayer2.trackselection.TrackSelectorResult;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.util.HandlerWrapper;
import com.google.android.exoplayer2.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

final class ExoPlayerImplInternal implements Callback, PlaybackParameterListener, Sender, MediaPeriod.Callback, SourceInfoRefreshListener, InvalidationListener {
    private static final int IDLE_INTERVAL_MS = 1000;
    private static final int MSG_DO_SOME_WORK = 2;
    public static final int MSG_ERROR = 2;
    private static final int MSG_PERIOD_PREPARED = 9;
    public static final int MSG_PLAYBACK_INFO_CHANGED = 0;
    public static final int MSG_PLAYBACK_PARAMETERS_CHANGED = 1;
    private static final int MSG_PLAYBACK_PARAMETERS_CHANGED_INTERNAL = 16;
    private static final int MSG_PREPARE = 0;
    private static final int MSG_REFRESH_SOURCE_INFO = 8;
    private static final int MSG_RELEASE = 7;
    private static final int MSG_SEEK_TO = 3;
    private static final int MSG_SEND_MESSAGE = 14;
    private static final int MSG_SEND_MESSAGE_TO_TARGET_THREAD = 15;
    private static final int MSG_SET_PLAYBACK_PARAMETERS = 4;
    private static final int MSG_SET_PLAY_WHEN_READY = 1;
    private static final int MSG_SET_REPEAT_MODE = 12;
    private static final int MSG_SET_SEEK_PARAMETERS = 5;
    private static final int MSG_SET_SHUFFLE_ENABLED = 13;
    private static final int MSG_SOURCE_CONTINUE_LOADING_REQUESTED = 10;
    private static final int MSG_STOP = 6;
    private static final int MSG_TRACK_SELECTION_INVALIDATED = 11;
    private static final int PREPARING_SOURCE_INTERVAL_MS = 10;
    private static final int RENDERING_INTERVAL_MS = 10;
    private static final String TAG = "ExoPlayerImplInternal";
    private final long backBufferDurationUs;
    private final BandwidthMeter bandwidthMeter;
    private final Clock clock;
    private final TrackSelectorResult emptyTrackSelectorResult;
    private Renderer[] enabledRenderers;
    private final Handler eventHandler;
    private final HandlerWrapper handler;
    private final HandlerThread internalPlaybackThread;
    private final LoadControl loadControl;
    private final DefaultMediaClock mediaClock;
    private MediaSource mediaSource;
    private int nextPendingMessageIndex;
    private SeekPosition pendingInitialSeekPosition;
    private final ArrayList<PendingMessageInfo> pendingMessages;
    private int pendingPrepareCount;
    private final Period period;
    private boolean playWhenReady;
    private PlaybackInfo playbackInfo;
    private final PlaybackInfoUpdate playbackInfoUpdate;
    private final ExoPlayer player;
    private final MediaPeriodQueue queue = new MediaPeriodQueue();
    private boolean rebuffering;
    private boolean released;
    private final RendererCapabilities[] rendererCapabilities;
    private long rendererPositionUs;
    private final Renderer[] renderers;
    private int repeatMode;
    private final boolean retainBackBufferFromKeyframe;
    private SeekParameters seekParameters;
    private boolean shuffleModeEnabled;
    private final TrackSelector trackSelector;
    private final Window window;

    private static final class MediaSourceRefreshInfo {
        public final Object manifest;
        public final MediaSource source;
        public final Timeline timeline;

        public MediaSourceRefreshInfo(MediaSource source, Timeline timeline, Object manifest) {
            this.source = source;
            this.timeline = timeline;
            this.manifest = manifest;
        }
    }

    private static final class PendingMessageInfo implements Comparable<PendingMessageInfo> {
        public final PlayerMessage message;
        public int resolvedPeriodIndex;
        public long resolvedPeriodTimeUs;
        public Object resolvedPeriodUid;

        public PendingMessageInfo(PlayerMessage message) {
            this.message = message;
        }

        public void setResolvedPosition(int periodIndex, long periodTimeUs, Object periodUid) {
            this.resolvedPeriodIndex = periodIndex;
            this.resolvedPeriodTimeUs = periodTimeUs;
            this.resolvedPeriodUid = periodUid;
        }

        public int compareTo(PendingMessageInfo other) {
            if ((this.resolvedPeriodUid == null ? 1 : 0) != (other.resolvedPeriodUid == null ? 1 : 0)) {
                if (this.resolvedPeriodUid != null) {
                    return -1;
                }
                return 1;
            } else if (this.resolvedPeriodUid == null) {
                return 0;
            } else {
                int comparePeriodIndex = this.resolvedPeriodIndex - other.resolvedPeriodIndex;
                if (comparePeriodIndex != 0) {
                    return comparePeriodIndex;
                }
                return Util.compareLong(this.resolvedPeriodTimeUs, other.resolvedPeriodTimeUs);
            }
        }
    }

    private static final class PlaybackInfoUpdate {
        private int discontinuityReason;
        private PlaybackInfo lastPlaybackInfo;
        private int operationAcks;
        private boolean positionDiscontinuity;

        private PlaybackInfoUpdate() {
        }

        public boolean hasPendingUpdate(PlaybackInfo playbackInfo) {
            return playbackInfo != this.lastPlaybackInfo || this.operationAcks > 0 || this.positionDiscontinuity;
        }

        public void reset(PlaybackInfo playbackInfo) {
            this.lastPlaybackInfo = playbackInfo;
            this.operationAcks = 0;
            this.positionDiscontinuity = false;
        }

        public void incrementPendingOperationAcks(int operationAcks) {
            this.operationAcks += operationAcks;
        }

        public void setPositionDiscontinuity(int discontinuityReason) {
            boolean z = true;
            if (!this.positionDiscontinuity || this.discontinuityReason == 4) {
                this.positionDiscontinuity = true;
                this.discontinuityReason = discontinuityReason;
                return;
            }
            if (discontinuityReason != 4) {
                z = false;
            }
            Assertions.checkArgument(z);
        }
    }

    private static final class SeekPosition {
        public final Timeline timeline;
        public final int windowIndex;
        public final long windowPositionUs;

        public SeekPosition(Timeline timeline, int windowIndex, long windowPositionUs) {
            this.timeline = timeline;
            this.windowIndex = windowIndex;
            this.windowPositionUs = windowPositionUs;
        }
    }

    public ExoPlayerImplInternal(Renderer[] renderers, TrackSelector trackSelector, TrackSelectorResult emptyTrackSelectorResult, LoadControl loadControl, BandwidthMeter bandwidthMeter, boolean playWhenReady, int repeatMode, boolean shuffleModeEnabled, Handler eventHandler, ExoPlayer player, Clock clock) {
        this.renderers = renderers;
        this.trackSelector = trackSelector;
        this.emptyTrackSelectorResult = emptyTrackSelectorResult;
        this.loadControl = loadControl;
        this.bandwidthMeter = bandwidthMeter;
        this.playWhenReady = playWhenReady;
        this.repeatMode = repeatMode;
        this.shuffleModeEnabled = shuffleModeEnabled;
        this.eventHandler = eventHandler;
        this.player = player;
        this.clock = clock;
        this.backBufferDurationUs = loadControl.getBackBufferDurationUs();
        this.retainBackBufferFromKeyframe = loadControl.retainBackBufferFromKeyframe();
        this.seekParameters = SeekParameters.DEFAULT;
        this.playbackInfo = PlaybackInfo.createDummy(C.TIME_UNSET, emptyTrackSelectorResult);
        this.playbackInfoUpdate = new PlaybackInfoUpdate();
        this.rendererCapabilities = new RendererCapabilities[renderers.length];
        for (int i = 0; i < renderers.length; i++) {
            renderers[i].setIndex(i);
            this.rendererCapabilities[i] = renderers[i].getCapabilities();
        }
        this.mediaClock = new DefaultMediaClock(this, clock);
        this.pendingMessages = new ArrayList();
        this.enabledRenderers = new Renderer[0];
        this.window = new Window();
        this.period = new Period();
        trackSelector.init(this, bandwidthMeter);
        this.internalPlaybackThread = new HandlerThread("ExoPlayerImplInternal:Handler", -16);
        this.internalPlaybackThread.start();
        this.handler = clock.createHandler(this.internalPlaybackThread.getLooper(), this);
    }

    public void prepare(MediaSource mediaSource, boolean resetPosition, boolean resetState) {
        int i;
        int i2 = 1;
        HandlerWrapper handlerWrapper = this.handler;
        if (resetPosition) {
            i = 1;
        } else {
            i = 0;
        }
        if (!resetState) {
            i2 = 0;
        }
        handlerWrapper.obtainMessage(0, i, i2, mediaSource).sendToTarget();
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        int i;
        HandlerWrapper handlerWrapper = this.handler;
        if (playWhenReady) {
            i = 1;
        } else {
            i = 0;
        }
        handlerWrapper.obtainMessage(1, i, 0).sendToTarget();
    }

    public void setRepeatMode(int repeatMode) {
        this.handler.obtainMessage(12, repeatMode, 0).sendToTarget();
    }

    public void setShuffleModeEnabled(boolean shuffleModeEnabled) {
        int i;
        HandlerWrapper handlerWrapper = this.handler;
        if (shuffleModeEnabled) {
            i = 1;
        } else {
            i = 0;
        }
        handlerWrapper.obtainMessage(13, i, 0).sendToTarget();
    }

    public void seekTo(Timeline timeline, int windowIndex, long positionUs) {
        this.handler.obtainMessage(3, new SeekPosition(timeline, windowIndex, positionUs)).sendToTarget();
    }

    public void setPlaybackParameters(PlaybackParameters playbackParameters) {
        this.handler.obtainMessage(4, playbackParameters).sendToTarget();
    }

    public void setSeekParameters(SeekParameters seekParameters) {
        this.handler.obtainMessage(5, seekParameters).sendToTarget();
    }

    public void stop(boolean reset) {
        int i;
        HandlerWrapper handlerWrapper = this.handler;
        if (reset) {
            i = 1;
        } else {
            i = 0;
        }
        handlerWrapper.obtainMessage(6, i, 0).sendToTarget();
    }

    public synchronized void sendMessage(PlayerMessage message) {
        if (this.released) {
            Log.w(TAG, "Ignoring messages sent after release.");
            message.markAsProcessed(false);
        } else {
            this.handler.obtainMessage(14, message).sendToTarget();
        }
    }

    public synchronized void release() {
        if (!this.released) {
            this.handler.sendEmptyMessage(7);
            boolean wasInterrupted = false;
            while (!this.released) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    wasInterrupted = true;
                }
            }
            if (wasInterrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public Looper getPlaybackLooper() {
        return this.internalPlaybackThread.getLooper();
    }

    public void onSourceInfoRefreshed(MediaSource source, Timeline timeline, Object manifest) {
        this.handler.obtainMessage(8, new MediaSourceRefreshInfo(source, timeline, manifest)).sendToTarget();
    }

    public void onPrepared(MediaPeriod source) {
        this.handler.obtainMessage(9, source).sendToTarget();
    }

    public void onContinueLoadingRequested(MediaPeriod source) {
        this.handler.obtainMessage(10, source).sendToTarget();
    }

    public void onTrackSelectionsInvalidated() {
        this.handler.sendEmptyMessage(11);
    }

    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        this.handler.obtainMessage(16, playbackParameters).sendToTarget();
    }

    public boolean handleMessage(Message msg) {
        try {
            boolean z;
            switch (msg.what) {
                case 0:
                    boolean z2;
                    boolean z3;
                    MediaSource mediaSource = (MediaSource) msg.obj;
                    if (msg.arg1 != 0) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    if (msg.arg2 != 0) {
                        z3 = true;
                    } else {
                        z3 = false;
                    }
                    prepareInternal(mediaSource, z2, z3);
                    break;
                case 1:
                    if (msg.arg1 != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    setPlayWhenReadyInternal(z);
                    break;
                case 2:
                    doSomeWork();
                    break;
                case 3:
                    seekToInternal((SeekPosition) msg.obj);
                    break;
                case 4:
                    setPlaybackParametersInternal((PlaybackParameters) msg.obj);
                    break;
                case 5:
                    setSeekParametersInternal((SeekParameters) msg.obj);
                    break;
                case 6:
                    stopInternal(msg.arg1 != 0, true);
                    break;
                case 7:
                    releaseInternal();
                    return true;
                case 8:
                    handleSourceInfoRefreshed((MediaSourceRefreshInfo) msg.obj);
                    break;
                case 9:
                    handlePeriodPrepared((MediaPeriod) msg.obj);
                    break;
                case 10:
                    handleContinueLoadingRequested((MediaPeriod) msg.obj);
                    break;
                case 11:
                    reselectTracksInternal();
                    break;
                case 12:
                    setRepeatModeInternal(msg.arg1);
                    break;
                case 13:
                    if (msg.arg1 != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    setShuffleModeEnabledInternal(z);
                    break;
                case 14:
                    sendMessageInternal((PlayerMessage) msg.obj);
                    break;
                case 15:
                    sendMessageToTargetThread((PlayerMessage) msg.obj);
                    break;
                case 16:
                    handlePlaybackParameters((PlaybackParameters) msg.obj);
                    break;
                default:
                    return false;
            }
            maybeNotifyPlaybackInfoChanged();
        } catch (ExoPlaybackException e) {
            Log.e(TAG, "Playback error.", e);
            stopInternal(false, false);
            this.eventHandler.obtainMessage(2, e).sendToTarget();
            maybeNotifyPlaybackInfoChanged();
        } catch (IOException e2) {
            Log.e(TAG, "Source error.", e2);
            stopInternal(false, false);
            this.eventHandler.obtainMessage(2, ExoPlaybackException.createForSource(e2)).sendToTarget();
            maybeNotifyPlaybackInfoChanged();
        } catch (RuntimeException e3) {
            Log.e(TAG, "Internal runtime error.", e3);
            stopInternal(false, false);
            this.eventHandler.obtainMessage(2, ExoPlaybackException.createForUnexpected(e3)).sendToTarget();
            maybeNotifyPlaybackInfoChanged();
        }
        return true;
    }

    private void setState(int state) {
        if (this.playbackInfo.playbackState != state) {
            this.playbackInfo = this.playbackInfo.copyWithPlaybackState(state);
        }
    }

    private void setIsLoading(boolean isLoading) {
        if (this.playbackInfo.isLoading != isLoading) {
            this.playbackInfo = this.playbackInfo.copyWithIsLoading(isLoading);
        }
    }

    private void maybeNotifyPlaybackInfoChanged() {
        if (this.playbackInfoUpdate.hasPendingUpdate(this.playbackInfo)) {
            this.eventHandler.obtainMessage(0, this.playbackInfoUpdate.operationAcks, this.playbackInfoUpdate.positionDiscontinuity ? this.playbackInfoUpdate.discontinuityReason : -1, this.playbackInfo).sendToTarget();
            this.playbackInfoUpdate.reset(this.playbackInfo);
        }
    }

    private void prepareInternal(MediaSource mediaSource, boolean resetPosition, boolean resetState) {
        this.pendingPrepareCount++;
        resetInternal(true, resetPosition, resetState);
        this.loadControl.onPrepared();
        this.mediaSource = mediaSource;
        setState(2);
        mediaSource.prepareSource(this.player, true, this, this.bandwidthMeter.getTransferListener());
        this.handler.sendEmptyMessage(2);
    }

    private void setPlayWhenReadyInternal(boolean playWhenReady) throws ExoPlaybackException {
        this.rebuffering = false;
        this.playWhenReady = playWhenReady;
        if (!playWhenReady) {
            stopRenderers();
            updatePlaybackPositions();
        } else if (this.playbackInfo.playbackState == 3) {
            startRenderers();
            this.handler.sendEmptyMessage(2);
        } else if (this.playbackInfo.playbackState == 2) {
            this.handler.sendEmptyMessage(2);
        }
    }

    private void setRepeatModeInternal(int repeatMode) throws ExoPlaybackException {
        this.repeatMode = repeatMode;
        if (!this.queue.updateRepeatMode(repeatMode)) {
            seekToCurrentPosition(true);
        }
        updateLoadingMediaPeriodId();
    }

    private void setShuffleModeEnabledInternal(boolean shuffleModeEnabled) throws ExoPlaybackException {
        this.shuffleModeEnabled = shuffleModeEnabled;
        if (!this.queue.updateShuffleModeEnabled(shuffleModeEnabled)) {
            seekToCurrentPosition(true);
        }
        updateLoadingMediaPeriodId();
    }

    private void seekToCurrentPosition(boolean sendDiscontinuity) throws ExoPlaybackException {
        MediaPeriodId periodId = this.queue.getPlayingPeriod().info.id;
        long newPositionUs = seekToPeriodPosition(periodId, this.playbackInfo.positionUs, true);
        if (newPositionUs != this.playbackInfo.positionUs) {
            this.playbackInfo = this.playbackInfo.fromNewPosition(periodId, newPositionUs, this.playbackInfo.contentPositionUs);
            if (sendDiscontinuity) {
                this.playbackInfoUpdate.setPositionDiscontinuity(4);
            }
        }
    }

    private void startRenderers() throws ExoPlaybackException {
        int i = 0;
        this.rebuffering = false;
        this.mediaClock.start();
        Renderer[] rendererArr = this.enabledRenderers;
        int length = rendererArr.length;
        while (i < length) {
            rendererArr[i].start();
            i++;
        }
    }

    private void stopRenderers() throws ExoPlaybackException {
        this.mediaClock.stop();
        for (Renderer renderer : this.enabledRenderers) {
            ensureStopped(renderer);
        }
    }

    private void updatePlaybackPositions() throws ExoPlaybackException {
        if (this.queue.hasPlayingPeriod()) {
            MediaPeriodHolder playingPeriodHolder = this.queue.getPlayingPeriod();
            long periodPositionUs = playingPeriodHolder.mediaPeriod.readDiscontinuity();
            if (periodPositionUs != C.TIME_UNSET) {
                resetRendererPosition(periodPositionUs);
                if (periodPositionUs != this.playbackInfo.positionUs) {
                    this.playbackInfo = this.playbackInfo.fromNewPosition(this.playbackInfo.periodId, periodPositionUs, this.playbackInfo.contentPositionUs);
                    this.playbackInfoUpdate.setPositionDiscontinuity(4);
                }
            } else {
                this.rendererPositionUs = this.mediaClock.syncAndGetPositionUs();
                periodPositionUs = playingPeriodHolder.toPeriodTime(this.rendererPositionUs);
                maybeTriggerPendingMessages(this.playbackInfo.positionUs, periodPositionUs);
                this.playbackInfo.positionUs = periodPositionUs;
            }
            MediaPeriodHolder loadingPeriod = this.queue.getLoadingPeriod();
            this.playbackInfo.bufferedPositionUs = loadingPeriod.getBufferedPositionUs(true);
            this.playbackInfo.totalBufferedDurationUs = this.playbackInfo.bufferedPositionUs - loadingPeriod.toPeriodTime(this.rendererPositionUs);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void doSomeWork() throws com.google.android.exoplayer2.ExoPlaybackException, java.io.IOException {
        /*
        r18 = this;
        r0 = r18;
        r13 = r0.clock;
        r2 = r13.uptimeMillis();
        r18.updatePeriods();
        r0 = r18;
        r13 = r0.queue;
        r13 = r13.hasPlayingPeriod();
        if (r13 != 0) goto L_0x0020;
    L_0x0015:
        r18.maybeThrowPeriodPrepareError();
        r14 = 10;
        r0 = r18;
        r0.scheduleNextWork(r2, r14);
    L_0x001f:
        return;
    L_0x0020:
        r0 = r18;
        r13 = r0.queue;
        r6 = r13.getPlayingPeriod();
        r13 = "doSomeWork";
        com.google.android.exoplayer2.util.TraceUtil.beginSection(r13);
        r18.updatePlaybackPositions();
        r14 = android.os.SystemClock.elapsedRealtime();
        r16 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r8 = r14 * r16;
        r13 = r6.mediaPeriod;
        r0 = r18;
        r14 = r0.playbackInfo;
        r14 = r14.positionUs;
        r0 = r18;
        r0 = r0.backBufferDurationUs;
        r16 = r0;
        r14 = r14 - r16;
        r0 = r18;
        r0 = r0.retainBackBufferFromKeyframe;
        r16 = r0;
        r13.discardBuffer(r14, r16);
        r11 = 1;
        r12 = 1;
        r0 = r18;
        r14 = r0.enabledRenderers;
        r15 = r14.length;
        r13 = 0;
    L_0x005a:
        if (r13 >= r15) goto L_0x009a;
    L_0x005c:
        r7 = r14[r13];
        r0 = r18;
        r0 = r0.rendererPositionUs;
        r16 = r0;
        r0 = r16;
        r7.render(r0, r8);
        if (r11 == 0) goto L_0x0094;
    L_0x006b:
        r16 = r7.isEnded();
        if (r16 == 0) goto L_0x0094;
    L_0x0071:
        r11 = 1;
    L_0x0072:
        r16 = r7.isReady();
        if (r16 != 0) goto L_0x0086;
    L_0x0078:
        r16 = r7.isEnded();
        if (r16 != 0) goto L_0x0086;
    L_0x007e:
        r0 = r18;
        r16 = r0.rendererWaitingForNextStream(r7);
        if (r16 == 0) goto L_0x0096;
    L_0x0086:
        r10 = 1;
    L_0x0087:
        if (r10 != 0) goto L_0x008c;
    L_0x0089:
        r7.maybeThrowStreamError();
    L_0x008c:
        if (r12 == 0) goto L_0x0098;
    L_0x008e:
        if (r10 == 0) goto L_0x0098;
    L_0x0090:
        r12 = 1;
    L_0x0091:
        r13 = r13 + 1;
        goto L_0x005a;
    L_0x0094:
        r11 = 0;
        goto L_0x0072;
    L_0x0096:
        r10 = 0;
        goto L_0x0087;
    L_0x0098:
        r12 = 0;
        goto L_0x0091;
    L_0x009a:
        if (r12 != 0) goto L_0x009f;
    L_0x009c:
        r18.maybeThrowPeriodPrepareError();
    L_0x009f:
        r13 = r6.info;
        r4 = r13.durationUs;
        if (r11 == 0) goto L_0x00e0;
    L_0x00a5:
        r14 = -9223372036854775807; // 0x8000000000000001 float:1.4E-45 double:-4.9E-324;
        r13 = (r4 > r14 ? 1 : (r4 == r14 ? 0 : -1));
        if (r13 == 0) goto L_0x00b8;
    L_0x00ae:
        r0 = r18;
        r13 = r0.playbackInfo;
        r14 = r13.positionUs;
        r13 = (r4 > r14 ? 1 : (r4 == r14 ? 0 : -1));
        if (r13 > 0) goto L_0x00e0;
    L_0x00b8:
        r13 = r6.info;
        r13 = r13.isFinal;
        if (r13 == 0) goto L_0x00e0;
    L_0x00be:
        r13 = 4;
        r0 = r18;
        r0.setState(r13);
        r18.stopRenderers();
    L_0x00c7:
        r0 = r18;
        r13 = r0.playbackInfo;
        r13 = r13.playbackState;
        r14 = 2;
        if (r13 != r14) goto L_0x012c;
    L_0x00d0:
        r0 = r18;
        r14 = r0.enabledRenderers;
        r15 = r14.length;
        r13 = 0;
    L_0x00d6:
        if (r13 >= r15) goto L_0x012c;
    L_0x00d8:
        r7 = r14[r13];
        r7.maybeThrowStreamError();
        r13 = r13 + 1;
        goto L_0x00d6;
    L_0x00e0:
        r0 = r18;
        r13 = r0.playbackInfo;
        r13 = r13.playbackState;
        r14 = 2;
        if (r13 != r14) goto L_0x0101;
    L_0x00e9:
        r0 = r18;
        r13 = r0.shouldTransitionToReadyState(r12);
        if (r13 == 0) goto L_0x0101;
    L_0x00f1:
        r13 = 3;
        r0 = r18;
        r0.setState(r13);
        r0 = r18;
        r13 = r0.playWhenReady;
        if (r13 == 0) goto L_0x00c7;
    L_0x00fd:
        r18.startRenderers();
        goto L_0x00c7;
    L_0x0101:
        r0 = r18;
        r13 = r0.playbackInfo;
        r13 = r13.playbackState;
        r14 = 3;
        if (r13 != r14) goto L_0x00c7;
    L_0x010a:
        r0 = r18;
        r13 = r0.enabledRenderers;
        r13 = r13.length;
        if (r13 != 0) goto L_0x0129;
    L_0x0111:
        r13 = r18.isTimelineReady();
        if (r13 != 0) goto L_0x00c7;
    L_0x0117:
        r0 = r18;
        r13 = r0.playWhenReady;
        r0 = r18;
        r0.rebuffering = r13;
        r13 = 2;
        r0 = r18;
        r0.setState(r13);
        r18.stopRenderers();
        goto L_0x00c7;
    L_0x0129:
        if (r12 != 0) goto L_0x00c7;
    L_0x012b:
        goto L_0x0117;
    L_0x012c:
        r0 = r18;
        r13 = r0.playWhenReady;
        if (r13 == 0) goto L_0x013b;
    L_0x0132:
        r0 = r18;
        r13 = r0.playbackInfo;
        r13 = r13.playbackState;
        r14 = 3;
        if (r13 == r14) goto L_0x0144;
    L_0x013b:
        r0 = r18;
        r13 = r0.playbackInfo;
        r13 = r13.playbackState;
        r14 = 2;
        if (r13 != r14) goto L_0x0150;
    L_0x0144:
        r14 = 10;
        r0 = r18;
        r0.scheduleNextWork(r2, r14);
    L_0x014b:
        com.google.android.exoplayer2.util.TraceUtil.endSection();
        goto L_0x001f;
    L_0x0150:
        r0 = r18;
        r13 = r0.enabledRenderers;
        r13 = r13.length;
        if (r13 == 0) goto L_0x0168;
    L_0x0157:
        r0 = r18;
        r13 = r0.playbackInfo;
        r13 = r13.playbackState;
        r14 = 4;
        if (r13 == r14) goto L_0x0168;
    L_0x0160:
        r14 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r0 = r18;
        r0.scheduleNextWork(r2, r14);
        goto L_0x014b;
    L_0x0168:
        r0 = r18;
        r13 = r0.handler;
        r14 = 2;
        r13.removeMessages(r14);
        goto L_0x014b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.exoplayer2.ExoPlayerImplInternal.doSomeWork():void");
    }

    private void scheduleNextWork(long thisOperationStartTimeMs, long intervalMs) {
        this.handler.removeMessages(2);
        this.handler.sendEmptyMessageAtTime(2, thisOperationStartTimeMs + intervalMs);
    }

    private void seekToInternal(SeekPosition seekPosition) throws ExoPlaybackException {
        MediaPeriodId periodId;
        long periodPositionUs;
        long contentPositionUs;
        boolean seekPositionAdjusted;
        this.playbackInfoUpdate.incrementPendingOperationAcks(1);
        Pair<Integer, Long> resolvedSeekPosition = resolveSeekPosition(seekPosition, true);
        if (resolvedSeekPosition == null) {
            periodId = getFirstMediaPeriodId();
            periodPositionUs = C.TIME_UNSET;
            contentPositionUs = C.TIME_UNSET;
            seekPositionAdjusted = true;
        } else {
            int periodIndex = ((Integer) resolvedSeekPosition.first).intValue();
            contentPositionUs = ((Long) resolvedSeekPosition.second).longValue();
            periodId = this.queue.resolveMediaPeriodIdForAds(periodIndex, contentPositionUs);
            if (periodId.isAd()) {
                periodPositionUs = 0;
                seekPositionAdjusted = true;
            } else {
                periodPositionUs = ((Long) resolvedSeekPosition.second).longValue();
                seekPositionAdjusted = seekPosition.windowPositionUs == C.TIME_UNSET;
            }
        }
        try {
            if (this.mediaSource == null || this.pendingPrepareCount > 0) {
                this.pendingInitialSeekPosition = seekPosition;
            } else if (periodPositionUs == C.TIME_UNSET) {
                setState(4);
                resetInternal(false, true, false);
            } else {
                long newPeriodPositionUs = periodPositionUs;
                if (periodId.equals(this.playbackInfo.periodId)) {
                    MediaPeriodHolder playingPeriodHolder = this.queue.getPlayingPeriod();
                    if (!(playingPeriodHolder == null || newPeriodPositionUs == 0)) {
                        newPeriodPositionUs = playingPeriodHolder.mediaPeriod.getAdjustedSeekPositionUs(newPeriodPositionUs, this.seekParameters);
                    }
                    if (C.usToMs(newPeriodPositionUs) == C.usToMs(this.playbackInfo.positionUs)) {
                        this.playbackInfo = this.playbackInfo.fromNewPosition(periodId, this.playbackInfo.positionUs, contentPositionUs);
                        if (seekPositionAdjusted) {
                            this.playbackInfoUpdate.setPositionDiscontinuity(2);
                            return;
                        }
                        return;
                    }
                }
                newPeriodPositionUs = seekToPeriodPosition(periodId, newPeriodPositionUs);
                seekPositionAdjusted |= periodPositionUs != newPeriodPositionUs ? 1 : 0;
                periodPositionUs = newPeriodPositionUs;
            }
            this.playbackInfo = this.playbackInfo.fromNewPosition(periodId, periodPositionUs, contentPositionUs);
            if (seekPositionAdjusted) {
                this.playbackInfoUpdate.setPositionDiscontinuity(2);
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            this.playbackInfo = this.playbackInfo.fromNewPosition(periodId, periodPositionUs, contentPositionUs);
            if (seekPositionAdjusted) {
                this.playbackInfoUpdate.setPositionDiscontinuity(2);
            }
        }
    }

    private long seekToPeriodPosition(MediaPeriodId periodId, long periodPositionUs) throws ExoPlaybackException {
        return seekToPeriodPosition(periodId, periodPositionUs, this.queue.getPlayingPeriod() != this.queue.getReadingPeriod());
    }

    private long seekToPeriodPosition(MediaPeriodId periodId, long periodPositionUs, boolean forceDisableRenderers) throws ExoPlaybackException {
        stopRenderers();
        this.rebuffering = false;
        setState(2);
        MediaPeriodHolder oldPlayingPeriodHolder = this.queue.getPlayingPeriod();
        MediaPeriodHolder newPlayingPeriodHolder = oldPlayingPeriodHolder;
        while (newPlayingPeriodHolder != null) {
            if (periodId.equals(newPlayingPeriodHolder.info.id) && newPlayingPeriodHolder.prepared) {
                this.queue.removeAfter(newPlayingPeriodHolder);
                break;
            }
            newPlayingPeriodHolder = this.queue.advancePlayingPeriod();
        }
        if (oldPlayingPeriodHolder != newPlayingPeriodHolder || forceDisableRenderers) {
            for (Renderer renderer : this.enabledRenderers) {
                disableRenderer(renderer);
            }
            this.enabledRenderers = new Renderer[0];
            oldPlayingPeriodHolder = null;
        }
        if (newPlayingPeriodHolder != null) {
            updatePlayingPeriodRenderers(oldPlayingPeriodHolder);
            if (newPlayingPeriodHolder.hasEnabledTracks) {
                periodPositionUs = newPlayingPeriodHolder.mediaPeriod.seekToUs(periodPositionUs);
                newPlayingPeriodHolder.mediaPeriod.discardBuffer(periodPositionUs - this.backBufferDurationUs, this.retainBackBufferFromKeyframe);
            }
            resetRendererPosition(periodPositionUs);
            maybeContinueLoading();
        } else {
            this.queue.clear(true);
            resetRendererPosition(periodPositionUs);
        }
        updateLoadingMediaPeriodId();
        this.handler.sendEmptyMessage(2);
        return periodPositionUs;
    }

    private void resetRendererPosition(long periodPositionUs) throws ExoPlaybackException {
        if (this.queue.hasPlayingPeriod()) {
            periodPositionUs = this.queue.getPlayingPeriod().toRendererTime(periodPositionUs);
        }
        this.rendererPositionUs = periodPositionUs;
        this.mediaClock.resetPosition(this.rendererPositionUs);
        for (Renderer renderer : this.enabledRenderers) {
            renderer.resetPosition(this.rendererPositionUs);
        }
    }

    private void setPlaybackParametersInternal(PlaybackParameters playbackParameters) {
        this.mediaClock.setPlaybackParameters(playbackParameters);
    }

    private void setSeekParametersInternal(SeekParameters seekParameters) {
        this.seekParameters = seekParameters;
    }

    private void stopInternal(boolean reset, boolean acknowledgeStop) {
        resetInternal(true, reset, reset);
        this.playbackInfoUpdate.incrementPendingOperationAcks((acknowledgeStop ? 1 : 0) + this.pendingPrepareCount);
        this.pendingPrepareCount = 0;
        this.loadControl.onStopped();
        setState(1);
    }

    private void releaseInternal() {
        resetInternal(true, true, true);
        this.loadControl.onReleased();
        setState(1);
        this.internalPlaybackThread.quit();
        synchronized (this) {
            this.released = true;
            notifyAll();
        }
    }

    private MediaPeriodId getFirstMediaPeriodId() {
        Timeline timeline = this.playbackInfo.timeline;
        if (timeline.isEmpty()) {
            return PlaybackInfo.DUMMY_MEDIA_PERIOD_ID;
        }
        return new MediaPeriodId(timeline.getWindow(timeline.getFirstWindowIndex(this.shuffleModeEnabled), this.window).firstPeriodIndex);
    }

    private void resetInternal(boolean releaseMediaSource, boolean resetPosition, boolean resetState) {
        Exception e;
        Timeline timeline;
        Object obj;
        TrackGroupArray trackGroupArray;
        TrackSelectorResult trackSelectorResult;
        this.handler.removeMessages(2);
        this.rebuffering = false;
        this.mediaClock.stop();
        this.rendererPositionUs = 0;
        for (Renderer renderer : this.enabledRenderers) {
            try {
                disableRenderer(renderer);
            } catch (ExoPlaybackException e2) {
                e = e2;
            } catch (RuntimeException e3) {
                e = e3;
            }
        }
        this.enabledRenderers = new Renderer[0];
        this.queue.clear(!resetPosition);
        setIsLoading(false);
        if (resetPosition) {
            this.pendingInitialSeekPosition = null;
        }
        if (resetState) {
            this.queue.setTimeline(Timeline.EMPTY);
            Iterator it = this.pendingMessages.iterator();
            while (it.hasNext()) {
                ((PendingMessageInfo) it.next()).message.markAsProcessed(false);
            }
            this.pendingMessages.clear();
            this.nextPendingMessageIndex = 0;
        }
        MediaPeriodId mediaPeriodId = resetPosition ? getFirstMediaPeriodId() : this.playbackInfo.periodId;
        long startPositionUs = resetPosition ? C.TIME_UNSET : this.playbackInfo.positionUs;
        long contentPositionUs = resetPosition ? C.TIME_UNSET : this.playbackInfo.contentPositionUs;
        if (resetState) {
            timeline = Timeline.EMPTY;
        } else {
            timeline = this.playbackInfo.timeline;
        }
        if (resetState) {
            obj = null;
        } else {
            obj = this.playbackInfo.manifest;
        }
        int i = this.playbackInfo.playbackState;
        if (resetState) {
            trackGroupArray = TrackGroupArray.EMPTY;
        } else {
            trackGroupArray = this.playbackInfo.trackGroups;
        }
        if (resetState) {
            trackSelectorResult = this.emptyTrackSelectorResult;
        } else {
            trackSelectorResult = this.playbackInfo.trackSelectorResult;
        }
        this.playbackInfo = new PlaybackInfo(timeline, obj, mediaPeriodId, startPositionUs, contentPositionUs, i, false, trackGroupArray, trackSelectorResult, mediaPeriodId, startPositionUs, 0, startPositionUs);
        if (releaseMediaSource && this.mediaSource != null) {
            this.mediaSource.releaseSource(this);
            this.mediaSource = null;
            return;
        }
        return;
        Log.e(TAG, "Stop failed.", e);
    }

    private void sendMessageInternal(PlayerMessage message) throws ExoPlaybackException {
        if (message.getPositionMs() == C.TIME_UNSET) {
            sendMessageToTarget(message);
        } else if (this.mediaSource == null || this.pendingPrepareCount > 0) {
            this.pendingMessages.add(new PendingMessageInfo(message));
        } else {
            PendingMessageInfo pendingMessageInfo = new PendingMessageInfo(message);
            if (resolvePendingMessagePosition(pendingMessageInfo)) {
                this.pendingMessages.add(pendingMessageInfo);
                Collections.sort(this.pendingMessages);
                return;
            }
            message.markAsProcessed(false);
        }
    }

    private void sendMessageToTarget(PlayerMessage message) throws ExoPlaybackException {
        if (message.getHandler().getLooper() == this.handler.getLooper()) {
            deliverMessage(message);
            if (this.playbackInfo.playbackState == 3 || this.playbackInfo.playbackState == 2) {
                this.handler.sendEmptyMessage(2);
                return;
            }
            return;
        }
        this.handler.obtainMessage(15, message).sendToTarget();
    }

    private void sendMessageToTargetThread(final PlayerMessage message) {
        message.getHandler().post(new Runnable() {
            public void run() {
                try {
                    ExoPlayerImplInternal.this.deliverMessage(message);
                } catch (ExoPlaybackException e) {
                    Log.e(ExoPlayerImplInternal.TAG, "Unexpected error delivering message on external thread.", e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void deliverMessage(PlayerMessage message) throws ExoPlaybackException {
        if (!message.isCanceled()) {
            try {
                message.getTarget().handleMessage(message.getType(), message.getPayload());
            } finally {
                message.markAsProcessed(true);
            }
        }
    }

    private void resolvePendingMessagePositions() {
        for (int i = this.pendingMessages.size() - 1; i >= 0; i--) {
            if (!resolvePendingMessagePosition((PendingMessageInfo) this.pendingMessages.get(i))) {
                ((PendingMessageInfo) this.pendingMessages.get(i)).message.markAsProcessed(false);
                this.pendingMessages.remove(i);
            }
        }
        Collections.sort(this.pendingMessages);
    }

    private boolean resolvePendingMessagePosition(PendingMessageInfo pendingMessageInfo) {
        if (pendingMessageInfo.resolvedPeriodUid == null) {
            Pair<Integer, Long> periodPosition = resolveSeekPosition(new SeekPosition(pendingMessageInfo.message.getTimeline(), pendingMessageInfo.message.getWindowIndex(), C.msToUs(pendingMessageInfo.message.getPositionMs())), false);
            if (periodPosition == null) {
                return false;
            }
            pendingMessageInfo.setResolvedPosition(((Integer) periodPosition.first).intValue(), ((Long) periodPosition.second).longValue(), this.playbackInfo.timeline.getUidOfPeriod(((Integer) periodPosition.first).intValue()));
        } else {
            int index = this.playbackInfo.timeline.getIndexOfPeriod(pendingMessageInfo.resolvedPeriodUid);
            if (index == -1) {
                return false;
            }
            pendingMessageInfo.resolvedPeriodIndex = index;
        }
        return true;
    }

    private void maybeTriggerPendingMessages(long oldPeriodPositionUs, long newPeriodPositionUs) throws ExoPlaybackException {
        if (!this.pendingMessages.isEmpty() && !this.playbackInfo.periodId.isAd()) {
            PendingMessageInfo previousInfo;
            PendingMessageInfo nextInfo;
            if (this.playbackInfo.startPositionUs == oldPeriodPositionUs) {
                oldPeriodPositionUs--;
            }
            int currentPeriodIndex = this.playbackInfo.periodId.periodIndex;
            if (this.nextPendingMessageIndex > 0) {
                previousInfo = (PendingMessageInfo) this.pendingMessages.get(this.nextPendingMessageIndex - 1);
            } else {
                previousInfo = null;
            }
            while (previousInfo != null && (previousInfo.resolvedPeriodIndex > currentPeriodIndex || (previousInfo.resolvedPeriodIndex == currentPeriodIndex && previousInfo.resolvedPeriodTimeUs > oldPeriodPositionUs))) {
                this.nextPendingMessageIndex--;
                if (this.nextPendingMessageIndex > 0) {
                    previousInfo = (PendingMessageInfo) this.pendingMessages.get(this.nextPendingMessageIndex - 1);
                } else {
                    previousInfo = null;
                }
            }
            if (this.nextPendingMessageIndex < this.pendingMessages.size()) {
                nextInfo = (PendingMessageInfo) this.pendingMessages.get(this.nextPendingMessageIndex);
            } else {
                nextInfo = null;
            }
            while (nextInfo != null && nextInfo.resolvedPeriodUid != null && (nextInfo.resolvedPeriodIndex < currentPeriodIndex || (nextInfo.resolvedPeriodIndex == currentPeriodIndex && nextInfo.resolvedPeriodTimeUs <= oldPeriodPositionUs))) {
                this.nextPendingMessageIndex++;
                if (this.nextPendingMessageIndex < this.pendingMessages.size()) {
                    nextInfo = (PendingMessageInfo) this.pendingMessages.get(this.nextPendingMessageIndex);
                } else {
                    nextInfo = null;
                }
            }
            while (nextInfo != null && nextInfo.resolvedPeriodUid != null && nextInfo.resolvedPeriodIndex == currentPeriodIndex && nextInfo.resolvedPeriodTimeUs > oldPeriodPositionUs && nextInfo.resolvedPeriodTimeUs <= newPeriodPositionUs) {
                sendMessageToTarget(nextInfo.message);
                if (nextInfo.message.getDeleteAfterDelivery() || nextInfo.message.isCanceled()) {
                    this.pendingMessages.remove(this.nextPendingMessageIndex);
                } else {
                    this.nextPendingMessageIndex++;
                }
                if (this.nextPendingMessageIndex < this.pendingMessages.size()) {
                    nextInfo = (PendingMessageInfo) this.pendingMessages.get(this.nextPendingMessageIndex);
                } else {
                    nextInfo = null;
                }
            }
        }
    }

    private void ensureStopped(Renderer renderer) throws ExoPlaybackException {
        if (renderer.getState() == 2) {
            renderer.stop();
        }
    }

    private void disableRenderer(Renderer renderer) throws ExoPlaybackException {
        this.mediaClock.onRendererDisabled(renderer);
        ensureStopped(renderer);
        renderer.disable();
    }

    private void reselectTracksInternal() throws ExoPlaybackException {
        if (this.queue.hasPlayingPeriod()) {
            float playbackSpeed = this.mediaClock.getPlaybackParameters().speed;
            MediaPeriodHolder periodHolder = this.queue.getPlayingPeriod();
            MediaPeriodHolder readingPeriodHolder = this.queue.getReadingPeriod();
            boolean selectionsChangedForReadPeriod = true;
            while (periodHolder != null && periodHolder.prepared) {
                if (periodHolder.selectTracks(playbackSpeed)) {
                    if (selectionsChangedForReadPeriod) {
                        MediaPeriodHolder playingPeriodHolder = this.queue.getPlayingPeriod();
                        boolean[] streamResetFlags = new boolean[this.renderers.length];
                        long periodPositionUs = playingPeriodHolder.applyTrackSelection(this.playbackInfo.positionUs, this.queue.removeAfter(playingPeriodHolder), streamResetFlags);
                        updateLoadControlTrackSelection(playingPeriodHolder.trackGroups, playingPeriodHolder.trackSelectorResult);
                        if (!(this.playbackInfo.playbackState == 4 || periodPositionUs == this.playbackInfo.positionUs)) {
                            this.playbackInfo = this.playbackInfo.fromNewPosition(this.playbackInfo.periodId, periodPositionUs, this.playbackInfo.contentPositionUs);
                            this.playbackInfoUpdate.setPositionDiscontinuity(4);
                            resetRendererPosition(periodPositionUs);
                        }
                        int enabledRendererCount = 0;
                        boolean[] rendererWasEnabledFlags = new boolean[this.renderers.length];
                        for (int i = 0; i < this.renderers.length; i++) {
                            Renderer renderer = this.renderers[i];
                            rendererWasEnabledFlags[i] = renderer.getState() != 0;
                            SampleStream sampleStream = playingPeriodHolder.sampleStreams[i];
                            if (sampleStream != null) {
                                enabledRendererCount++;
                            }
                            if (rendererWasEnabledFlags[i]) {
                                if (sampleStream != renderer.getStream()) {
                                    disableRenderer(renderer);
                                } else if (streamResetFlags[i]) {
                                    renderer.resetPosition(this.rendererPositionUs);
                                }
                            }
                        }
                        this.playbackInfo = this.playbackInfo.copyWithTrackInfo(playingPeriodHolder.trackGroups, playingPeriodHolder.trackSelectorResult);
                        enableRenderers(rendererWasEnabledFlags, enabledRendererCount);
                    } else {
                        this.queue.removeAfter(periodHolder);
                        if (periodHolder.prepared) {
                            periodHolder.applyTrackSelection(Math.max(periodHolder.info.startPositionUs, periodHolder.toPeriodTime(this.rendererPositionUs)), false);
                            updateLoadControlTrackSelection(periodHolder.trackGroups, periodHolder.trackSelectorResult);
                        }
                    }
                    updateLoadingMediaPeriodId();
                    if (this.playbackInfo.playbackState != 4) {
                        maybeContinueLoading();
                        updatePlaybackPositions();
                        this.handler.sendEmptyMessage(2);
                        return;
                    }
                    return;
                }
                if (periodHolder == readingPeriodHolder) {
                    selectionsChangedForReadPeriod = false;
                }
                periodHolder = periodHolder.next;
            }
        }
    }

    private void updateLoadControlTrackSelection(TrackGroupArray trackGroups, TrackSelectorResult trackSelectorResult) {
        this.loadControl.onTracksSelected(this.renderers, trackGroups, trackSelectorResult.selections);
    }

    private void updateTrackSelectionPlaybackSpeed(float playbackSpeed) {
        for (MediaPeriodHolder periodHolder = this.queue.getFrontPeriod(); periodHolder != null; periodHolder = periodHolder.next) {
            if (periodHolder.trackSelectorResult != null) {
                for (TrackSelection trackSelection : periodHolder.trackSelectorResult.selections.getAll()) {
                    if (trackSelection != null) {
                        trackSelection.onPlaybackSpeed(playbackSpeed);
                    }
                }
            }
        }
    }

    private boolean shouldTransitionToReadyState(boolean renderersReadyOrEnded) {
        if (this.enabledRenderers.length == 0) {
            return isTimelineReady();
        }
        if (!renderersReadyOrEnded) {
            return false;
        }
        if (!this.playbackInfo.isLoading) {
            return true;
        }
        boolean z;
        MediaPeriodHolder loadingHolder = this.queue.getLoadingPeriod();
        if (loadingHolder.info.isFinal) {
            z = false;
        } else {
            z = true;
        }
        long bufferedPositionUs = loadingHolder.getBufferedPositionUs(z);
        if (bufferedPositionUs == Long.MIN_VALUE || this.loadControl.shouldStartPlayback(bufferedPositionUs - loadingHolder.toPeriodTime(this.rendererPositionUs), this.mediaClock.getPlaybackParameters().speed, this.rebuffering)) {
            return true;
        }
        return false;
    }

    private boolean isTimelineReady() {
        MediaPeriodHolder playingPeriodHolder = this.queue.getPlayingPeriod();
        long playingPeriodDurationUs = playingPeriodHolder.info.durationUs;
        return playingPeriodDurationUs == C.TIME_UNSET || this.playbackInfo.positionUs < playingPeriodDurationUs || (playingPeriodHolder.next != null && (playingPeriodHolder.next.prepared || playingPeriodHolder.next.info.id.isAd()));
    }

    private void maybeThrowPeriodPrepareError() throws IOException {
        MediaPeriodHolder loadingPeriodHolder = this.queue.getLoadingPeriod();
        MediaPeriodHolder readingPeriodHolder = this.queue.getReadingPeriod();
        if (loadingPeriodHolder != null && !loadingPeriodHolder.prepared) {
            if (readingPeriodHolder == null || readingPeriodHolder.next == loadingPeriodHolder) {
                Renderer[] rendererArr = this.enabledRenderers;
                int length = rendererArr.length;
                int i = 0;
                while (i < length) {
                    if (rendererArr[i].hasReadStreamToEnd()) {
                        i++;
                    } else {
                        return;
                    }
                }
                loadingPeriodHolder.mediaPeriod.maybeThrowPrepareError();
            }
        }
    }

    private void handleSourceInfoRefreshed(MediaSourceRefreshInfo sourceRefreshInfo) throws ExoPlaybackException {
        if (sourceRefreshInfo.source == this.mediaSource) {
            Timeline oldTimeline = this.playbackInfo.timeline;
            Timeline timeline = sourceRefreshInfo.timeline;
            Object manifest = sourceRefreshInfo.manifest;
            this.queue.setTimeline(timeline);
            this.playbackInfo = this.playbackInfo.copyWithTimeline(timeline, manifest);
            resolvePendingMessagePositions();
            int periodIndex;
            MediaPeriodId periodId;
            long j;
            Pair<Integer, Long> defaultPosition;
            long startPositionUs;
            PlaybackInfo playbackInfo;
            long j2;
            if (this.pendingPrepareCount > 0) {
                this.playbackInfoUpdate.incrementPendingOperationAcks(this.pendingPrepareCount);
                this.pendingPrepareCount = 0;
                if (this.pendingInitialSeekPosition != null) {
                    try {
                        Pair<Integer, Long> periodPosition = resolveSeekPosition(this.pendingInitialSeekPosition, true);
                        this.pendingInitialSeekPosition = null;
                        if (periodPosition == null) {
                            handleSourceInfoRefreshEndedPlayback();
                            return;
                        }
                        periodIndex = ((Integer) periodPosition.first).intValue();
                        long positionUs = ((Long) periodPosition.second).longValue();
                        periodId = this.queue.resolveMediaPeriodIdForAds(periodIndex, positionUs);
                        PlaybackInfo playbackInfo2 = this.playbackInfo;
                        if (periodId.isAd()) {
                            j = 0;
                        } else {
                            j = positionUs;
                        }
                        this.playbackInfo = playbackInfo2.fromNewPosition(periodId, j, positionUs);
                    } catch (IllegalSeekPositionException e) {
                        this.playbackInfo = this.playbackInfo.fromNewPosition(getFirstMediaPeriodId(), C.TIME_UNSET, C.TIME_UNSET);
                        throw e;
                    }
                } else if (this.playbackInfo.startPositionUs != C.TIME_UNSET) {
                } else {
                    if (timeline.isEmpty()) {
                        handleSourceInfoRefreshEndedPlayback();
                        return;
                    }
                    defaultPosition = getPeriodPosition(timeline, timeline.getFirstWindowIndex(this.shuffleModeEnabled), C.TIME_UNSET);
                    periodIndex = ((Integer) defaultPosition.first).intValue();
                    startPositionUs = ((Long) defaultPosition.second).longValue();
                    periodId = this.queue.resolveMediaPeriodIdForAds(periodIndex, startPositionUs);
                    playbackInfo = this.playbackInfo;
                    if (periodId.isAd()) {
                        j2 = 0;
                    } else {
                        j2 = startPositionUs;
                    }
                    this.playbackInfo = playbackInfo.fromNewPosition(periodId, j2, startPositionUs);
                }
            } else if (!oldTimeline.isEmpty()) {
                Object playingPeriodUid;
                MediaPeriodHolder periodHolder = this.queue.getFrontPeriod();
                int playingPeriodIndex = this.playbackInfo.periodId.periodIndex;
                long contentPositionUs = this.playbackInfo.contentPositionUs;
                if (periodHolder == null) {
                    playingPeriodUid = oldTimeline.getUidOfPeriod(playingPeriodIndex);
                } else {
                    playingPeriodUid = periodHolder.uid;
                }
                periodIndex = timeline.getIndexOfPeriod(playingPeriodUid);
                if (periodIndex == -1) {
                    int newPeriodIndex = resolveSubsequentPeriod(playingPeriodIndex, oldTimeline, timeline);
                    if (newPeriodIndex == -1) {
                        handleSourceInfoRefreshEndedPlayback();
                        return;
                    }
                    Timeline timeline2 = timeline;
                    defaultPosition = getPeriodPosition(timeline2, timeline.getPeriod(newPeriodIndex, this.period).windowIndex, C.TIME_UNSET);
                    newPeriodIndex = ((Integer) defaultPosition.first).intValue();
                    contentPositionUs = ((Long) defaultPosition.second).longValue();
                    periodId = this.queue.resolveMediaPeriodIdForAds(newPeriodIndex, contentPositionUs);
                    if (periodHolder != null) {
                        Object newPeriodUid = timeline.getUidOfPeriod(newPeriodIndex);
                        periodHolder.info = periodHolder.info.copyWithPeriodIndex(-1);
                        while (periodHolder.next != null) {
                            periodHolder = periodHolder.next;
                            if (periodHolder.uid.equals(newPeriodUid)) {
                                periodHolder.info = this.queue.getUpdatedMediaPeriodInfo(periodHolder.info, newPeriodIndex);
                            } else {
                                periodHolder.info = periodHolder.info.copyWithPeriodIndex(-1);
                            }
                        }
                    }
                    if (periodId.isAd()) {
                        j = 0;
                    } else {
                        j = contentPositionUs;
                    }
                    this.playbackInfo = this.playbackInfo.fromNewPosition(periodId, seekToPeriodPosition(periodId, j), contentPositionUs);
                    return;
                }
                if (periodIndex != playingPeriodIndex) {
                    this.playbackInfo = this.playbackInfo.copyWithPeriodIndex(periodIndex);
                }
                MediaPeriodId playingPeriodId = this.playbackInfo.periodId;
                if (playingPeriodId.isAd()) {
                    periodId = this.queue.resolveMediaPeriodIdForAds(periodIndex, contentPositionUs);
                    if (!periodId.equals(playingPeriodId)) {
                        if (periodId.isAd()) {
                            j = 0;
                        } else {
                            j = contentPositionUs;
                        }
                        this.playbackInfo = this.playbackInfo.fromNewPosition(periodId, seekToPeriodPosition(periodId, j), contentPositionUs);
                        return;
                    }
                }
                if (!this.queue.updateQueuedPeriods(playingPeriodId, this.rendererPositionUs)) {
                    seekToCurrentPosition(false);
                }
                updateLoadingMediaPeriodId();
            } else if (!timeline.isEmpty()) {
                defaultPosition = getPeriodPosition(timeline, timeline.getFirstWindowIndex(this.shuffleModeEnabled), C.TIME_UNSET);
                periodIndex = ((Integer) defaultPosition.first).intValue();
                startPositionUs = ((Long) defaultPosition.second).longValue();
                periodId = this.queue.resolveMediaPeriodIdForAds(periodIndex, startPositionUs);
                playbackInfo = this.playbackInfo;
                if (periodId.isAd()) {
                    j2 = 0;
                } else {
                    j2 = startPositionUs;
                }
                this.playbackInfo = playbackInfo.fromNewPosition(periodId, j2, startPositionUs);
            }
        }
    }

    private void handleSourceInfoRefreshEndedPlayback() {
        setState(4);
        resetInternal(false, true, false);
    }

    private int resolveSubsequentPeriod(int oldPeriodIndex, Timeline oldTimeline, Timeline newTimeline) {
        int newPeriodIndex = -1;
        int maxIterations = oldTimeline.getPeriodCount();
        for (int i = 0; i < maxIterations && newPeriodIndex == -1; i++) {
            oldPeriodIndex = oldTimeline.getNextPeriodIndex(oldPeriodIndex, this.period, this.window, this.repeatMode, this.shuffleModeEnabled);
            if (oldPeriodIndex == -1) {
                break;
            }
            newPeriodIndex = newTimeline.getIndexOfPeriod(oldTimeline.getUidOfPeriod(oldPeriodIndex));
        }
        return newPeriodIndex;
    }

    private Pair<Integer, Long> resolveSeekPosition(SeekPosition seekPosition, boolean trySubsequentPeriods) {
        Timeline timeline = this.playbackInfo.timeline;
        Timeline seekTimeline = seekPosition.timeline;
        if (timeline.isEmpty()) {
            return null;
        }
        if (seekTimeline.isEmpty()) {
            seekTimeline = timeline;
        }
        try {
            Pair<Integer, Long> periodPosition = seekTimeline.getPeriodPosition(this.window, this.period, seekPosition.windowIndex, seekPosition.windowPositionUs);
            if (timeline == seekTimeline) {
                return periodPosition;
            }
            int periodIndex = timeline.getIndexOfPeriod(seekTimeline.getUidOfPeriod(((Integer) periodPosition.first).intValue()));
            if (periodIndex != -1) {
                return Pair.create(Integer.valueOf(periodIndex), periodPosition.second);
            }
            if (trySubsequentPeriods) {
                periodIndex = resolveSubsequentPeriod(((Integer) periodPosition.first).intValue(), seekTimeline, timeline);
                if (periodIndex != -1) {
                    return getPeriodPosition(timeline, timeline.getPeriod(periodIndex, this.period).windowIndex, C.TIME_UNSET);
                }
            }
            return null;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalSeekPositionException(timeline, seekPosition.windowIndex, seekPosition.windowPositionUs);
        }
    }

    private Pair<Integer, Long> getPeriodPosition(Timeline timeline, int windowIndex, long windowPositionUs) {
        return timeline.getPeriodPosition(this.window, this.period, windowIndex, windowPositionUs);
    }

    private void updatePeriods() throws ExoPlaybackException, IOException {
        if (this.mediaSource != null) {
            if (this.pendingPrepareCount > 0) {
                this.mediaSource.maybeThrowSourceInfoRefreshError();
                return;
            }
            maybeUpdateLoadingPeriod();
            MediaPeriodHolder loadingPeriodHolder = this.queue.getLoadingPeriod();
            if (loadingPeriodHolder == null || loadingPeriodHolder.isFullyBuffered()) {
                setIsLoading(false);
            } else if (!this.playbackInfo.isLoading) {
                maybeContinueLoading();
            }
            if (this.queue.hasPlayingPeriod()) {
                MediaPeriodHolder playingPeriodHolder = this.queue.getPlayingPeriod();
                MediaPeriodHolder readingPeriodHolder = this.queue.getReadingPeriod();
                boolean advancedPlayingPeriod = false;
                while (this.playWhenReady && playingPeriodHolder != readingPeriodHolder && this.rendererPositionUs >= playingPeriodHolder.next.rendererPositionOffsetUs) {
                    if (advancedPlayingPeriod) {
                        maybeNotifyPlaybackInfoChanged();
                    }
                    int discontinuityReason = playingPeriodHolder.info.isLastInTimelinePeriod ? 0 : 3;
                    MediaPeriodHolder oldPlayingPeriodHolder = playingPeriodHolder;
                    playingPeriodHolder = this.queue.advancePlayingPeriod();
                    updatePlayingPeriodRenderers(oldPlayingPeriodHolder);
                    this.playbackInfo = this.playbackInfo.fromNewPosition(playingPeriodHolder.info.id, playingPeriodHolder.info.startPositionUs, playingPeriodHolder.info.contentPositionUs);
                    this.playbackInfoUpdate.setPositionDiscontinuity(discontinuityReason);
                    updatePlaybackPositions();
                    advancedPlayingPeriod = true;
                }
                int i;
                Renderer renderer;
                SampleStream sampleStream;
                if (readingPeriodHolder.info.isFinal) {
                    for (i = 0; i < this.renderers.length; i++) {
                        renderer = this.renderers[i];
                        sampleStream = readingPeriodHolder.sampleStreams[i];
                        if (sampleStream != null && renderer.getStream() == sampleStream && renderer.hasReadStreamToEnd()) {
                            renderer.setCurrentStreamFinal();
                        }
                    }
                } else if (readingPeriodHolder.next != null && readingPeriodHolder.next.prepared) {
                    i = 0;
                    while (i < this.renderers.length) {
                        renderer = this.renderers[i];
                        sampleStream = readingPeriodHolder.sampleStreams[i];
                        if (renderer.getStream() != sampleStream) {
                            return;
                        }
                        if (sampleStream == null || renderer.hasReadStreamToEnd()) {
                            i++;
                        } else {
                            return;
                        }
                    }
                    TrackSelectorResult oldTrackSelectorResult = readingPeriodHolder.trackSelectorResult;
                    readingPeriodHolder = this.queue.advanceReadingPeriod();
                    TrackSelectorResult newTrackSelectorResult = readingPeriodHolder.trackSelectorResult;
                    boolean initialDiscontinuity = readingPeriodHolder.mediaPeriod.readDiscontinuity() != C.TIME_UNSET;
                    for (i = 0; i < this.renderers.length; i++) {
                        renderer = this.renderers[i];
                        if (oldTrackSelectorResult.isRendererEnabled(i)) {
                            if (initialDiscontinuity) {
                                renderer.setCurrentStreamFinal();
                            } else if (!renderer.isCurrentStreamFinal()) {
                                TrackSelection newSelection = newTrackSelectorResult.selections.get(i);
                                boolean newRendererEnabled = newTrackSelectorResult.isRendererEnabled(i);
                                boolean isNoSampleRenderer = this.rendererCapabilities[i].getTrackType() == 5;
                                RendererConfiguration oldConfig = oldTrackSelectorResult.rendererConfigurations[i];
                                RendererConfiguration newConfig = newTrackSelectorResult.rendererConfigurations[i];
                                if (newRendererEnabled && newConfig.equals(oldConfig) && !isNoSampleRenderer) {
                                    renderer.replaceStream(getFormats(newSelection), readingPeriodHolder.sampleStreams[i], readingPeriodHolder.getRendererOffset());
                                } else {
                                    renderer.setCurrentStreamFinal();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void maybeUpdateLoadingPeriod() throws IOException {
        this.queue.reevaluateBuffer(this.rendererPositionUs);
        if (this.queue.shouldLoadNextMediaPeriod()) {
            MediaPeriodInfo info = this.queue.getNextMediaPeriodInfo(this.rendererPositionUs, this.playbackInfo);
            if (info == null) {
                this.mediaSource.maybeThrowSourceInfoRefreshError();
                return;
            }
            this.queue.enqueueNextMediaPeriod(this.rendererCapabilities, this.trackSelector, this.loadControl.getAllocator(), this.mediaSource, this.playbackInfo.timeline.getUidOfPeriod(info.id.periodIndex), info).prepare(this, info.startPositionUs);
            setIsLoading(true);
            updateLoadingMediaPeriodId();
        }
    }

    private void handlePeriodPrepared(MediaPeriod mediaPeriod) throws ExoPlaybackException {
        if (this.queue.isLoading(mediaPeriod)) {
            MediaPeriodHolder loadingPeriodHolder = this.queue.getLoadingPeriod();
            loadingPeriodHolder.handlePrepared(this.mediaClock.getPlaybackParameters().speed);
            updateLoadControlTrackSelection(loadingPeriodHolder.trackGroups, loadingPeriodHolder.trackSelectorResult);
            if (!this.queue.hasPlayingPeriod()) {
                resetRendererPosition(this.queue.advancePlayingPeriod().info.startPositionUs);
                updatePlayingPeriodRenderers(null);
            }
            maybeContinueLoading();
        }
    }

    private void handleContinueLoadingRequested(MediaPeriod mediaPeriod) {
        if (this.queue.isLoading(mediaPeriod)) {
            this.queue.reevaluateBuffer(this.rendererPositionUs);
            maybeContinueLoading();
        }
    }

    private void handlePlaybackParameters(PlaybackParameters playbackParameters) throws ExoPlaybackException {
        this.eventHandler.obtainMessage(1, playbackParameters).sendToTarget();
        updateTrackSelectionPlaybackSpeed(playbackParameters.speed);
        for (Renderer renderer : this.renderers) {
            if (renderer != null) {
                renderer.setOperatingRate(playbackParameters.speed);
            }
        }
    }

    private void maybeContinueLoading() {
        MediaPeriodHolder loadingPeriodHolder = this.queue.getLoadingPeriod();
        long nextLoadPositionUs = loadingPeriodHolder.getNextLoadPositionUs();
        if (nextLoadPositionUs == Long.MIN_VALUE) {
            setIsLoading(false);
            return;
        }
        boolean continueLoading = this.loadControl.shouldContinueLoading(nextLoadPositionUs - loadingPeriodHolder.toPeriodTime(this.rendererPositionUs), this.mediaClock.getPlaybackParameters().speed);
        setIsLoading(continueLoading);
        if (continueLoading) {
            loadingPeriodHolder.continueLoading(this.rendererPositionUs);
        }
    }

    private void updatePlayingPeriodRenderers(MediaPeriodHolder oldPlayingPeriodHolder) throws ExoPlaybackException {
        MediaPeriodHolder newPlayingPeriodHolder = this.queue.getPlayingPeriod();
        if (newPlayingPeriodHolder != null && oldPlayingPeriodHolder != newPlayingPeriodHolder) {
            int enabledRendererCount = 0;
            boolean[] rendererWasEnabledFlags = new boolean[this.renderers.length];
            int i = 0;
            while (i < this.renderers.length) {
                Renderer renderer = this.renderers[i];
                rendererWasEnabledFlags[i] = renderer.getState() != 0;
                if (newPlayingPeriodHolder.trackSelectorResult.isRendererEnabled(i)) {
                    enabledRendererCount++;
                }
                if (rendererWasEnabledFlags[i] && (!newPlayingPeriodHolder.trackSelectorResult.isRendererEnabled(i) || (renderer.isCurrentStreamFinal() && renderer.getStream() == oldPlayingPeriodHolder.sampleStreams[i]))) {
                    disableRenderer(renderer);
                }
                i++;
            }
            this.playbackInfo = this.playbackInfo.copyWithTrackInfo(newPlayingPeriodHolder.trackGroups, newPlayingPeriodHolder.trackSelectorResult);
            enableRenderers(rendererWasEnabledFlags, enabledRendererCount);
        }
    }

    private void enableRenderers(boolean[] rendererWasEnabledFlags, int totalEnabledRendererCount) throws ExoPlaybackException {
        this.enabledRenderers = new Renderer[totalEnabledRendererCount];
        int enabledRendererCount = 0;
        MediaPeriodHolder playingPeriodHolder = this.queue.getPlayingPeriod();
        for (int i = 0; i < this.renderers.length; i++) {
            if (playingPeriodHolder.trackSelectorResult.isRendererEnabled(i)) {
                int enabledRendererCount2 = enabledRendererCount + 1;
                enableRenderer(i, rendererWasEnabledFlags[i], enabledRendererCount);
                enabledRendererCount = enabledRendererCount2;
            }
        }
    }

    private void enableRenderer(int rendererIndex, boolean wasRendererEnabled, int enabledRendererIndex) throws ExoPlaybackException {
        boolean joining = true;
        MediaPeriodHolder playingPeriodHolder = this.queue.getPlayingPeriod();
        Renderer renderer = this.renderers[rendererIndex];
        this.enabledRenderers[enabledRendererIndex] = renderer;
        if (renderer.getState() == 0) {
            boolean playing;
            RendererConfiguration rendererConfiguration = playingPeriodHolder.trackSelectorResult.rendererConfigurations[rendererIndex];
            Format[] formats = getFormats(playingPeriodHolder.trackSelectorResult.selections.get(rendererIndex));
            if (this.playWhenReady && this.playbackInfo.playbackState == 3) {
                playing = true;
            } else {
                playing = false;
            }
            if (wasRendererEnabled || !playing) {
                joining = false;
            }
            renderer.enable(rendererConfiguration, formats, playingPeriodHolder.sampleStreams[rendererIndex], this.rendererPositionUs, joining, playingPeriodHolder.getRendererOffset());
            this.mediaClock.onRendererEnabled(renderer);
            if (playing) {
                renderer.start();
            }
        }
    }

    private boolean rendererWaitingForNextStream(Renderer renderer) {
        MediaPeriodHolder readingPeriodHolder = this.queue.getReadingPeriod();
        return readingPeriodHolder.next != null && readingPeriodHolder.next.prepared && renderer.hasReadStreamToEnd();
    }

    private void updateLoadingMediaPeriodId() {
        MediaPeriodHolder loadingMediaPeriodHolder = this.queue.getLoadingPeriod();
        this.playbackInfo = this.playbackInfo.copyWithLoadingMediaPeriodId(loadingMediaPeriodHolder == null ? this.playbackInfo.periodId : loadingMediaPeriodHolder.info.id);
    }

    private static Format[] getFormats(TrackSelection newSelection) {
        int length = newSelection != null ? newSelection.length() : 0;
        Format[] formats = new Format[length];
        for (int i = 0; i < length; i++) {
            formats[i] = newSelection.getFormat(i);
        }
        return formats;
    }
}
