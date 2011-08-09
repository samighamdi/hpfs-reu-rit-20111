package kmeansmr;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;



public class KMeansReduce extends Reducer<IntWritable, DoubleArrayWritable, IntWritable, DoubleArrayWritable>
{
	MultipleOutputs mos;
	@Override
	public void setup(Context context) throws IOException, InterruptedException
	{
		super.setup(context);
		mos = new MultipleOutputs(context);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void reduce(IntWritable key, Iterable<DoubleArrayWritable> values, Context context) throws
	IOException, InterruptedException
	{
	
		final int BUFFERSIZE = KMeansConstants.BUFFERSIZE;  
	
		DoubleWritable point[] = values.iterator().next().get(); //first point is used to get the dimensionality
		double totals[] = new double[point.length];
		ArrayList<DoubleWritable[]> points = new ArrayList<DoubleWritable[]>();
		int numPoints = 1, tmpCounter = 1;
		
		
		
		//initialize totals array to first point coordinates 
		for(int i = 0; i < totals.length; i++)
		{
			totals[i] = point[i].get();
			
		}
		
		points.add(point); //add the initial point to the the collection of points
		
		
		
		//loop through all the points in the cluster
		while(values.iterator().hasNext())
		{
			point = (DoubleWritable[]) values.iterator().next().get();
			
			
			for(int i = 0; i < totals.length; i++)
			{
				totals[i] += point[i].get();
				
			}
			
			
			points.add(point); //add the point to the points arraylist
			numPoints++; tmpCounter++;
			
			if(tmpCounter >= BUFFERSIZE)
			{
				mos.write("pic", key, new Double2DArrayWritable(points));
				points.clear();
				tmpCounter = 0;
			}
		}
		
		
		DoubleArrayWritable ccWrapper = new DoubleArrayWritable();
		DoubleWritable clusterCoords[] = new DoubleWritable[totals.length];
		
		//calculate the new cluster centers
		for(int i = 0; i < totals.length; i++)
		{
			clusterCoords[i] = new DoubleWritable(totals[i] / numPoints);
			
		}
		
		
		
		
		ccWrapper.set(clusterCoords);
		
		
		
		
		
		
		mos.write("text", key, ccWrapper);
		
		if(points.size() != 0)
			mos.write("pic", key, new Double2DArrayWritable(points));
		
		
		
		
	//context.write(key, ccWrapper);
		
		
		
	}
	
	@Override
	public void cleanup(Context context) throws IOException, InterruptedException
	{
		mos.close();
		super.cleanup(context);
	}
	
}
