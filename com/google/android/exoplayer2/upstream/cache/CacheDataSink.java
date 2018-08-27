package com.google.android.exoplayer2.upstream.cache;

import com.google.android.exoplayer2.upstream.DataSink;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.cache.Cache.CacheException;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.ReusableBufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class CacheDataSink implements DataSink {
    public static final int DEFAULT_BUFFER_SIZE = 20480;
    private final int bufferSize;
    private ReusableBufferedOutputStream bufferedOutputStream;
    private final Cache cache;
    private DataSpec dataSpec;
    private long dataSpecBytesWritten;
    private File file;
    private final long maxCacheFileSize;
    private OutputStream outputStream;
    private long outputStreamBytesWritten;
    private final boolean syncFileDescriptor;
    private FileOutputStream underlyingFileOutputStream;

    public static class CacheDataSinkException extends CacheException {
        public CacheDataSinkException(IOException cause) {
            super((Throwable) cause);
        }
    }

    public CacheDataSink(Cache cache, long maxCacheFileSize) {
        this(cache, maxCacheFileSize, DEFAULT_BUFFER_SIZE, true);
    }

    public CacheDataSink(Cache cache, long maxCacheFileSize, boolean syncFileDescriptor) {
        this(cache, maxCacheFileSize, DEFAULT_BUFFER_SIZE, syncFileDescriptor);
    }

    public CacheDataSink(Cache cache, long maxCacheFileSize, int bufferSize) {
        this(cache, maxCacheFileSize, bufferSize, true);
    }

    public CacheDataSink(Cache cache, long maxCacheFileSize, int bufferSize, boolean syncFileDescriptor) {
        this.cache = (Cache) Assertions.checkNotNull(cache);
        this.maxCacheFileSize = maxCacheFileSize;
        this.bufferSize = bufferSize;
        this.syncFileDescriptor = syncFileDescriptor;
    }

    public void open(DataSpec dataSpec) throws CacheDataSinkException {
        if (dataSpec.length != -1 || dataSpec.isFlagSet(2)) {
            this.dataSpec = dataSpec;
            this.dataSpecBytesWritten = 0;
            try {
                openNextOutputStream();
                return;
            } catch (IOException e) {
                throw new CacheDataSinkException(e);
            }
        }
        this.dataSpec = null;
    }

    public void write(byte[] buffer, int offset, int length) throws CacheDataSinkException {
        if (this.dataSpec != null) {
            int bytesWritten = 0;
            while (bytesWritten < length) {
                try {
                    if (this.outputStreamBytesWritten == this.maxCacheFileSize) {
                        closeCurrentOutputStream();
                        openNextOutputStream();
                    }
                    int bytesToWrite = (int) Math.min((long) (length - bytesWritten), this.maxCacheFileSize - this.outputStreamBytesWritten);
                    this.outputStream.write(buffer, offset + bytesWritten, bytesToWrite);
                    bytesWritten += bytesToWrite;
                    this.outputStreamBytesWritten += (long) bytesToWrite;
                    this.dataSpecBytesWritten += (long) bytesToWrite;
                } catch (IOException e) {
                    throw new CacheDataSinkException(e);
                }
            }
        }
    }

    public void close() throws CacheDataSinkException {
        if (this.dataSpec != null) {
            try {
                closeCurrentOutputStream();
            } catch (IOException e) {
                throw new CacheDataSinkException(e);
            }
        }
    }

    private void openNextOutputStream() throws IOException {
        long maxLength;
        if (this.dataSpec.length == -1) {
            maxLength = this.maxCacheFileSize;
        } else {
            maxLength = Math.min(this.dataSpec.length - this.dataSpecBytesWritten, this.maxCacheFileSize);
        }
        this.file = this.cache.startFile(this.dataSpec.key, this.dataSpec.absoluteStreamPosition + this.dataSpecBytesWritten, maxLength);
        this.underlyingFileOutputStream = new FileOutputStream(this.file);
        if (this.bufferSize > 0) {
            if (this.bufferedOutputStream == null) {
                this.bufferedOutputStream = new ReusableBufferedOutputStream(this.underlyingFileOutputStream, this.bufferSize);
            } else {
                this.bufferedOutputStream.reset(this.underlyingFileOutputStream);
            }
            this.outputStream = this.bufferedOutputStream;
        } else {
            this.outputStream = this.underlyingFileOutputStream;
        }
        this.outputStreamBytesWritten = 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void closeCurrentOutputStream() throws java.io.IOException {
        /*
        r5 = this;
        r4 = 0;
        r2 = r5.outputStream;
        if (r2 != 0) goto L_0x0006;
    L_0x0005:
        return;
    L_0x0006:
        r1 = 0;
        r2 = r5.outputStream;	 Catch:{ all -> 0x0031 }
        r2.flush();	 Catch:{ all -> 0x0031 }
        r2 = r5.syncFileDescriptor;	 Catch:{ all -> 0x0031 }
        if (r2 == 0) goto L_0x0019;
    L_0x0010:
        r2 = r5.underlyingFileOutputStream;	 Catch:{ all -> 0x0031 }
        r2 = r2.getFD();	 Catch:{ all -> 0x0031 }
        r2.sync();	 Catch:{ all -> 0x0031 }
    L_0x0019:
        r1 = 1;
        r2 = r5.outputStream;
        com.google.android.exoplayer2.util.Util.closeQuietly(r2);
        r5.outputStream = r4;
        r0 = r5.file;
        r5.file = r4;
        if (r1 == 0) goto L_0x002d;
    L_0x0027:
        r2 = r5.cache;
        r2.commitFile(r0);
        goto L_0x0005;
    L_0x002d:
        r0.delete();
        goto L_0x0005;
    L_0x0031:
        r2 = move-exception;
        r3 = r5.outputStream;
        com.google.android.exoplayer2.util.Util.closeQuietly(r3);
        r5.outputStream = r4;
        r0 = r5.file;
        r5.file = r4;
        if (r1 == 0) goto L_0x0045;
    L_0x003f:
        r3 = r5.cache;
        r3.commitFile(r0);
    L_0x0044:
        throw r2;
    L_0x0045:
        r0.delete();
        goto L_0x0044;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.exoplayer2.upstream.cache.CacheDataSink.closeCurrentOutputStream():void");
    }
}
