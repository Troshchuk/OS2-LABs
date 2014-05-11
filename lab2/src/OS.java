import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * OS
 *
 * @author Dmytro Troshchuk
 * @version 1.02 08.05.14
 */
public class OS {
    /**
     * RAM
     */
    private final RAM ram;
    /**
     * MMU
     */
    private final MMU mmu;
    /**
     * Array of processes
     */
    private final ArrayList<Process> processes;

    /**
     * @param ram RAM
     */
    public OS(RAM ram) {
        this.ram = ram;
        mmu = new MMU(ram);
        processes = new ArrayList<>();
    }

    /**
     * Run OS and run processes which were loaded and write log file about
     * which process are in RAM and which process which command do
     * Also it kill process in course
     */
    public void run() {
        try (BufferedWriter out = new BufferedWriter(
                new FileWriter("log.txt"))) {

            //Sleep before load processes
            Thread.sleep(5);

            for (int j = 0; j < processes.size(); j++) {
                //Set bit handled 0 in all processes every 20 tact
                mmu.checkHandled();
                out.write("Set handled 0\n");

                //The process scheduler ;)
                for (int i = 0; i < 20; i++) {
                    int readOrWrite = (int) (Math.random() * 2);
                    int process =
                            (int) (Math.random() * (processes.size() - j) + j);
                    int length = processes.get(process).getVirtualAddressSpace()
                                          .getLength();
                    int command = (int) (Math.random() * 1024 * length);

                    mmu.doCommand(process, command, readOrWrite);
                    out.write("Process " + process + " do command " + command +
                              " Write " + readOrWrite + "\n");
                    out.write(mmu.toString());
                }

                if (j < processes.size()) {
                    killProcess(processes.get(j));
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add program to hard drive
     *
     * @param program program which add to
     */
    public void addProgram(Program program) {
        if (!mmu.addProgramToHardDrive(program)) {
            System.out.println("Not enough memory on the hard drive");
        }
    }

    /**
     * Create processes which load program from hard drive
     */
    public void runPrograms() {
        int numberOfPrograms = mmu.getNumberOfPrograms();

        for (int i = 0; i < numberOfPrograms; i++) {
            if (ram.isFreeSpace()) {
                Process process = createProcess(i);
                processes.add(process);
            }
        }
    }

    /**
     * Create process
     *
     * @param program number of program
     * @return created process
     */
    private Process createProcess(int program) {
        return mmu.loadProgramToRam(program);
    }

    /**
     * Kill process
     *
     * @param process process
     */
    private void killProcess(Process process) {
        mmu.removeProcess(processes.indexOf(process));
    }
}
