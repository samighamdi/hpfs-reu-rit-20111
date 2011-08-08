package kmeansmr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.hadoop.io.DoubleWritable;

public class ImgGen 
{
	private final int width = 1024;
	private final int height = 768;
	private double maxX;
	private double maxY;
	
	private int k;
	private Rectangle2D pointsRec;
	private BufferedImage pointsImg;
	private BufferedImage gridImg;
	private Color colors[];
	private String title;
	
	
	/**
	 * 
	 * @param xmax
	 * @param ymax
	 * @param createGrid
	 * @param k
	 * @param title
	 */
	public ImgGen(double xmax, double ymax, boolean createGrid, int k, String title)
	{
		maxX = xmax;
		maxY = ymax;
		this.k = k;
		this.title = title;
		Color colors[] = {Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW, Color.MAGENTA, 
				Color.ORANGE, Color.PINK, Color.LIGHT_GRAY, Color.CYAN, Color.GRAY};
		
		this.colors = colors;
		
		double gridPosX = width * .1;
		double gridPosY = height * .05;
		double gridWidth = width * .85;
		double gridHeight = height * .82;
		pointsRec = new Rectangle2D.Double(gridPosX, gridPosY, gridWidth, gridHeight);
		
		pointsImg = null;
		
		if(createGrid)
			generateGrid();
		
		
		
	}
	
	
	private double[] computeAxis(double max, int numLines)
	{
		double axis[] = new double[numLines];
		
		
		double units = max/numLines; //find the number to increment by on the grid
		double currentUnit = units;
		
		//create the scale that will go along the axis
		for(int i = 0; i < axis.length; i++)
		{
			axis[i] = currentUnit;
			currentUnit += units;
		}
		
		return axis;
	}
	
	public void addPointsImage(BufferedImage img)
	{
		if(pointsImg == null)
			pointsImg = img;
		else
			pointsImg.createGraphics().drawImage(img, 0, 0, null);
		
	}
	
	
	
	
	public void generatePoints(double points[][], int cluster)
	{
		BufferedImage pointsImg = new BufferedImage((int)pointsRec.getWidth() + 10, 
				(int)pointsRec.getHeight() + 10, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		
		
		Graphics2D img = pointsImg.createGraphics();
		
		
		
		
		double xRatio = pointsRec.getWidth() / maxX;
		double yRatio = pointsRec.getHeight() / maxY;
		
		
		
		
		
		//draw the circles
		for(int i = 0; i < points.length; i++)
		{
			img.setPaint(colors[cluster % colors.length]); 
			img.fill(new Ellipse2D.Double(points[i][0] * xRatio - 2, pointsRec.getHeight() - 
					(points[i][1] * yRatio ), 4, 4));
		}
		
		if(this.pointsImg == null)
			this.pointsImg = pointsImg;
		else
			addPointsImage(pointsImg);
		
	}
	
	private void drawLinesAndScale()
	{
		//using different variable names so i dont have to change code
		Graphics2D img = gridImg.createGraphics();
		Rectangle2D rec = pointsRec;
		double gridPosX = rec.getX();
		double gridPosY = rec.getY();
		double gridWidth = rec.getWidth();
		double gridHeight = rec.getHeight();
		
		//units used to make the grid
		double heightUnit = (1/20.0) * gridHeight;
		double widthUnit = (1/20.0) * gridWidth;

		int numXLines = 1;
		int numYLines = 1;

		img.setColor(Color.BLACK); //set outline to black
		img.setStroke(new BasicStroke(2)); //set the thickness of the outline
		img.draw(rec); //draw the outline

		
		//draw the grid lines
		img.setStroke(new BasicStroke((float).5));
		img.setPaint(Color.BLACK);
		//draw the horizontal lines
		for(double i = gridPosY + heightUnit; i < gridHeight * 1.05; i += heightUnit)
		{
			img.draw(new Line2D.Double(gridPosX * .9, i, gridPosX + gridWidth, i));
			numYLines++;
		}
		//draw the vertical lines
		for(double i = gridPosX + widthUnit; i < gridWidth * 1.1; i += widthUnit)
		{
			img.draw(new Line2D.Double(i, gridPosY, i, (gridPosY + gridHeight) * 1.015));
			numXLines++;
		}
		
		//this segment creates the x scale
		double scale[] = computeAxis(maxX, numXLines);
		
		Font font = new Font("Times New Roman", Font.PLAIN, 1);
		FontMetrics metrics = img.getFontMetrics(font);
		
		int j = 0;
		String num;
		int strWidth;
		
		img.drawString("0", (int)gridPosX - 5, (int)(gridPosY + gridHeight) + 22);
		
		for(double i = gridPosX + widthUnit; i < gridWidth * 1.1 + widthUnit ; i += widthUnit, j++)
		{
			num = new DecimalFormat("0.00").format(scale[j]);
			strWidth = metrics.stringWidth(num);
			
			img.drawString(num, (float)(i - (strWidth/2.0) - 5), (float)(gridHeight + 60));
			
			
		}
		
		
		//draw y scale
		scale = computeAxis(maxY, numYLines);
		
		j = scale.length - 1;
		for(double i = gridPosY; i < gridHeight * 1.05; i += heightUnit, j--)
		{
			num = new DecimalFormat("0.00").format(scale[j]);
			strWidth = metrics.stringWidth(num);
			
			img.drawString(num, (float) (gridPosX - strWidth - 50), (float) i + 5);
		}
		
		
		//draw cluster key
		img.drawString("Clusters: ", 10, gridImg.getHeight() - 10);
		
		for(int i = 0; i < k; i++)
		{
			img.setColor(colors[i % colors.length]);
			
			img.drawString(Integer.toString(i), metrics.stringWidth("Clusters: ") + 70 + 10 * i, gridImg.getHeight() - 10);
		}
		
		img.setColor(Color.BLACK);
		img.setFont(new Font("Dialog", Font.BOLD, 20));
		img.drawString(title, gridImg.getWidth()/2 - metrics.stringWidth(title), 25);
	}
	
	
	
	
	private void generateGrid()
	{
		
		
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		Graphics2D img = bi.createGraphics();
		
		img.setBackground(Color.WHITE);
		img.clearRect(0, 0, width, height);
		
		
		
		gridImg = bi;
	}
	
	public BufferedImage getPointsImg() { return pointsImg; }
	
	public BufferedImage getFullImg()
	{
		if(gridImg != null)
		{
			gridImg.createGraphics().drawImage(pointsImg, (int) pointsRec.getX(), (int) pointsRec.getY(), null);
			drawLinesAndScale();
		}
		else
			System.out.println("You never generated the grid!  And it's too late for that HAHA!");
		return gridImg;
	}
	
	/*
	public static void main(String args[])
	{
		Random rng = new Random();
		double points[][] = new double[100000][3];
		
		for(int i = 0; i < points.length; i++)
		{
			points[i][0] = i % 3;
			points[i][1] = rng.nextDouble() * 100;
			points[i][2] = rng.nextDouble() * 100;
		}
		
		
		
		
		ImgGen ig1 = new ImgGen(100, 100, true, 3, "Iteration");
		ig1.generatePoints(points);
		BufferedImage bi = ig1.getFullImg();
		
		
		
		try {
			ImageIO.write(bi, "png", new File("/Users/danielbokser/Desktop/blurb.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		JFrame win = new JFrame();
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		win.add(new JLabel(new ImageIcon(bi)));
		
		win.pack();
		
		win.setVisible(true);
		
		
		
	}
	*/
}
