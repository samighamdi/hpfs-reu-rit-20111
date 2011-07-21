package kmeansmr;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;


import org.apache.hadoop.io.DoubleWritable;

import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.FileSplit;

import arrayfs.ArrayDoubleFS;
@SuppressWarnings("deprecation")
public class KMeansRecordReader implements RecordReader<DoubleWritable, ArrayList<DoubleWritable>> {

	private double points[][];
	private int i;
	
	public KMeansRecordReader( FileSplit split)
	{
		ArrayDoubleFS fr = null;
		try {
			fr = new ArrayDoubleFS(new File(split.getPath().toUri()));
			points = fr.getArray();
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 i = 0;
		 
		
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DoubleWritable createKey() {
		// TODO Auto-generated method stub
		return new DoubleWritable();
	}

	@Override
	public ArrayList<DoubleWritable> createValue() {
		
		return new ArrayList<DoubleWritable>();
	}

	@Override
	public long getPos() throws IOException {
		// TODO Auto-generated method stub
		return i;
	}

	@Override
	public float getProgress() throws IOException {
		// TODO Auto-generated method stub
		return i / (points.length - 1);
	}

	@Override
	public boolean next(DoubleWritable key, ArrayList<DoubleWritable> point)
			throws IOException {
		
		key.set((double) i);
		
		for(int j = 0; j < points[i].length; j++)
			point.add(new DoubleWritable(points[i][j]));
		
		i++;
		
		
		
		return true;
	}


	

	

}
