import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientData> clientsQueue = new ArrayList<>();
    private String fileName;

    public Server(String port) throws IOException {
        serverSocket = new ServerSocket(Integer.parseInt(port));
    }

    private FileOutputStream createFile(String fileName) throws IOException {
        File uploadDir = new File("uploads");

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File file = new File(uploadDir, fileName);

        return new FileOutputStream(file);
    }

    private void receiveFile(ClientData clientData,Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream));

        String fileNameStr = reader.readLine();
        String fileSizeStr = reader.readLine();

        fileName = fileNameStr.replaceFirst("NAME:", "");
        clientData.setFileName(fileName);
        long fileSize = Long.parseLong(fileSizeStr.replaceFirst("SIZE:", ""));
        clientData.setFileSize(fileSize);
        System.out.println(fileSize + " " + fileName);

        FileOutputStream fileOutputStream = createFile(fileName);

        final int minDataSize = 512;
        byte[] buffer = new byte[minDataSize];
        int bytesRead;
        long summBytes = 0;

        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                summBytes += bytesRead;


                if (summBytes >= fileSize) {
                    break;
                }
            }

            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void Listen(){
        try {
        ClientData clientData = new ClientData();
        Socket socket = serverSocket.accept();

        clientsQueue.add(clientData);

        Thread newListenThread = new Thread(this::Listen);
        newListenThread.start();

        receiveFile(clientData, socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(){
        Listen();
    }

    private static final String PORT = "8000";

    public static void main(String[] args) throws IOException {
        Server server = new Server(PORT);

        server.run();
    }
}
