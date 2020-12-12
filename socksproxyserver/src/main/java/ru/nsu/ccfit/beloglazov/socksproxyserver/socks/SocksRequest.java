package ru.nsu.ccfit.beloglazov.socksproxyserver.socks;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class SocksRequest {
    private String domainName;
    private byte parseError = 0x00;
    private byte version;
    private byte command;
    private byte addressType;
    private byte[] ip4Address = new byte[4];
    private short targetPort;

    public String getDomainName() { return domainName; }
    public byte getParseError() { return parseError; }
    public byte getVersion() { return version; }
    public byte getCommand() { return command; }
    public byte getAddressType() { return addressType; }
    public byte[] getIp4Address() { return ip4Address; }
    public short getTargetPort() { return targetPort; }

    public InetSocketAddress getAddress() throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getByAddress(ip4Address), targetPort);
    }

    public void setDomainName(String domainName) { this.domainName = domainName; }
    public void setParseError(byte parseError) { this.parseError = parseError; }
    public void setVersion(byte version) { this.version = version; }
    public void setCommand(byte command) { this.command = command; }
    public void setAddressType(byte addressType) { this.addressType = addressType; }
    public void setTargetPort(short targetPort) { this.targetPort = targetPort; }
}