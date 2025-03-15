package chatjava.client.gui;

import chatjava.client.ClientWithHooks;
import chatjava.common.Message;

import javax.swing.*;
import java.awt.*;

public class ClientMainGui {
    private static JFrame mainFrame;

    private static void setupMacosCommandQ(ClientWithHooks client) {
        // Add Mac-specific quit handler
        if (System.getProperty("os.name").startsWith("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "YourApp");

            // Set up Quit Action
            MenuBar menuBar = new MenuBar();
            Menu fileMenu = new Menu("File");
            MenuItem quitItem = new MenuItem("Quit");
            quitItem.addActionListener(e -> {
                client.sendMessage(new Message.Disconnect.Request());
                System.out.println("Quitting app...");
                System.exit(0);
            });
            fileMenu.add(quitItem);
            menuBar.add(fileMenu);
            mainFrame.setMenuBar(menuBar);
        }
    }

    static ClientWithHooks client = new ClientWithHooks("localhost", 3333);
    public static void main(String[] args) {
        client.addMessageHook(Message.Disconnect.Response.class, (Message.Disconnect.Response response) -> {
            System.out.println("Disconnect");
        });
        client.addMessageHook(Message.SendTextMessage.Response.class, (response) -> {
            System.out.println("Send text message");
        });
        client.addMessageHook(Message.JoinRoom.Response.class, (response) -> {
            System.out.println("Join room");
        });
        client.showMessageHooks();

        createAndDisplayGUI();
    }


    private static void createAndDisplayGUI()
    {
        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame("Swing Layout Example");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(400, 300);
            mainFrame.setLayout(new BorderLayout());

            // INFORMATION PANEL
            {
                JPanel informationPanel = new JPanel();
                informationPanel.setLayout(new BoxLayout(informationPanel, BoxLayout.Y_AXIS));
                for (int i = 0; i < 10; i++) {
                    JPanel roomEntry = new JPanel();
                    roomEntry.setLayout(new FlowLayout());
                    roomEntry.add(new JLabel("Room #" + i));
                    roomEntry.add(new JButton("Join"));
                    informationPanel.add(roomEntry);
                }

                mainFrame.add(informationPanel, BorderLayout.WEST);
            }

            // SCROLL PANE FOR CHAT ENTRIES
            {
                ChatScrollPane scrollPane = new ChatScrollPane();
                scrollPane.submitNewChatEntry("User1", "Hello");
                scrollPane.submitNewChatEntry("User2", "Goodbye");
                mainFrame.add(scrollPane);
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
                gbc.gridx = 2;
                gbc.weightx = 2;
                gbc.fill = GridBagConstraints.EAST;
                inputPanel.add(sendButton);

                mainFrame.add(inputPanel, BorderLayout.SOUTH);
            }

            mainFrame.setVisible(true);
            setupMacosCommandQ(client);
        });
    }
}
