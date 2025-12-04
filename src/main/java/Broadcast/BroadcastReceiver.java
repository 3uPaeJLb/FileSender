package Broadcast;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class BroadcastReceiver {
    private List<String> runningServers = new ArrayList<>();
    private boolean running = true;
    private CopyChecker copyChecker;
    private final int port;

    public BroadcastReceiver(int port) {
        this.port = port;
        this.copyChecker = new CopyChecker();
    }

    public void receiveBroadcast() throws IOException {
        DatagramSocket socket = new DatagramSocket(port);
        socket.setBroadcast(true);
        socket.setSoTimeout(1000);

        System.out.println("=== Broadcast Receiver Started ===");
        System.out.println("Listening on port: " + port);
        System.out.println("Local IP: " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("================================");

        byte[] buffer = new byte[1024];
        int timeoutCount = 0;

        while (running) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(packet);
                timeoutCount = 0;

                String data = new String(
                        packet.getData(),
                        0,
                        packet.getLength(),
                        "UTF-8"
                );

                System.out.println("=== Broadcast Received ===");
                System.out.println("From: " + packet.getAddress().getHostAddress());
                System.out.println("Data: " + data);
                System.out.println("==========================");

                runningServers = copyChecker.checkCopy(packet);

            } catch (SocketTimeoutException e) {
                timeoutCount++;
                if (timeoutCount % 10 == 0) {
                    System.out.println("Waiting for broadcasts... (timeout " + timeoutCount + ")");
                }
            } catch (Exception e) {
                System.err.println("Error receiving broadcast: " + e.getMessage());
            }
        }

        socket.close();
        System.out.println("Broadcast Receiver stopped");
    }

    public List<String> getRunningServers() {
        return new ArrayList<>(runningServers);
    }

    public void stop() {
        running = false;
    }
}