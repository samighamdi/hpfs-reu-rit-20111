package kmeansmr;


import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;



import org.apache.hadoop.mapreduce.Mapper;




public class KMeansMap extends Mapper<IntWritable, Double2DArrayWritable, 
IntWritable, DoubleArrayWritable>

{

	@Override
	public void map(IntWritable key, Double2DArrayWritable value, Context context) throws IOException,
	InterruptedException
	{
		Configuration conf = context.getConfiguration();


		double points[][] = value.getAsDoubles();
		double point[] = null; //will hold one point

		int k = conf.getInt("k", -1);
		double minTotal = Double.POSITIVE_INFINITY, tempTotal;


		if(k == -1)
			throw new RuntimeException("K was -1");



		double clusters[][] = new double[k][points[0].length];
		String clustCoords[];
		int closestCluster = -1;

		//turn the clusters into a double array
		for(int i = 0; i < clusters.length; i++)
		{
			clustCoords = conf.getStrings("Cluster" + i);
			for(int j = 0; j < clusters[i].length; j++)
			{
				clusters[i][j] = Double.parseDouble(clustCoords[j]);
			}

		}

		

		for(int i = 0; i < points.length; i++)
		{
			point = points[i];
			minTotal = Double.POSITIVE_INFINITY;
			for(int j = 0; j < clusters.length; j++)
			{

				tempTotal = KMeansJobGenerator.fitness(point, clusters[j]);
				if(minTotal > tempTotal)
				{
					minTotal = tempTotal;
					closestCluster = j;

				}
			}
			context.write(new IntWritable(closestCluster), new DoubleArrayWritable(point));
		}


		



	}




}

