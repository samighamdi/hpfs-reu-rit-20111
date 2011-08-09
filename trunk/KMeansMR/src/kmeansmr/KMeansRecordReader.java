package kmeansmr;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;



public class KMeansRecordReader extends RecordReader<IntWritable, Double2DArrayWritable> {

	final int BUFFERSIZE = KMeansConstants.BUFFERSIZE;
	private double points[][];
	private ArrayDoubleFS fr = null;
	private Double2DArrayWritable value;
	private IntWritable key;
	private int arraySize;
	private int currentIndex;

	@Override
	public void close() throws IOException {
		fr.closeHadoop();

	}


	@Override
	public float getProgress() throws IOException {

		return (float)(key.get()/ fr.size());
	}



	@Override
	public IntWritable getCurrentKey() throws IOException,
	InterruptedException {

		return key;
	}

	@Override
	public Double2DArrayWritable getCurrentValue() throws IOException,
	InterruptedException {

		return value;
	}

	@Override
	public void initialize(InputSplit inSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {

		key = new IntWritable(-1);
		currentIndex = BUFFERSIZE;

		FileSplit split = (FileSplit) inSplit;
		

		
		
		try {
			fr = new ArrayDoubleFS(split.getPath(), context.getConfiguration());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		

	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {

		
		
		arraySize = Math.min(BUFFERSIZE, (int)fr.size() - key.get());
		key.set(arraySize + key.get()); 
		currentIndex += arraySize;
		
		if(currentIndex >= arraySize && !fr.hasNext())
		{
			return false;
		}
		
		if(currentIndex >= arraySize)
		{
			
			points = fr.getNext(arraySize);	
			currentIndex = 0;
			
			for(int i = 0; i < points.length; i++)
				points[i] = getCoords(points[i]);
		}
		
		DoubleWritable dw[][] = new DoubleWritable[points.length][points[0].length];
		
		for(int i = 0; i < points.length; i++)
			for(int j = 0; j < points[i].length; j++)
				dw[i][j] = new DoubleWritable(points[i][j]);

		value = new Double2DArrayWritable(dw);

		/*
		for(int j = 0; j < points[currentIndex].length; j++)
			p[j] = new DoubleWritable(points[currentIndex][j]);
		
		value.set(p);
		*/

		return true;
	}


	private double[] getCoords(double[] array) {
		double[] coords = new double[array.length - 1];
		System.arraycopy(array, 1, coords, 0, coords.length);
		return coords;
	}



}
