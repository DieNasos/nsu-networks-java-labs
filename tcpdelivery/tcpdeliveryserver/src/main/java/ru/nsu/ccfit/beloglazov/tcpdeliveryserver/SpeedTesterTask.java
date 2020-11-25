package ru.nsu.ccfit.beloglazov.tcpdeliveryserver;

import java.util.ArrayList;
import java.util.TimerTask;

public class SpeedTesterTask extends TimerTask {
    private int tickAmount;
    private ArrayList<Integer> bytesRead;
    private long totalBytes;

    public SpeedTesterTask(ArrayList<Integer> bytesRead) {
        super();
        tickAmount = 0;
        this.bytesRead = bytesRead;
    }

    @Override
    public void run() {
        tickAmount++;
        totalBytes = totalBytes + bytesRead.get(0);
        System.out.println(String.format("AVERAGE SPEED :: %.3f MB/s", totalBytes / tickAmount / 1024.0 / 1024.0 / 3));
        System.out.println(String.format("CURRENT SPEED :: %.3f MB/s", bytesRead.get(0) / 1024.0 / 1024.0 / 3));
        bytesRead.set(0, 0);
    }
}