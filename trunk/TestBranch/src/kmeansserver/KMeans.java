/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansserver;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.imageio.ImageIO;

/**
 *
 * @author Kevin
 */
public abstract class KMeans {
    public static final int DEFAULT_SIZE = 100, DEFAULT_K = 5, DEFAULT_D = 2,
                            DEFAULT_X = 0, DEFAULT_Y = 1;
    public static final String FILE_FORMAT = "png";
    public static final String CHART_TITLE = "K-Means Clustering";
    protected int size, k, d;
    protected double[][] points;
    protected double[][] clusters;
    protected boolean changedFlag = true;
    
    private LinkedList<BufferedImage> imageArchive;
    
    protected int start, end;
    
    public KMeans() {
        this(DEFAULT_SIZE, DEFAULT_K, DEFAULT_D);
    }
    
    public KMeans(int size, int k, int d) {
        imageArchive = new LinkedList<BufferedImage>();
        this.size = size;
        this.k = k;
        this.d = d;
        this.start = 0;
        this.end = size;
    }
    
    public KMeans(String[] args ) {
        this(); // this puts default values for instance vars
        int i = args.length;
        if( i > 3 ) i = 3;
        switch( i ) {
            case 3:
                if( isInteger(args[2]))
                    d = Integer.valueOf( args[2] );
            case 2:    
                if( isInteger(args[1]))
                    k = Integer.valueOf( args[1] );
            case 1:
                if( isInteger(args[0]))
                    size = Integer.valueOf(args[0]);
        }
    }
    
    /**
     * Thanks StackOverflow, this is probably much better than my lazy regex crap
     * http://stackoverflow.com/questions/237159/whats-the-best-way-to-check-to-see-if-a-string-represents-an-integer-in-java/237204#237204
     * @param str  the string to check
     * @return     if the string is made of only digits (Doesn't check overflow)
     */
    public static boolean isInteger(String str) {
        if (str == null) {
                return false;
        }
        int length = str.length();
        if (length == 0) {
                return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
                if (length == 1) {
                        return false;
                }
                i = 1;
        }
        for (; i < length; i++) {
                char c = str.charAt(i);
                if (c <= '/' || c >= ':') {
                        return false;
                }
        }
        return true;
    }
    
    public abstract void prepareData();
   
    public abstract void prepareClusters();
    
    /**
     * precondition: k and points are initialized appropriately
     * postcondition: clusters is initialized according to the kmeans++ algorithm
     */
//    public void clusterPlusPlus() {
//        clusters = new DemoPoint[k];
//        double[] in = points[(int)(Math.random() * points.length)].getCoords();
//        clusters[0] = new DemoPoint( in, 0 );
//        // for each remaining cluster
//        for( int i = 1; i < k; ++i ) {
//            double[] cum = new double[points.length]; // cumulative distribution
//            // for each data point assign it to the nearest cluster
//            for( int m = 0; m < points.length; ++m ) {
//                DemoPoint p = points[m];
//                // choose the nearest cluster
//                double pfit = Double.NEGATIVE_INFINITY;
//                int choice = 0;
//                // for each initialized cluster
//                for( int j = 0; j < i; ++j ) {
//                    double fit = KMeans.fitness(p.getCoords(), clusters[j].getCoords());
//                    if( pfit < fit ) {
//                        pfit = fit;
//                        choice = j;
//                    }
//                }
//                if( m > 0 ) {
//                    cum[m] = cum[m-1] + pfit*pfit;
//                } else {
//                    cum[m] = pfit*pfit;
//                }
//            }
//            int position;
//            do {
//                double choice = Math.random()*cum[cum.length-1];
//                position = 0;
//                while( choice > cum[position] ) ++position;
//            } while( points[position].getCluster() != -1 );
//            
//            clusters[i] = new DemoPoint(points[position].getCoords(), i);
//        }
//    }
    
    public String getCoordName(int i) {
        return new Integer(i).toString();
    }
    
    /**
     * Computes the total of differences squared between two double arrays
     * @param a first array
     * @param b second array
     * @return the square difference
     */
    public static double fitness( double[] a, double[] b ) {
        double total = 0;
        for( int i = 0; i < a.length; ++i ) {
            total += Math.pow((a[i] - b[i]),2);
        }
        return total;
    }
    
