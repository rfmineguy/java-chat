package chatjava.client.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RoomsScrollPane extends JPanel {
    private JPanel contentPanel;
    private JScrollPane scrollPane;
    private List<String> rooms;

    public RoomsScrollPane() {
        super();
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane);

        rooms = new ArrayList<>();
    }

    public void update(Consumer<String> consumer) {
        contentPanel.removeAll();
        System.out.println("Updating with: " + rooms);
        for (String roomName : rooms) {
            JPanel roomEntry = new JPanel(new BorderLayout());
            roomEntry.setLayout(new BoxLayout(roomEntry, BoxLayout.X_AXIS));
            roomEntry.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            roomEntry.setAlignmentX(Component.LEFT_ALIGNMENT);
            roomEntry.add(new JLabel(roomName));
            JButton joinButton = new JButton("Join");
            joinButton.addActionListener(e -> {
                System.out.println("Join: " + roomName);
                consumer.accept(roomName);
            });
            roomEntry.add(joinButton);
            contentPanel.add(roomEntry);
            System.out.println("Added: " + roomName);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void clear() {
        rooms.clear();
        contentPanel.removeAll();
    }

    public void addRoomEntry(String roomName, Consumer<String> consumer) {
        rooms.add(roomName);
        update(consumer);
    }
}
