/**
 * Program
 *
 * @author Dmytro Troshchuk
 * @version 1.02 08.05.14
 */
public class Program {
    /** memory in Kb */
    private int memory;

    /**
     * @param memory memory in Kb
     */
    public Program(int memory) {
        this.memory = memory;
    }

    /**
     * @return memory of program
     */
    public int getMemory() {
        return memory;
    }
}
