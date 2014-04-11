/**
 * Automata class for parsing
 */
public class Automata {
    /**
     * Row number show state
     * Column number show char classes:
     * 0 - letter or digit
     * 1 - '-'
     * 2 - '@'
     * 3 - '.'
     * 4 - '_'
     * 5 - empty symbol
     */
                                   //0   1   2   3   4   5
    private final int[][] matrix = {{1, -1, -1, -1, -1, -1},  //0
                                    {1,  2,  3,  9,  2, -1},  //1
                                    {1, -1, -1, -1, -1, -1},  //2
                                    {4, -1, -1, -1, -1, -1},  //3
                                    {4,  5, -1,  6,  5, -1},  //4
                                    {4, -1, -1, -1, -1, -1},  //5
                                    {7, -1, -1, -1, -1, -1},  //6
                                    {7,  8, -1,  6,  8, 10}, //7
                                    {7, -1, -1, -1, -1, -1},  //8
                                    {1, -1, -1, -1, -1, -1}}; //9

    /** Current state */
    private int currentState;

    /**
     * return next state by char class
     *
     * @param charClass char class
     * @return next state
     */
    public int nextState(int charClass) {
        currentState = matrix[currentState][charClass];
        return currentState;
    }

    /** change current state to initial */
    public void setInitialState() {
        currentState = 0;
    }
}
