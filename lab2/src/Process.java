/**
 * Process
 *
 * @author Dmytro Troshchuk
 * @version 1.02 08.05.14
 */
public class Process {
    /** Virtual address space */
    private VirtualAddressSpace virtualAddressSpace;

    /**
     * @param program program
     */
    public Process(Program program) {
        virtualAddressSpace = new VirtualAddressSpace(program.getMemory());
    }

    /**
     * @return virtual address space of process
     */
    public VirtualAddressSpace getVirtualAddressSpace() {
        return virtualAddressSpace;
    }
}