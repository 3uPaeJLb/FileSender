import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URL;

public class Client {
    private static final String PORT = "8000";

    private String fileName;
    private Socket socket;

    private void sendFile(String filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)){
            int minDataSize = 512;
            byte[] Data = new byte[minDataSize];
            int readCountBytes;

            while (true){
                readCountBytes = fileInputStream.read(Data);

                if (readCountBytes < 0)
                    break;

                socket.getOutputStream().write(Data, 0, readCountBytes);

            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Long getFileSize(String filePath) {
        File file = new File(filePath);
        return file.length();
    }

    private String extractFileName(String filePath) {
        File file = new File(filePath);
        fileName = file.getName();
        return fileName;
    }

    public void run(String ip, String port, String filePath) throws IOException {
        socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));

        //send file name
        fileName = extractFileName(filePath);
        socket.getOutputStream().write(("NAME:" + fileName + "\n").getBytes(StandardCharsets.UTF_8));

        //send file size
        long fileSize = getFileSize(filePath);
        if (fileSize > 1024 * 1024 * 1024) {
            System.out.println("File size must not exceed 1 Tb.");
            return;
        }
        socket.getOutputStream().write(("SIZE:" + fileSize + "\n").getBytes(StandardCharsets.UTF_8));

        sendFile(filePath);

    }

    private static String getFilePath(String fileName) {
        URL resource = Main.class.getClassLoader().getResource(fileName);

        if (resource != null) {
            return resource.getFile();
        } else {
            throw new RuntimeException("File not found in resources: " + fileName);
        }
    }

    public static void main(String[] args) throws IOException {
        String ServerIP = InetAddress.getLocalHost().getHostAddress();
        String filePath = getFilePath("Text");

        Client client = new Client();

        client.run(ServerIP, PORT, filePath);
    }
}
