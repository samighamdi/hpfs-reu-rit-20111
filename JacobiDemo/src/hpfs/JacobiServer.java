package hpfs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.NumberFormatException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Ben Paretzky
 */
public class JacobiServer {

    static final Long EHLO = 0xbeefFA1L;
    private static final boolean DEBUG = true;
    private final Map<Long, MetaClient> clientMap;
    private AtomicLong CLIENT_ID_GEN = new AtomicLong(1L);

    public static void main(String [] args) {
        JacobiServer js = new JacobiServer();
        js.forkREPL();
    }

    public JacobiServer() {
        clientMap = new ConcurrentSkipListMap<>();
    }

    private boolean addClient(String address) {
        String [] addrParts = address.split(":",2);
        boolean retVal = false;
        try {
            InetAddress addr = InetAddress.getByName(addrParts[0]);
            int port = Integer.parseInt(addrParts[1]);
            Socket s = new Socket(addr, port);
            s.setSoTimeout(3000);
            ObjectOutputStream tempOut = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream tempIn = new ObjectInputStream(s.getInputStream());
            tempOut.writeObject(EHLO);
            tempOut.flush();
            long l = tempIn.readLong();

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

    private boolean removeClient(String client) {
        return false;
    }

    public void printREPLHelp() {
        
    }

    public void forkREPL() {
        new Thread() {
            @Override
            public void run() {
                Scanner stdIn = new Scanner(System.in);
                String s = null;
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
                            s = stdIn.next();
                            if(removeClient(s)) {
                                System.out.println("Successfully removed " + s);
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
