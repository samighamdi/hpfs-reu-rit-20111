package kmeansmr;


import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;



import org.apache.hadoop.mapreduce.Mapper;




public class KMeansMap extends Mapper<IntWritable, ArrayList<DoubleWritable>, 
Cluster, ArrayList<DoubleWritable>>

{

@Override
public void map(IntWritable key, ArrayList<DoubleWritable> value, Context context)
{
	Configuration conf = context.getConfiguration();
	double point[] = new double[value.size()];
	int k = conf.getInt("k", -1);
	double minTotal = Double.POSITIVE_INFINITY, tempTotal;
	
		
	if(k == -1)
		throw new RuntimeException("K was -1");
	

	
	double clusters[][] = new double[k][value.size()];
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
	
	//turn the point into a double array
	for(int i = 0; i < value.size(); i++)
		point[i] = value.get(i).get();
	
	
	for(int i = 0; i < clusters.length; i++)
	{
		tempTotal = KMeansDriver.fitness(point, clusters[i]);
		if(minTotal > tempTotal)
		{
			minTotal = tempTotal;
			closestCluster = i;
			
		}
	}
	
	try {
		context.write(new Cluster(closestCluster, clusters[closestCluster]), value);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
}


	
	
}

