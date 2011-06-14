package hpfs;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author ben
 */
public class JacobiTask extends RecursiveTask<Integer> {

    private static final int THRESHOLD = 300;
    private final int[][] prev, cur;
    final int yOffset;  // data[N] is actually data[yOffset+N]
    final List<JacobiTask> childList;

    /**
     * For prev and cur, if you want this to work on copies of your data: give
     * it copies.
     * @param prev
     * @param cur
     * @param yOffset
     */
    public JacobiTask(int[][] prev, int[][] cur, int yOffset) {
        int[][] tempPrev, tempCur;
        int i = 0;
        this.prev = prev;
        this.cur = cur;
        this.yOffset = yOffset;
        if ((prev.length * prev[0].length) < THRESHOLD) {
            childList = null;
        } else {
            childList = new LinkedList<>();
            i = prev.length >> 1;
            tempPrev = Arrays.copyOfRange(prev, 0, i);
            tempCur = Arrays.copyOfRange(cur, 0, i);
            childList.add(new JacobiTask(tempPrev, tempCur, yOffset));
            tempPrev = Arrays.copyOfRange(prev, i, i << 1);
            tempCur = Arrays.copyOfRange(cur, i, i << 1);
            childList.add(new JacobiTask(tempPrev, tempCur, i + yOffset));
        }
    }

    int stepPoint(int[][] from, int[][] to, int w, int h) {
        int l, r, u, d, nCount; // left, right, up, down
        if (w != 0) {
            l = 1;
        } else {
            l = 0;
        }
        if (h != 0) {
            u = 1;
        } else {
            u = 0;
        }
        if (w + 1 < from[0].length) {
            r = 1;
        } else {
            r = 0;
        }
        if (h + 1 < from.length) {
            d = 1;
        } else {
            d = 0;
        }
        nCount = l + u + r + d;
        double runVal = 0d;
        if (l == 1) {
            runVal += from[w - 1][h];
        }
        if (u == 1) {
            runVal += from[w][h - 1];
        }
        if (r == 1) {
            runVal += from[w + 1][h];
        }
        if (d == 1) {
            runVal += from[w][h + 1];
        }
        runVal += from[w][h];
        nCount++;
        to[w][h] = (int) (runVal / nCount);
        return to[w][h];
    }

    @Override
    public Integer compute() {
        int max = Integer.MIN_VALUE + 1;
        if (childList != null) {
            for (JacobiTask t : invokeAll(childList)) {
                try {
                    if (max < t.get()) {
                        max = t.get();
                    }
                } catch (InterruptedException ex) {
                    continue;
                } catch (ExecutionException ex) {
                    return Integer.MIN_VALUE;
                }
            }
            return max;
        }
        for(int i = 0; i < prev.length) {

        }
    }
}
