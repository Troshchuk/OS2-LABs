import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by troshchuk on 08.05.14.
 */
public class OS {
    private RAM ram;
    private MMU mmu;
    private ArrayList<Process> processes;

    public OS(RAM ram) {
        this.ram = ram;
        mmu = new MMU(ram);
        processes = new ArrayList<Process>();
    }

    public void run() {
        try (BufferedWriter out = new BufferedWriter(new FileWriter("file.txt"))) {
            for (int j = 0; j < 500; j++) {
                mmu.checkHandled();
                out.write("Set handled 0\n");

                for (int i = 0; i < 20; i++) {
                    int readOrWrite = (int) (Math.random() * 2);

                    int process = (int) (Math.random() * (processes.size()));
                    int command = (int) (Math.random() * 1024 * processes.get(process).getVirtualAddressSpace().getLength());

                    mmu.doCommand(process, command, readOrWrite);
                    out.write("Process " + process + " do command " + command + " Write " + readOrWrite + "\n");
                    out.write(mmu.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addProgram(Program program) {
        if (!mmu.addProgramToHardDrive(program)) {
            System.out.println("Not enough memory on the hard drive");
        }
    }

    public void runPrograms() {
        int numberOfPrograms = mmu.getNumberOfPrograms();

        for (int i = 0; i < numberOfPrograms; i++) {
            if (ram.isFreeSpace()) {
                Process process = createProcess(i);
                processes.add(process);
            }
        }
    }

    private Process createProcess(int program) {
        return mmu.loadProgramToRam(program);
    }

    public void killProcess(Process process) {
        mmu.removeProcess(processes.indexOf(process));
    }
}
