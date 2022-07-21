package com.zavar.bootstrapper.util;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public final class ReadableByteChannelWrapper implements ReadableByteChannel {

    private final ReadableByteChannel readableByteChannel;
    private final long fileSize;
    private long readSoFar;
    private final DoubleProperty progressProperty = new SimpleDoubleProperty(0);

    public ReadableByteChannelWrapper(ReadableByteChannel readableByteChannel, long fileSize) {
        this.readableByteChannel = readableByteChannel;
        this.fileSize = fileSize;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int n;

        if ((n = readableByteChannel.read(dst)) > 0) {
            readSoFar += n;
            progressProperty.set(fileSize > 0 ? (double) readSoFar / (double) fileSize : 0);
        }

        return n;
    }

    public DoubleProperty getProgressProperty() {
        return progressProperty;
    }

    @Override
    public boolean isOpen() {
        return readableByteChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        readableByteChannel.close();
    }
}
