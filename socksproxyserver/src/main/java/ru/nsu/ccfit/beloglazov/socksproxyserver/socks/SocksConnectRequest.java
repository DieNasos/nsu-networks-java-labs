package ru.nsu.ccfit.beloglazov.socksproxyserver.socks;

public class SocksConnectRequest {
    private byte version;
    private byte[] methods;

    public byte getVersion() { return version; }
    public byte[] getMethods() { return methods; }

    public void setVersion(byte version) { this.version = version; }

    public void setNumOfMethods(byte numOfMethods) {
        this.methods = new byte[numOfMethods];
    }
}