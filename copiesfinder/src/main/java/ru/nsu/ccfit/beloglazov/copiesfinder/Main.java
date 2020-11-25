package ru.nsu.ccfit.beloglazov.copiesfinder;

public class Main {
    public static void main(String[] args) {
        try {
//            String groupIP = "228.5.6.7";
//            int port = 2048;
            String groupIP = (args[0]);
            int port = Integer.parseInt(args[1]);

            AddressMap addressMap = new AddressMap(5000);

            Sender s = new Sender(groupIP, port, "Who's here?", 1000);
            s.start();

            Receiver r = new Receiver(groupIP, port, addressMap);
            r.start();

            Checker c = new Checker(addressMap, 1000);
            c.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}