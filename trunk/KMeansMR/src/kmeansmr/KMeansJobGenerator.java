package kmeansmr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;







public class KMeansJobGenerator {


	private String classClusterCoords[][];
	private int k;
	private String pointsFN;
	private boolean fileShouldExist;
	private String initCC[][];


	public KMeansJobGenerator(String pfn, int k)
	{
		this.k = k;
		pointsFN = pfn;
		fileShouldExist = false;


	}

	/**
	 * Creates a kmeans hadoop job starts it and returns the job
	 * @param pointsFN The file name of the points binary file
	 * @param k number of clusters
	 * @return the job that it started
	 */
	public Job generateJob()
	{

		Configuration conf;

		//if the output file exists, read from that instead of randomly generating points
		if(fileShouldExist)
		{
			conf = readFile(new Path("kmmrout/o.txt"));
			String c[]; //temporarily holds a cluster's coords from conf
			boolean convergence = true;

			//see if convergence is achieved.  if so, return null
			for(int i = 0; i < classClusterCoords.length; i++)
			{
				c = conf.getStrings("Cluster" + i);

				for(int j = 0; j < classClusterCoords[i].length; j++)
				{
					if(!classClusterCoords[i][j].equals(c[j]))
					{
						classClusterCoords[i][j] = c[j];
						convergence = false;
					}
				}

			}
			if(convergence)
			{
//				
//				for(int i = 0; i < initCC.length; i++)
//				{
//					System.out.print("Cluster " + i + ": ");
//					for(int j = 0; j < initCC[i].length; j++)
//					{
//						System.out.print(initCC[i][j] + ", ");
//					}
//				}
//				System.out.print("\n");
				return null;
			}
		}
		else
		{
			conf = randCentroids();
			fileShouldExist = true;
		}



//		System.out.println("Before iteration:");
//
//		for(int i = 0; i < k; i++)
//		{
//			System.out.print("Cluster " + i + ": ");
//
//			for(String str : conf.getStrings("Cluster" + i))
//			{
//				System.out.print(str + ", ");
//			}
//
//			System.out.print("\n");
//		}
//
//		System.out.print("\n");
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
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(DoubleArrayWritable.class);
		job.setJarByClass(KMeansJobGenerator.class);


		try {
			FileInputFormat.addInputPath(job, new Path(pointsFN));
		} catch (IOException e) {

			e.printStackTrace();
		}

		FileOutputFormat.setOutputPath(job, new Path("kmmrout"));


		//	job.setOutputFormatClass(TextOutputFormat.class);		
		MultipleOutputs.addNamedOutput(job, "text", TextOutputFormat.class, IntWritable.class, DoubleArrayWritable.class);
		MultipleOutputs.addNamedOutput(job, "pic", PicOutputFormat.class, IntWritable.class, Double2DArrayWritable.class);






		return job;




	}




	private Configuration readFile(Path file)
	{
		Configuration conf = new Configuration();
		BufferedReader br = null;
		int i = 0;
		String line = "", coords[];


		try {
			FileSystem fs = file.getFileSystem(conf);

			if(!fs.exists(file))
				throw new RuntimeException("file should exist, but doesn't");

			br = new BufferedReader(new InputStreamReader(fs.open(file)));

			while((line = br.readLine()) != null)
			{
				coords = line.split(":")[1].split(",");

				for(String str : coords)
				{
					str.trim();
					conf.setStrings("Cluster" + i, coords);
				}
				i++;
			}
			br.close();
			fs.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		conf.setInt("k", k);





		return conf;
	}

	private Configuration randCentroids()
	{

		Configuration conf = new Configuration();
	
		//get the points from binary file
		int dimensionality = -1;
		try {

			ArrayDoubleFS pointsReader = new ArrayDoubleFS(new Path(pointsFN), conf);
			dimensionality = pointsReader.innerSize() - 1; 
			pointsReader.closeHadoop();
		} catch (IOException e) {

			e.printStackTrace();
		} 

		if(dimensionality == -1)
			throw new RuntimeException("Dimensionality was -1");



		//get the centroids (cluster coordinates)
		//any point's array length is equal to k
		double clusters[][] = new double[k][dimensionality];
		classClusterCoords = new String[k][dimensionality]; 
		initCC = new String[k][dimensionality]; 
		String clusterCoords[] = new String[dimensionality]; //will hold a cluster's coordinates
		Random rng = new Random(1337);

		for(int i = 0; i < clusters.length; i++)
		{
			for(int j = 0; j < clusters[i].length; j++)
			{
				clusters[i][j] = rng.nextDouble();
			}
		}


		//copy the centroids into the conf's "strings" property
		for(int i = 0; i < clusters.length; i++)
		{
			for(int j = 0; j < clusters[i].length; j++)
			{
				//convert the cluster coordinates to a string
				clusterCoords[j] = classClusterCoords[i][j] = initCC[i][j] =
						Double.toString(clusters[i][j]);

			}

			conf.setStrings("Cluster" + i, clusterCoords);


		}




		conf.setInt("k", k);

		return conf;
	}
	/*
	 private static double[][] clusterPlusPlus(int k, double points[][]) {
	        double clusters[][] = new double [k][points[0].length];
	        // for each remaining cluster
	        for( int i = 1; i < k; ++i ) {
	            double[] cum = new double[points.length]; // cumulative distribution
	            // for each data point assign it to the nearest cluster
	            for( int m = 0; m < points.length; ++m ) {
	                double p[] = points[m];
	                // choose the nearest cluster
	                double pfit = Double.NEGATIVE_INFINITY;
	                // for each initialized cluster
	                for( int j = 0; j < i; ++j ) {
	                    double fit = fitness(p, clusters[j]);
	                    if( pfit < fit ) {
	                        pfit = fit;
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


		 for(int i = 0; i < a.length; i++)
		 {
			 if(a[i] != b[i])
				 return false;
		 }

		 return true;
	 }
	 */
	public static double fitness( double[] a, double[] b ) {
		double total = 0;
		for( int i = 0; i < a.length; ++i ) {
			total += Math.pow((a[i] - b[i]),2);
		}
		return total;
	}





}