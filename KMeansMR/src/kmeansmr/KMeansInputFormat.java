package kmeansmr;

import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;




public class KMeansInputFormat extends FileInputFormat<IntWritable, Double2DArrayWritable>{

	@Override
	public RecordReader<IntWritable, Double2DArrayWritable> createRecordReader(
			InputSplit split, TaskAttemptContext arg1) throws IOException,
			InterruptedException {
		
		
		return new KMeansRecordReader() ;
	}


	

	
	
	

	
	}


