package kmeansmr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Writable;

public class Double2DArrayWritable  implements Writable{
	
	
	private DoubleWritable darray[][];
	
	
	public Double2DArrayWritable()
	{
		
	}
	
	public Double2DArrayWritable(DoubleWritable[][] a)
	{
		set(a);
	}
	public void set(DoubleWritable twoDArray[][])
	{
		this.darray = twoDArray;
	}
	
	@Override
	public void readFields(DataInput in) throws IOException
	{
		int outer = in.readInt();
		int inner = in.readInt();
		darray = new DoubleWritable[outer][inner];
		
		for(int i = 0; i < darray.length; i++)
			for(int j = 0; j < darray[0].length; j++)
				darray[i][j] = new DoubleWritable(in.readDouble());
		
		
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeInt(darray.length);
		out.writeInt(darray[0].length);
		
		for(int i = 0; i < darray.length; i++)
			for(int j = 0; j < darray[0].length; j++)
				out.writeDouble(darray[i][j].get());
		
		
	}
	
	
	public DoubleWritable[][] get()
	{
		return darray;
	}
	
	public double[][] getAsDoubles()
	{
		double nums[][] = new double[darray.length][darray[0].length];
		
		for(int i = 0; i < darray.length; i++)
			for(int j = 0; j < darray[0].length; j++)
				nums[i][j] = darray[i][j].get();
		
		return nums;
	}
	
	

}
