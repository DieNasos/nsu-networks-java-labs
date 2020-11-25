package ru.nsu.ccfit.beloglazov.tcpdeliveryserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;

public class ServerReadThread implements Runnable {
    private Socket clientSocket;
    private InputStream clientInputStream;
    private DigestOutputStream outputStream;
    private String threadName;

    public ServerReadThread(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            clientInputStream = clientSocket.getInputStream();
            threadName = Thread.currentThread().getName();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println(threadName + " :: STARTED DOWNLOADING FILE");
        Timer timer = new Timer();

        try {
            System.out.println(threadName + " :: RECEIVING SIZE OF FILENAME...");
            byte[] fileNameSize = new byte[2];
            clientInputStream.read(fileNameSize, 0, 2);
            System.out.println(threadName + " :: RECEIVED SIZE OF FILENAME: " + fileNameSize);

            System.out.println(threadName + " :: RECEIVING FILENAME...");
            byte[] fileName = new byte[new BigInteger(fileNameSize).intValue()];
            clientInputStream.read(fileName, 0, fileName.length);
            System.out.println(threadName + " :: RECEIVED FILENAME: " + fileName);

            System.out.println(threadName + " :: RECEIVING SIZE OF FILE...");
            byte[] fileSize = new byte[8];
            clientInputStream.read(fileSize, 0, 8);
            System.out.println(threadName + " :: RECEIVED SIZE OF FILENAME: " + fileSize);

            System.out.println(threadName + " :: RECEIVING FILE...");

            int bytesRead;
            long bytesRemain = new BigInteger(fileSize).longValue();
            byte[] fileBuffer = new byte [4096];
            File file = new File("uploads/" + new String(fileName).trim());
            file.createNewFile();
            outputStream = new DigestOutputStream(new FileOutputStream(file), MessageDigest.getInstance("MD5"));
            ArrayList<Integer> bytesReadForTimer = new ArrayList<>();
            bytesReadForTimer.add(0);

            // task is scheduled to run after the period in milliseconds passed in the delay parameter
            // then the task is repeated periodically - every period of milliseconds
            timer.schedule(new SpeedTesterTask(bytesReadForTimer), 0, 3000);

            do {
                bytesRead = clientInputStream.read(fileBuffer, 0, bytesRemain < 4096L ? Math.toIntExact(bytesRemain) : 4096);
                bytesRemain -= bytesRead;
                bytesReadForTimer.set(0, bytesReadForTimer.get(0) + bytesRead);
                outputStream.write(fileBuffer, 0, bytesRead);
            } while (bytesRemain > 0);

            // the MD5 message-digest algorithm is a widely used hash function producing a 128-bit hash value
            byte[] md5digest = new byte[16];
            clientInputStream.read(md5digest, 0, 16);

            if (Arrays.equals(md5digest, outputStream.getMessageDigest().digest())) {
                System.out.println(threadName + " :: FILE DOWNLOADED SUCCESSFULLY");
            } else {
                System.out.println(threadName + " :: ERROR :: DOWNLOADED FILE IS INVALID");
            }

            clientSocket.getOutputStream().write(1);
            clientSocket.getOutputStream().flush();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println(threadName + " :: GOT SOME ERROR, CLOSING CONNECTION");
        } finally {
            try {
                System.out.println(threadName + " :: CLOSING CONNECTION");
                timer.cancel();
                clientSocket.getOutputStream().close();
                clientSocket.close();
                clientInputStream.close();
                outputStream.close();
                System.out.println(threadName + " :: CONNECTION CLOSED SUCCESSFULLY");
            } catch (IOException e1) {
                System.out.println(threadName + " :: ERROR :: COULD NOT CLOSE CONNECTION");
                e1.printStackTrace();
            }
        }
    }
}