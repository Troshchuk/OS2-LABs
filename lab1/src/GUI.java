import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

/**
 * GUI class
 *
 * @author Dmytro Troshchuk
 * @version 1.01 8 Apr 2014
 */
public class GUI {
    /** Main frame */
    private JFrame frame;
    /** Panel for email list */
    private JPanel leftPanel;
    /** Panel for result list */
    private JPanel rightPanel;

    /** Build gui */
    public void build() {
        //Build main frame
        frame = new JFrame("Automata");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(200, 100);

        //Build center panel for "Open file" button
        JPanel centerPanel = new JPanel();
        frame.getContentPane().add(centerPanel, BorderLayout.SOUTH);

        //Build "Open file" button"
        JButton openFileButton = new JButton("Open file...");
        openFileButton.addActionListener(new openFileButtonListener());
        centerPanel.add(openFileButton);

        //Build left panel for output emails list
        leftPanel = new JPanel();
        BoxLayout leftBoxLayout = new BoxLayout(leftPanel, BoxLayout.Y_AXIS);
        leftPanel.setLayout(leftBoxLayout);
        frame.getContentPane().add(leftPanel, BorderLayout.WEST);

        //Build right panel for output results list
        rightPanel = new JPanel();
        BoxLayout rightBoxLayout = new BoxLayout(rightPanel, BoxLayout.Y_AXIS);
        rightPanel.setLayout(rightBoxLayout);
        frame.getContentPane().add(rightPanel, BorderLayout.EAST);

        //Show frame
        frame.setVisible(true);
    }

    /** Open file button listener */
    private class openFileButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Parser parser = new Parser();

            //Open file and parse it
            JFileChooser fileChooser = new JFileChooser();
            int ret = fileChooser.showDialog(null, "Open file");
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                parser.setFIle(file);
                parser.parse();

                //Create ArrayLists for emails and results
                ArrayList<String> emails =
                        (ArrayList<String>) parser.getEmails();
                ArrayList<Boolean> results =
                        (ArrayList<Boolean>) parser.getResults();

                //Output emails and results
                for (int i = 0; i < emails.size(); i++) {
                    String email = emails.get(i);
                    boolean result = results.get(i);

                    JLabel leftLabel = new JLabel(email);
                    JLabel rightLabel;

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
