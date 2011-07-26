package kmeansmr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class Cluster implements WritableComparable<Cluster> {

	private int id;
	private double coords[];
	
	public Cluster(int id, double coords[])
	{
		this.id = id;
		this.coords = coords;
	}
	
	public int getId() { return id; }
	
	@Override
	public void readFields(DataInput in) throws IOException {
		
		id = in.readInt();
		
		for(int i = 0; i < coords.length; i++)
			coords[i] = in.readDouble();
		
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(id);
		
		for(int i = 0; i < coords.length; i++)
			out.writeDouble(coords[i]);
		
	}

	@Override
	public int compareTo(Cluster o) {
		return id - o.getId();
	}

	
	

}
