package ru.nsu.ccfit.beloglazov.socksproxyserver.dns;

import java.nio.channels.SelectionKey;

public class DnsMapValue {
    private SelectionKey selectionKey;
    private short targetPort;

    public DnsMapValue(SelectionKey selectionKey, short targetPort) {
        this.selectionKey = selectionKey;
        this.targetPort = targetPort;
    }

    public SelectionKey getSelectionKey() { return selectionKey; }
    public short getTargetPort() { return targetPort; }
}