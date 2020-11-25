package ru.nsu.ccfit.beloglazov.tcpdeliveryclient;

public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("ERROR :: NOT ENOUGH PARAMETERS");
            System.exit(0);
        }
        TCPClient client = new TCPClient(args[0], args[1], Integer.parseInt(args[2]));
        client.sendFile();
    }
}