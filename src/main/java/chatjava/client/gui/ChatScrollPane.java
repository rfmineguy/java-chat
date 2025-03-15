package chatjava.client.gui;

import javax.swing.*;
import java.awt.*;

public class ChatScrollPane extends JPanel {
    private JPanel contentPanel;
    private JScrollPane scrollPane;

    public ChatScrollPane() {
        super();
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane);
    }

    public void submitNewChatEntry(String user, String s) {
        JPanel chatEntry = new JPanel(new BorderLayout());
        chatEntry.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        chatEntry.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatEntry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        JLabel label = new JLabel(user + "   |   ");
        label.setMaximumSize(new Dimension(40, 25));
        label.setMinimumSize(new Dimension(40, 25));
        JLabel label2 = new JLabel(s);
        chatEntry.add(label, BorderLayout.WEST);
        chatEntry.add(label2);
        contentPanel.add(chatEntry, BorderLayout.WEST);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
