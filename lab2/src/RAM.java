/**
 * RAM
 *
 * @author Dmytro Troshchuk
 * @version 1.02 08.05.14
 */
public class RAM {
    /** Memory in Kb */
    private final int memory;
    /** Head of page blocks */
    private final PageBlocks head;
    /** Length of page blocks */
    private final int length;
    /** Length of used page blocks */
    private int used;

    /**
     * @param memory memory in Kb
     */
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

    /**
     * @return length of page blocks
     */
    public int getLength() {
        return length;
    }

    /**
     * @return length of used page blocks
     */
    public int getUsedLength() {
        return used;
    }

    /**
     * @return head of page blocks
     */
    public PageBlocks getPageBlocks() {
        return head;
    }

    /**
     * @return true if is free space
     */
    public boolean isFreeSpace() {
        return length != used;
    }

    /** Increment used */
    public void incrementUsed() {
        used++;
    }

    /** Decrement used */
    public void decrementUsed() {
        used--;
    }

    /**
     * Set bit free of block
     *
     * @param block number of block
     * @param free bit of block
     */
    public void setFreeBlock(int block, boolean free) {
        PageBlocks pageBlocks = head;
        for (int i = 0; i < block; i++) {
            pageBlocks = pageBlocks.next;
        }
        pageBlocks.free = free;
    }

    /** Class page block */
    class PageBlocks {
        Page page;
        boolean free = true;

        PageBlocks next;
    }
}