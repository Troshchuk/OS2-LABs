import java.nio.ByteBuffer;

/**
 * Super Block
 *
 * @author Dmytro Troshchuk
 * @version 1.00 17.05.14
 */
public class SuperBlock {
    /** Block size in bytes */
    int BLOCK_SIZE = 1024;
    /** Inode suze in bytes */
    int INODE_SIZE = 256;
    /** Blocks count */
    int BLOCKS_COUNT = (Helper.MEMORY * 1024) / BLOCK_SIZE;
    /** Descriptor count */
    int INODES_COUNT = Helper.INODES_COUNT;
    /** Blocks bitmaps count */
    int BLOCKS_BITMAPS_COUNT;
    /** Inodes bitmaps count */
    int INODES_BITMAPS_COUNT;
    /** Free blocks count */
    int FREE_BLOCKS_COUNT;
    /** Free inodes count */
    int FREE_INODES_COUNT;
    /** First data block */
    int FIRST_DATA_BLOCK;
    /** Link size in bytes */
    int LINK_SIZE = 32;
    /** Data blocks count */
    int DATA_BLOCKS_COUNT;
    /** Filling up to 1024 bytes */
    int[] RESERVED = new int[((BLOCK_SIZE) - 44) / 4];

    /**
     * Converte superblock to bytes array
     *
     * @return converted superblock in bytes array
     */
    public byte[] toBytes() {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        bb.putInt(BLOCK_SIZE);
        bb.putInt(INODE_SIZE);
        bb.putInt(BLOCKS_COUNT);
        bb.putInt(INODES_COUNT);
        bb.putInt(BLOCKS_BITMAPS_COUNT);
        bb.putInt(INODES_BITMAPS_COUNT);
        bb.putInt(FREE_BLOCKS_COUNT);
        bb.putInt(FREE_INODES_COUNT);
        bb.putInt(FIRST_DATA_BLOCK);
        bb.putInt(LINK_SIZE);
        bb.putInt(DATA_BLOCKS_COUNT);
        for(int i = 0; i < RESERVED.length; i++) {
            bb.putInt(RESERVED[i]);
        }
        return bb.array();
    }

    /**
     * Converte bytes to superblock
     *
     * @param bytes bytes array
     */
    public void load(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        BLOCK_SIZE = bb.getInt();
        INODE_SIZE = bb.getInt();
        BLOCKS_COUNT = bb.getInt();
        INODES_COUNT = bb.getInt();
        BLOCKS_BITMAPS_COUNT = bb.getInt();
        INODES_BITMAPS_COUNT = bb.getInt();
        FREE_BLOCKS_COUNT = bb.getInt();
        FREE_INODES_COUNT = bb.getInt();
        FIRST_DATA_BLOCK = bb.getInt();
        LINK_SIZE = bb.getInt();
        DATA_BLOCKS_COUNT = bb.getInt();
    }
}