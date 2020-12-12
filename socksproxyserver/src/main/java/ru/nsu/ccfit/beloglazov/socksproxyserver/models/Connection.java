package ru.nsu.ccfit.beloglazov.socksproxyserver.models;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Connection {
    // user reads from
    private ObservableByteBuffer inputBuffer;
    // user writes to
    private ObservableByteBuffer outputBuffer;
    private SocketChannel associate;
    private int writeStartPosition = 0;

    public Connection(ObservableByteBuffer inputBuffer, ObservableByteBuffer outputBuffer) {
        this.inputBuffer = inputBuffer;
        this.outputBuffer = outputBuffer;
    }

    public Connection(int buffLength) {
        this.inputBuffer = new ObservableByteBuffer(ByteBuffer.allocate(buffLength));
        this.outputBuffer = new ObservableByteBuffer(ByteBuffer.allocate(buffLength));
    }

    public ByteBuffer getInputBuffer() { return inputBuffer.getByteBuffer(); }
    public ObservableByteBuffer getObservableInputBuffer() { return inputBuffer; }
    public ByteBuffer getOutputBuffer() { return outputBuffer.getByteBuffer(); }
    public ObservableByteBuffer getObservableOutputBuffer() { return outputBuffer; }

    public void setAssociate(SocketChannel associate) { this.associate = associate; }

    public void registerBufferListener(ObservableByteBuffer.BufferListener bufferListener){
        inputBuffer.registerBufferListener(bufferListener);
    }

    public void notifyBufferListener(){ outputBuffer.notifyListener(); }

    public void closeAssociate() throws IOException {
        if (associate != null) {
            System.out.println("SOCKET CLOSED: " + associate.getRemoteAddress());
            associate.close();
        }
    }

    public void shutdown() { outputBuffer.shutdown(); }

    public boolean isAssociateShutDown() {
        return inputBuffer.isReadyToClose();
    }

    public void prepareToWrite() {
        ByteBuffer inputBuffer = getInputBuffer();
        inputBuffer.flip();
        inputBuffer.position(writeStartPosition);
    }

    public boolean isReadyToClose() {
        return inputBuffer.isReadyToClose() && outputBuffer.isReadyToClose();
    }

    public void resetWriteStartPosition() { writeStartPosition = 0; }

    public void setWriteStartPosition() {
        ByteBuffer inputBuffer = getInputBuffer();
        writeStartPosition = inputBuffer.position();
        int newStartPosition = inputBuffer.limit();
        inputBuffer.clear();
        inputBuffer.position(newStartPosition);
    }
}