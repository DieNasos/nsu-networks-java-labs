package ru.nsu.ccfit.beloglazov.socksproxyserver.handlers;

import java.io.IOException;
import java.nio.channels.*;
import ru.nsu.ccfit.beloglazov.socksproxyserver.models.Connection;

public class AcceptHandler extends Handler {
    private ServerSocketChannel serverSocketChannel;

    public AcceptHandler(ServerSocketChannel serverSocketChannel) {
        super(null);
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        Connection connection = new Connection(getBuffLength());
        SocksConnectHandler connectHandler = new SocksConnectHandler(connection);
        SelectionKey key = socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, connectHandler);
        connection.registerBufferListener(() -> key.interestOpsOr(SelectionKey.OP_WRITE));
        System.out.println("New connection: " + socketChannel.getRemoteAddress());
    }
}