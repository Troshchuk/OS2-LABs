/**
 * Created by troshchuk on 08.05.14.
 */
public class VirtualAddressSpace {
    private VirtualPages head;
    private int length;

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

    public VirtualPages getVirtualPages() {
        return head;
    }

    public int getLength() {
        return length;
    }

    class VirtualPages {
        Page page;

        VirtualPages next;
    }
}
