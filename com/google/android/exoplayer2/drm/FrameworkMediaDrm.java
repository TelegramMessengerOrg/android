package com.google.android.exoplayer2.drm;

import android.annotation.TargetApi;
import android.media.DeniedByServerException;
import android.media.MediaCrypto;
import android.media.MediaCryptoException;
import android.media.MediaDrm;
import android.media.MediaDrm.KeyStatus;
import android.media.MediaDrmException;
import android.media.NotProvisionedException;
import android.media.UnsupportedSchemeException;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.ExoMediaDrm.DefaultKeyRequest;
import com.google.android.exoplayer2.drm.ExoMediaDrm.DefaultKeyStatus;
import com.google.android.exoplayer2.drm.ExoMediaDrm.DefaultProvisionRequest;
import com.google.android.exoplayer2.drm.ExoMediaDrm.KeyRequest;
import com.google.android.exoplayer2.drm.ExoMediaDrm.OnEventListener;
import com.google.android.exoplayer2.drm.ExoMediaDrm.OnKeyStatusChangeListener;
import com.google.android.exoplayer2.drm.ExoMediaDrm.ProvisionRequest;
import com.google.android.exoplayer2.extractor.mp4.PsshAtomUtil;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@TargetApi(23)
public final class FrameworkMediaDrm implements ExoMediaDrm<FrameworkMediaCrypto> {
    private static final String CENC_SCHEME_MIME_TYPE = "cenc";
    private final MediaDrm mediaDrm;
    private final UUID uuid;

    public static FrameworkMediaDrm newInstance(UUID uuid) throws UnsupportedDrmException {
        try {
            return new FrameworkMediaDrm(uuid);
        } catch (UnsupportedSchemeException e) {
            throw new UnsupportedDrmException(1, e);
        } catch (Exception e2) {
            throw new UnsupportedDrmException(2, e2);
        }
    }

    private FrameworkMediaDrm(UUID uuid) throws UnsupportedSchemeException {
        Assertions.checkNotNull(uuid);
        Assertions.checkArgument(!C.COMMON_PSSH_UUID.equals(uuid), "Use C.CLEARKEY_UUID instead");
        if (Util.SDK_INT < 27 && C.CLEARKEY_UUID.equals(uuid)) {
            uuid = C.COMMON_PSSH_UUID;
        }
        this.uuid = uuid;
        this.mediaDrm = new MediaDrm(uuid);
        if (C.WIDEVINE_UUID.equals(uuid) && needsForceL3Workaround()) {
            this.mediaDrm.setPropertyString("securityLevel", "L3");
        }
    }

    public void setOnEventListener(final OnEventListener<? super FrameworkMediaCrypto> listener) {
        this.mediaDrm.setOnEventListener(listener == null ? null : new MediaDrm.OnEventListener() {
            public void onEvent(MediaDrm md, byte[] sessionId, int event, int extra, byte[] data) {
                listener.onEvent(FrameworkMediaDrm.this, sessionId, event, extra, data);
            }
        });
    }

    public void setOnKeyStatusChangeListener(final OnKeyStatusChangeListener<? super FrameworkMediaCrypto> listener) {
        if (Util.SDK_INT < 23) {
            throw new UnsupportedOperationException();
        }
        MediaDrm.OnKeyStatusChangeListener onKeyStatusChangeListener;
        MediaDrm mediaDrm = this.mediaDrm;
        if (listener == null) {
            onKeyStatusChangeListener = null;
        } else {
            onKeyStatusChangeListener = new MediaDrm.OnKeyStatusChangeListener() {
                public void onKeyStatusChange(MediaDrm md, byte[] sessionId, List<KeyStatus> keyInfo, boolean hasNewUsableKey) {
                    List<ExoMediaDrm.KeyStatus> exoKeyInfo = new ArrayList();
                    for (KeyStatus keyStatus : keyInfo) {
                        exoKeyInfo.add(new DefaultKeyStatus(keyStatus.getStatusCode(), keyStatus.getKeyId()));
                    }
                    listener.onKeyStatusChange(FrameworkMediaDrm.this, sessionId, exoKeyInfo, hasNewUsableKey);
                }
            };
        }
        mediaDrm.setOnKeyStatusChangeListener(onKeyStatusChangeListener, null);
    }

