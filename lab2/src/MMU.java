import com.sun.deploy.panel.ITreeNode;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Memory Management Unit
 * Created by troshchuk on 08.05.14.
 */
public class MMU {
    private RAM ram;
    private CPU cpu;
    private ArrayList<Table> table;
    private ArrayList<Integer> processIndex;
    private HardDrive hardDrive;

    public MMU(RAM ram) {
        this.ram = ram;
        table = new ArrayList<Table>();
        processIndex = new ArrayList<Integer>();
        cpu = new CPU();
        hardDrive = new HardDrive();
    }

    public void add(VirtualAddressSpace virtualAddressSpace, int process) {
        int length = virtualAddressSpace.getLength();
        processIndex.add(table.size());
        for (int i = 0; i < length; i++) {
            Table t = new Table();
            t.process = process;
            t.virtualPage = i;
            table.add(t);
        }

    }

    public synchronized boolean addProgramToHardDrive(Program program) {
        return hardDrive.addProgram(program);
    }

    public int getNumberOfPrograms() {
        return hardDrive.getNumberOfPrograms();
    }

    public Process loadProgramToRam(int program) {
        Process process = new Process(hardDrive.getProgram(program));

        add(process.getVirtualAddressSpace(), program);

        VirtualAddressSpace virtualAddressSpace = process.getVirtualAddressSpace();

        int lengthPageBlocks = ram.getLength();
        int lengthVirtualPages = virtualAddressSpace.getLength();

        RAM.PageBlocks pageBlocks = ram.getPageBlocks();
        VirtualAddressSpace.VirtualPages virtualPages = virtualAddressSpace.getVirtualPages();

        int usedLength = ram.getUsedLength();

        for (int i = 0; i < usedLength; i++) {
            pageBlocks = pageBlocks.next;
        }

        for (int i = usedLength, j = 0; j < lengthVirtualPages && i < lengthPageBlocks; i++, j++) {
            pageBlocks.page = virtualPages.page;

            pageBlocks = pageBlocks.next;
            virtualPages = virtualPages.next;

            copyToRam(program, i - usedLength, i);

            ram.incrementUsed();
            ram.setFreeBlock(i, false);
        }
        return process;
    }

    public void copyToRam(int process, int virtualPage, int virtualBlock) {
        Table t = table.get(virtualPage + processIndex.get(process));
        t.involved = true;
        t.virtualBlock = virtualBlock;
    }

    public void removeProcess(int process) {
        int index = processIndex.get(process);
        ArrayList<Integer> list = new ArrayList<Integer>();

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

    public void checkHandled() {

        int length = table.size();


        for (int i = 0; i < length; i++) {
            if (table.get(i).involved && table.get(i).handled) {
                table.get(i).handled = false;
            }
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

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


    private void NRU(int process, int virtualPage, int readOrWrite) {
        int length = table.size();
        int low = 3;
        ArrayList<Integer> classes = new ArrayList<Integer>();
        for (int i = 0; i < length; i++) {
            Table t = table.get(i);

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

    public String toString() {
        int length = table.size();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            Table t = table.get(i);
            sb.append(t.toString());
        }
        sb.append("\n");

        return sb.toString();
    }


    class Table {
        boolean involved;
        int process;
        int virtualPage;
        boolean handled;
        boolean changed;
        boolean defended;
        int virtualBlock;

        public String toString() {
            return Boolean.toString(involved) + " " + process + " " + virtualPage + " " + handled + " " + changed + " " + defended + " " + virtualBlock + "\n";
        }
    }


}
