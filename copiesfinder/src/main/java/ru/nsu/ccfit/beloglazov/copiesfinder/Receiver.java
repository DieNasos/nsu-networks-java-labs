package ru.nsu.ccfit.beloglazov.copiesfinder;

import java.io.IOException;
import java.net.*;

public class Receiver extends Thread {
    private MulticastSocket socket;
    private AddressMap addressMap;

    public Receiver(String groupIP, int port, AddressMap addressMap) throws IOException {
        // initializing members

        InetAddress group = InetAddress.getByName(groupIP);

        socket = new MulticastSocket(port);
        socket.joinGroup(new InetSocketAddress(group, port), NetworkInterface.getByInetAddress(group));

        this.addressMap = addressMap;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            // while our thread is not interrupted doing some work

            // creating buffer for message symbols
            byte[] buffer = new byte[1024];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            try {
                // receiving packet with message
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // binding receive-time with sender's address in addressMap
            addressMap.add(packet.getSocketAddress(), System.currentTimeMillis());
        }
    }
}