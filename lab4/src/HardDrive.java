/**
 * Hard drive
 *
 * @author Dmytro Troshchuk
 * @version 1.00 17.05.14
 */
public class HardDrive {
    /** Memory in Kb */
    private final static int MEMORY = Helper.MEMORY;
    /** File where saved virtual hard drive */
    private final static String file = "file.img";

    /**
     * @return size of memory of hard drive
     */
    public int getMemory() {
        return MEMORY;
    }

    /*
     * @return file, where fs is
     */
    public String getFile() {
        return file;
    }
}
