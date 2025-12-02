package ServersObserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalTime;

public class UDPSender {

    private static final int PORT = 8000;
    private boolean running = true;
    private final String multicastGroupAddress;

    public UDPSender(String multicastGroupAddress) {
        this.multicastGroupAddress = multicastGroupAddress;
    }

    public void sendUDP() throws IOException, InterruptedException {
        MulticastSocket sendSocket = new MulticastSocket();
        InetAddress group = InetAddress.getByName(multicastGroupAddress);
        sendSocket.joinGroup(group);

        while (running) {
            String localIP = InetAddress.getLocalHost().getHostAddress();

            String message = String.format(
                    "%s|%d",
                    localIP,
                    PORT
            );

            byte[] data = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, group, PORT);
            sendSocket.send(sendPacket);

            Thread.sleep(2000);
        }

        sendSocket.close();
    }
}
