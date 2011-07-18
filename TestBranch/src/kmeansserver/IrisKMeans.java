/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansserver;

import edu.rit.mp.DoubleBuf;
import edu.rit.pj.Comm;
import edu.rit.util.Range;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * @author Kevin
 */
public class IrisKMeans extends KMeans {
    public static final String DEFAULT_IN = "iris.data.txt", 
                               DEFAULT_OUT = "iris_out.txt",
                               IMG_OUT_BASE = "iris";
    public static final String[] ELEM_NAMES = new String[] { "sepal length",
                                                             "sepal width",
                                                             "petal length",
                                                             "petal width" };
    public static final int IRIS_D = 4, // dimensions in iris data
                            IRIS_K = 2; // identifiable species
    private String infile = DEFAULT_IN;
    
    /**
     * Parses args in order: String name, int size, int k, int d
     * If fewer args are given, default values are used
     * Values may be overwritten by the file parsing process
     * @param args 
     */
    public IrisKMeans( String[] args ) {
        this(); // this puts default values for instance vars
        switch( args.length ) {
            case 4:
                if( isInteger(args[3]))
                    d = Integer.valueOf( args[3] );
            case 3:
                if( isInteger(args[2]))
                    k = Integer.valueOf( args[2] );
            case 2:    
                if( isInteger(args[1]))
                    size = Integer.valueOf(args[1]);
            case 1:
                if( new File(args[0]).exists() )
                    infile = args[0];
        }
    }
    
    /**
     * See documentation of kmeansdemo for the meaning of the arguments;
     * they are passed in order to the int,int,int,long constructor
     * @param file the data file to be parsed
     * @param a
     * @param b
     * @param c
     */
    public IrisKMeans( String file, int size, int k, int d) {
        super(size, k, d);
        this.infile = file;
    }
    
    public IrisKMeans() {
        super( DEFAULT_SIZE, IRIS_K, IRIS_D );
        this.infile = DEFAULT_IN;
    }
    
    /**
     * Unlike the prepareData in KMeansDemo, this method ignores
     * and overwrites the "size" parameter. It scans a file and
     * changes them to the appropriate values for the contents of the file
     */
    @Override
    public void prepareData() {
        if( infile == null ) {
            infile = DEFAULT_IN;
        }
        DemoPoint.setDimension(this.d);
        try {
            Scanner in = new Scanner( new File(infile)).useDelimiter("\n");
            LinkedList<String> l = new LinkedList<String>();
            int count = 0;
            while( in.hasNext() && count < size ) {
                l.add(in.next());
                ++count;
            }
            this.size = count;
            this.points = new DemoPoint[size];
            this.clusters = new DemoPoint[k];
            int pointsp = 0;
            // parse the big list into data points
            for( String inS : l ) {
                String[] split = inS.split(",");
                double[] data = new double[d];
                for( int i = 0; i < d; ++i ) {
                    Double din = new Double(split[i]);
                    data[i] = din;
                }
                points[pointsp++] = new DemoPoint(data);
            }
        } catch( IOException e) {
            throw new RuntimeException(e); // gotta make it out somehow
        }
    }
    
    @Override
    public void prepareClusters() {
        for( DemoPoint dp : points ) {
            int c = (int)(k*Math.random());
            //System.out.println(dp);
            dp.setCluster(c);
        }
        updateStep();
    }
    
    public int getSize() { return size; }
    
    /* Sequential main
    public static void main(String[] args) {
        IrisKMeans me = new IrisKMeans(args);
        me.prepareData();
        me.prepareClusters();
        //PrintStream o = null;

        int count = 0;
        while( count < 100 && me.changed() ){
            System.out.println("Iteration " + count);
            //me.printStatus(o);
            try {
                me.generateImage(String.format("Irisx0y1%02d", count), "Iris " + count, ELEM_NAMES[0], ELEM_NAMES[1], 0, 1);
                me.generateImage(String.format("Irisx1y2%02d", count), "Iris " + count, ELEM_NAMES[1], ELEM_NAMES[2], 1, 2);
                me.generateImage(String.format("Irisx2y3%02d", count), "Iris " + count, ELEM_NAMES[2], ELEM_NAMES[3], 2, 3);
            } catch( IOException e ) {
                throw new RuntimeException(e);
            }
            me.clusterStep();
            ++count;
        }
    }
     /* 
     */
    //*
    public static void main( String[] args ) throws Exception { 
        Comm.init(args);
        Comm world = Comm.world();
        int rank = world.rank();
        int size = world.size();
        
        int dim = DEFAULT_D, kay = DEFAULT_K, datasize = Integer.MAX_VALUE;
        String infile = DEFAULT_IN;
        switch( args.length ) {
            case 4:
                if( isInteger(args[3]))
                    dim = Integer.valueOf( args[3] );
            case 3:
                if( isInteger(args[2]))
                    kay = Integer.valueOf( args[2] );
            case 2:    
                if( isInteger(args[1]))
                    datasize = Integer.valueOf(args[1]);
            case 1:
                if( new File(args[0]).exists() )
                    infile = args[0];
        }
        // the size argument to constructor is the max # of lines to read
        IrisKMeans me = new IrisKMeans(infile, datasize, kay, dim);
        me.prepareData();
        if( rank == 0 )
            me.prepareClusters();
        
        // we want the actual number of elements read
        datasize = me.getSize();
        Range ir = new Range(0, datasize-1);
        me.setStart(ir.lb());
        me.setEnd(ir.ub());
        
        while( !me.changed() ) {
            // broadcast ciuster assignments
            double[][] assignments = new double[kay][dim];
            DoubleBuf dbuf = DoubleBuf.buffer(assignments);
            if( rank == 0 ) dbuf = DoubleBuf.buffer( me.clusterCenters());
            world.broadcast(0, dbuf);
            
            me.assignmentStep();
            if( rank == 0 ) {
                // gather total-count pairs
                me.updateStep();
            }
        }
    }
    /* */
}