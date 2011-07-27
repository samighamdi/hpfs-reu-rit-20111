/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansdemondp;

import java.io.File;
import edu.rit.util.Random;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.util.LinkedList;
import java.awt.image.BufferedImage;

import java.util.Arrays;

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
    protected double[] maxPoints;
    protected boolean changedFlag = true;
    protected long seed;
    
    protected LinkedList<BufferedImage> imageArchive;
    
    protected int start, end;
    
    protected Random rng;
    
    public String getTitle() { 
        return CHART_TITLE;
    }
    
    public KMeans() {
        this(DEFAULT_SIZE, DEFAULT_K, DEFAULT_D, System.currentTimeMillis());
    }
    
    public KMeans(int size, int k, int d) {
    	this(size, k, d, System.currentTimeMillis());	
    }
    
    public KMeans(int size, int k, int d, long seed) {
        this.size = size;
        this.k = k;
        this.d = d;
        this.start = 0;
        this.end = size;
        maxPoints = new double[d];
        
        rng = Random.getInstance(seed);
        imageArchive = new LinkedList<BufferedImage>();
    }
    
    public KMeans(String[] args ) {
        this(); // this puts default values for instance vars
        int ar = args.length > 4 ? 4 : args.length;
        switch( ar ) {
        	case 4:
        		if( isInteger(args[3]))
        			seed = Long.valueOf( args[3] );//isInteger doesn't check overflow anyway
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
        this.start = 0;
        this.end = size;

        rng = Random.getInstance(seed);
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
    /*
    /**
     * Expectation: prepareData initializes the points[] array with appropriate data 
     * from any source and maxPoints[] to be a point (same number of dimensions as 
     * a data point) with the maximum of all data in each dimension, 
     * /
    public abstract void prepareData();
     */
    
   public void prepareData(String infile) {
		
		DPoint.setDimension(this.d);
		try {
			ArrayDoubleFS myfs = new ArrayDoubleFS( infile, this.d );
			myfs.seek(start);
			this.points = myfs.getNext( size );
			this.clusters = new double[k][];
			for( int i = 0 ; i < size; ++i ) {
				for( int  j = 0 ; j < d ; ++j ) {
					if( points[i][j] > maxPoints[j] ) {
						maxPoints[j] = points[i][j];
					}
				}
			}	
		} catch( IOException e) {
			throw new RuntimeException(e); // gotta make it out somehow
		}
	}
    
    public abstract void prepareClusters();
    
    /**
     * precondition: k and points are initialized appropriately
     * postcondition: clusters is initialized according to the kmeans++ algorithm
     */
    public void clusterPlusPlus() {
        clusters = new double[k][];
        double[] in = DPoint.getCoords(points[(int)(rng.nextDouble() * points.length)]);
        clusters[0] = DPoint.dPoint( in, 0 );
        // for each remaining cluster
        for( int i = 1; i < k; ++i ) {
            double[] cum = new double[points.length]; // cumulative distribution
            // for each data point assign it to the nearest cluster
            for( int m = 0; m < points.length; ++m ) {
                double[] p = points[m];
                // choose the nearest cluster
                double pfit = Double.NEGATIVE_INFINITY;
                // for each initialized cluster
                for( int j = 0; j < i; ++j ) {
                    int choice = 0;
                    double fit = KMeans.fitness(DPoint.getCoords(p), DPoint.getCoords(clusters[j]));
                    if( pfit < fit ) {
                        pfit = fit;
                        choice = j;
                    }
                }
                if( m > 0 ) {
                    cum[m] = cum[m-1] + pfit*pfit;
                } else {
                    cum[m] = pfit*pfit;
                }
            }
            int position;
            do {
                double choice = rng.nextDouble()*cum[cum.length-1];
                position = 0;
                while( choice > cum[position] ) ++position;
            } while( DPoint.getCluster(points[position]) != -1 );
            
            clusters[i] = DPoint.dPoint(DPoint.getCoords(points[position]), i);
        }
    }
    
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
        // assign each point to a cluster
        //for( DemoPoint p : points ) {
    	//System.out.println( "getStart: " + getStart() + " and end " + getEnd());
        for( int dpp = getStart() ; dpp < getEnd(); ++dpp ) {
            double[] p = points[dpp];
            double min = Double.MAX_VALUE;
            double[] pc = DPoint.getCoords(p);
            int choice = DPoint.DEFAULT_CLUSTER;
            for(double[] cluster : clusters) {
                double my = 0;
                double[] center = DPoint.getCoords(cluster);
                //System.out.println( "before fitness" + Arrays.toString(pc) + "\n" + Arrays.toString(center));
                my = fitness(center, pc);
                
                 /*
                System.out.println( String.format( "Cluster:%d has fitness %f",
                                                    cluster.getCluster(),
                                                    my));
                / /*/
                if( my < min ) {
                    min = my;
                    choice = DPoint.getCluster(cluster);
                }
            }
            //System.out.println("choice " + choice );
            DPoint.setCluster(choice, p);
        }
    }
    
    /**
     * Changes the coordinates of each cluster to the center of its points
     */
    public void updateStep() {
        changedFlag = false;
        // move the clusters to the center
        double[][] totals = new double[k][d];
        for( int i = 0 ; i < k; ++i ) 
            for( int j = 0; j < d; ++j )
                totals[i][j] = 0;
            
        int[] count = new int[k];
        for(double[] p : points) {
            double[] coords = DPoint.getCoords(p);
            int c = DPoint.getCluster(p);
            for( int i = 0; i < d; ++i ) {
                totals[c][i] += coords[i];
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
                clusters[i] = DPoint.dPoint( totals[i], i );
                changedFlag = true;
                continue;
            }
            double[] before = DPoint.getCoords(clusters[i]);
            for( int j = 0; j < d; ++j ) {
                if( before[j] != totals[i][j] ) {
                    DPoint.setCoords(totals[i], clusters[i]);
                    changedFlag = true;
                    break;
                }
            }
        }
    }
    
    public boolean changed() {
        return changedFlag;
    }
    
    public double[][] clusterCenters() {
        double[][] ret = new double[k][d];
        for( int i = 0 ; i < k ; ++i) {
            ret[i] = DPoint.getCoords(clusters[i]);
        }
        return ret;
    }
    
    /**
     * Creates an IntegerBuf containing the cluster-assignments of each
     * point in this object's points array. Additionally, the first
     * element refers to the array position of the first point
     */
    public edu.rit.mp.IntegerBuf pointAssignments() {
    	int range = getEnd() - getStart();
    	int[] val = new int[range];
    	for( int i = 0; i < range; ++i ) {
    		val[i] = DPoint.getCluster(points[i + getStart()]);
    	}
    	return edu.rit.mp.IntegerBuf.buffer( val );
    }
    
    public void printStatus(PrintStream out) {
        if( out == null ) { 
            out = System.out;
        }
        for( double[] dp : clusters ) {
        	if(dp == null) continue;
            out.println( "Cluster " + DPoint.getCluster(dp) + " at coords: " );
            for( int i = 0 ; i < d; ++i ) {
                out.print( DPoint.getCoords(dp)[i] + " " );
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
        for( double[] dp : points ) {
            out.println( DPoint.getCoords(dp)[0] + " " + DPoint.getCoords(dp)[1]);
        }
    }

	public double[][] pointsData() {
		return points;
	}
    
    public double[][][] clusterData() {
        double[][][] clus = new double[k][][];
        
        long[] clusterSizes = new long[k];
        
        for( int i = 0; i < size; ++i) {
            int assignment = DPoint.getCluster(points[i]);
            if ( assignment == DPoint.DEFAULT_CLUSTER ) continue;
            if (clus[assignment] == null) clus[assignment] = new double[size][];
            clus[assignment][(int)(clusterSizes[assignment]++)] = points[i];
        }
        
        for (int i = 0; i < clusterSizes.length; i++) {
            if (clusterSizes[i] > 0) {
                double[][] temp = clus[i];
                clus[i] = null;
                double[][] newCluster = new double[(int)clusterSizes[i]][];
                System.arraycopy(temp, 0, newCluster, 0, newCluster.length);
                clus[i] = newCluster;
            }
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
        double[][][] clus = clusterData();
        double[][][] ret = new double[clus.length][][];
        for( int i = 0 ; i < ret.length; ++i ) {
            double[][] elemsInCluster = clus[i];
            if (elemsInCluster != null) {
                ret[i] = new double[rd][elemsInCluster.length];
                for( int j = 0; j < elemsInCluster.length; ++j ) {
                    for( int innie = 0 ; innie < rd; ++innie ) {
                        ret[i][innie][j] = DPoint.getCoords(elemsInCluster[j])[innie];
                    }
                }
            }
            else {
                ret[i] = new double[rd][0];
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
        double[][][] clus = clusterData();
        double[][][] ret = new double[clus.length][elems.length][];
        for( int i = 0 ; i < ret.length ; ++ i ) {//for each cluster
            double[][] elemsInCluster = clus[i];
            for( int j = 0 ; j < elems.length; ++j ) {//for each coordinate
                if (elemsInCluster != null) {
                    ret[i][j] = new double[elemsInCluster.length];
                    for( int m = 0; m < elemsInCluster.length; ++m ) {//for each data point
                        ret[i][j][m] = DPoint.getCoords(elemsInCluster[m])[elems[j]];
                    }
                }
                else {
                    ret[i][j] = new double[0];
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
        double[][][] clus = clusterData();
        for (int i = 0; i < clus.length; i++) {
            if( clus[i] == null || clus[i].length == 0 ) continue;
            out.println( "Cluster " + DPoint.getCluster(clus[i][0]) );
            for (int j = 0; j < clus[i].length; j++) {
                out.println( DPoint.getCoords(clus[i][j])[0] + " " + DPoint.getCoords(clus[i][j])[1]);
            }
            out.println();
        }
    }
    
    public void generateImage( String filename, String title, String xlabel, String ylabel, int xaxis, int yaxis ) throws IOException {
        if (imageArchive.size() > 200) {
            imageArchive.removeFirst();
        }
        
        long time = -System.currentTimeMillis();
        ImgGen generator = new ImgGen(xaxis, yaxis, true, k);
        generator.generatePoints(points);
        //BufferedImage resized = new BufferedImage(640, 480, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        //resized.getGraphics().drawImage(generator.getFullImg(), 0, 0, 600, 400, null);
        imageArchive.add(generator.getFullImg());
        System.out.println("Image Generation Took: " + (time + System.currentTimeMillis()));
    }
    
    public LinkedList<BufferedImage> getImageArchive(boolean reset) {
        System.out.println("Linked List Size: " + imageArchive.size());
       LinkedList<BufferedImage> archive = (LinkedList<BufferedImage>)imageArchive.clone();
       if (reset) {
        imageArchive.clear();
       }
       return archive;
    }
    
    public void addArchivedImage(BufferedImage image) {
       if (imageArchive.size() > 200) {
            imageArchive.removeFirst();
        }
        imageArchive.add(image);
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
