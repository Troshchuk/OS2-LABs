import java.util.ArrayList;

/**
 * Created by troshchuk on 08.05.14.
 */

public class HardDrive {
    /** memory in Kb */
    private int freeMemory;
    private ArrayList<Program> programs;
    private int numberOfPrograms;

    public HardDrive() {
        programs = new ArrayList<Program>();
        freeMemory = 1 << 30;
    }

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

    public Program getProgram(int index) {
        return programs.get(index);
    }

    public void rewritePage(int indexProgram, Page page) {

    }

    public Page loadPage(int program) {
        return new Page();
    }

    public int getNumberOfPrograms() {
        return numberOfPrograms;
    }
}
