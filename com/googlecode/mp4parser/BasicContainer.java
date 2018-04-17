package com.googlecode.mp4parser;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.util.CastUtils;
import com.googlecode.mp4parser.util.LazyList;
import com.googlecode.mp4parser.util.Logger;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class BasicContainer implements Container, Closeable, Iterator<Box> {
    private static final Box EOF = new AnonymousClass1("eof ");
    private static Logger LOG = Logger.getLogger(BasicContainer.class);
    protected BoxParser boxParser;
    private List<Box> boxes = new ArrayList();
    protected DataSource dataSource;
    long endPosition = 0;
    Box lookahead = null;
    long parsePosition = 0;
    long startPosition = 0;

    class AnonymousClass1 extends AbstractBox {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        protected long getContentSize() {
            return 0;
        }

        protected void getContent(ByteBuffer byteBuffer) {
        }

        protected void _parseDetails(ByteBuffer content) {
        }
    }

    public List<Box> getBoxes() {
        if (this.dataSource == null || this.lookahead == EOF) {
            return this.boxes;
        }
        return new LazyList(this.boxes, this);
    }

    public void setBoxes(List<Box> boxes) {
        this.boxes = new ArrayList(boxes);
        this.lookahead = EOF;
        this.dataSource = null;
    }

    protected long getContainerSize() {
        long contentSize = 0;
        int i = 0;
        while (i < getBoxes().size()) {
            i++;
            contentSize += ((Box) this.boxes.get(i)).getSize();
        }
        return contentSize;
    }

    public <T extends Box> List<T> getBoxes(Class<T> clazz) {
        List<T> boxesToBeReturned = null;
        T oneBox = null;
        List<Box> boxes = getBoxes();
        for (int i = 0; i < boxes.size(); i++) {
            T boxe = (Box) boxes.get(i);
            if (clazz.isInstance(boxe)) {
                if (oneBox == null) {
                    oneBox = boxe;
                } else {
                    if (boxesToBeReturned == null) {
                        boxesToBeReturned = new ArrayList(2);
                        boxesToBeReturned.add(oneBox);
                    }
                    boxesToBeReturned.add(boxe);
                }
            }
        }
        if (boxesToBeReturned != null) {
            return boxesToBeReturned;
        }
        if (oneBox != null) {
            return Collections.singletonList(oneBox);
        }
        return Collections.emptyList();
    }

    public <T extends Box> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        List<T> boxesToBeReturned = new ArrayList(2);
        List<Box> boxes = getBoxes();
        for (int i = 0; i < boxes.size(); i++) {
            Box boxe = (Box) boxes.get(i);
            if (clazz.isInstance(boxe)) {
                boxesToBeReturned.add(boxe);
            }
            if (recursive && (boxe instanceof Container)) {
                boxesToBeReturned.addAll(((Container) boxe).getBoxes(clazz, recursive));
            }
        }
        return boxesToBeReturned;
    }

    public void addBox(Box box) {
        if (box != null) {
            this.boxes = new ArrayList(getBoxes());
            box.setParent(this);
            this.boxes.add(box);
        }
    }

    public void initContainer(DataSource dataSource, long containerSize, BoxParser boxParser) throws IOException {
        this.dataSource = dataSource;
        long position = dataSource.position();
        this.startPosition = position;
        this.parsePosition = position;
        dataSource.position(dataSource.position() + containerSize);
        this.endPosition = dataSource.position();
        this.boxParser = boxParser;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        if (this.lookahead == EOF) {
            return false;
        }
        if (this.lookahead != null) {
            return true;
        }
        try {
            this.lookahead = next();
            return true;
        } catch (NoSuchElementException e) {
            this.lookahead = EOF;
            return false;
        }
    }

    public Box next() {
        if (this.lookahead == null || this.lookahead == EOF) {
            if (this.dataSource != null) {
                if (this.parsePosition < this.endPosition) {
                    try {
                        Box b;
                        synchronized (this.dataSource) {
                            this.dataSource.position(this.parsePosition);
                            b = this.boxParser.parseBox(this.dataSource, this);
                            this.parsePosition = this.dataSource.position();
                        }
                        return b;
                    } catch (EOFException e) {
                        throw new NoSuchElementException();
                    } catch (IOException e2) {
                        throw new NoSuchElementException();
                    }
                }
            }
            this.lookahead = EOF;
            throw new NoSuchElementException();
        }
        Box b2 = this.lookahead;
        this.lookahead = null;
        return b2;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getSimpleName());
        buffer.append("[");
        for (int i = 0; i < this.boxes.size(); i++) {
            if (i > 0) {
                buffer.append(";");
            }
            buffer.append(((Box) this.boxes.get(i)).toString());
        }
        buffer.append("]");
        return buffer.toString();
    }

    public final void writeContainer(WritableByteChannel bb) throws IOException {
        for (Box box : getBoxes()) {
            box.getBox(bb);
        }
    }

    public ByteBuffer getByteBuffer(long rangeStart, long size) throws IOException {
        long j = size;
        BasicContainer basicContainer;
        if (this.dataSource != null) {
            ByteBuffer map;
            synchronized (basicContainer.dataSource) {
                try {
                    map = basicContainer.dataSource.map(basicContainer.startPosition + rangeStart, j);
                } catch (Throwable th) {
                    Throwable th2 = th;
                }
            }
            return map;
        }
        ByteBuffer out = ByteBuffer.allocate(CastUtils.l2i(size));
        long rangeEnd = rangeStart + j;
        long boxEnd = 0;
        Iterator it = basicContainer.boxes.iterator();
        while (it.hasNext()) {
            long rangeEnd2;
            Iterator it2;
            Box box = (Box) it.next();
            long boxStart = boxEnd;
            boxEnd = boxStart + box.getSize();
            if (boxEnd <= rangeStart || boxStart >= rangeEnd) {
                rangeEnd2 = rangeEnd;
                it2 = it;
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                WritableByteChannel wbc = Channels.newChannel(baos);
                box.getBox(wbc);
                wbc.close();
                if (boxStart < rangeStart || boxEnd > rangeEnd) {
                    if (boxStart >= rangeStart || boxEnd <= rangeEnd) {
                        it2 = it;
                        Box box2 = box;
                        if (boxStart >= rangeStart || boxEnd > rangeEnd) {
                            rangeEnd2 = rangeEnd;
                            Box box3 = box2;
                            if (boxStart >= rangeStart && boxEnd > rangeEnd2) {
                                out.put(baos.toByteArray(), 0, CastUtils.l2i(box3.getSize() - (boxEnd - rangeEnd2)));
                            }
                        } else {
                            rangeEnd2 = rangeEnd;
                            out.put(baos.toByteArray(), CastUtils.l2i(rangeStart - boxStart), CastUtils.l2i(box2.getSize() - (rangeStart - boxStart)));
                        }
                    } else {
                        it2 = it;
                        out.put(baos.toByteArray(), CastUtils.l2i(rangeStart - boxStart), CastUtils.l2i((box.getSize() - (rangeStart - boxStart)) - (boxEnd - rangeEnd)));
                        rangeEnd2 = rangeEnd;
                    }
                } else {
                    out.put(baos.toByteArray());
                    rangeEnd2 = rangeEnd;
                    it2 = it;
                }
            }
            it = it2;
            rangeEnd = rangeEnd2;
            basicContainer = this;
            j = size;
        }
        return (ByteBuffer) out.rewind();
    }

    public void close() throws IOException {
        this.dataSource.close();
    }
}