    /**
     * Executes one iteration of the k-means clustering algorithm
     * by assigning each datum to a cluster and then moving each cluster.
     */
    public void step() {
        assignmentStep();
        updateStep();
    }
    
    /**
     * Places each point in points[] into a cluster (via setCluster(int))
     * according to its fitness 
     */
    public void assignmentStep() {
        long time = -System.currentTimeMillis();
        // assign each point to a cluster
        //for( DemoPoint p : points ) {
        for( int dpp = getStart() ; dpp < getEnd(); ++dpp ) {
            double[] p = points[dpp];
            double min = Double.MAX_VALUE;
            double[] pc = DPoint.getCoords(p);
            int choice = DPoint.DEFAULT_CLUSTER;
            for( double cluster : clusters ) {
                double my = 0;
                double[] center = cluster.getCoords();
                //System.out.println( Arrays.toString(pc) + "\n" + Arrays.toString(center));
                my = fitness(center, pc);
                /*System.out.println( String.format( "Cluster:%d has fitness %f",
                                                    cluster.getCluster(),
                                                    my));
                 */
                if( my < min ) {
                    min = my;
                    choice = cluster.getCluster();
                }
            }
            p.setCluster(choice);
        }
        
        System.out.println("Assignment Step Took: " + (time + System.currentTimeMillis()));
    }
    
    /**
     * Changes the coordinates of each cluster to the center of its points
     */
    public void updateStep() {
        
        long time = -System.currentTimeMillis();
        changedFlag = false;
        // move the clusters to the center
        double[][] totals = new double[k][d];
        for( int i = 0 ; i < k; ++i ) 
            for( int j = 0; j < d; ++j )
                totals[i][j] = 0;
            
        int[] count = new int[k];
        for( DemoPoint p : points ) {
            double[] coords = p.getCoords();
            int c = p.getCluster();
            for( int i = 0; i < d; ++i ) {
                try {
                    totals[c][i] += coords[i];
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw e;
                }
            }
            ++count[c];
        }
        
        for( int i = 0; i < k; ++i ) {
            for( int j = 0; j < d; ++j ) {
                //System.out.println(String.format("cluster:%d dimension:%d sum=%f count=%d",
                //        i,j,totals[i][j], count[i]));
                totals[i][j] = totals[i][j] / count[i];
            }
        }
        
        // for each cluster, set his position to the average of his points
        for( int i = 0; i < k; ++i ) {
            if( clusters[i] == null ) {
                clusters[i] = new DemoPoint( totals[i], i );
                changedFlag = true;
                continue;
            }
            double[] before = clusters[i].getCoords();
            for( int j = 0; j < d; ++j ) {
                if( before[j] != totals[i][j] ) {
                    clusters[i].setCoords(totals[i]);
                    changedFlag = true;
                    break;
                }
            }
        }
        
        
        System.out.println("Update Step Took: " + (time + System.currentTimeMillis()));
    }
    
    public boolean changed() {
        return changedFlag;
    }
    
    public double[][] clusterCenters() {
        double[][] ret = new double[k][d];
        for( int i = 0 ; i < k ; ++i) {
            ret[i] = clusters[i].getCoords();
        }
        return ret;
    }
    
    public void printStatus(PrintStream out) {
        if( out == null ) { 
            out = System.out;
        }
        for( DemoPoint dp : clusters ) {
            out.println( "Cluster " + dp.getCluster() + " at coords: " );
            for( int i = 0 ; i < d; ++i ) {
                out.print( dp.getCoords()[i] + " " );
            }
            out.println();
        }
        out.println();
    }
    
    /**
     * Iterates through every point and prints its coordinates
     * @param out the stream to print on
     */
    public void printUnclusteredData(PrintStream out) {
        for( DemoPoint dp : points ) {
            out.println( dp.getCoords()[0] + " " + dp.getCoords()[1]);
        }
    }
    
    public ArrayList<ArrayList<DemoPoint>> clusterData() {
        ArrayList<ArrayList<DemoPoint>> clus = new ArrayList<ArrayList<DemoPoint>>();
        for( int i = 0; i < k; ++i ) {
            clus.add(i, new ArrayList<DemoPoint>());
        }
        
        for( int i = 0; i < size; ++i) {
            int assignment = points[i].getCluster();
            if( assignment == DemoPoint.DEFAULT_CLUSTER ) continue;
            clus.get(assignment).add(points[i]);
        }
        
        return clus;
    }
    
