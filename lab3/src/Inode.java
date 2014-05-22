import java.nio.ByteBuffer;

/**
 * Inode
 *
 * @author Dmytro Troshchuk
 * @version 1.00 17.05.14
 */
class Inode {
    /** Id of regular file */
    public final static int REGULAR = 0;
    /** Id of directory */
    public final static int DIRECTORY = 1;
    /** Id of symlink */
    public final static int SYMLINK = 2;
    /**
     * File type
     * 0 - Regular
     * 1 - Directory
     * 2 - Symlink
     */
    byte fileType;
    /** Size of inode in b */
    int size;
    /** Count of links */
    byte links;
    /** Number of blocks where this file is */
    int[] numberOfBlocks;
    /** Filling up to 256 bytes */
    byte[] RESERVED = new byte[2];

    public Inode() {
        int length = Helper.MAX_FILE_SIZE;
        numberOfBlocks = new int[length];
        for (int i = 0; i < length; i++) {
            numberOfBlocks[i] = -1;
        }
    }

    /**
     * Converte this inode to bytes and return it
     *
     * @return converted inode in bytes
     */
    public byte[] toByte() {
        ByteBuffer bb = ByteBuffer.allocate(256);
        bb.put(fileType);
        bb.putInt(size);
        bb.put(links);
        for (int i = 0; i < numberOfBlocks.length; i++) {
            bb.putInt(numberOfBlocks[i]);
        }
        for (int i = 0; i < RESERVED.length; i++) {
            bb.put(RESERVED[i]);
        }
        return bb.array();
    }

    /**
     * Converte bytes to inode
     *
     * @param bytes array of bytes
     */
    public void load(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        fileType = bb.get();
        size = bb.getInt();
        links = bb.get();
        for (int i = 0; i < numberOfBlocks.length; i++) {
            numberOfBlocks[i] = bb.getInt();
        }
    }
}
