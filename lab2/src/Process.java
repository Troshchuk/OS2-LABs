import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by troshchuk on 10.05.14.
 */
public class Process {
    private VirtualAddressSpace virtualAddressSpace;

    public Process(Program program) {
        virtualAddressSpace = new VirtualAddressSpace(program.getMemory());
    }

    public VirtualAddressSpace getVirtualAddressSpace() {
        return virtualAddressSpace;
    }
}
