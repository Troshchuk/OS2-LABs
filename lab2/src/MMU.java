import java.util.ArrayList;
import java.util.Iterator;

/**
 * Memory Management Unit
 *
 * @author Dmytro Troshchuk
 * @version 1.02 08.05.14
 */
public class MMU {
    /**
     * RAM
     */
    private final RAM ram;
    /**
     * CPU
     */
    private final CPU cpu;
    /**
     * Table of all process
     */
    private final ArrayList<Table> table;
    /**
     * Show process position on the table
     */
    private final ArrayList<Integer> processIndex;
    /**
     * Hard drive
     */
    private final HardDrive hardDrive;

    /**
     * @param ram RAM
     */
    public MMU(RAM ram) {
        this.ram = ram;
        table = new ArrayList<>();
        processIndex = new ArrayList<>();
        cpu = new CPU();
        hardDrive = new HardDrive();
    }

    /**
     * Add information of process to the table
     *
     * @param virtualAddressSpace virtual address space of process
     * @param process             number of process
     */
    private void add(VirtualAddressSpace virtualAddressSpace, int process) {
        int length = virtualAddressSpace.getLength();
        processIndex.add(table.size());
        for (int i = 0; i < length; i++) {
            Table t = new Table();
            t.process = process;
            t.virtualPage = i;
            table.add(t);
        }
    }

    /**
     * Add program to hard drive
     *
     * @param program program
     * @return result of adding
     */
    public boolean addProgramToHardDrive(Program program) {
        return hardDrive.addProgram(program);
    }

    /**
     * @return number of programs on the hard drive
     */
    public int getNumberOfPrograms() {
        return hardDrive.getNumberOfPrograms();
    }

    /**
     * Load program to RAM
     *
     * @param program number of program
     * @return created process
     */
    public Process loadProgramToRam(int program) {
        Process process = new Process(hardDrive.getProgram(program));

        add(process.getVirtualAddressSpace(), program);

        VirtualAddressSpace virtualAddressSpace =
                process.getVirtualAddressSpace();

        int lengthPageBlocks = ram.getLength();
        int lengthVirtualPages = virtualAddressSpace.getLength();

        RAM.PageBlocks pageBlocks = ram.getPageBlocks();
        VirtualAddressSpace.VirtualPages virtualPages =
                virtualAddressSpace.getVirtualPages();

        int usedLength = ram.getUsedLength();

        for (int i = 0; i < usedLength; i++) {
            pageBlocks = pageBlocks.next;
        }

        for (int i = usedLength, j = 0;
             j < lengthVirtualPages && i < lengthPageBlocks; i++, j++) {

            pageBlocks.page = virtualPages.page;

            pageBlocks = pageBlocks.next;
            virtualPages = virtualPages.next;

            editTable(program, i - usedLength, i);

            ram.incrementUsed();
            ram.setFreeBlock(i, false);
        }
        return process;
    }

    /**
     * Set that process is involved and set which virtual block involved
     *
     * @param process      number of process
     * @param virtualPage  number of virtual page
     * @param virtualBlock number of virtual block
     */
    private void editTable(int process, int virtualPage, int virtualBlock) {
        Table t = table.get(virtualPage + processIndex.get(process));
        t.involved = true;
        t.virtualBlock = virtualBlock;
    }

    /**
     * Remove process
     *
     * @param process number of process
     */
    public void removeProcess(int process) {
        Iterator<Table> itr = table.listIterator(process);
        while (itr.hasNext()) {
            Table t = itr.next();
            if (t.process == process) {
                if (t.involved) {
                    t.involved = false;
                    ram.setFreeBlock(t.virtualBlock, true);
                    ram.decrementUsed();
                }
            } else {
                break;
            }
        }
    }

    /**
     * Set bit handled 0 to all process in the table
     */
    public void checkHandled() {
        int length = table.size();

        for (Table row : table) {
            if (row.involved && row.handled) {
                row.handled = false;
            }
        }
    }

    /**
     * Do command of process
     *
     * @param process number of process
     * @param command command
     * @param readOrWrite read 0 write 1
     */
    public void doCommand(int process, int command, int readOrWrite) {
        int virtualPage = command / 1024;

        Iterator<Table> itr = table.listIterator(processIndex.get(process));

        while (true) {
            Table t = itr.next();

            if (t.virtualPage == virtualPage) {
                if (t.involved) {
                    cpu.doCommand(command);
                    t.handled = true;
                    if (readOrWrite == 1) {
                        t.changed = true;
                    }
                } else if (ram.isFreeSpace()) {
                    RAM.PageBlocks pageBlocks = ram.getPageBlocks();
                    int count = 0;
                    while (!pageBlocks.free) {
                        pageBlocks = pageBlocks.next;
                        count++;
                    }

                    Page page = hardDrive.loadPage(process);
                    pageBlocks.page = page;
                    pageBlocks.free = false;

                    ram.incrementUsed();
                    t.involved = true;
                    t.virtualBlock = count;

                    cpu.doCommand(command);
                    t.handled = true;
                    if (readOrWrite == 1) {
                        t.changed = true;
                    }
                } else {
                    NRU(process, virtualPage, readOrWrite);
                }

                break;
            }
        }
    }

    /**
     * NRU algorithm
     *
     * @param process number of process
     * @param virtualPage number of virtual page
     * @param readOrWrite read 0 write 1
     */
    private void NRU(int process, int virtualPage, int readOrWrite) {
        int length = table.size();
        int low = 3;
        ArrayList<Integer> classes = new ArrayList<>();
        for (Table t : table) {
            if (t.involved) {
                if (!t.handled) {
                    if (!t.changed) {
                        low = 0;
                        classes.add(low);
                        break;
                    } else {
                        low = 1;
                        classes.add(low);
                    }
                } else {
                    if (!t.changed) {
                        if (low > 2) {
                            low = 2;
                        }
                        classes.add(2);
                    } else {
                        classes.add(3);
                    }
                }
            } else {
                classes.add(-1);
            }
        }

        int index = classes.indexOf(low);

        Table t = table.get(index);
        if (t.changed) {
            hardDrive.rewritePage(table.get(index).process, new Page());
        }
        t.involved = false;
        t.changed = false;
        int virtualBlock = t.virtualBlock;


        Page page = hardDrive.loadPage(process);
        RAM.PageBlocks pageBlocks = ram.getPageBlocks();

        for (int i = 0; i < t.virtualBlock; i++) {
            pageBlocks.page = page;
        }

        Iterator<Table> itr = table.listIterator(processIndex.get(process));

        while (true) {
            t = itr.next();

            if (t.virtualPage == virtualPage) {
                t.involved = true;

                t.handled = true;
                if (readOrWrite == 1) {
                    t.changed = true;
                }
                t.virtualBlock = virtualBlock;
                break;
            }
        }


    }

    /**
     * For log
     *
     * @return table
     */
    public String toString() {
        int length = table.size();
        StringBuilder sb = new StringBuilder();
        for (Table t : table) {
            sb.append(t.toString());
        }
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Class table
     */
    class Table {
        boolean involved;
        int process;
        int virtualPage;
        boolean handled;
        boolean changed;
        boolean defended;
        int virtualBlock;

        public String toString() {
            return Boolean.toString(involved) + " " + process + " " +
                   virtualPage + " " + handled + " " + changed + " " +
                   defended + " " + virtualBlock + "\n";
        }
    }
}