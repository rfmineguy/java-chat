package chatjava.client.gui;

import chatjava.client.ClientWithHooks;
import chatjava.common.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientGui {
    private static final Logger log = LogManager.getLogger(ClientGui.class);
    private JFrame mainFrame;
    private RoomsScrollPane roomsScrollPane;
    private ChatScrollPane chatScrollPane;
    private ClientWithHooks client;
    private boolean loginRequestSubmitted = false;
    public ClientGui(ClientWithHooks client) {
        this.client = client;
        this.roomsScrollPane = new RoomsScrollPane();
        this.chatScrollPane = new ChatScrollPane();
        SwingUtilities.invokeLater(() -> {
            // Create Account / Login Screen
            JPanel loginPanel = new JPanel();
            JTextField usernameField = new JTextField("Username", 8);
            JTextField passwordField = new JTextField("Password", 8);
            loginPanel.add(usernameField);
            loginPanel.add(passwordField);

            do {
                if (!loginRequestSubmitted) {
                    int v = JOptionPane.showConfirmDialog(null, loginPanel, "Login", JOptionPane.OK_CANCEL_OPTION);
                    if (v == JOptionPane.CANCEL_OPTION) {
                        client.sendNetworkMessage(new Message.Disconnect.Request());
                        return;
                    }
                    client.sendNetworkMessage(new Message.Login.Request(usernameField.getText(), passwordField.getText()));
                    loginRequestSubmitted = true;
                }
            } while (!client.isLoggedIn());

            System.out.println("User logged in. Username: " + client.getUsername());

            mainFrame = new JFrame("Chat Client");
            mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            mainFrame.setSize(400, 300);
            mainFrame.setLayout(new BorderLayout());

            mainFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("Closing window with X");
                    client.sendNetworkMessage(new Message.Disconnect.Request());
                }
            });

            // INFORMATION PANEL
            {
                JPanel informationPanel = new JPanel();
                informationPanel.setLayout(new BoxLayout(informationPanel, BoxLayout.Y_AXIS));
                informationPanel.add(roomsScrollPane);
                
                JButton button = new JButton("New Room");
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String roomName = JOptionPane.showInputDialog(null, null);
                        if (roomName.isEmpty()) return;
                        client.sendNetworkMessage(new Message.CreateRoom.Request(roomName));
                    }
                });
                informationPanel.add(button);

                JButton refresh = new JButton("Refresh");
                refresh.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        client.sendNetworkMessage(new Message.Rooms.Request());
                    }
                });
                informationPanel.add(refresh);
                mainFrame.add(informationPanel, BorderLayout.WEST);
            }

            // SCROLL PANE FOR CHAT ENTRIES
            {
                // chatScrollPane.submitNewChatEntry("User1", "Hello");
                // chatScrollPane.submitNewChatEntry("User2", "Goodbye");
                mainFrame.add(chatScrollPane);
                // JTextArea textArea = new JTextArea(10, 30);
                // JScrollPane scrollPane = new JScrollPane(textArea);
                // mainFrame.add(scrollPane, BorderLayout.CENTER);
            }

            // SEND MESSAGE PANEL
            {
                JPanel inputPanel = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.anchor = GridBagConstraints.WEST;

                // Text field that stretches
                JTextField textField = new JTextField();
                gbc.gridx = 1;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                inputPanel.add(textField, gbc);

                JButton sendButton = new JButton("Send");
                sendButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (textField.getText().isEmpty()) return;

                        client.sendNetworkMessage(new Message.SendTextMessage.Request(client.getUsername(), textField.getText()));
                    }
                });
                gbc.gridx = 2;
                gbc.weightx = 2;
                gbc.fill = GridBagConstraints.EAST;
                inputPanel.add(sendButton);

                mainFrame.add(inputPanel, BorderLayout.SOUTH);
            }

            mainFrame.setVisible(true);
            setupMacosCommandQ(client);
        });
        // this.clientRef.sendNetworkMessage(new Message.Rooms.Request());
    }

    public void acceptLogin() {
        client.setLoggedIn(true);
    }
    public void denyLogin() {
        client.setLoggedIn(false);
        loginRequestSubmitted = false;
    }

    private void setupMacosCommandQ(ClientWithHooks client) {
        // Add Mac-specific quit handler
        if (System.getProperty("os.name").startsWith("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "YourApp");

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
                Desktop.getDesktop().setQuitHandler(((e, response) -> {
                    System.out.println("Quitting");
                    client.sendNetworkMessage(new Message.Disconnect.Request());
                    System.exit(0);
                }));
            }

            // Set up Quit Action
            MenuBar menuBar = new MenuBar();
            Menu fileMenu = new Menu("File");
            MenuItem quitItem = new MenuItem("Quit");
            quitItem.addActionListener(e -> {
                System.out.println("Sent disconnect request");
                client.sendNetworkMessage(new Message.Disconnect.Request());
            });
            fileMenu.add(quitItem);
            menuBar.add(fileMenu);
            mainFrame.setMenuBar(menuBar);
        }
    }

    public void updateRooms(Message.Rooms.Response response) {
        System.out.println("ClientGui.updateRooms(): Received room response, but do nothing with it");
        roomsScrollPane.clear();
        for (String s : response.getRooms()) {
            roomsScrollPane.addRoomEntry(s, (room) -> {
                System.out.println("ClientGUI: Join: " + room);
                updateTitleWithRoom(room);
            });
            System.out.println(s);
        }
        // roomsScrollPane.update();
    }

    public void updateTitleWithRoom(String room) {
        System.out.println("Update title with room: " + room);
        mainFrame.setTitle("Chat Client [" + room + "]");
        mainFrame.repaint();
    }

    public void addChatEntry(String user, String message) {
        chatScrollPane.submitNewChatEntry(user, message);
    }
}
