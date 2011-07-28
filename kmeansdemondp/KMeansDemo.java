/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansdemondp;

import java.io.FileOutputStream;
import edu.rit.util.Random;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * KMeansDemo creates a random data set of the given size and dimensionality,
 * then executes Lloyd's k-means algorithm on those clusters
 * @author Kevin
 */
public class KMeansDemo extends KMeans {
    public static final int DEFAULT_SIZE = 10000, DEFAULT_K = 5, DEFAULT_D = 2;
    public static final String DEFAULT_OUT = "out.txt", DEMO_POINTS_FILE = "kmeansdemopoints";
    
    private long seed;
    
    /**
     * Parses command line arguments, assigning default values to any missing.
     * Strings are parsed into, in order, size, k, d, seed
     * (This means, size = args[0], size = args[1], and so on)
     * To leave a value at default, give it a value other than a positive
     * integer
     * @param args array of strings to be parsed for assignment
     */
    public KMeansDemo( String[] args ) {
        super(args);
    }
    
    /**
     * Constructs a demo with each argument explicitly provided.
     * @param size
     * @param k
     * @param d
     * @param seed 
     */
    public KMeansDemo( int size, int k, int d, long seed ) {
        super(size,k,d,seed);
    }
    
    public KMeansDemo() {
        super();
    }
    
    /**
     * Initialize and fill the data array using a random number generator
     */
    public static void prepareArrayFS( String arrayfile, int dimension, int numPoints, int maxiter, long seed ) throws IOException {
    	ArrayDoubleFS outfs = new ArrayDoubleFS(arrayfile, dimension+1);
		DemoPoint.setDimension(dimension);
        Random rng = Random.getInstance(seed);
        
        while( numPoints > 0 ) {
            DemoPoint[] points = new DemoPoint[maxiter];
            int count = 0;
        	while( count < maxiter ) {
        		points[count] = new DemoPoint( rng );
        		++count;
        	}
			outfs.append(DemoPointArray.toArray(points));
			numPoints -= maxiter;
        }
        
    }
    
    @Override
    public void prepareClusters() {
        for( int i = 0 ; i < k ; ++i ) {
            clusters[i] = new DemoPoint( rng, i );
        }        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws java.io.IOException {
        KMeansDemo me = new KMeansDemo(args);
        KMeansDemo.prepareArrayFS( DEMO_POINTS_FILE, me.d, me.size, (int) 1e6, me.seed);
        /*
         * me.prepareData(DEMO_POINTS_FILE);
         
        me.prepareClusters();
        me.assignmentStep();
        //PrintStream o = new PrintStream( new FileOutputStream(DEFAULT_OUT));
        int count = 0;
        while( count < 100 && me.changed() ){
            System.out.println("Iteration " + count);
            //me.printStatus(o);
            try {
                me.generateImage(String.format("Kmeans%02d", count), "KMeans " + count, "xaxis", "yaxis", 0, 1);
            } catch( IOException e ) {
                throw new RuntimeException(e);
            }
            me.step();
            ++count;
        }
        me.generateImage(String.format("Kmeans%02d", count), "KMeans " + count, "xaxis", "yaxis", 0, 1);
        */
    }
}
