/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansserver;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

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

    public static KMeans startKmeans(final String[] args) {
        final KMeans kmean = new KMeansDemo(args);
        new Thread() {

            @Override
            public void run() {
                kmean.prepareData();
                kmean.prepareClusters();
                kmean.assignmentStep();
                //PrintStream o = new PrintStream( new FileOutputStream(DEFAULT_OUT));
                int count = 0;
                while (count < 100 && kmean.changed()) {
                    System.out.println("Iteration " + count);
                    //me.printStatus(o);
                    kmean.generateImage(String.format("Kmeans%02d", count), "KMeans Clustering: Iteration " + count, "xaxis", "yaxis", 0, 1);
                    kmean.step();
                    ++count;
                }
                kmean.generateImage(String.format("Kmeans%02d", count), "KMeans Clustering: Iteration " + count, "xaxis", "yaxis", 0, 1);
                size = count;
            }
        }.start();
        
        return kmean;
    }

    public static void startServer() {
        new Thread() {

            public void run() {

                ServerSocket server = null;
                try {
                    server = new ServerSocket(9996);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                while (true) {
                    try {
                        final Socket client = server.accept();


                        new Thread() {

                            public void run() {
                                try {
                                    System.out.println("Connection Established");


                                    BufferedReader get = new BufferedReader(
                                            new InputStreamReader(client.getInputStream()));

                                    String[] params = new String[4];
                                    params[0] = get.readLine();


                                    params[1] = get.readLine();


                                    params[2] = get.readLine();


                                    params[3] = get.readLine();

                                    for (int i = 0; i < params.length; i++) {
                                        System.out.println("Param: " + params[i]);
                                    }


                                    KMeans kmean = startKmeans(params);

                                    int count = 0;

                                    while (getSize() == -1 || count < getSize()) {
                                        LinkedList<BufferedImage> archive = kmean.getImageArchive(true);
                                        if (archive.size() != 0) {
                                            for (BufferedImage image : archive) {
//                                                System.out.println("Sending Image");

//                                                System.out.println("Image Num : " + archive.size());

                                                ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
                                                ImageIO.write(image, "png", imageBytes);
                                                PrintStream out = new PrintStream(client.getOutputStream());
//                                                System.out.println("Image Size: " + new DecimalFormat("000000000").format(imageBytes.toByteArray().length));
                                                out.print(new DecimalFormat("000000000").format(imageBytes.toByteArray().length));
                                                client.getOutputStream().write(new byte[]{0});
                                                imageBytes.writeTo(client.getOutputStream());
                                                imageBytes.close();
                                                imageBytes = null;
                                                count++;

//                                                System.out.println("Sent Image");
                                                try {
                                                    Thread.sleep(16);
                                                } catch (InterruptedException ex) {
                                                }
                                            }
                                        } else {   
                                            try {
                                                Thread.sleep(600);
                                            } catch (InterruptedException ex) {
                                            }
                                        }
                                        archive = null;
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
    public static void main(String[] args) {
        startServer();
    }
}
