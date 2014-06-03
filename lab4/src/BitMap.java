/**
 * Bit map
 *
 * @author Dmytro Troshchuk
 * @version 1.00 17.05.14
 */
class BitMap {
    /** Block size in b */
    private static int blockSize;
    /** bits */
    private byte[] bits;

    /**
     * Create bits map on bytes array
     */
    public BitMap() {
        bits = new byte[blockSize];
    }

    /**
     * @return bits (array of bytes)
     */
    public byte[] getBits() {
        return bits;
    }

    /**
     * Set bit on bitmap in index
     *
     * @param index index
     * @param bit bit (1 or 0)
     */
    public void setBits(int index, byte bit) {
        int i = index / 8;
        StringBuilder binaryString = new StringBuilder();
        binaryString.append(
                String.format("%8s", Integer.toBinaryString(bits[i]))
                      .replace(' ', '0')
                           );
        binaryString.setCharAt(7 - (index % 8), Byte.toString(bit).charAt(0));
        bits[i] = (byte) Integer.parseInt(binaryString.toString(), 2);
    }

    /**
     * Set bits on bitmap
     *
     * @param bits bits
     */
    public void setBits(byte[] bits) {
        this.bits = bits;
    }

    /**
     * Set size of bitmap
     *
     * @param blockSizeToSet block size to set
     */
    public static void setBlockSize(int blockSizeToSet) {
        blockSize = blockSizeToSet;
    }
}
