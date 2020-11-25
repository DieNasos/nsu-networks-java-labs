package ru.nsu.ccfit.beloglazov.chattree.util;

import java.net.DatagramPacket;

public class PendingMessageRecord {
    private Long firstTimeStamp;
    private Long lastTimeStamp;
    private DatagramPacket sendingBytes;

    public PendingMessageRecord(Long firstTimeStamp, DatagramPacket sendingBytes) {
        this.firstTimeStamp = firstTimeStamp;
        this.lastTimeStamp = firstTimeStamp;
        this.sendingBytes = sendingBytes;
    }

    public PendingMessageRecord(Long firstTimeStamp) {
        this.firstTimeStamp = firstTimeStamp;
        this.lastTimeStamp = firstTimeStamp;
    }

    public PendingMessageRecord(DatagramPacket sendingBytes) { this.sendingBytes = sendingBytes; }

    public Long getFirstTimeStamp() { return firstTimeStamp; }
    public Long getLastTimeStamp() { return lastTimeStamp; }
    public DatagramPacket getPacket() { return sendingBytes; }

    public void setLastTimeStamp(Long lastTimeStamp) { this.lastTimeStamp = lastTimeStamp; }
}