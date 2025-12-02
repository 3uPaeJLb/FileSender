package ServersObserver;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

public class CopyChecker {

    private List<String> runningServers = new ArrayList<>();
    private String ip;
    private int port;
    private long timeout;

    public List<String> checkCopy(DatagramPacket packet) {
        String data = new String(packet.getData(), 0, packet.getLength());

        String[] parts = data.split("\\|");

        if (parts.length >= 2) {
            ip =  parts[0].trim();
            port = Integer.parseInt(parts[1].trim());
            timeout = System.currentTimeMillis();
            System.out.println(ip + ":" + port);

            if(!runningServers.contains(ip + ":" + port)) {
                runningServers.add(ip + ":" + port);
            }
        }

        return runningServers;
    }
}
