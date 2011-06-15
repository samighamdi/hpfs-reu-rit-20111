package hpfs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.NumberFormatException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentSkipListMap;

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

}
