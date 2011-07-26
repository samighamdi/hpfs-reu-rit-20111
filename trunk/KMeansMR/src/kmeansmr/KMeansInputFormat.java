package kmeansmr;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;




public class KMeansInputFormat extends FileInputFormat<IntWritable, ArrayList<DoubleWritable>>{

	@Override
	public RecordReader<IntWritable, ArrayList<DoubleWritable>> createRecordReader(
			InputSplit split, TaskAttemptContext arg1) throws IOException,
			InterruptedException {
		
		
		return new KMeansRecordReader() ;
	}


	

	
	
	

	
	}


