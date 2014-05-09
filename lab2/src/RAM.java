/**
 * Created by troshchuk on 08.05.14.
 */
public class RAM {
    /** Memory in Kb */
    private int memory;
    /** Page blocks */
    private PageBlocks head;
    /** Length of page blocks */
    private int length;
    /** Length of used page blocks */
    private int used;


    public RAM(int memory) {
        this.memory = memory;
        PageBlocks pageBlocks = new PageBlocks();
        head = pageBlocks;

        length = memory / Page.memory;

        for (int i = 1; i < length; i++) {
            pageBlocks.next = new PageBlocks();
            pageBlocks = pageBlocks.next;
        }
    }

    public int getLength() {
        return length;
    }

    public int getUsedLength() {
        return used;
    }

    public PageBlocks getPageBlocks() {
        return head;
    }

    public boolean isFreeSpace() {
        return length != used;
    }

    public void incrementUsed() {
        used++;
    }

    public void decrementUsed() {
        used--;
    }

    public void setFreeBlock(int block, boolean free) {
        PageBlocks pageBlocks = head;
        for (int i = 0; i < block; i++) {
            pageBlocks = pageBlocks.next;
        }
        pageBlocks.free = free;
    }

    class PageBlocks {
        Page page;
        boolean free = true;

        PageBlocks next;
    }
}