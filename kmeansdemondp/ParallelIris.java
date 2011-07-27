package kmeansdemondp;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import edu.rit.mp.DoubleBuf;
import edu.rit.mp.IntegerBuf;
import edu.rit.pj.Comm;
import edu.rit.mp.ByteBuf;
import edu.rit.pj.CommStatus;
import edu.rit.util.Range;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import edu.rit.util.Random;

public class ParallelIris extends ParallelKMeans {

	public static final String DEFAULT_IN = "iris.data.txt", 
			DEFAULT_OUT = "iris_out.txt",
			IMG_OUT_BASE = "iris";
	public static final String[] ELEM_NAMES = new String[] { "sepal length",
		"sepal width",
		"petal length",
	"petal width" };
	public static final int IRIS_D = 4, // dimensions in iris data
			IRIS_K = 2; // identifiable species
	private String infile = DEFAULT_IN;

	/**
	 * Parses args in order: String name, int size, int k, int d
	 * If fewer args are given, default values are used
	 * Values may be overwritten by the file parsing process
	 * @param args 
	 */
	public ParallelIris( Comm world, int frontRank, String[] args ) {
		this(world, frontRank); // this puts default values for instance vars
		switch( args.length ) {
		case 4:
			if( isInteger(args[3]))
				d = Integer.valueOf( args[3] );
		case 3:
			if( isInteger(args[2]))
				k = Integer.valueOf( args[2] );
		case 2:    
			if( isInteger(args[1]))
				size = Integer.valueOf(args[1]);
		case 1:
			if( new File(args[0]).exists() )
				infile = args[0];
		}
	}

	/**
	 * See documentation of kmeansdemo for the meaning of the arguments;
	 * they are passed in order to the int,int,int,long constructor
	 * @param file the data file to be parsed
	 * @param a
	 * @param b
	 * @param c
	 */
	public ParallelIris( Comm world, int frontRank, String file, int size, int k, int d) {
		super( world, frontRank, size, k, d);
		this.infile = file;
	}

	public ParallelIris(Comm world, int frontRank) {
		super( world, frontRank, DEFAULT_SIZE, IRIS_K, IRIS_D );
		this.infile = DEFAULT_IN;
	}
	
	/**
	 * Parses the given datafile into a collection of iris points and stores it 
	 * into an ArrayDoubleFS file. Writes into the file every 1 million points
	 * @param source
	 * @param dimension
	 */
	public static void prepareArrayFS(String source, String arrayfile, int dimension) throws IOException {
		prepareArrayFS(source, arrayfile, dimension, 1000000);
	}
	
	/**
	 * Parses the source datafile as an iris dataset and stores it into an arrayfs file at
	 * that location. It uses only the given number of dimensions (dimension > 4 will crash)
	 * and writes into the file after reading maxiter records or until all records are read
	 * (whichever comes first)
	 * @param source
	 * @param arrayfile
	 * @param dimension
	 * @param maxiter
	 * @throws IOException
	 */
	public static void prepareArrayFS( String source, String arrayfile, int dimension, int maxiter ) throws IOException {
		Scanner in = new Scanner( new File(source)).useDelimiter("\n");
		LinkedList<String> l = new LinkedList<String>();
		int count = 0;
		ArrayDoubleFS outfs = new ArrayDoubleFS(arrayfile, dimension+1);
		DPoint.setDimension(dimension);
		double[][] points = new double[maxiter][dimension+1];
		while( in.hasNext() ) {
			int pointsp = 0;
			// grab the next bunch of lines
			while( in.hasNext() && count < maxiter ) {
				String inline = in.next();
				if( inline.length() < 2 ) break;
				l.add(inline);
				++count;
			}
			// parse them into data points
			for( String inS : l ) {
				if( inS.equals("")) break;
				String[] split = inS.split(",");
				double[] data = new double[dimension];
				for( int i = 0; i < dimension; ++i ) {
					Double din = new Double(split[i]);
					data[i] = din;
				}
				points[pointsp] = DPoint.dPoint(data);
				pointsp++;
			}
			// store them in the array file
			outfs.append(points);
		}

	}

	

	public int getSize() { return size; }
	
	public double[][] pointsData() {
		return points;
	}

	public static void main( String[] args ) throws IOException { 
		Comm.init(args);
		Comm world = Comm.world();
		/*
		int dim = IRIS_D, kay = IRIS_K, datasize = Integer.MAX_VALUE;
		String infile = DEFAULT_IN;
		switch( args.length ) {
		case 4:
			if( isInteger(args[3]))
				dim = Integer.valueOf( args[3] );
		case 3:
			if( isInteger(args[2]))
				kay = Integer.valueOf( args[2] );
		case 2:    
			if( isInteger(args[1]))
				datasize = Integer.valueOf(args[1]);
		case 1:
			if( new File(args[0]).exists() )
				infile = args[0];
		}
		*/
		// the size argument to constructor is the max # of lines to read
		//new ParallelIris(world, args).startKMeans(10);
		
	}
	/* */
}
