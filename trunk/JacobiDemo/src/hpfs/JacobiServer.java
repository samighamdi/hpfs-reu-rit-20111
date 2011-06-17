package hpfs;

import java.awt.image.BufferedImage;
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

    public static void main(String[] args) {
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
        String[] addrParts = address.split(":", 2);
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
            if (DEBUG) {
                ex.printStackTrace();
            }
        } catch (NumberFormatException ex) {
        }
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
        if (client == null) {
            return false;
        }
        try {
            client.socket.close();
        } catch (IOException ex) {
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
                    switch (s) {
                        case "a":
                        case "add":
                            s = stdIn.next();
                            if (addClient(s)) {
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
                            if (removeClient(l)) {
                                System.out.println("Successfully removed client: " + l);
                            } else {
                                System.out.println("Unable to remove client");
                            }
                            break;
                        case "help":
                            printREPLHelp();
                            break;
                    }
                } while (!s.equalsIgnoreCase("q") && !s.equalsIgnoreCase("quit"));
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

    private static class nextFrameTask extends RecursiveTask {

        private int[][] cur, prev;
        private int startx, starty, width, height;

        public nextFrameTask(int[][] frame, int startx, int starty, int height, int width) {
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
            InnerNode compute = new InnerNode(prev, startx, starty, startx + width,
                    starty + height, height, width);
            int max = compute.invoke();

            return compute.getArray();
        }
    }

    private static class renderImageTask extends RecursiveTask {
        
        int[][] frame;
        BufferedImage img;
        int width, height, top, bottom, max;
        public renderImageTask( int[][] frame, int width, int height, int top, int bottom, int max ) {
            this.frame = frame;
            img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);   
            this.width = width;
            this.height = height;
            this.top = top;
            this.bottom = bottom;
            this.max = max;
        }
        
        @Override
        protected Object compute() {
            WritableRaster ras = img.getRaster();
            for( int i = top; i < bottom; ++i ) {
                for( int j = 0; j < width; ++j ) {
                    ras.setPixel(i, j, makeBGR(frame[i][j], max));
                }
            }
                
                
            return img;
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
}
