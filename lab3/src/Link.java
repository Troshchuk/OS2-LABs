import java.nio.ByteBuffer;

/**
 * Link
 * Each link have size 32 bytes
 *
 * @author Dmytro Troshchuk
 * @version 1.00 18.05.14
 */
class Link {
    /** Inode id */
    private int inodeId;
    /* Filename */
    private String filename;

    /**
     * Create link. Set to inode id -1
     */
    public Link() {
        inodeId = -1;
        filename = "";
    }

    /**
     * Create link by inodeId and filename
     *
     * @param inodeId inode id
     * @param filename filename
     */
    public Link(int inodeId, String filename) {
        set(inodeId, filename);
    }

    /**
     * Set inode id and filename to this  link.
     * If filename bigger than max filename return false result
     *
     * @param inodeId inode id
     * @param filename filename
     * @return return result of setting
     */
    public boolean set(int inodeId, String filename) {
        if (filename.length() > Helper.MAX_FILE_NAME) {
            return false;
        }

        this.filename = filename;
        this.inodeId = inodeId;
        return true;
    }

    /**
     * Converte this link to the bytes
     *
     * @return converted link
     */
    public byte[] toBytes() {
        ByteBuffer bb = ByteBuffer.allocate(32);
        String filename = this.filename;
        while (filename.length() < Helper.MAX_FILE_NAME) {
            filename = filename + " ";
        }

        bb.putInt(inodeId);
        char[] chars = filename.toCharArray();
        for (int i = 0; i < Helper.MAX_FILE_NAME; i++) {
            bb.putChar(chars[i]);
        }

        return bb.array();
    }

    /**
     * Converte bytes to link
     *
     * @param bytes array of bytes
     */
    public void load(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        inodeId = bb.getInt();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < Helper.MAX_FILE_NAME; i++) {
            char c = bb.getChar();
            if (c == ' ') {
                break;
            }
            s.append(c);
        }

        filename = s.toString();
    }

    /**
     * @return inode id
     */
    public int getInodeId() {
        return inodeId;
    }

    /*
     * @return filename
     */
    public String getFilename() {
        return filename;
    }
}
