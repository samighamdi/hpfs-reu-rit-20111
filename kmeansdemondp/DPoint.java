/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansdemondp;

/**
 *
 * @author reu
 */
public class DPoint {
    
    
    public static final int DEFAULT_CLUSTER = -1;
    private static int d;
        /**
     * Plz don't abuse this ;o;
     * @param d the new number of dimensions for all DemoPoints
     * @return  the value overwritten by this call
     */
    public static int setDimension( int d ) {
        int temp = DPoint.d;
        DPoint.d = d;
        return temp;
    }
    
    public static int getDimension(){
        return d;
    }
    
    public static double[] dPoint(edu.rit.util.Random rng, int c) {
        double[] array = new double[d + 1];
        array[0] = c;
        for( int i = 0; i < d; ++i ) {
            array[i + 1] = rng.nextDouble();
        }
        return array;
    }
    
    public static double[] dPoint( edu.rit.util.Random rng ) {
        return dPoint(rng, DEFAULT_CLUSTER);
    }
    
    public static double[] dPoint( double[] coords ) {
        if( coords.length < d ) {
            throw new IllegalArgumentException();
        }
        double[] array = new double[coords.length + 1];
        array[0] = DEFAULT_CLUSTER;
        System.arraycopy(coords, 0, array, 1, coords.length);
        return array;
    }  
    
    public static double[] dPoint( double[] coords, int c ) {
        if( coords.length < d ) {
            throw new IllegalArgumentException();
        }
        double[] array = new double[coords.length + 1];
        array[0] = c;
        System.arraycopy(coords, 0, array, 1, coords.length);
        return array;
    }
    
    public static int getCluster(double[] array) {
        return (int)array[0];
    }

    public static void setCluster(int cluster, double[] array) {
        array[0] = cluster;
    }

    public static double[] getCoords(double[] array) {
        double[] coords = new double[array.length - 1];
        System.arraycopy(array, 1, coords, 0, coords.length);
        return coords;
    }

    public static void setCoords(double[] coords, double[] array) {
        System.arraycopy(coords, 0, array, 1, coords.length);
    }
    
}
