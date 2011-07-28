/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansdemondp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author reu
 */
public class KMeansServer {

    private static int size = -1;
    //private static KMeans kmean;

    public static int getSize() {
        return size;
    }

    public static void startServer() {
        new Thread() {

            @Override
            public void run() {
                ServerSocket server = null;
                try {
                    server = new ServerSocket(9996);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                while (true) {
                    try {
                        System.out.println("Waiting for Socket");
                        final Socket client = server.accept();

                        
                        System.out.println("Got IT");
                        new Thread() {

                            @Override
                            public void run() {
                                try {
                                    System.out.println("Connection Established");
                                    Socket frontend = new Socket("reu1.cs.rit.edu", 9997);
                                    PrintStream frontendOut = new PrintStream(frontend.getOutputStream());
                                    PrintStream out = new PrintStream(client.getOutputStream());

                                    BufferedReader get = new BufferedReader(
                                            new InputStreamReader(client.getInputStream()));

                                    String[] params = new String[4];
                                    frontendOut.println(get.readLine());
                                    frontendOut.println(get.readLine());
                                    frontendOut.println(get.readLine());
                                    frontendOut.println(get.readLine());
                                    
                                    int count = -10;

                                    while (getSize() == -1 || count < getSize()) {
                                        
                                        byte[] h = new byte[9];
                                        new DataInputStream(frontend.getInputStream()).readFully(h, 0, 9);
                                        
                                        client.getOutputStream().write(h);
                                        client.getOutputStream().write(new byte[]{0});
                                        String sizeString = new String(h);
                                        System.out.println(sizeString);
                                        int size = Integer.parseInt(sizeString);
                                        
                                        frontend.getInputStream().read();
                                        
                                        byte[] imageBytes = new byte[size];
                                        new DataInputStream(frontend.getInputStream()).readFully(imageBytes, 0, size);

                                        client.getOutputStream().write(imageBytes);
                                        count++;
                                    }

                                    System.out.println("Ending Connection");

                                    client.close();
                                    get = null;
                                    
                                } catch (IOException ex) {
                                    System.out.println("Connection closed unexpectedly");

                                } catch (NumberFormatException ex) {
                                    System.out.println("Improper Parameter Data" + ex.getMessage());

                                }

                            }
                        }.start();

                    } catch (IOException ex) {
                        System.out.println("Connection closed unexpectedly");

                    }
                }
            }
        }.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
	startServer();
    }
}
