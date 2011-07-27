package kmeansmr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;



public class KMeansRecordReader extends RecordReader<IntWritable, ArrayList<DoubleWritable>> {

	private double points[][];
	
	private ArrayList<DoubleWritable> value;
	private IntWritable key;
	
	@Override
	public void close() throws IOException {
		
		
	}

	
	@Override
	public float getProgress() throws IOException {
		
		return (float)key.get() / (points.length - 1);
	}

	

	@Override
	public IntWritable getCurrentKey() throws IOException,
			InterruptedException {
		
		return key;
	}

	@Override
	public ArrayList<DoubleWritable> getCurrentValue() throws IOException,
			InterruptedException {
		
		return value;
	}

	@Override
	public void initialize(InputSplit inSplit, TaskAttemptContext arg1)
			throws IOException, InterruptedException {
		
		key = new IntWritable(0);
		value = new ArrayList<DoubleWritable>();
		
		FileSplit split = (FileSplit) inSplit;
		
		ArrayDoubleFS fr = null;
		try {
			fr = new ArrayDoubleFS(new File(split.getPath().toUri()));
			points = fr.getArray();
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		
		key.set(key.get() + 1);
		
		value.clear();
		
		for(int j = 0; j < points[key.get()].length; j++)
			value.add(new DoubleWritable(points[key.get()][j]));
		
		return true;
	}


	

	

}
