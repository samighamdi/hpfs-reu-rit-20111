package kmeansmr;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;



public class KMeansReduce extends Reducer<IntWritable, ArrayList<DoubleWritable>, Object, Object>
{
	@Override
	protected void reduce(IntWritable key, Iterable<ArrayList<DoubleWritable>> values, Context context) throws
	IOException, InterruptedException
	{
		ArrayList<DoubleWritable> point = values.iterator().next(); //first point is used to get the dimensionality
		double totals[] = new double[point.size()];
		int numPoints = 1;
		
		//initialize totals array to first point coordinates 
		for(int i = 0; i < totals.length; i++)
			totals[i] = point.get(i).get();
		
		while(values.iterator().hasNext())
		{
			point = values.iterator().next();
			
			for(int i = 0; i < totals.length; i++)
				totals[i] += point.get(i).get();
			
			numPoints++;
		}
		
		String clusterCoords[] = new String[totals.length];
		
		for(int i = 0; i < totals.length; i++)
			clusterCoords[i] = Double.toString(totals[i] / numPoints);
		
		context.getConfiguration().setStrings("Cluster" + key.get(), clusterCoords);
	}
	
}
