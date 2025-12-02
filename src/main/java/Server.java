import ServersObserver.UDPSender;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final ServerSocket serverSocket;
    private static final String PORT = "8000";
    private final static String MULTICAST_GROUP_ADDRESS = "230.0.0.1";


    public Server(String port) throws IOException {
        serverSocket = new ServerSocket(Integer.parseInt(port));
    }

    private String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') break;
            outputStream.write(b);
        }
        return outputStream.toString("UTF-8");
    }

    private FileOutputStream createFile(String fileName) throws IOException {
        File dir = new File("uploads");
        if (!dir.exists()) dir.mkdirs();
        return new FileOutputStream(new File(dir, fileName));
    }

    private void Listen(Socket socket) {
        try {
            InputStream in = socket.getInputStream();

            String fileNameStr = readLine(in);
            String fileSizeStr = readLine(in);
            String dataStart = readLine(in);

            System.out.println("fileNameStr: " + fileNameStr);
            System.out.println("fileSizeStr: " + fileSizeStr);

            String fileName = fileNameStr.replace("NAME:", "");
            long fileSize = Long.parseLong(fileSizeStr.replace("SIZE:", ""));

            System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes)");

            FileOutputStream out = createFile(fileName);

            int buffSize = 4096;
            byte[] buffer = new byte[buffSize];
            long received = 0;

            while (received < fileSize) {
                int read = in.read(buffer);
                if (read == -1) break;
                out.write(buffer, 0, read);
                received += read;
            }

            out.close();
            socket.close();

            System.out.println("File saved! Received bytes: " + received);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(String serverIP) throws IOException {
        System.out.println("Server is running on host with IP " + serverIP + "\nListening on port " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected: " + socket.getInetAddress());

            new Thread(() -> Listen(socket)).start();
        }
    }


    public static void main (String[]args) throws IOException {
        Server server = new Server(PORT);
        String ServerIP = InetAddress.getLocalHost().getHostAddress();

        new Thread(() -> {
            UDPSender udpSender = new UDPSender(MULTICAST_GROUP_ADDRESS);
            try {
                udpSender.sendUDP();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        server.run(ServerIP);
    }
}