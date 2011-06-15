package hpfs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
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
        Selector selector = null;
        Set<SelectionKey> selectSet = null;
        ObjectInputStream socketIn = null;
        Object o = null;
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
                        socket.getChannel().configureBlocking(false);
                        socket.getChannel().register(selector, SelectionKey.OP_READ, socket);
                    }
                    if((sk.attachment() instanceof Socket) && sk.isReadable()) {
                        socket = (Socket) sk.attachment();
                        socketIn = new ObjectInputStream(socket.getInputStream());
                        try {
                            o = socketIn.readObject();
                        } catch (ClassNotFoundException ex) {
                            continue;
                        }
                        if(!(o instanceof JacobiMessage)) {
                            continue;
                        }
                        handleMessage((JacobiMessage)o);
                    }
                }
            } catch (IOException ex) {
                continue;
            }
        }
    }

    private static void handleMessage(JacobiMessage message) {
        // TO DO
    }


}
