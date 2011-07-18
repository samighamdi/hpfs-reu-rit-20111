/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansserver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

/**
 * KMeansDemo creates a random data set of the given size and dimensionality,
 * then executes Lloyd's k-means algorithm on those clusters
 * @author Kevin
 */
public class KMeansDemo extends KMeans {
    public static final int DEFAULT_SIZE = 10000, DEFAULT_K = 5, DEFAULT_D = 2;
    public static final String DEFAULT_OUT = "out.txt";
    
    private long seed;
    private Random rng;
    
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
        if( args.length >= 4 )
            seed = Long.valueOf(args[3]);
    }
    
    /**
     * Constructs a demo with each argument explicitly provided.
     * @param size
     * @param k
     * @param d
     * @param seed 
     */
    public KMeansDemo( int size, int k, int d, long seed ) {
        super(size,k,d);
        this.seed = seed;
    }
    
    public KMeansDemo() {
        super();
        this.seed = System.currentTimeMillis();
    }
    
    /**
     * Initialize and fill the data array using a random number generator
     */
    @Override
    public void prepareData() {
        DemoPoint.setDimension(d);
        rng = new Random( seed );
        points = new DemoPoint[size];
        clusters = new DemoPoint[k];
        for( int i = 0 ; i < size; ++i ) {
            int rand = rng.nextInt(k);
            points[i] = new DemoPoint( rng, rand );
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
        me.prepareData();
        me.prepareClusters();
        me.assignmentStep();
        //PrintStream o = new PrintStream( new FileOutputStream(DEFAULT_OUT));
        int count = 0;
        while( count < 100 && me.changed() ){
            System.out.println("Iteration " + count);
            //me.printStatus(o);
            me.generateImage(String.format("Kmeans%02d", count), "KMeans " + count, "xaxis", "yaxis", 0, 1);
         
            me.step();
            ++count;
        }
        me.generateImage(String.format("Kmeans%02d", count), "KMeans " + count, "xaxis", "yaxis", 0, 1);
    }
}