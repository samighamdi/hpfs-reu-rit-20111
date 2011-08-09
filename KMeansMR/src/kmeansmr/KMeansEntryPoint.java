package kmeansmr;



import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;




public class KMeansEntryPoint {

	private static class ShowImage extends JFrame
	{
		JLabel image;
		 public void setImage(BufferedImage img) {
		        if (image != null) {
		            this.remove(image);
		        }
		        image = new JLabel(new ImageIcon(img));
		        this.add(image);
		    }
	}
	
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
		
//		
//		ShowImage jf = new ShowImage();
//		jf.setVisible(true);
		
		
		long initTime = System.currentTimeMillis(), initITime = System.currentTimeMillis();
		double iterationTime;
		while((job = jg.generateJob()) != null)
		{
			
			outputPath = FileOutputFormat.getOutputPath(job);
			fs = outputPath.getFileSystem(job.getConfiguration());
			fs.delete(outputPath, true);
			
			
			job.waitForCompletion(false);
			
			
			ptsPath = outputPath.suffix("/points.png");
			
			in = fs.open(ptsPath);
			points = ImageIO.read(in);
			in.close();
			ig.addPointsImage(points);
			fullImg = ig.getFullImg();
			
			imgPath = outputPath.suffix("/" + i++ + ".png");
//			jf.setImage(fullImg);
//			jf.pack();
			
			out = fs.create(imgPath);
			ImageIO.write(fullImg, "png", out);
			out.close();
			
//			iterationTime = (System.currentTimeMillis() - initITime) / 1000.0;
//			System.out.println("Iteration time: " +  iterationTime + " seconds" );
//			initITime = System.currentTimeMillis();
			
			
			
			
			
		}
		double finalTime = (System.currentTimeMillis() - initTime) / 60000.0;
		
		//System.out.println("KMeans did indeed ahoy");
		System.out.println("Finished in " + finalTime + " minutes with " + (i - 1) + " iterations.");
		
		

		
		
	}
	
	
}
