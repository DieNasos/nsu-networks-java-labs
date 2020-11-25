package ru.nsu.ccfit.beloglazov.copiesfinder;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AddressMap {
    private final HashMap<SocketAddress, Long> map;
    private int timeoutMs;

    public AddressMap(int timeoutMs) {
        // initializing members
        map = new HashMap<>();
        this.timeoutMs = timeoutMs;
    }

    public void add(SocketAddress address, long time) {
        if (map.containsKey(address)) {
            // if key (address) already exists in map =>
            // => replacing value (time) with new one
            map.replace(address, time);
            return;
        }

        // else => creating new pair in map
        map.put(address, time);
    }

    public void update() {
        // checking 'alive' group-mates ==
        // == ones with bearable timeouts

        Iterator<Map.Entry<SocketAddress, Long>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            // going through all map
            Map.Entry<SocketAddress, Long> entry = iterator.next();

            if (System.currentTimeMillis() - entry.getValue() > timeoutMs) {
                // this mate's timeout is unbearable => removing him
                iterator.remove();
            }
        }
    }

    public void printAddresses() {
        // printing current group-mates' addresses list
        update();
        for (SocketAddress socketAddress : map.keySet()) {
            System.out.println(socketAddress);
        }
        System.out.println("----------");
    }
}