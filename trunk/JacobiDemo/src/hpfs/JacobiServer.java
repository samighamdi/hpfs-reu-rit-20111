package hpfs;

import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.NumberFormatException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author Ben Paretzky
 */
public class JacobiServer {

    static final Long EHLO = 0xbeefFA1L;
    private static final boolean DEBUG = true;
    private final Map<Long, MetaClient> clientMap;
    private final Selector selector; //All channels attach their clientId

    public static void main(String [] args) {
        JacobiServer js = null;
        try {
            js = new JacobiServer();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        js.forkREPL();
    }

    public JacobiServer() throws IOException {
        clientMap = new ConcurrentSkipListMap<>();
        selector = Selector.open();
    }

    private boolean addClient(String address) {
        String [] addrParts = address.split(":",2);
        MetaClient client = null;
        long clientId = Long.MIN_VALUE;
        boolean retVal = false;
        try {
            InetAddress addr = InetAddress.getByName(addrParts[0]);
            int port = Integer.parseInt(addrParts[1]);
            Socket s = new Socket(addr, port);
            client = new MetaClient(s);
            clientId = client.in.readLong();
            clientMap.put(clientId, client);
            s.getChannel().configureBlocking(false);
            s.getChannel().register(selector, SelectionKey.OP_READ, clientId);
            retVal = true;
        } catch (IOException ex) {
            if(DEBUG) {
                ex.printStackTrace();
            }
        } catch(NumberFormatException ex) {}
        return retVal;
    }

    private void printStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("JacobiServer - Front-end Node\n");
        sb.append("Currently connected to ");
        sb.append(clientMap.size());
        sb.append(" compute nodes.\n");
    }

    private boolean removeClient(long clientId) {
        MetaClient client = clientMap.remove(clientId);
        if(client == null) {
            return false;
        }
        try {
            client.socket.close();
        } catch(IOException ex) {
            
        }
        return true;
    }

    public void printREPLHelp() {
        
    }

    public void forkREPL() {
        new Thread() {
            @Override
            public void run() {
                Scanner stdIn = new Scanner(System.in);
                String s = null;
                long l = 0;
                System.out.println("Starting JacobiServer Admin Console");
                do {
                    System.out.print("> ");
                    s = stdIn.next();
                    switch(s) {
                        case "a":
                        case "add":
                            s = stdIn.next();
                            if(addClient(s)) {
                                System.out.println("Successfully added " + s);
                            } else {
                                System.out.println("Unable to add client");
                            }
                            break;
                        case "s":
                        case "status":
                            printStatus();
                            break;
                        case "r":
                        case "remove":
                            l = stdIn.nextLong();
                            if(removeClient(l)) {
                                System.out.println("Successfully removed client: " + l);
                            } else {
                                System.out.println("Unable to remove client");
                            }
                            break;
                        case "help":
                            printREPLHelp();
                            break;
                    }
                } while(!s.equalsIgnoreCase("q") && !s.equalsIgnoreCase("quit"));
            }
        }.start();
    }

    private static class MetaClient {
        final Socket socket;
        final ObjectOutputStream out;
        final ObjectInputStream in;
        MetaClient(Socket s) throws IOException {
            socket = s;
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
        }
    }
    
    // copied from JacobiDemoFJ
    
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
        int max = Integer.MIN_VALUE;

        InnerNode(int left, int top, int right, int bottom) {
            super(left, top, right, bottom);
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
                children.add(new InnerNode(left, top, wMid, hMid));
                children.add(new InnerNode(wMid, top, right, hMid));
                children.add(new InnerNode(left, hMid, wMid, bottom));
                children.add(new InnerNode(wMid, hMid, right, bottom));
            } else {
                children.add(new LeafNode(left, top, wMid, hMid));
                children.add(new LeafNode(wMid, top, right, hMid));
                children.add(new LeafNode(left, hMid, wMid, bottom));
                children.add(new LeafNode(wMid, hMid, right, bottom));
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
    
    // end copied classes for Jacobi computations
    
    private static class nextFrameTask extends RecursiveTask {
        private int[][] cur, prev;
        private int startx, starty, width, height;
        
        public nextFrameTask( int[][] frame, int startx, int starty, int width, int height ) {
            this.cur = frame;
            this.prev = frame;
            this.startx = startx;
            this.starty = starty;
            this.width = width;
            this.height = height;
        }
        
        @Override
        protected Object compute() {
            // compute next frame of Jacobi; champ = max value in new array
            int[][] temp = cur;
            cur = prev;
            prev = temp;
        
            
            return max;
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
    }
    
    private static class renderImageTask extends RecursiveTask {
        @Override
        protected Object compute() {
            return this;
        }
    }
}
