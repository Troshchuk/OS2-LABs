import java.nio.ByteBuffer;

/**
 * Block
 *
 * @author Dmytro Troshchuk
 * @version 1.00 17.05.14
 */
public class Block {
    /** Block size in b */
    private static int blockSize;
    /** Bytes of file */
    private final byte[] bytes;

    /**
     * Create block.
     */
    public Block() {
        bytes = new byte[blockSize];
    }

    /**
     * Create block by links array
     *
     * @param links links array
     */
    public Block(Link[] links) {
        ByteBuffer bb = ByteBuffer.allocate(blockSize);
        int length = links.length;
        for (int i = 0; i < length; i++) {
            bb.put(links[i].toBytes());
        }
        bytes = bb.array();
    }

    /**
     * Create block by bytes array
     *
     * @param bytes bytes array
     */
    public Block(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * @return bytes of block
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * @param begin start index
     * @param end end index
     * @return bytes of sector
     */
    public byte[] getSector(int begin, int end) {
        byte[] bytes1 = new byte[end - begin];
        for (int i = begin, j = 0; i < end; i++, j++) {
            bytes1[j] = bytes[i];
        }
        return bytes1;
    }

    /**
     * write bytes to sector
     *
     * @param bytes bytes to write
     * @param begin start index
     */
    public void writeSector(byte[] bytes, int begin) {
        for (int i = begin, j = 0; j < bytes.length; i++, j++) {
            this.bytes[i] = bytes[j];
        }
    }

    /**
     * Set size of block
     *
     * @param blockSizeToSet block size to set
     */
    public static void setBlockSize(int blockSizeToSet) {
        blockSize = blockSizeToSet;
    }
}
