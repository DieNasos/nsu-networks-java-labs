package ru.nsu.ccfit.beloglazov.socksproxyserver.dns;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import ru.nsu.ccfit.beloglazov.socksproxyserver.handlers.*;
import ru.nsu.ccfit.beloglazov.socksproxyserver.models.FiniteTreeMap;
import ru.nsu.ccfit.beloglazov.socksproxyserver.socks.SocksRequest;

public class DnsService {
    private static final byte HOST_UNREACHABLE_ERROR = 0x04;
    private static final int DNS_SERVER_PORT = 53;
    private static final int BUFFER_SIZE = 1024;
    private static final int CACHE_SIZE = 256;
    private int messageID = 0;
    private DatagramChannel datagramChannel;
    private InetSocketAddress dnsServerAddress;
    private Handler dnsResponseHandler;
    // map: key - dns message id
    private Map<Integer, DnsMapValue> unresolvedNames = new HashMap<>();
    // tree: key - hostname, value - ip
    private FiniteTreeMap<String, String> dnsCache = new FiniteTreeMap<>(CACHE_SIZE);

    /* PUBLIC METHODS */

    public DnsService() {
        dnsServerAddress = new InetSocketAddress(ResolverConfig.getCurrentConfig().server(), DNS_SERVER_PORT);
    }

    public void setDatagramChannel(DatagramChannel channel) {
        datagramChannel = channel;
        initResponseHandler();
    }

    public void registerSelector(Selector selector) throws ClosedChannelException {
        datagramChannel.register(selector, SelectionKey.OP_READ, dnsResponseHandler);
    }

    public void resolveName(SocksRequest request, SelectionKey selectionKey) throws IOException {
        try {
            String name = request.getDomainName();
            String cachedAddress = dnsCache.get(name + ".");

            if (cachedAddress != null) {
                connectToTarget(cachedAddress, selectionKey, request.getTargetPort());
                return;
            }

            System.out.println("NEW DOMAIN NAME TO RESOLVE: " + request.getDomainName());
            DnsMapValue mapValue = new DnsMapValue(selectionKey, request.getTargetPort());
            Message query = getQuery(name);
            byte[] queryBytes = query.toWire();
            unresolvedNames.put(query.getHeader().getID(), mapValue);
            datagramChannel.send(ByteBuffer.wrap(queryBytes), dnsServerAddress);
        } catch (TextParseException e){
            SocksRequestHandler.onError(selectionKey, HOST_UNREACHABLE_ERROR);
            e.printStackTrace();
        }
    }

    /* PRIVATE METHODS */

    private void initResponseHandler() {
        dnsResponseHandler = new Handler(null) {
            @Override
            public void handle(SelectionKey selectionKey) throws IOException {
                ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);

                if (datagramChannel.receive(byteBuffer) == null) {
                    return;
                }

                Message response = new Message(byteBuffer.flip().array());
                Record[] answers = response.getSectionArray(Section.ANSWER);
                int responseID = response.getHeader().getID();
                DnsMapValue unresolvedName = unresolvedNames.get(response.getHeader().getID());

                if (answers.length == 0) {
                    SocksRequestHandler.onError(unresolvedName.getSelectionKey(), HOST_UNREACHABLE_ERROR);
                    return;
                }

                String hostname = response.getQuestion().getName().toString();
                System.out.println(hostname + " RESOLVED");
                String address = answers[0].rdataToString();
                dnsCache.put(hostname, address);
                connectToTarget(address, unresolvedName.getSelectionKey(), unresolvedName.getTargetPort());
                unresolvedNames.remove(responseID);
            }
        };
    }

    private void connectToTarget(String address, SelectionKey selectionKey, int port) throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);
        ConnectHandler.connectToTarget(selectionKey, socketAddress);
    }

    private Message getQuery(String domainName) throws TextParseException {
        Header header = new Header(messageID++);
        header.setFlag(Flags.RD);
        header.setOpcode(0);

        Message message = new Message();
        message.setHeader(header);

        Record record = Record.newRecord(new Name(domainName + "."), Type.A, DClass.IN);
        message.addRecord(record, Section.QUESTION);

        return message;
    }
}