    /**
     * Returns an array of 2-dimensional double arrays representing the
     * data, grouped by clusters then 'rows'. The dimensions are cluster,
     * coordinate (x, y, z...) and finally data point. so the x-coordinate
     * of the 30th item in the 3rd cluster would be stored in ret[2][0][30]
     * 
     * Equivalent to clusterDataAsArray(new int[] {1, 2...rd} )
     * @param rd the dimension of the return array, for Return Dimension
     * 
     * The coordinates used are simply the first rd elements in the data array
     * @return k by rd by cluster size (variable) array
     */
    public double[][][] clusterDataAsArray(int rd) {
        ArrayList<ArrayList<DemoPoint>> clus = clusterData();
        double[][][] ret = new double[clus.size()][][];
        for( int i = 0 ; i < ret.length; ++i ) {
            ArrayList<DemoPoint> elemsInCluster = clus.get(i);
            ret[i] = new double[rd][elemsInCluster.size()];
            for( int j = 0; j < elemsInCluster.size(); ++j ) {
                for( int innie = 0 ; innie < rd; ++innie ) {
                    ret[i][innie][j] = elemsInCluster.get(j).getCoords()[innie];
                }
            }
        }
        return ret;
    }
    
    /**
     * Functions similarly to clusterDataAsArray(int) but instead of using the
     * first rd coordinates in the list, it uses the coordinates stored at
     * the array indices given in the arg array.
     * @param elems  the choices of indices for the return array
     * @return a k x elems.length x cluster size (variable) array
     */
    public double[][][] clusterDataAsArray(int[] elems) {
        ArrayList<ArrayList<DemoPoint>> clus = clusterData();
        double[][][] ret = new double[clus.size()][elems.length][];
        for( int i = 0 ; i < ret.length ; ++ i ) {//for each cluster
            ArrayList<DemoPoint> elemsInCluster = clus.get(i);
            for( int j = 0 ; j < elems.length; ++j ) {//for each coordinate
                ret[i][j] = new double[elemsInCluster.size()];
                for( int m = 0; m < elemsInCluster.size(); ++m ) {//for each data point
                    ret[i][j][m] = elemsInCluster.get(m).getCoords()[elems[j]];
                }
            }
        }
        return ret;
    }
    
    /**
     * Constructs a list of the data points in each cluster, then prints
     * out each list
     * @param out the stream to print on
     */
    public void printClusteredData(PrintStream out) {
        ArrayList<ArrayList<DemoPoint>> clus = clusterData();
        
        for( ArrayList<DemoPoint> c : clus ) {
            if( c.isEmpty() ) continue;
            out.println( "Cluster " + c.get(0).getCluster() );
            for( DemoPoint pt : c ) {
                out.println( pt.getCoords()[0] + " " + pt.getCoords()[1]);
            }
            out.println();
        }
    }
    
    public void generateImage( String filename, String title, String xlabel, String ylabel, int xaxis, int yaxis ) {
        if (imageArchive.size() > 200) {
            imageArchive.removeFirst();
        }
        
        long time = -System.currentTimeMillis();
        imageArchive.add(KMeansPlot.generateImage(this, title, xlabel, ylabel, new int[]{xaxis, yaxis}));
        
        System.out.println("Image Generation Took: " + (time + System.currentTimeMillis()));
    }
    
    public void displayChart( String title, String xlabel, String ylabel, int xaxis, int yaxis ) {
        final KMeansPlot demo = new KMeansPlot(this, title, xlabel, ylabel, new int[]{xaxis, yaxis});
        demo.pack();
        demo.setVisible(true);
    }
    
    public LinkedList<BufferedImage> getImageArchive(boolean reset) {
        System.out.println("Linked List Size: " + imageArchive.size());
       LinkedList<BufferedImage> archive = (LinkedList<BufferedImage>)imageArchive.clone();
       if (reset) {
        imageArchive.clear();
       }
       return archive;
    }

    public int getK() {
        return k;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public int getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(int end) {
        this.end = end;
    }
}