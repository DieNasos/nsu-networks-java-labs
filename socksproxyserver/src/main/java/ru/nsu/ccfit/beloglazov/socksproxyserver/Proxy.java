package ru.nsu.ccfit.beloglazov.socksproxyserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import ru.nsu.ccfit.beloglazov.socksproxyserver.dns.DnsService;
import ru.nsu.ccfit.beloglazov.socksproxyserver.handlers.*;
import ru.nsu.ccfit.beloglazov.socksproxyserver.models.Connection;

public class Proxy {
    private final int proxyPort;

    /* PUBLIC METHODS */

    public Proxy(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void start() {
        try (Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            // initializing server socket channel
            // making server socket channel non-blocking
            serverSocketChannel.configureBlocking(false);
            // binding server socket channel with address
            serverSocketChannel.bind(new InetSocketAddress(proxyPort));
            // registering server socket channel in selector
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, new AcceptHandler(serverSocketChannel));

            // creating and initializing datagram channel
            DatagramChannel datagramChannel = DatagramChannel.open();
            // making datagram channel non-blocking
            datagramChannel.configureBlocking(false);

            // creating and initializing DNS service
            DnsService dnsService = new DnsService();
            dnsService.setDatagramChannel(datagramChannel);
            dnsService.registerSelector(selector);

            // running our proxy
            run(selector);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* PRIVATE METHODS */

    private void run(Selector selector) throws IOException {
        while (true) {
            // .select() is blocking until at least one channel is ready for operation
            selector.select();
            // getting keys of ready channels
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            // getting iterator of keys-set
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                // getting next key from set
                SelectionKey readyKey = iterator.next();

                try {
                    // removing current key from set
                    iterator.remove();

                    if (readyKey.isValid()) {
                        // handling key
                        handleSelectionKey(readyKey);
                    }
                } catch (IOException exception) {
                    closeConnection(readyKey);
                }
            }
        }
    }

    private void handleSelectionKey(SelectionKey selectionKey) throws IOException {
        // getting handler of key
        Handler handler = (Handler) selectionKey.attachment();

        if (selectionKey.isWritable()) {
            // writing...
            handler.write(selectionKey);
        }

        // not only writable
        if (selectionKey.isValid() && selectionKey.readyOps() != SelectionKey.OP_WRITE) {
            // handling...
            handler.handle(selectionKey);
        }
    }

    private void closeConnection(SelectionKey selectionKey) throws IOException {
        // closing connection

        Handler handler = (Handler) selectionKey.attachment();
        Connection connection = handler.getConnection();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        try {
            System.out.println("CLOSING SOCKET " + socketChannel.getRemoteAddress());
            socketChannel.close();
            connection.closeAssociate();
        } catch (ClosedChannelException e){
            e.printStackTrace();
        }
    }
}