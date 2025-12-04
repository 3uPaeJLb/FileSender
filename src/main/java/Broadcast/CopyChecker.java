package Broadcast;

import java.net.DatagramPacket;
import java.util.*;

public class CopyChecker {
    private Map<String, Long> serverMap = new HashMap<>();
    private static final long TIMEOUT_MS = 10000;

    public List<String> checkCopy(DatagramPacket packet) {
        String data = new String(packet.getData(), 0, packet.getLength()).trim();

        System.out.println("Processing packet: " + data);

        String[] parts = data.split("\\|");

        if (parts.length == 2) {
            try {
                String ip = parts[0].trim();
                int port = Integer.parseInt(parts[1].trim());

                String serverKey = ip + ":" + port;

                serverMap.put(serverKey, System.currentTimeMillis());

                System.out.println("Server found: " + serverKey);

            } catch (NumberFormatException e) {
                System.err.println("Invalid port format: " + data);
            }
        } else {
            System.err.println("Invalid packet format: " + data);
        }

        return getActiveServers();
    }

    public List<String> getActiveServers() {
        cleanupOldServers();
        return new ArrayList<>(serverMap.keySet());
    }

    private void cleanupOldServers() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = serverMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (currentTime - entry.getValue() > TIMEOUT_MS) {
                System.out.println("Server timeout: " + entry.getKey());
                iterator.remove();
            }
        }
    }
}