/**
 * Created by troshchuk on 08.05.14.
 */

//Must be rewrite
public class HardDrive {
    /** memory in Kb */
    private int memory;

    public HardDrive() {
        memory = 1 << 30;
    }

    public void rewritePage(Page page) {

    }

    public Page loadPage() {
        return new Page();
    }
}
