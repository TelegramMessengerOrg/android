package com.google.android.exoplayer2.ext.flac;

import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.decoder.SimpleDecoder;
import com.google.android.exoplayer2.decoder.SimpleOutputBuffer;
import com.google.android.exoplayer2.ext.flac.FlacDecoderJni.FlacFrameDecodeException;
import com.google.android.exoplayer2.util.FlacStreamInfo;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

final class FlacDecoder extends SimpleDecoder<DecoderInputBuffer, SimpleOutputBuffer, FlacDecoderException> {
    private final FlacDecoderJni decoderJni;
    private final int maxOutputBufferSize;

    public FlacDecoder(int numInputBuffers, int numOutputBuffers, int maxInputBufferSize, List<byte[]> initializationData) throws FlacDecoderException {
        Exception e;
        super(new DecoderInputBuffer[numInputBuffers], new SimpleOutputBuffer[numOutputBuffers]);
        if (initializationData.size() != 1) {
            throw new FlacDecoderException("Initialization data must be of length 1");
        }
        this.decoderJni = new FlacDecoderJni();
        this.decoderJni.setData(ByteBuffer.wrap((byte[]) initializationData.get(0)));
        try {
            FlacStreamInfo streamInfo = this.decoderJni.decodeMetadata();
            if (streamInfo == null) {
                throw new FlacDecoderException("Metadata decoding failed");
            }
            setInitialInputBufferSize(maxInputBufferSize != -1 ? maxInputBufferSize : streamInfo.maxFrameSize);
            this.maxOutputBufferSize = streamInfo.maxDecodedFrameSize();
        } catch (IOException e2) {
            e = e2;
            throw new IllegalStateException(e);
        } catch (InterruptedException e3) {
            e = e3;
            throw new IllegalStateException(e);
        }
    }

    public String getName() {
        return "libflac";
    }

    protected DecoderInputBuffer createInputBuffer() {
        return new DecoderInputBuffer(1);
    }

    protected SimpleOutputBuffer createOutputBuffer() {
        return new SimpleOutputBuffer(this);
    }

    protected FlacDecoderException createUnexpectedDecodeException(Throwable error) {
        return new FlacDecoderException("Unexpected decode error", error);
    }

    protected FlacDecoderException decode(DecoderInputBuffer inputBuffer, SimpleOutputBuffer outputBuffer, boolean reset) {
        Exception e;
        if (reset) {
            this.decoderJni.flush();
        }
        this.decoderJni.setData(inputBuffer.data);
        try {
            this.decoderJni.decodeSample(outputBuffer.init(inputBuffer.timeUs, this.maxOutputBufferSize));
            return null;
        } catch (FlacFrameDecodeException e2) {
            return new FlacDecoderException("Frame decoding failed", e2);
        } catch (IOException e3) {
            e = e3;
            throw new IllegalStateException(e);
        } catch (InterruptedException e4) {
            e = e4;
            throw new IllegalStateException(e);
        }
    }

    public void release() {
        super.release();
        this.decoderJni.release();
    }
}
