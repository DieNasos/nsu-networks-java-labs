package ru.nsu.ccfit.beloglazov.socksproxyserver.handlers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import ru.nsu.ccfit.beloglazov.socksproxyserver.models.Connection;

public abstract class SocksHandler extends Handler{
    public SocksHandler(Connection connection) {
        super(connection);
    }

    @Override
    public int read(SelectionKey selectionKey) throws IOException {
        int readCount = super.read(selectionKey);
        if (readCount < 0) {
            throw new IOException("SOCKET CLOSED DURING SOCKS5 HANDSHAKE");
        }
        return readCount;
    }
}