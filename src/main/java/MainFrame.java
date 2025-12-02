import ServersObserver.UDPReceiver;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private DefaultListModel<String> serverModel = new DefaultListModel<>();
    private List<String> servers = new ArrayList<>();
    private Client client = new Client();
    private String filepath = "";
    private final static String MULTICAST_GROUP_ADDRESS = "230.0.0.1";
    UDPReceiver udpReceiver = new UDPReceiver(MULTICAST_GROUP_ADDRESS);
    private Timer updateTimer;
    JList<String> serverTable = new JList<>(serverModel);

    private Boolean connectionStatus = false;

    public MainFrame() {
        super("FileSender");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(400, 100, 900, 500);
        setLayout(null);

        //servers.add("Server:192.168.0.149,port:8000");

        serverTable.setBounds(10, 10, 250, 250);
        serverTable.setVisible(true);
        this.add(serverTable);

        JTextArea logArea = new JTextArea();
        logArea.setBounds(40, 280, 600, 170);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        this.add(logArea);

        JTextField fileField = new JTextField();
        fileField.setBounds(270, 10, 400, 30);
        fileField.setText("Empty...");
        fileField.setVisible(true);
        this.add(fileField);

        JButton browseButton = new JButton("File");
        browseButton.setBounds(680, 10, 80, 30);
        this.add(browseButton);

        JButton sendFileButton = new JButton("Send");
        sendFileButton.setBounds(430, 50, 80, 30);
        this.add(sendFileButton);

        new Thread(() -> {
            try {
                udpReceiver.receiveUDP();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }).start();

        updateTimer = new Timer(1000, e -> updateServerList());
        updateTimer.start();

        serverTable.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String server = serverTable.getSelectedValue();
                System.out.println(server);

                if (server != null) {
                    try {
                        String[] parts = server.split(":");
                        String serverIP = "";
                        String serverPort = "";

                        if (parts.length == 2) {
                            serverIP = parts[0].trim();
                            serverPort = parts[1].trim();
                        }

                        System.out.println("Connecting to: " + serverIP + ":" + serverPort);

                        if (!serverIP.isEmpty() && !serverPort.isEmpty()) {
                            connectionStatus = client.setConnectionWithServer(serverIP, serverPort);
                            logArea.setText("Connected to: " + server + "\nStatus: " + connectionStatus);
                        } else {
                            logArea.setText("Error: Could not parse server address");
                        }

                    } catch (IOException ex) {
                        logArea.setText("Connection error: " + ex.getMessage());
                    }
                } else {
                    logArea.setText("Please select a server from the list");
                }
            }
        });
        System.out.println(connectionStatus);

        browseButton.addActionListener(e -> {
            client.openFileExplorer(fileField, logArea, this);
            filepath = fileField.getText();
        });

        sendFileButton.addActionListener(e -> {
            try {
                client.run(String.valueOf(filepath));
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        setVisible(true);
    }
    private void updateServerList() {
        if (udpReceiver != null) {
            List<String> servers = udpReceiver.getRunningServers();

            SwingUtilities.invokeLater(() -> {
                String currentSelection = serverTable.getSelectedValue();
                serverModel.clear();

                for (String server : servers) {
                    serverModel.addElement(server);
                }

                if (currentSelection != null && servers.contains(currentSelection)) {
                    serverTable.setSelectedValue(currentSelection, true);
                }
            });
        }
    }

    public static void main (String[]args)  {
        SwingUtilities.invokeLater(() -> {
            new MainFrame();
        });
    }

}