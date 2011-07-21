package kmeansmr;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Reporter;

@SuppressWarnings("deprecation")
public class KMeansInputFormat extends FileInputFormat<DoubleWritable, ArrayList<DoubleWritable>>{

	@Override
	public org.apache.hadoop.mapred.RecordReader<DoubleWritable, ArrayList<DoubleWritable>> getRecordReader(
			org.apache.hadoop.mapred.InputSplit split, JobConf arg1,
			Reporter arg2) throws IOException {
		
		return new KMeansRecordReader((FileSplit) split);
	}

	
	

	
	}


