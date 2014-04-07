/**
 * Created by lansk8er on 07.04.14.
 */
public class Automata {
    //AZ09  -   @   .   _  \0
    private final int[][] matrix = {{1, -1, -1, -1, -1, -1},
                                    {1,  2,  3, -1,  2, -1},
                                    {1, -1, -1, -1, -1, -1},
                                    {4, -1, -1, -1, -1, -1},
                                    {4,  5, -1,  6,  5, -1},
                                    {4, -1, -1, -1, -1, -1},
                                    {7, -1, -1, -1, -1, -1},
                                    {7,  8, -1,  6,  8,  9},
                                    {7, -1, -1, -1, -1, -1}};

    private int currentState;

    public int nextState(int charClass) {
        currentState = matrix[currentState][charClass];
        return currentState;
    }

    public void setInitialState() {
        currentState = 0;
    }
}