    public byte[] openSession() throws MediaDrmException {
        return this.mediaDrm.openSession();
    }

    public void closeSession(byte[] sessionId) {
        this.mediaDrm.closeSession(sessionId);
    }

    public KeyRequest getKeyRequest(byte[] scope, byte[] init, String mimeType, int keyType, HashMap<String, String> optionalParameters) throws NotProvisionedException {
        if ((Util.SDK_INT < 21 && C.WIDEVINE_UUID.equals(this.uuid)) || (C.PLAYREADY_UUID.equals(this.uuid) && "Amazon".equals(Util.MANUFACTURER) && ("AFTB".equals(Util.MODEL) || "AFTS".equals(Util.MODEL) || "AFTM".equals(Util.MODEL)))) {
            byte[] psshData = PsshAtomUtil.parseSchemeSpecificData(init, this.uuid);
            if (psshData != null) {
                init = psshData;
            }
        }
        if (Util.SDK_INT < 26 && C.CLEARKEY_UUID.equals(this.uuid) && (MimeTypes.VIDEO_MP4.equals(mimeType) || MimeTypes.AUDIO_MP4.equals(mimeType))) {
            mimeType = "cenc";
        }
        MediaDrm.KeyRequest request = this.mediaDrm.getKeyRequest(scope, init, mimeType, keyType, optionalParameters);
        byte[] requestData = request.getData();
        if (C.CLEARKEY_UUID.equals(this.uuid)) {
            requestData = ClearKeyUtil.adjustRequestData(requestData);
        }
        return new DefaultKeyRequest(requestData, request.getDefaultUrl());
    }

    public byte[] provideKeyResponse(byte[] scope, byte[] response) throws NotProvisionedException, DeniedByServerException {
        if (C.CLEARKEY_UUID.equals(this.uuid)) {
            response = ClearKeyUtil.adjustResponseData(response);
        }
        return this.mediaDrm.provideKeyResponse(scope, response);
    }

    public ProvisionRequest getProvisionRequest() {
        MediaDrm.ProvisionRequest request = this.mediaDrm.getProvisionRequest();
        return new DefaultProvisionRequest(request.getData(), request.getDefaultUrl());
    }

    public void provideProvisionResponse(byte[] response) throws DeniedByServerException {
        this.mediaDrm.provideProvisionResponse(response);
    }

    public Map<String, String> queryKeyStatus(byte[] sessionId) {
        return this.mediaDrm.queryKeyStatus(sessionId);
    }

    public void release() {
        this.mediaDrm.release();
    }

    public void restoreKeys(byte[] sessionId, byte[] keySetId) {
        this.mediaDrm.restoreKeys(sessionId, keySetId);
    }

    public String getPropertyString(String propertyName) {
        return this.mediaDrm.getPropertyString(propertyName);
    }

    public byte[] getPropertyByteArray(String propertyName) {
        return this.mediaDrm.getPropertyByteArray(propertyName);
    }

    public void setPropertyString(String propertyName, String value) {
        this.mediaDrm.setPropertyString(propertyName, value);
    }

    public void setPropertyByteArray(String propertyName, byte[] value) {
        this.mediaDrm.setPropertyByteArray(propertyName, value);
    }

    public FrameworkMediaCrypto createMediaCrypto(byte[] initData) throws MediaCryptoException {
        boolean forceAllowInsecureDecoderComponents = Util.SDK_INT < 21 && C.WIDEVINE_UUID.equals(this.uuid) && "L3".equals(getPropertyString("securityLevel"));
        return new FrameworkMediaCrypto(new MediaCrypto(this.uuid, initData), forceAllowInsecureDecoderComponents);
    }

    private static boolean needsForceL3Workaround() {
        return "ASUS_Z00AD".equals(Util.MODEL);
    }
}
