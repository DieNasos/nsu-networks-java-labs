package ru.nsu.ccfit.beloglazov.copiesfinder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Sender extends Thread {
    private MulticastSocket socket;
    private DatagramPacket packet;
    private int sleepTimeMs;

    public Sender(String groupIP, int port, String message, int sleepTimeMs) throws IOException {
        // initializing members
        socket = new MulticastSocket();
        packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(groupIP), port);
        this.sleepTimeMs = sleepTimeMs;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            // while our thread is not interrupted doing some work
            try {
                // sending packet with message to our group-mates
                socket.send(packet);
                // waiting sleepTimeMs milliseconds
                Thread.sleep(sleepTimeMs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}