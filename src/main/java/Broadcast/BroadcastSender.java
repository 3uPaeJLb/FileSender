package Broadcast;

import java.io.IOException;
import java.net.*;

public class BroadcastSender {
    private static final int PORT = 8000;
    private boolean running = true;

    public BroadcastSender() {
    }

    public void sendBroadcast() throws IOException, InterruptedException {
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);

        System.out.println("=== Broadcast Sender Started ===");
        System.out.println("Local IP: " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("Broadcast port: " + PORT);
        System.out.println("================================");

        while (running) {
            String localIP = InetAddress.getLocalHost().getHostAddress();

            String message = String.format("%s|%d", localIP, PORT);
            byte[] data = message.getBytes("UTF-8");


            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");

            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    broadcastAddress,
                    PORT
            );

            try {
                socket.send(packet);
                System.out.println("Broadcast sent: " + message);
            } catch (IOException e) {
                System.err.println("Failed to send broadcast: " + e.getMessage());
            }

            Thread.sleep(2000);
        }

        socket.close();
        System.out.println("Broadcast Sender stopped");
    }

    public void stop() {
        running = false;
    }
}