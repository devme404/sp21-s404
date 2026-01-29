package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> listNoResizing = new AListNoResizing<>();
        BuggyAList<Integer> listBuggy = new BuggyAList<>();

        for (int i = 0; i < 3; i++) {
            listNoResizing.addLast(i);
            listBuggy.addLast(i);
            assertEquals(listNoResizing.getLast(), listBuggy.getLast());
        }

        for (int i = 0; i < 3; i++) {
            assertEquals(listNoResizing.removeLast(), listBuggy.removeLast());
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> L2 = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 5);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                L2.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int size2 = L2.size();
            } else if (operationNumber == 2) {
                // getLast
                if (L.size() == 0 || L2.size() == 0) {
                    // System.out.println("list is empty, skip getLast");
                } else {
                    int val = L.getLast();
                    int val2 = L2.getLast();
                }
            } else if (operationNumber == 3) {
                // removeLast
                if (L.size() == 0 || L2.size() == 0) {
                    // System.out.println("list is empty, skip removeLast");
                } else {
                    int val = L.removeLast();
                    int val2 = L2.removeLast();
                }
            } else if (operationNumber == 4) {
                // get
                if (L.size() == 0 || L2.size() == 0) {
                    // System.out.println("list is empty, skip get");
                } else {
                    int index = StdRandom.uniform(0, L.size());
                    int val = L.get(index);
                    int val2 = L2.get(index);
                }
            }
        }
    }
}
