import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Ben Paretzky
 * @version Wed Jun  8 08:53:06 EDT 2011
 * Serial JacobiDemo
 * Run with -help for usage.
 * Simplied Jacobi Relaxtion with Swing GUI.
 */
public final class JacobiDemo {

    static final int DEFAULT_WIDTH = 400, DEFAULT_HEIGHT = 200, BORDER = 10, DEFAULT_DELAY = 40;
    static boolean DEBUG = false;
    BufferedImage img;
    int[][] data;
    int width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT, delay = DEFAULT_DELAY;

    public static void main(String[] args) {
        new JacobiDemo(args).start();
    }

    /**
     * @param args Arguments passed from command line, check -help
     */
    JacobiDemo(String[] args) {
        parseArgs(args);
        data = new int[width][height];
        img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        int[] blue = new int[3];
        blue[2] = Byte.MAX_VALUE;
    }

    void start() {
        JFrame frame = new JFrame("JacobiDemo by Ben Paretzky");
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
                    data[x][y] = Integer.MAX_VALUE >> 5;
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
            updateImg();
            frame.repaint();
            if (DEBUG) {
                for (int i = 0; i < width; i++) {
                    System.out.println(Arrays.toString(data[i]));
                }
                System.out.println("");
            }
        }

    }

    void updateImg() {
        WritableRaster r = img.getRaster();
        int nCount = 0, sum; // Neighbor Count
        int max = 0;
        int newVal = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                nCount = sum = newVal = 0;
                if (i != 0) {
                    nCount++;
                    sum += data[i - 1][j];
                }
                if (j != height - 1) {
                    nCount++;
                    sum += data[i][j + 1];
                }
                if (i != width - 1) {
                    nCount++;
                    sum += data[i + 1][j];
                }
                if (j != 0) {
                    nCount++;
                    sum += data[i][j - 1];
                }
                sum += data[i][j];
                nCount++;
                newVal = sum / nCount;
                if (newVal > max) {
                    max = newVal;
                }
                if (newVal != data[i][j]) {
                    data[i][j] = newVal;
                }
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                r.setPixel(i, j, makeBGR(data[i][j], max));
            }
        }
    }

    /**
     * Given a number and a max from a set, returns int [] BGR value from
     * blue to red.  b/max == 1 for Red, and b == 0 for Blue
     * @param b Value
     * @param max Maximum
     * @return BGR array. All values will sum to where 0 <= (b / max)  <= 1
     */
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
            if (s.startsWith("-h") && !s.startsWith("-help")) {
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

    /**
     * Prints help to stdout
     */
    static void printHelp() {
        final String help =
                "Jacobi Relaxation Demo by Ben Paretzky <ben@benparetzky.com> (6/7/11)\n"
                + "Arguments:\n\t-wN\tWidth, N number of pixels.  Default: " + DEFAULT_WIDTH + "\n"
                + "\t-hN\tHeight, N number of pixels.  Default: " + DEFAULT_HEIGHT + "\n"
                + "\t-dN\tDelay, sleep ~N milliseconds inbetween painting.  Default: " + DEFAULT_DELAY + "\n";
        System.out.println(help);
    }
}
