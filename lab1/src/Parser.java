import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser class for parsing file with emails
 */
public class Parser {
    /** File for read list of emails */
    private File file;
    /** List of emails */
    private List<String> emails;
    /** List of results */
    private List<Boolean> results;

    /** Constructor */
    public Parser() {
        emails = new ArrayList<>();
        results = new ArrayList<>();
    }

    /** Parse file */
    public void parse() {
        //Create automata
        Automata automata = new Automata();

        //Read file ana parse
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String email = in.readLine();
            do {
                automata.setInitialState();
                //Add '\0' for show end of email
                email = email + "\0";
                emails.add(email);
                results.add(false);

                for (int i = 0; i < email.length(); i++) {
                    char c = email.charAt(i);
                    int charClass = getCharClass(c);
                    int state = automata.nextState(charClass);

                    //If return -1, email is invalid
                    if (state == -1) {
                        break;
                    }

                    //If return 9, email is valid
                    if (state == 9) {
                        results.set(results.size() - 1, true);
                    }
                }
                email = in.readLine();

            } while (email != null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Set file */
    public void setFIle(File file) {
        this.file = file;
    }

    /**
     * Get char class
     *
     * @param c char
     * @return char class
     */
    public int getCharClass(char c) {
        if (Character.isLetterOrDigit(c)) {
            return 0;
        }
        switch (c) {
            case '-':
                return 1;
            case '@':
                return 2;
            case '.':
                return 3;
            case '_':
                return 4;
            case '\0':
                return 5;
            default:
                return -1;
        }
    }

    /**
     * Get emails
     *
     * @return list of emails
     */
    public List<String> getEmails() {
        return emails;
    }

    /**
     * Get results
     *
     * @return list of results
     */
    public List<Boolean> getResults() {
        return results;
    }
}
