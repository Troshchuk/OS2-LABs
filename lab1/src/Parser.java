import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by lansk8er on 07.04.14.
 */
public class Parser {
    private File file;
    private ArrayList<String> emails;
    private ArrayList<Boolean> results;

    public Parser() {
        emails = new ArrayList<>();
        results = new ArrayList<>();
    }

    public boolean parse() {
        Automata automata = new Automata();

        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String email = in.readLine();
            do {
                automata.setInitialState();
                email = email + "\0";
                emails.add(email);
                results.add(false);

                for (int i = 0; i < email.length(); i++) {
                    char c = email.charAt(i);
                    int charClass = getCharClass(c);
                    int state = automata.nextState(charClass);

                    if (state == -1) {
                        break;
                    }

                    if (state == 9) {
                        results.set(results.size() - 1, true);
                    }
                }

                email = in.readLine();

            } while (email != null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void setFIle(File file) {
        this.file = file;
    }

    public int getCharClass(char c) {
        if (Character.isLetterOrDigit(c)) {
            return 0;
        }

        if (c == '-') {
            return 1;
        }

        if (c == '@') {
            return 2;
        }

        if (c == '.') {
            return 3;
        }

        if (c == '_') {
            return 4;
        }

        if (c == '\0') {
            return 5;
        }

        return -1;
    }

    public ArrayList<String> getEmails() {
        return emails;
    }

    public ArrayList<Boolean> getResults() {
        return results;
    }
}
