package hpfs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Ben Paretzky
 */
public class JacobiClient extends Thread {

    private static final AtomicInteger CLIENT_COUNTER = new AtomicInteger(0);
    private static final boolean DEBUG = true;
    private static final int TCP_BACKLOG = 16;
    private static final ForkJoinPool pool = new ForkJoinPool();
    private final InetAddress listenAddress;
    private final int listenPort, localId;

    public JacobiClient(InetAddress listenAddress, int port) {
        super();
        this.listenAddress = listenAddress;
        this.listenPort = port;
        this.localId = CLIENT_COUNTER.getAndIncrement();
        this.setName("JacobiClient-" + this.localId);
    }

    public static void main(String[] args) {
        InetAddress addr = InetAddress.getLoopbackAddress();
        int port = 15000;
        new JacobiClient(addr, port).start();
    }

    @Override
    public void run() {
        ServerSocket listenSocket = null;
        Socket s = null;
        try {
            listenSocket = new ServerSocket(listenPort, TCP_BACKLOG, listenAddress);
            listenSocket.setSoTimeout(0);
        } catch (IOException ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        }
        if (listenSocket == null) {
            return;
        }
        for (;;) {
            try {
                s = listenSocket.accept();
                handleNewReq(s);
            } catch (IOException ex) {
                break;
            }
        }

    }

    private void handleNewReq(final Socket s) {
        new Thread() {
            /*
             * There is some room for optimizations here, though it is worth
             * noting that calling new Thread here probably won't be creating
             * new os threads for each call.
             */

            @Override
            public void run() {
                try {
                    labelA:
                    {
                        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                        RecursiveTask task = null;
                        Object o = null;
                        try {
                            s.setSoTimeout(3000);
                            /*
                             * The above line raises as SocketTimeoutException
                             * if the read below times out.  That happens to be
                             * an IOExpcetion thus getting caught and breaking
                             * to labelA.
                             */
                            o = in.readObject();
                            s.setSoTimeout(0);
                        } catch (ClassNotFoundException ex) {
                            break labelA;
                        } catch (IOException ex) {
                            break labelA;
                        }
                        // Minimal EHLO, timeout, or close connection
                        if (!(o instanceof Long)) {
                            break labelA;
                        }
                        if (((Long) o).longValue() != JacobiServer.EHLO.longValue()) {
                            break labelA;
                        }
                        for (;;) {
                            try {
                                o = in.readObject();
                                if (!(o instanceof RecursiveTask)) {
                                    break labelA;
                                }
                                task = (RecursiveTask) o;
                                o = pool.invoke(task);
                                out.writeObject(o);
                                out.flush();
                                /*
                                 * Image processing goes here.  Task should get cast
                                 * to something where we have access to the internal
                                 * maytricks.  No spoon, there is.
                                 */
                            } catch (IOException ex) {
                                if(DEBUG) {
                                    ex.printStackTrace();
                                }
                                break labelA;
                            } catch(ClassNotFoundException ex) {
                                break labelA;
                            }
                        }
                    }
                } catch (IOException ex) {
                    if (DEBUG) {
                        ex.printStackTrace();
                    }
                } finally {
                    try {
                        s.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }.start();
    }

    public static final class ClientStatus implements Serializable {

        long jobsComplete;
        long clientRunningSince;
        boolean isWorking;
        String machineInfo;
    }
}
