package ru.nsu.ccfit.beloglazov.socksproxyserver.socks;

import java.nio.ByteBuffer;

public class SocksResponse {
    private static final int RESPONSE_LENGTH = 10;
    private byte version = 0x05;
    private byte reply = 0x00;
    private byte addressType = 0x01;
    // 4 bytes
    private byte[] boundIp4Address;
    private short boundPort;

    public void setReply(byte reply) { this.reply = reply; }
    public void setBoundIp4Address(byte[] boundIp4Address) { this.boundIp4Address = boundIp4Address; }
    public void setBoundPort(short boundPort) { this.boundPort = boundPort; }

    public ByteBuffer toByteBuffer(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(RESPONSE_LENGTH);
        byteBuffer
                .put(version)
                .put(reply)
                .put((byte) 0x00)
                .put(addressType)
                .put(boundIp4Address)
                .putShort(boundPort);
        byteBuffer.flip();
        return byteBuffer;
    }

    public ByteBuffer toByteBufferWithoutAddress(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(RESPONSE_LENGTH);
        byteBuffer
                .put(version)
                .put(reply)
                .put((byte) 0x00);
        byteBuffer.flip();
        return byteBuffer;
    }
}