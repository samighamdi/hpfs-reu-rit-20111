/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansdemondp;

import edu.rit.mp.IntegerBuf;
import edu.rit.mp.LongBuf;
import edu.rit.pj.Comm;
import edu.rit.pj.CommRequest;
import edu.rit.pj.CommStatus;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author reu
 */
public class KMeansFrontend {

    private ParallelKMeans kmeans;
    private static int frontRank;
    public int subRank;
    private final Socket client;
    private int paramSize;
    private Comm localWorld;
    private LinkedList<BufferedImage> imageList;

    public KMeansFrontend(final Socket client, Comm localWorld) {
        this.client = client;
        this.localWorld = localWorld;
        kmeans = null;
        imageList = new LinkedList<BufferedImage>();
    }

    public void receiveParams() throws IOException {
        String[] params = null;
        if (paramSize > 0) {
            BufferedReader get = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));

            params = new String[paramSize];

            for (int i = 0; i < params.length; i++) {
                params[i] = get.readLine();
            }
        }
        paramSetup(params);
    }

    public void setParamSize(int size) {
        paramSize = size;
    }

    public void paramSetup(String[] params) throws IOException {
            long[] longParams = new long[paramSize];
            for (int i = 0; i < params.length; i++) {
                longParams[i] = Long.parseLong(params[i]);
                System.out.println("Param: " + longParams[i]);
            }
            LongBuf buf = LongBuf.buffer(longParams);
            localWorld.floodReceive(buf, new CommRequest());
            localWorld.floodSend(buf);
        kmeans = new ParallelKMeans(localWorld, subRank);
        new Thread() {
            public void run() {
                try {
                    kmeans.startKMeans("kmeansdemopoints", 10);
                } catch (IOException ex) {
                    
                }
            }
        }.start();
            
    }

    public BufferedImage getDisplay() {
        if (imageList.isEmpty()) {
            imageList = kmeans.getImageArchive(true);
            while (imageList.isEmpty()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                imageList = kmeans.getImageArchive(true);
            }

        }
        System.out.println("Images Available: " + imageList.size());
        return imageList.removeFirst();
    }

    public void computeServer() throws IOException {
        int count = 0;
        while (count < 100) {
            ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
            ImageIO.write(getDisplay(), "png", imageBytes);
            PrintStream out = new PrintStream(client.getOutputStream());
            System.out.println("Image Size: " + imageBytes.toByteArray().length);
            out.print(new DecimalFormat("000000000").format(imageBytes.toByteArray().length));
            System.out.println("sizeString: " + new DecimalFormat("000000000").format(imageBytes.toByteArray().length));
            client.getOutputStream().write(new byte[]{0});
            imageBytes.writeTo(client.getOutputStream());
            imageBytes.close();
            imageBytes = null;
            count++;
        }
        client.close();
    }

    public static void initServer() throws IOException {

        ServerSocket server = new ServerSocket(9997);
        boolean cont = true;
        while (cont) {
            try {
                final Socket client = server.accept();

                new Thread() {

                    @Override
                    public void run() {
                        configInstance(client);
                    }
                }.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void configInstance(Socket client) {
        try {
            Comm world = Comm.world();
            Comm localWorld = world.createComm(true);
            int rank = world.rank();
            if (rank == KMeansFrontend.frontRank) {
                KMeansFrontend frontend = new KMeansFrontend(client, localWorld);
                frontend.subRank = localWorld.rank();
                
                System.out.println("Home World rank " + rank + " entered config with local rank " + localWorld.rank());
                
                IntegerBuf buf = IntegerBuf.buffer(frontend.subRank);
                world.floodReceive(buf, new CommRequest());
                world.floodSend(buf);
                frontend.setParamSize(4);
                frontend.receiveParams();
                frontend.computeServer();
            } else {
                int frontRank = 0;
                
                System.out.println("World rank " + rank + " entered config");
                IntegerBuf intBuf = IntegerBuf.buffer(frontRank);
                world.floodReceive(intBuf);
                
                System.out.println("World rank " + rank + " knows local home rank is " + intBuf.get(0));
                frontRank = intBuf.get(0);
                
                String[] params = new String[4];
                long[] longParams = new long[4];
                LongBuf buf = LongBuf.buffer(longParams);
                CommStatus status = localWorld.floodReceive(buf);
                for (int i = 0; i < 4; i++) {
                    params[i] = Long.toString(longParams[i]);
                }
                ParallelKMeans kmean = new ParallelKMeans(localWorld, frontRank);
                kmean.startKMeans("kmeansdemopoints", 10);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void initiateFrontend(String[] args) {
        try {
            Comm.init(args);
            Comm world = Comm.world();
            int rank = world.rank();
            if (world.host().equals("129.21.37.181") && rank % 2 == 0) {
                KMeansFrontend.frontRank = rank;
                IntegerBuf buf = IntegerBuf.buffer(rank);
                world.floodReceive(buf, new CommRequest());
                world.floodSend(buf);
                initServer();

            } else {
                IntegerBuf buf = IntegerBuf.buffer(KMeansFrontend.frontRank);
                world.floodReceive(buf);
                KMeansFrontend.frontRank = buf.get(0);
                configInstance(null);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        initiateFrontend(args);
    }
}
