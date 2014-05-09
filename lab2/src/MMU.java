import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/**
 * Memory Management Unit
 * Created by troshchuk on 08.05.14.
 */
public class MMU {
    private RAM ram;
    private ArrayList<Table> table;
    private ArrayList<Integer> processIndex;
    private HardDrive hardDrive;

    public MMU(RAM ram) {
        this.ram = ram;
        table = new ArrayList<Table>();
        processIndex = new ArrayList<Integer>();
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

    public int getSizeOfTable() {
        return table.size();
    }

    class Table {
        boolean involved;
        int process;
        int virtualPage;
        boolean handled;
        boolean changed;
        boolean defended;
        int virtualBlock;
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

    public void doCommand(int process, int command, int readOrWrite) {
        int virtualPage = command / 1024;

        Iterator<Table> itr = table.listIterator(process);

        while (true) {
            Table t = itr.next();

            if (t.virtualPage == virtualPage) {
                if (t.involved) {
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

                    // Must be rewrite
                    Page page = hardDrive.loadPage();
                    pageBlocks.page = page;
                    pageBlocks.free = false;
                    //

                    ram.incrementUsed();

                    t.involved = true;
                    t.virtualBlock = count;
                    t.handled = true;
                    if (readOrWrite == 1) {
                        t.changed = true;
                    }
                } else {
                    NRU();
                }

                break;
            }
        }
    }

    private void NRU() {

    }

    public void checkHandled() {
        while (true) {
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
    }
}
