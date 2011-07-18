/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansserver;

/**l
 *
 * @author Kevin
 */
public class DemoPoint {
    public static final int DEFAULT_CLUSTER = -1;
    private static int d;
    private int cluster;
    private double[] coords;

    /**
     * Plz don't abuse this ;o;
     * @param d the new number of dimensions for all DemoPoints
     * @return  the value overwritten by this call
     */
    public static int setDimension( int d ) {
        int temp = DemoPoint.d;
        DemoPoint.d = d;
        return temp;
    }
    
    public static int getDimension(){
        return d;
    }
    
    public DemoPoint( java.util.Random rng, int c ) {
        this.cluster = c;
        this.coords = new double[d];
        for( int i = 0 ; i < d; ++i ) {
            coords[i] = rng.nextDouble();
        }
    }
    
    /**
     * Constructs a demo point with a default cluster
     * @param rng the RNG to generate coords with
     */
    public DemoPoint( java.util.Random rng ) {
        this(rng, DEFAULT_CLUSTER);
    }
    
    /**
     * Copies the given array and sets this point's cluster assignment
     * @param coords  the coordinates of this point
     * @param c       initial cluster assignment
     */
    public DemoPoint( double[] coords, int c ) {
        if( coords.length < d ) {
            throw new IllegalArgumentException();
        }
        this.coords = new double[d];
        System.arraycopy(coords, 0, this.coords, 0, d);
        this.cluster = c;
    }
    
    /**
     * Copies the given array as this object's position vector
     * @param coords   the coordinates
     */
    public DemoPoint( double[] coords ) {
        this.coords = new double[d];
        System.arraycopy(coords, 0, this.coords, 0, d);
        this.cluster = DEFAULT_CLUSTER;
        if( coords.length < d ) {
            throw new IllegalArgumentException();
        }
    }
    
    public int getCluster() {
        return cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }

    public double[] getCoords() {
        return coords;
    }

    public void setCoords(double[] coords) {
        this.coords = coords;
    }
}
