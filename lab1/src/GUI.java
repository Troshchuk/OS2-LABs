import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by lansk8er on 07.04.14.
 */
public class GUI {
    JFrame frame;
    private JLabel result;
    private JPanel leftPanel;
    private JPanel rightPanel;

    public void build() {
        frame = new JFrame("Automata");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 100);

        JPanel centerPanel = new JPanel();
        frame.getContentPane().add(centerPanel, BorderLayout.SOUTH);

        JButton openFileButton = new JButton("Open file...");
        openFileButton.addActionListener(new openFileButtonListener());
        centerPanel.add(openFileButton);

        result = new JLabel();
        centerPanel.add(result);

        leftPanel = new JPanel();
        BoxLayout leftBoxLayout = new BoxLayout(leftPanel, BoxLayout.Y_AXIS);
        leftPanel.setLayout(leftBoxLayout);
        frame.getContentPane().add(leftPanel, BorderLayout.WEST);

        rightPanel = new JPanel();
        BoxLayout rightBoxLayout = new BoxLayout(rightPanel, BoxLayout.Y_AXIS);
        rightPanel.setLayout(rightBoxLayout);
        frame.getContentPane().add(rightPanel, BorderLayout.EAST);

        frame.setVisible(true);
    }

    public class openFileButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Parser parser = new Parser();

            JFileChooser fileChooser = new JFileChooser();
            int ret = fileChooser.showDialog(null, "Open file");
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                parser.setFIle(file);
                parser.parse();

                ArrayList<String> emails = parser.getEmails();
                ArrayList<Boolean> results = parser.getResults();

                for (int i = 0; i < emails.size(); i++) {
                    String email = emails.get(i);
                    boolean result = results.get(i);

                    JLabel leftLabel = new JLabel(email);
                    JLabel rightLabel = null;

                    if (result) {
                        rightLabel = new JLabel("Valid");
                    } else {
                        rightLabel = new JLabel("Invalid");
                    }

                    leftPanel.add(leftLabel);
                    rightPanel.add(rightLabel);

                    frame.setSize(frame.getWidth(),
                                  frame.getHeight() + email.length());
                }
            }
        }
    }
}
