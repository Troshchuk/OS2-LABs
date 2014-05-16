/**
 * Virtual address space
 *
 * @author Dmytro Troshchuk
 * @version 1.02 08.05.14
 */
public class VirtualAddressSpace {
    /** Head of virtual pages */
    private VirtualPages head;
    /** Length virtual address space in virtual pages */
    private int length;

    /**
     * @param memory memory in Kb
     */
    public VirtualAddressSpace(int memory) {
        VirtualPages virtualPages = new VirtualPages();

        length = memory / Page.memory;

        virtualPages.page = new Page();
        head = virtualPages;
        for (int i = 1; i < length; i++) {
            virtualPages.next = new VirtualPages();
            virtualPages = virtualPages.next;
            virtualPages.page = new Page();
        }
    }

    /**
     * @return head of virtual pages
     */
    public VirtualPages getVirtualPages() {
        return head;
    }

    /**
     * @return length virtual address space in virtual pages
     */
    public int getLength() {
        return length;
    }

    /** Class virtual pages */
    class VirtualPages {
        Page page;
        VirtualPages next;
    }
}