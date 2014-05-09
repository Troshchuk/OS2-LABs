import java.util.ArrayList;

/**
 * Created by troshchuk on 08.05.14.
 */
public class OS {
    private RAM ram;
    private ArrayList<VirtualAddressSpace> virtualAddressSpaces;
    private MMU mmu;

    public OS(RAM ram) {
        this.ram = ram;
        mmu = new MMU(ram);
        virtualAddressSpaces = new ArrayList<VirtualAddressSpace>();
    }

    public void run() {
        mmu.checkHandled();
    }

    public void runProcess(Process process) {
        final VirtualAddressSpace virtualAddressSpace = new VirtualAddressSpace(process);
        if (ram.isFreeSpace()) {
            virtualAddressSpaces.add(virtualAddressSpace);
            final int processIndex = virtualAddressSpaces.indexOf(virtualAddressSpace);
            mmu.add(virtualAddressSpace, processIndex);
            loadProcess(virtualAddressSpace);

            final int lifetimeProcess = (int) (Math.random() * 100);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < lifetimeProcess; i++) {
                        int readOrWrite = (int) (Math.random() * 2);

                        int command = (int) (Math.random() * 1024 * virtualAddressSpace.getLength());
                        mmu.doCommand(processIndex, command, readOrWrite);

                    }
                    killProcess(virtualAddressSpace);
                }
            });

            thread.start();


        } else {
            System.out.println("Process cannot be load to RAM. RAM is full");
        }
    }

    // Must be rewrite
    public void loadProcess(VirtualAddressSpace virtualAddressSpace) {

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

            mmu.copyToRam(virtualAddressSpaces.indexOf(virtualAddressSpace), i - usedLength, i);

            ram.incrementUsed();
            ram.setFreeBlock(i, false);
        }
    }

    public void killProcess(VirtualAddressSpace virtualAddressSpace) {
        mmu.removeProcess(virtualAddressSpaces.indexOf(virtualAddressSpace));


    }
}
