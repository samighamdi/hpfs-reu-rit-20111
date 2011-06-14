package hpfs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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

    private static enum MY_STATE {

        WAITING, WORKING
    };
    private static final boolean DEBUG = true;
    private static final ForkJoinPool pool = new ForkJoinPool();
    private final InetAddress listenAddress;
    private final int port, localId;
    private final AtomicReference<MY_STATE> state;

    public JacobiClient(InetAddress listenAddress, int port) {
        super();
        this.listenAddress = listenAddress;
        this.port = port;
        this.localId = CLIENT_COUNTER.getAndIncrement();
        this.setName("JacobiClient-" + this.localId);
        state = new AtomicReference<>(MY_STATE.WAITING);
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
            listenSocket = new ServerSocket(port, 2, listenAddress);
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
        if (!state.compareAndSet(MY_STATE.WAITING, MY_STATE.WORKING)) {
            try {
                s.close();
            } catch (IOException ex) {
                return;
            }
        }
        new Thread() {

            @Override
            public void run() {
                try {
                    labelA:
                    {
                        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                        JacobiServer.JacobiMatrixTask task = null;
                        Object o = null;
                        try {
                            o = in.readObject();
                        } catch (ClassNotFoundException ex) {
                            break labelA;
                        } catch (IOException ex) {
                            break labelA;
                        }
                        if (!(o instanceof JacobiServer.JacobiMatrixTask)) {
                            break labelA;
                        }
                        task = (JacobiServer.JacobiMatrixTask) o;
                        o = pool.invoke(task);
                        out.writeObject(o);
                        //task
                        out.flush();
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
                    state.set(MY_STATE.WAITING);
                }
            }
        }.start();
    }
}
