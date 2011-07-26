package kmeansmr;


import java.util.ArrayList;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;



import org.apache.hadoop.mapreduce.Mapper;




public class KMeansMap extends Mapper<IntWritable, ArrayList<DoubleWritable>, 
Cluster, ArrayList<DoubleWritable>>

{

@Override
public void map(IntWritable key, ArrayList<DoubleWritable> value, Context context)
{
	
}
	
	
}

