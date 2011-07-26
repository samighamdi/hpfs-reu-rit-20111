package kmeansmr;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import arrayfs.ArrayDoubleFS;





public class KMeansDriver {

	
	/**
	 * Creates a kmeans hadoop job starts it and returns the job
	 * @param pointsFN The file name of the points binary file
	 * @return the job that it started
	 */
	public static Job startJob(String pointsFN)
	{
		
		Configuration conf = createConfWithCentroids(pointsFN);
		
		
		Job job = null;
		try {
			job = new Job(conf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		job.setInputFormatClass(KMeansInputFormat.class);
		job.setMapperClass(KMeansMap.class);
		job.setReducerClass(KMeansReduce.class);
		
		
		//submit the job to hadoop
		try {
			job.submit();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		}
		
		return job;
		
	
		
		
	}
	
	/**
	 * Creates a Configuration object that has all the centroids
	 * @param pointsFN the file name of the points file
	 * @return generated Configuration object 
	 * @throws Exception 
	 */
	private static Configuration createConfWithCentroids(String pointsFN)
	{
		Configuration conf = new Configuration();
		

		//get the points from binary file
		
		double points[][] = null;
		try {
			
			ArrayDoubleFS pointsReader = new ArrayDoubleFS(new File(pointsFN));
			points = pointsReader.getArray(); 
			pointsReader.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		



		//get the centroids (cluster coordinates)
		//any point's array length is equal to k
		double clusters[][] = clusterPlusPlus(points[0].length, points);
		String clusterCoords[] = new String[points[0].length]; //will hold a cluster's coordinates


		//copy the centroids into the conf's "strings" property
		for(int i = 0; i < clusters.length; i++)
		{
			for(int j = 0; j < clusters[i].length; j++)
			{
				//convert the cluster coordinates to a string
				clusterCoords[j] = Double.toString(clusters[i][j]);
			}

			conf.setStrings("Cluster" + Integer.toString(i), clusterCoords);
		}




		return conf;
	}
	
	 public static double[][] clusterPlusPlus(int k, double points[][]) {
	        double clusters[][] = new double [k][points[0].length];
	        double[] in = points[(int)(Math.random() * points.length)];
	        // for each remaining cluster
	        for( int i = 1; i < k; ++i ) {
	            double[] cum = new double[points.length]; // cumulative distribution
	            // for each data point assign it to the nearest cluster
	            for( int m = 0; m < points.length; ++m ) {
	                double p[] = points[m];
	                // choose the nearest cluster
	                double pfit = Double.NEGATIVE_INFINITY;
	                int choice = 0;
	                // for each initialized cluster
	                for( int j = 0; j < i; ++j ) {
	                    double fit = fitness(p, clusters[j]);
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
	            boolean posUsedBefore = false;
	            do {
	                double choice = Math.random()*cum[cum.length-1];
	                position = 0;
	                while( choice > cum[position] ) 
	                	++position;
	                
	             //make sure the cluster points havent been used before
	             for(int j = 0; j < i; j++)
	             {
	            	 posUsedBefore = isSameCoords(clusters[j], points[position]);
	             }
	                
	                
	            } while( posUsedBefore );
	            
	            clusters[i] = points[position];
	        }
	        return clusters;
	    }
	 
	 
	 
	 private static boolean isSameCoords(double a[], double b[])
	 {
		 int length = (a.length >= b.length) ? a.length : b.length;
		 
		 for(int i = 0; i < length; i++)
		 {
			 if(a[i] != b[i])
				 return false;
		 }
		 
		 return true;
	 }
	 
	 private static double fitness( double[] a, double[] b ) {
	        double total = 0;
	        for( int i = 0; i < a.length; ++i ) {
	            total += Math.pow((a[i] - b[i]),2);
	        }
	        return total;
	    }


}