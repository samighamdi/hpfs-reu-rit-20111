package kmeansmr;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class TextOutputFormat extends FileOutputFormat<IntWritable, DoubleArrayWritable>
{
	private class TextRecordWriter extends RecordWriter<IntWritable, DoubleArrayWritable>
	{

		private PrintStream out;
		public TextRecordWriter(PrintStream out) throws IOException
		{
			
			this.out = out;
			

		}

		@Override
		public void close(TaskAttemptContext context) throws IOException,
		InterruptedException {

			
			out.close();
			
		}

		@Override
		public void write(IntWritable key, DoubleArrayWritable value)
				throws IOException, InterruptedException {

			
			String curString = "Cluster " + key.get() + ": ";
			DoubleWritable v[] = value.get();

			for(int i = 0; i < v.length - 1; i++)
			{
				curString += v[i].get() + ", ";
			}


			curString += "" + v[v.length - 1].get();
			

			out.println(curString);
			
			

		}

	}

	@Override
	public RecordWriter<IntWritable, DoubleArrayWritable> getRecordWriter(
			TaskAttemptContext job) throws IOException, InterruptedException {

		Path textPath = FileOutputFormat.getOutputPath(job).suffix("/o.txt");
		FileSystem fs = textPath.getFileSystem(job.getConfiguration());
		

		return new TextRecordWriter(new PrintStream(fs.create(textPath)));
	}

}



