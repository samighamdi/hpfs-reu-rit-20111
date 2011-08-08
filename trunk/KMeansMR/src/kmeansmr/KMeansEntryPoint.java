package kmeansmr;



import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;




public class KMeansEntryPoint {

	public static void main(String args[]) throws Exception
	{
		
		
		if(args.length < 1)
			throw new IllegalArgumentException();
			
		int k = Integer.parseInt(args[0].trim());
		KMeansJobGenerator jg = new KMeansJobGenerator("kmmrin/kmeansdemopoints.adfs", k);
		
		Path ptsPath, outputPath, imgPath;
		FileSystem fs;
		Job job; 
		BufferedImage points, fullImg;
		InputStream in;
		OutputStream out;
		ImgGen ig = new ImgGen(1, 1, true, k, "Random Points");
		int i = 0;
		
		
		long initTime = System.currentTimeMillis();
		while((job = jg.generateJob()) != null && i < 11)
		{
			outputPath = FileOutputFormat.getOutputPath(job);
			fs = outputPath.getFileSystem(job.getConfiguration());
			fs.delete(outputPath, true);
			
			job.waitForCompletion(true);
			
			
			
			ptsPath = outputPath.suffix("/points.png");
			
			in = fs.open(ptsPath);
			points = ImageIO.read(in);
			in.close();
			ig.addPointsImage(points);
			fullImg = ig.getFullImg();
			
			imgPath = outputPath.suffix("/" + i++ + ".png");
			
			out = fs.create(imgPath);
			ImageIO.write(fullImg, "png", out);
			out.close();
			
			
		}
		double finalTime = (System.currentTimeMillis() - initTime) / 60000.0;
		
		System.out.println("KMeans did indeed ahoy");
		System.out.println("Finished in " + finalTime + " minutes with " + (i - 1) + " iterations.");
		
		

		
		
	}
	
	
}
