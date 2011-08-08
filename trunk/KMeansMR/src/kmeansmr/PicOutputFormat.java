package kmeansmr;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PicOutputFormat extends FileOutputFormat<IntWritable, Double2DArrayWritable>{

	private class PicRecordWriter extends RecordWriter<IntWritable, Double2DArrayWritable>
	{

		
		private ImgGen ig;
		private Path imgPath;
		private FileSystem fs;
		BufferedImage img;
		
		public PicRecordWriter(Path imgPath, int k, Configuration conf) throws IOException
		{
			this.imgPath = imgPath;
			fs = imgPath.getFileSystem(conf);
			ig = new ImgGen(1, 1, false, k, "Random Points");
		}
		
		
		
		@Override
		public void close(TaskAttemptContext context) throws IOException,
				InterruptedException {
			
			img = ig.getPointsImg();
			OutputStream out = fs.create(imgPath);
			ImageIO.write(img, "png", out);
			out.close();
			
			
		}

		@Override
		public void write(IntWritable key, Double2DArrayWritable value)
				throws IOException, InterruptedException {
			
			
			
			ig.generatePoints(value.getAsDoubles(), key.get());
			
			
			
			
		}
		
	}
	
	@Override
	public RecordWriter<IntWritable, Double2DArrayWritable> getRecordWriter(
			TaskAttemptContext context) throws IOException, InterruptedException {
		
		//setup variables for image generation
		FileSystem fs = FileSystem.get(context.getConfiguration());
		Path picTempPath = FileOutputFormat.getOutputPath(context);
		fs.mkdirs(picTempPath);
		int k = context.getConfiguration().getInt("k", -1);
		
		Path imgPath = picTempPath.suffix("/points.png");

		if(k == -1)
			throw new RuntimeException("k is -1");
		
		
		
		
		return new PicRecordWriter(imgPath, k, context.getConfiguration());
	}

}
