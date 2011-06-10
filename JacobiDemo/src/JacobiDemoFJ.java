
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;	//jdk7
import java.util.concurrent.RecursiveTask;	//jdk7
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * ver scruffy.Thu Jun  9 00:25:04 EDT 2011
 * Forked from JacobiDemo:   ver. scruffy.Wed Jun  8 08:53:06 EDT 2011
 */
public final class JacobiDemoFJ {

    static final int DEFAULT_WIDTH = 400, DEFAULT_HEIGHT = 200, BORDER = 10, DEFAULT_DELAY = 5,
            THREASHOLD = 400;
    static boolean DEBUG = false;
    BufferedImage img;
    int[][] prev, cur;
    int width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT, delay = DEFAULT_DELAY;
    static final ForkJoinPool pool = new ForkJoinPool();
    final Node rootJob;

    public static void main(String[] args) {
        new JacobiDemoFJ(args).start();
    }

    JacobiDemoFJ(String[] args) {
        parseArgs(args);
        prev = new int[width][height];
        cur = new int[width][height];
        img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        int[] blue = new int[3];
        blue[2] = Byte.MAX_VALUE;
        rootJob = new InnerNode(this, 0, 0, width, height);
    }

    void start() {
        JFrame frame = new JFrame("JacobiDemoForkJoin by Ben Paretzky");
        frame.setPreferredSize(new Dimension(width + (BORDER << 1) + 10, height + (BORDER << 1) + 45));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        JPanel panel = new JPanel() {

            @Override
            public void paint(Graphics g) {
                g.drawImage(img, BORDER, BORDER, null);
            }
        };
        panel.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                int x, y;
                x = e.getX() - BORDER - 1;
                y = e.getY() - BORDER - 1;
                //System.out.printf("(%d,%d)\n", x,y);
                try {
                    cur[x][y] = prev[x][y] = Integer.MAX_VALUE >> 9;
                } catch (IndexOutOfBoundsException ex) {
                }
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        for (;;) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
            }
            step();
            frame.repaint();
        }

    }

    void step() {
        WritableRaster r = img.getRaster();
        int[][] temp = cur;
        cur = prev;
        prev = temp;
        int max = pool.invoke(rootJob);
        for (int i = 0; i < width; i++) {
            if (DEBUG) {
                System.out.println(Arrays.toString(cur[i]));
            }
            for (int j = 0; j < height; j++) {
                int[] local = makeBGR(cur[i][j], max);
                r.setPixel(i, j, local);
            }
        }
        if (DEBUG) {
            System.out.println(max + " " + rootJob.getMax());
        }
        rootJob.reinitialize();
    }

    int[] makeBGR(int b, int max) {
        float percent = (max == 0) ? 0 : ((float) b) / max;
        int[] c = new int[3];
        c[0] = (int) (percent * Byte.MAX_VALUE);
        c[2] = Byte.MAX_VALUE - c[0];
        return c;
    }

    void parseArgs(String[] args) {
        int i = 0;
        for (String s : args) {
            if (s.startsWith("-w")) {
                s = s.substring(2);
                try {
                    i = Integer.parseInt(s);
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (i > 0) {
                    width = i;
                    continue;
                } else {
                    System.out.println("Invalid width");
                    System.exit(1);
                }
            }
            if (s.startsWith("-h") && !s.equals("-help")) {
                s = s.substring(2);
                try {
                    i = Integer.parseInt(s);
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (i > 0) {
                    height = i;
                    continue;
                } else {
                    System.out.println("Invalid height");
                    System.exit(1);
                }
            }
            if (s.startsWith("-d")) {
                s = s.substring(2);
                try {
                    i = Integer.parseInt(s);
                } catch (NumberFormatException ex) {
                    if (s.equals("ebug")) {
                        DEBUG = true;
                    }
                    continue;
                }
                if (i >= 0) {
                    delay = i;
                    continue;
                } else {
                    System.out.println("Invalid delay");
                    System.exit(1);
                }
            }
            if (s.equals("-help") || s.equals("--help")) {
                printHelp();
                System.exit(0);
            }
        }
    }

    int[][] getPreviousData() {
        return prev;
    }

    int[][] getCurrentData() {
        return cur;
    }

    static void printHelp() {
        final String help =
                "Jacobi Relaxation Demo by Ben Paretzky <ben@benparetzky.com> (6/7/11)\n"
                + "Arguments:\n\t-wN\tWidth, N number of pixels.  Default: " + DEFAULT_WIDTH + "\n"
                + "\t-hN\tHeight, N number of pixels.  Default: " + DEFAULT_HEIGHT + "\n"
                + "\t-dN\tDelay, sleep ~N milliseconds inbetween painting.  Default: " + DEFAULT_DELAY + "\n";
        System.out.println(help);
    }

    class LeafNode extends Node {

        final JacobiDemoFJ demo;
        int max = Integer.MIN_VALUE;

        LeafNode(JacobiDemoFJ demo, int left, int top, int right, int bottom) {
            super(left, top, right, bottom);
            this.demo = demo;
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
            if (w + 1 < demo.width) {
                r = 1;
            } else {
                r = 0;
            }
            if (h + 1 < demo.height) {
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
        protected Integer compute() {
            int[][] prev = demo.getPreviousData();
            int[][] cur = demo.getCurrentData();
            int retVal = Integer.MIN_VALUE, i;
            for (int c = left; c < right; c++) {
                for (int r = top; r < bottom; r++) {
                    i = stepPoint(prev, cur, c, r);
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

        final Collection<Node> children;
        final JacobiDemoFJ demo;
        int max = Integer.MIN_VALUE;

        InnerNode(JacobiDemoFJ demo, int left, int top, int right, int bottom) {
            super(left, top, right, bottom);
            this.demo = demo;
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
                children.add(new InnerNode(demo, left, top, wMid, hMid));
                children.add(new InnerNode(demo, wMid, top, right, hMid));
                children.add(new InnerNode(demo, left, hMid, wMid, bottom));
                children.add(new InnerNode(demo, wMid, hMid, right, bottom));
            } else {
                children.add(new LeafNode(demo, left, top, wMid, hMid));
                children.add(new LeafNode(demo, wMid, top, right, hMid));
                children.add(new LeafNode(demo, left, hMid, wMid, bottom));
                children.add(new LeafNode(demo, wMid, hMid, right, bottom));
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

        final int left, top, right, bottom;

        Node(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        @Override
        protected abstract Integer compute();

        abstract int getMax();
    }
}
