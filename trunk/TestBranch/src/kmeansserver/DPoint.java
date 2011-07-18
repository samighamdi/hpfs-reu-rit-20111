/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansserver;

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
    
    public static double[] dPoint(java.util.Random rng, int c) {
        double[] array = new double[d + 1];
        array[0] = c;
        for( int i = 1; i < d; ++i ) {
            array[i] = rng.nextDouble();
        }
        return array;
    }
    
    public static double[] dPoint( java.util.Random rng ) {
        return dPoint(rng, DEFAULT_CLUSTER);
    }
    
    public static double[] dPoint( double[] coords ) {
        if( coords.length < d ) {
            throw new IllegalArgumentException();
        }
        double[] array = new double[d + 1];
        array[0] = DEFAULT_CLUSTER;
        System.arraycopy(coords, 0, array, 1, d);
        return array;
    }  
    
    public static int getCluster(double[] array) {
        return (int)array[0];
    }

    public static void setCluster(int cluster, double[] array) {
        array[0] = cluster;
    }

    public static double[] getCoords(double[] array) {
        double[] coords = new double[d];
        System.arraycopy(array, 1, coords, 0, d);
        return coords;
    }

    public static void setCoords(double[] coords, double[] array) {
        System.arraycopy(coords, 0, array, 1, d);
    }
    
}
