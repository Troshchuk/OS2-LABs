import java.util.ArrayList;

/**
 * Hard drive
 *
 * @author Dmytro Troshchuk
 * @version 1.02 08.05.14
 */
public class HardDrive {
    /** Free memory in Kb */
    private int freeMemory;
    /** List of programs */
    private final ArrayList<Program> programs;
    /** Number of programs */
    private int numberOfPrograms;

    public HardDrive() {
        programs = new ArrayList<>();
        freeMemory = 1 << 30;
    }

    /**
     * Add program
     *
     * @param program program
     * @return result of adding
     */
    public boolean addProgram(Program program) {
        int programMemory = program.getMemory();
        if (programMemory < freeMemory) {
            programs.add(program);
            freeMemory -= programMemory;
            numberOfPrograms++;
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param index number of program
     * @return program
     */
    public Program getProgram(int index) {
        return programs.get(index);
    }

    /**
     * Rewrite page of program
     *
     * @param indexProgram number of program
     * @param page page
     */
    public void rewritePage(int indexProgram, Page page) {

    }

    /**
     * Load page of program
     *
     * @param program number of program
     * @return page of program
     */
    public Page loadPage(int program) {
        return new Page();
    }

    /**
     * @return number of programs
     */
    public int getNumberOfPrograms() {
        return numberOfPrograms;
    }
}
