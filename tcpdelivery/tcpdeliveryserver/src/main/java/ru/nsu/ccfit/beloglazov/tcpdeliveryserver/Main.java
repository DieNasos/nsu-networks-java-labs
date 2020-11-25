package ru.nsu.ccfit.beloglazov.tcpdeliveryserver;

public class Main {
    public static void main(String[] args){
        if (args.length < 1) {
            System.out.println("ERROR :: NOT ENOUGH PARAMETERS");
            System.exit(0);
        }
        TCPServer server = new TCPServer(Integer.parseInt(args[0]));
        server.listen();
    }
}