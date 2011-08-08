package kmeansmr;


import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;



import org.apache.hadoop.mapreduce.Mapper;




public class KMeansMap extends Mapper<IntWritable, DoubleArrayWritable, 
IntWritable, DoubleArrayWritable>

{

	@Override
	public void map(IntWritable key, DoubleArrayWritable value, Context context) throws IOException,
	InterruptedException
	{
		Configuration conf = context.getConfiguration();
		double point[] = new double[value.get().length];
		int k = conf.getInt("k", -1);
		double minTotal = Double.POSITIVE_INFINITY, tempTotal;


		if(k == -1)
			throw new RuntimeException("K was -1");



		double clusters[][] = new double[k][value.get().length];
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

		DoubleWritable p[] = (DoubleWritable[]) value.get();

		//turn the point into a double array
		for(int i = 0; i < value.get().length; i++)
			point[i] = p[i].get();

		context.getConfiguration().setFloat("yes", (float) point[0]);

		for(int i = 0; i < clusters.length; i++)
		{
			tempTotal = KMeansJobGenerator.fitness(point, clusters[i]);
			if(minTotal > tempTotal)
			{
				minTotal = tempTotal;
				closestCluster = i;

			}
		}


		context.write(new IntWritable(closestCluster), value);



	}




}

