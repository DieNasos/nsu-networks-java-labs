package ru.nsu.ccfit.beloglazov.tcpdeliveryserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private ServerSocket serverSocket;
    private int numOfClients = 0;

    public TCPServer(int serverPort) {
        try {
            serverSocket = new ServerSocket(serverPort);
            // creating directory for uploaded files
            new File("uploads").mkdirs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        System.out.println("SERVER :: STARTED LISTENING");
        try {
            while (true){
                System.out.println("SERVER :: WAITING FOR NEW CLIENT...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("SERVER :: NEW CLIENT CONNECTED");
                Thread clientThread = new Thread(new ServerReadThread(clientSocket),"TCPClient " + ++numOfClients);
                System.out.println("SERVER :: STARTING NEW CLIENT THREAD...");
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("SERVER :: CLOSING SERVER SOCKET...");
                serverSocket.close();
            } catch (IOException e1) {
                System.out.println("SERVER :: ERROR :: COULD NOT CLOSE SERVER SOCKET");
                e1.printStackTrace();
            }
        }
    }
}