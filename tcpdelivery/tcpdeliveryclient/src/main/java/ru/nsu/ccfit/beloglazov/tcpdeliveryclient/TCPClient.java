package ru.nsu.ccfit.beloglazov.tcpdeliveryclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class TCPClient {
    private Socket socket;
    private File file;
    private DigestInputStream inputStream;

    private void sendToSocket(byte[] data) throws IOException {
        socket.getOutputStream().write(data);
    }

    private void sendToSocket(byte[] data, int desiredLength) throws IOException {
        if (desiredLength == data.length) {
            socket.getOutputStream().write(data);
        } else if (desiredLength > data.length) {
            byte[] newData = new byte[desiredLength];
            System.arraycopy(data, 0, newData, desiredLength - data.length, data.length);
            socket.getOutputStream().write(newData);
        } else {
            byte[] newData = Arrays.copyOf(data, desiredLength);
            socket.getOutputStream().write(newData);
        }
    }

    public TCPClient(String path, String ip, Integer port) {
        try {
            file = new File(path);
            // the MD5 message-digest algorithm is a widely used hash function producing a 128-bit hash value
            inputStream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance("MD5"));
            socket = new Socket(ip, port);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void sendFile() {
        try {
            System.out.println("CLIENT :: STARTING WORK");

            System.out.println("CLIENT :: SENDING SIZE OF FILENAME...");
            byte[] fileName = file.getName().getBytes(StandardCharsets.UTF_8);
            sendToSocket(BigInteger.valueOf(fileName.length).toByteArray(), 2);

            System.out.println("CLIENT :: SENDING FILENAME...");
            sendToSocket(fileName);

            System.out.println("CLIENT :: SENDING SIZE OF FILE...");
            sendToSocket(BigInteger.valueOf(file.length()).toByteArray(), 8);

            System.out.println("CLIENT :: SENDING FILE...");
            byte[] buffer = new byte[4096];
            int readBytes;
            while ((readBytes = inputStream.read(buffer)) > 0) {
                sendToSocket(buffer, readBytes);
            }

            System.out.println("CLIENT :: SENDING DIGEST...");
            sendToSocket(inputStream.getMessageDigest().digest());

            System.out.println("CLIENT :: RECEIVING ANSWER FROM SERVER...");
            byte success = ((byte) socket.getInputStream().read());
            if (success == 1) {
                System.out.println("CLIENT :: FILE WAS UPLOADED SUCCESSFULLY");
            } else {
                System.out.println("CLIENT :: ERROR :: UPLOADED FILE IS INVALID");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            finish();
        }
    }

    public void finish() {
        try {
            System.out.println("CLIENT :: FINISHING WORK");
            inputStream.close();
            socket.getOutputStream().close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}