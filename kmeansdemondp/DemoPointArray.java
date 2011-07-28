/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansdemondp;

/**
 *
 * @author reu
 */
public class DemoPointArray {
    public static DemoPoint[] toDemoPoints(double[][] array) {
        DemoPoint[] points = new DemoPoint[array.length];
        for (int i = 0; i < array.length; i++) {
            double[] inner = array[i];
            double[] coords = new double[DemoPoint.getDimension()];
            System.arraycopy(inner, 1, coords, 0, coords.length);
            points[i] = new DemoPoint(coords);
        }
        return points;
    }
    
    
    public static double[][] toArray(DemoPoint[] points) {
        double[][] array = new double[points.length][];
        for (int i = 0; i < points.length; i++) {
            DemoPoint point = points[i];
            double[] innerArray = new double[DemoPoint.getDimension() + 1];
            innerArray[0] = point.getCluster();
            System.arraycopy(point.getCoords(), 0, innerArray, 1, innerArray.length - 1);
            array[i] = innerArray;
        }
        return array;
    }
}
