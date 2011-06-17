package hpfs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author Ben Paretzky
 */
public class TaskMessage extends JacobiMessage {

    public TaskMessage(RecursiveTask<Serializable> task, long clientId) {
        super(MSG_TYPE.TASK, clientId, task);
    }

    public RecursiveTask<Serializable> getTask() {
        return (RecursiveTask<Serializable>) data;
    }
}
// copied from JacobiDemoFJ
    
class LeafNode extends Node {

    int max = Integer.MIN_VALUE;
    LeafNode(int[][] start, int left, int top, int right, int bottom, int height, int width) {
        super(start, left, top, right, bottom, height, width);
        
    }

    int stepPoint(int w, int h) {
        int l, r, u, d, nCount; // left, right, up, down
        int[][] from = start;
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
        if (w + 1 < this.width) {
            r = 1;
        } else {
            r = 0;
        }
        if (h + 1 < this.height) {
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
        //to[w][h] = (int) (runVal / nCount);
        return (int) (runVal / nCount);
    }

    @Override
    protected Integer compute() {
        int retVal = Integer.MIN_VALUE, i;
        for (int c = left; c < right; c++) {
            for (int r = top; r < bottom; r++) {
                i = stepPoint(c, r);
                next[c][r] = i;
                if (i > retVal) {
                    retVal = i;
                }
            }
        }
        return max = retVal;
    }

    @Override
    int getMax() {
        return max;
    }
}

class InnerNode extends Node {
    static final int THREASHOLD = 400;
    final Collection<Node> children;
    int max = Integer.MIN_VALUE;

    InnerNode(int[][] start, int left, int top, int right, int bottom, int height, int width) {
        super(start, left, top, right, bottom, height, width);
        children = new ArrayList(4);
        int wMid = (left + right) / 2;
        if (wMid == left) {
            wMid = right;
        }
        int hMid = (top + bottom) / 2;
        if (hMid == top) {
            hMid = bottom;
        }
        if ((right - left) * (bottom - top) > THREASHOLD) {
            children.add(new InnerNode(start, left, top, wMid, hMid, height, width));
            children.add(new InnerNode(start, wMid, top, right, hMid, height, width));
            children.add(new InnerNode(start, left, hMid, wMid, bottom, height, width));
            children.add(new InnerNode(start, wMid, hMid, right, bottom, height, width));
        } else {
            children.add(new LeafNode(start, left, top, wMid, hMid, height, width));
            children.add(new LeafNode(start, wMid, top, right, hMid, height, width));
            children.add(new LeafNode(start, left, hMid, wMid, bottom, height, width));
            children.add(new LeafNode(start, wMid, hMid, right, bottom, height, width));
        }
    }

    @Override
    protected Integer compute() {
        int retVal = Integer.MIN_VALUE;
        //invokeAll(children);
        for (Node n : invokeAll(children)) {
            if (n.getMax() > retVal) {
                retVal = n.getMax();
            }
            n.reinitialize();
        }
        return max = retVal;
    }

    @Override
    int getMax() {
        return max;
    }
}

abstract class Node extends RecursiveTask<Integer> {

    final int left, top, right, bottom, height, width;
    final int[][] start;
    protected int[][] next;
    
    Node(int[][] start, int left, int top, int right, int bottom, int height, int width) {
        this.start = start;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.height = height;
        this.width = width;
        next = new int[height][];
        for( int i = top; i < bottom; ++i ) {
            next[i] = new int[width];
        }
    }

    @Override
    protected abstract Integer compute();
    
    abstract int getMax();
    /**
     * 
     * @return The array used to hold the next frame
     */
    public final int[][] getArray() {
        return start;
    }
}
// end copied classes for Jacobi computations
    
