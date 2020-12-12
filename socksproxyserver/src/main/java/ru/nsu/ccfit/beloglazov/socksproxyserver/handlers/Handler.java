package ru.nsu.ccfit.beloglazov.socksproxyserver.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import ru.nsu.ccfit.beloglazov.socksproxyserver.models.Connection;

public abstract class Handler {
    private static final int BUFF_LENGTH = 65536;
    private static final int NO_REMAINING = 0;
    private Connection connection;

    public Handler(Connection connection) {
        this.connection = connection;
    }

    public static int getBuffLength() {
        return BUFF_LENGTH;
    }

    public Connection getConnection() {
        return connection;
    }

    // abstract method!
    abstract public void handle(SelectionKey selectionKey) throws IOException;

    public int read(SelectionKey selectionKey) throws IOException {
        Handler handler = (Handler) selectionKey.attachment();
        SocketChannel socket = (SocketChannel) selectionKey.channel();
        Connection connection = handler.getConnection();
        ByteBuffer outputBuffer = connection.getOutputBuffer();

        if (!isReadyToRead(outputBuffer, connection)) {
            return 0;
        }

        int readCount = socket.read(outputBuffer);

        if (readCount <= 0) {
            connection.shutdown();
            selectionKey.interestOps(0);
            checkConnectionClose(socket);
        }

        return readCount;
    }

    public int write(SelectionKey selectionKey) throws IOException {
        ByteBuffer inputBuffer = connection.getInputBuffer();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        connection.prepareToWrite();
        socketChannel.write(inputBuffer);
        int remaining = inputBuffer.remaining();

        if (remaining == NO_REMAINING) {
            selectionKey.interestOps(SelectionKey.OP_READ);
            checkAssociate(socketChannel, inputBuffer);
        } else {
            connection.setWriteStartPosition();
        }

        return remaining;
    }

    private boolean isReadyToRead(ByteBuffer buffer, Connection connection){
        return buffer.position() < BUFF_LENGTH / 2 || connection.isAssociateShutDown();
    }

    private void checkConnectionClose(SocketChannel socketChannel) throws IOException {
        if (connection.isReadyToClose()) {
            System.out.println("SOCKET CLOSED: " + socketChannel.getRemoteAddress());
            socketChannel.close();
            connection.closeAssociate();
        }
    }

    private void checkAssociate(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        if (connection.isAssociateShutDown()) {
            socketChannel.shutdownOutput();
            return;
        }

        buffer.clear();
        connection.resetWriteStartPosition();
    }
}