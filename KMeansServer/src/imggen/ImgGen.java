package imggen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

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
	
	
	
	public ImgGen(double xmax, double ymax, boolean createGrid, int k)
	{
		maxX = xmax;
		maxY = ymax;
		this.k = k;
		
		Color colors[] = {Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW, Color.MAGENTA, 
				Color.ORANGE, Color.PINK, Color.LIGHT_GRAY, Color.BLACK, Color.CYAN, Color.GRAY};
		
		this.colors = colors;
		
		double gridPosX = width * .1;
		double gridPosY = height * .05;
		double gridWidth = width * .85;
		double gridHeight = height * .82;
		pointsRec = new Rectangle2D.Double(gridPosX, gridPosY, gridWidth, gridHeight);
		
		
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
	
	
	
	public void generatePoints(double points[][])
	{
		BufferedImage pointsImg = new BufferedImage((int)pointsRec.getWidth() + 10, 
				(int)pointsRec.getHeight() + 10, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		Graphics2D img = pointsImg.createGraphics();
		
		
		
		double xRatio = pointsRec.getWidth() / maxX;
		double yRatio = pointsRec.getHeight() / maxY;
		
		
		
		
		
		//draw the circles
		for(int i = 0; i < points.length; i++)
		{
			img.setPaint(colors[(int)points[i][0] % colors.length]); 
			img.fill(new Ellipse2D.Double(points[i][1] * xRatio - 2, pointsRec.getHeight() - 
					(points[i][2] * yRatio ), 4, 4));
		}
		
		
		this.pointsImg = pointsImg;
		
	}
	
	
	
	
	private void generateGrid()
	{
		
		
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		Graphics2D img = bi.createGraphics();
		
		//these values are renamed so i dont have to change code
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
		img.setPaint(Color.WHITE); //make the grid white
		img.fill(rec); //fill the shape
		
		
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
		img.drawString("Clusters: ", 10, bi.getHeight() - 10);
		
		for(int i = 0; i < k; i++)
		{
			img.setColor(colors[i % colors.length]);
			
			img.drawString(Integer.toString(i), metrics.stringWidth("Clusters: ") + 70 + 10 * i, bi.getHeight() - 10);
		}
		
		gridImg = bi;
	}
	
	public BufferedImage getPointsImg() { return pointsImg; }
	
	public BufferedImage getFullImg()
	{
		if(gridImg != null)
			gridImg.createGraphics().drawImage(pointsImg, (int) pointsRec.getX(), (int) pointsRec.getY(), null);
		else
			System.out.println("You never generated the grid!  And it's too late for that HAHA!");
		return gridImg;
	}
	
	
	
	
}
