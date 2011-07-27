/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansdemondp;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * @author Kevin
 */
public class CarKMeans extends KMeans {
    public static final int CAR_K = 3, CAR_D = 6;
    public static final String[] ELEM_NAMES = new String[] { "buying", "maint",
                                              "doors", "people capacity",
                                              "luggage room", "safety" };
    public static final String DEFAULT_IN = "car.data.txt";
    public static final String[][] parseHelp = new String[][]{
            // buying, maint
            new String[]{"vhigh", "high", "med", "low" }, 
            new String[]{"vhigh", "high", "med", "low" }, 
            // doors
            new String[]{"2","3","4","5more"},
            // people
            new String[]{"2","4","more"},
            // luggage
            new String[]{"small", "med", "big" },
            // safety
            new String[]{"low", "med", "high" }
        };
    
    private String infile;
    
    public CarKMeans() {
        super( 1728, CAR_K, CAR_D);
    }
    
    public CarKMeans( String[] args ) {
        
    }
    
    /*
    @Override
    public void prepareData() {
        if( infile == null ) {
            infile = DEFAULT_IN;
        }
        DPoint.setDimension(CAR_D);
        try 
        {
            Scanner in = new Scanner( new File(infile)).useDelimiter("\n");
            LinkedList<String> lines = new LinkedList<String>();
            int count = 0;
            while( in.hasNext() && count < size ) {
                lines.add(in.next());
                ++count;
            }
            this.size = count;
            this.points = new double[size][];
            this.clusters = new double[k][];
            // buyprice, maintprice, doors, people, size, luggage space, safety
            for( int i = 0 ; i < size ; ++i ) {
                if( lines.isEmpty() ) { 
                    break;                
                }
                double[] parsed = parseCarData( lines.removeFirst().split(",") );
                points[i] = DPoint.dPoint(parsed);
            }
        } catch( IOException e ) {
            throw new RuntimeException( e );
        }
    }
    */
    
    @Override
    public void prepareClusters() {
        this.clusterPlusPlus();
    }

    /**
     * 
     */
    private double[] parseCarData(String[] line) {
        if( line.length < this.d ) {
            throw new IllegalArgumentException("string[] must contain at least " + 
                    this.d + " elements");
        }
        double[] ret = new double[this.d];
        
        for( int x = 0; x < this.d; ++x ) {
            for( int i = 0; i < parseHelp[x].length; ++i ) {
                if( line[x].compareTo(parseHelp[x][i]) == 0 )
                    ret[x] = i + 1;
            }
        }
        return ret;
    }
    /*
    public static void main( String[] args ) {
        CarKMeans inst = new CarKMeans();
        inst.prepareData();
        inst.clusterPlusPlus();
        int count = 0;
        while( count < 100 && inst.changed() ){
            System.out.println("Iteration " + count);
            inst.printStatus(null);
            try {
                for( int i = 0; i < inst.d - 1; ++i) {
                    String title = String.format( "Car x%d y%d %02d" , i, i+1, count );
                    inst.generateImage(title, title, ELEM_NAMES[i], ELEM_NAMES[i+1], i, i+1);
                
                } 
            } catch( IOException e ) {
                throw new RuntimeException(e);
            }
            inst.step();
            ++count;
        }
    }
    /* */
}
