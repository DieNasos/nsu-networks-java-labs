package ru.nsu.ccfit.beloglazov.chattree;

public class Main {
    public static void main(String[] args) {
        if (args.length == 3) {
            new ChatNode(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2])).startCommunication();
        } else if(args.length == 5) {
            new ChatNode(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4])).startCommunication();
        } else {
            System.out.println("ERROR :: EXPECTED PARAMS: (1) name, (2) packet loss, (3) port, [(4) ip to connect, (5) port to connect (optional)]");
        }
    }
}