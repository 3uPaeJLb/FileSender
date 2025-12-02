import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Client {
    private static final String PORT = "8000";

    private String fileName;
    private Socket socket;
    private MainFrame mainFrame;
    private Boolean connectionStatus;

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

    public void run(String filePath) throws IOException, InterruptedException {
        try {


            fileName = extractFileName(filePath);
            long fileSize = getFileSize(filePath);

            System.out.println("Sending file: " + fileName + " (" + fileSize + " bytes)");

            socket.getOutputStream().write(("NAME:" + fileName + "\n").getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().write(("SIZE:" + fileSize + "\n").getBytes(StandardCharsets.UTF_8));

            socket.getOutputStream().write("DATA_START\n".getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();

            System.out.println("Headers sent, starting file transfer...");

            sendFile(filePath);

            Thread.sleep(1000);

            System.out.println("File sent successfully!");

        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public Boolean setConnectionWithServer(String ip, String port) throws IOException {
        socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
        System.out.println("Connected to server!");
        return true;
    }

    private static String getFilePath(String fileName) {
        URL resource = Main.class.getClassLoader().getResource(fileName);

        if (resource != null) {
            return resource.getFile();
        } else {
            throw new RuntimeException("File not found in resources: " + fileName);
        }
    }

    public void openFileExplorer(JTextField fileField, JTextArea logArea, MainFrame mainFrame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл для отправки");

        int result = fileChooser.showOpenDialog(mainFrame);

        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            fileField.setText(filePath);
            logArea.append("\n Chosen file: " + fileChooser.getSelectedFile().getName());
        } else {
            logArea.append("\nFile selection canceled");
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String ServerIP ="192.168.0.109";
        String filePath = getFilePath("Text");
        System.out.println("File successful found. Path: " + filePath);
        Client client = new Client();

        client.run(filePath);
    }
}