/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansserver;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.ApplicationFrame;

/**
 *
 * @author Kevin
 */
public class KMeansPlot extends ApplicationFrame {
    public static final String XLABEL = "x-axis", YLABEL = "y-axis", CHART_TITLE = "K-Means";
    public static final int IMAGE_SIZE_X = 600, IMAGE_SIZE_Y = 400;
    private KMeansPlot( final String title ) {
        super(title);
    }
    
    public KMeansPlot( KMeans source, final String title ) {
        this(source, title, XLABEL, YLABEL, new int[]{0,1});
    }
    
    public KMeansPlot( KMeans source, final String title, String xlabel, String ylabel ) {
        this(source, title, xlabel, ylabel, new int[]{0,1});
    }
    
    public KMeansPlot( KMeans source, final String title, String xlabel, String ylabel, int[] axes ) {
        super(title);
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][][] clues = source.clusterDataAsArray(axes);
        source.printClusteredData(System.out);
        System.out.println( "clues: " + clues.length + 
                           " and k: " + source.getK());
        for( int i = 0; i < clues.length; ++i ) {
            dataset.addSeries("Cluster " + i, clues[i]);
        }
        
        final NumberAxis domainAxis = new NumberAxis(xlabel);
        domainAxis.setAutoRangeIncludesZero(false);
        final NumberAxis rangeAxis = new NumberAxis(ylabel);
        rangeAxis.setAutoRangeIncludesZero(false);
        
        final XYPlot xyp = new XYPlot(dataset, domainAxis, rangeAxis, new XYShapeRenderer());
        final JFreeChart chart = new JFreeChart(CHART_TITLE, xyp);
//        chart.setLegend(null);

        // force aliasing of the rendered content..
        chart.getRenderingHints().put
            (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final ChartPanel panel = new ChartPanel(chart, true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
  //      panel.setHorizontalZoom(true);
    //    panel.setVerticalZoom(true);
        panel.setMinimumDrawHeight(10);
        panel.setMaximumDrawHeight(2000);
        panel.setMinimumDrawWidth(20);
        panel.setMaximumDrawWidth(2000);
        
        setContentPane(panel);
    }
    
    public static BufferedImage generateImage( KMeans source, final String title, String xlabel, String ylabel, int[] axes ) {
        
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][][] clues = source.clusterDataAsArray(axes);
        for( int i = 0; i < clues.length; ++i ) {
            dataset.addSeries("Cluster " + i, clues[i]);
        }
        
        final NumberAxis domainAxis = new NumberAxis(xlabel);
        domainAxis.setAutoRangeIncludesZero(false);
        final NumberAxis rangeAxis = new NumberAxis(ylabel);
        rangeAxis.setAutoRangeIncludesZero(false);
        
        final XYPlot xyp = new XYPlot(dataset, domainAxis, rangeAxis, new XYShapeRenderer());
        final JFreeChart chart = new JFreeChart(title, xyp);
        
        return chart.createBufferedImage( IMAGE_SIZE_X, IMAGE_SIZE_Y );
    }
}
