package hpfs;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final long id;

    public JacobiClient(InetAddress listenAddress, int port) {
        super();
        this.listenAddress = listenAddress;
        this.listenPort = port;
        this.localId = CLIENT_COUNTER.getAndIncrement();
        this.setName("JacobiClient-" + this.localId);
        id = ((System.currentTimeMillis() & 0xffffff) << 31) | System.currentTimeMillis();
        // That ID will probably work for now, will we even need UUIDs?
    }

    public static void main(String[] args) {
        InetAddress addr = InetAddress.getLoopbackAddress();
        int port = 15000;
        new JacobiClient(addr, port).start();
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        Selector selector = null; // channels attach their Socket or ServerSocket
        Set<SelectionKey> selectSet = null;
        ObjectInputStream socketIn = null;
        ObjectOutputStream socketOut = null;
        Object o = null;
        Closeable c = null;
        try {
            serverSocket = new ServerSocket(listenPort, TCP_BACKLOG, listenAddress);
            serverSocket.getChannel().configureBlocking(false);
            serverSocket.getChannel().register(selector, SelectionKey.OP_ACCEPT, serverSocket);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        for (;;) {
            try {
                if (selector.select() < 1) {
                    continue;
                }
                selectSet = selector.selectedKeys();
                for (SelectionKey sk : selectSet) {
                    if ((sk.attachment() instanceof ServerSocket) && sk.isAcceptable()) {
                        serverSocket = (ServerSocket) sk.attachment();
                        socket = serverSocket.accept();
                        socketOut = new ObjectOutputStream(socket.getOutputStream());
                        socketOut.writeLong(id);
                        socketOut.flush();
                        socket.getChannel().configureBlocking(false);
                        socket.getChannel().register(selector, SelectionKey.OP_READ, socket);
                    }
                    if ((sk.attachment() instanceof Socket) && sk.isReadable()) {
                        socket = (Socket) sk.attachment();
                        socketIn = new ObjectInputStream(socket.getInputStream());
                        try {
                            o = socketIn.readObject();
                        } catch (ClassNotFoundException ex) {
                            continue;
                        }
                        if (!(o instanceof JacobiMessage)) {
                            continue;
                        }
                        pool.execute(new MessageAction((JacobiMessage)o, socketOut));
                    }
                }
            } catch (IOException ex) {
                continue;
            } finally {
                // Make some change so this code is reachable without an exception
                // being thrown.  We need something in order to know when
                // the clients should shut off.  Or do we?
                for (SelectionKey sk : selector.keys()) {
                    try {
                        if (sk.attachment() instanceof Closeable) {
                            c = (Closeable) sk.attachment();
                            c.close();
                        }
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }

    private static class MessageAction extends RecursiveAction {

        final JacobiMessage msg;
        final ObjectOutputStream out;

        MessageAction(JacobiMessage msg, ObjectOutputStream out) {
            this.msg = msg;
            this.out = out;
        }

        @Override
        protected void compute() {
            if(msg == null) {
                return;
            }
            
            switch( msg.msgType ) {
                case TASK:
                    TaskMessage t = (TaskMessage)msg;
                    try {
                        out.writeObject(t.getTask().invoke());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                // clients only receive tasks    
                case RESULT:
                // monitor communication not yet implemented
                case ANSWER:
                case QUERY:
                    break;
            }
        }    
    }
}
