package ru.nsu.ccfit.beloglazov.copiesfinder;

public class Checker extends Thread {
    private AddressMap addressMap;
    private int sleepTimeMs;

    public Checker(AddressMap addressMap, int sleepTimeMs) {
        // initializing members
        this.addressMap = addressMap;
        this.sleepTimeMs = sleepTimeMs;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            // while our thread is not interrupted doing some work
            try {
                // waiting sleepTimeMs milliseconds
                Thread.sleep(sleepTimeMs);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            // checking address map
            addressMap.printAddresses();
        }
    }
}