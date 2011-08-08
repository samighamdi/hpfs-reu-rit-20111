package kmeansmr;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Writable;

public class DoubleArrayWritable implements Writable{


	private DoubleWritable darray[];


	public DoubleArrayWritable()
	{

	}

	public DoubleArrayWritable(DoubleWritable[] a)
	{
		set(a);
	}
	public void set(DoubleWritable array[])
	{
		this.darray = array;
	}

	@Override
	public void readFields(DataInput in) throws IOException
	{
		darray = new DoubleWritable[in.readInt()];

		for(int i = 0; i < darray.length; i++)
			darray[i] = new DoubleWritable(in.readDouble());


	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeInt(darray.length);

		for(int i = 0; i < darray.length; i++)
			out.writeDouble(darray[i].get());


	}


	public DoubleWritable[] get()
	{
		return darray;
	}

	public double[] getAsDoubles()
	{
		double nums[] = new double[darray.length];

		for(int i = 0; i < darray.length; i++)
			nums[i] = darray[i].get();

		return nums;
	}
}
