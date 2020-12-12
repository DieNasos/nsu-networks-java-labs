package ru.nsu.ccfit.beloglazov.socksproxyserver.models;

import java.nio.ByteBuffer;

public class ObservableByteBuffer {
    private ByteBuffer byteBuffer;
    private BufferListener bufferListener;
    private boolean isShutdown = false;

    public interface BufferListener {
        void onUpdate();
    }

    public ObservableByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public ByteBuffer getByteBuffer() { return byteBuffer; }

    public void notifyListener(){
        bufferListener.onUpdate();
    }

    public void registerBufferListener(ObservableByteBuffer.BufferListener bufferListener){
        this.bufferListener = bufferListener;
    }

    public void shutdown() {
        isShutdown = true;
    }

    public boolean isReadyToClose(){
        return byteBuffer.remaining() == 0 && isShutdown;
    }
}