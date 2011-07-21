package kmeansmr;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;



@SuppressWarnings("deprecation")
public class KMeansMap extends MapReduceBase implements Mapper<DoubleWritable, ArrayList<DoubleWritable>, 
Cluster, ArrayList<DoubleWritable>>

{

	@Override
	public void map(DoubleWritable arg0, ArrayList<DoubleWritable> arg1,
			OutputCollector<Cluster, ArrayList<DoubleWritable>> arg2,
			Reporter arg3) throws IOException {
		
		
	}

	
	
}

