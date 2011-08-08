package kmeansmr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;



public class KMeansRecordReader extends RecordReader<IntWritable, DoubleArrayWritable> {

	private double points[][];

	private DoubleArrayWritable value;
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
	public DoubleArrayWritable getCurrentValue() throws IOException,
	InterruptedException {

		return value;
	}

	@Override
	public void initialize(InputSplit inSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {

		key = new IntWritable(-1);
		value = new DoubleArrayWritable();

		FileSplit split = (FileSplit) inSplit;
		

		
		ArrayDoubleFS fr = null;
		try {
			fr = new ArrayDoubleFS(split.getPath(), context.getConfiguration());
			points = fr.getArray();
			fr.closeHadoop();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		for(int i = 0; i < points.length; i++)
		{
			points[i] = getCoords(points[i]);
		}

	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {

		key.set(key.get() + 1);
		
		if(key.get() >= points.length)
			return false;

		DoubleWritable p[] = new DoubleWritable[points[key.get()].length];

		for(int j = 0; j < points[key.get()].length; j++)
			p[j] = new DoubleWritable(points[key.get()][j]);
		
		value.set(p);

		return true;
	}


	private double[] getCoords(double[] array) {
		double[] coords = new double[array.length - 1];
		System.arraycopy(array, 1, coords, 0, coords.length);
		return coords;
	}



}
