package kmeansdemondp;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import edu.rit.pj.Comm;

public class Census extends ParallelKMeans {
	public static final int CENSUS_K = 2, CENSUS_D = 5;
	public static final String[] ELEM_NAMES = { "age",
												"workclass",
												"final weight",
												"education",
												"education-num",
												"marital status",
												"occupation",
												"transport-moving",
												"relationship",
												"race",
												"sex",
												"capital gain",
												"capital loss",
												"hours per week",
												"native country"
	};
	
	private Census(Comm talk, int frontrank) {
		super(talk, frontrank);
		// TODO Auto-generated constructor stub
	}
	
	public static Census prepareData( Comm talk, String filename, int frontrank ) throws IOException {
		
		Census inst = new Census(talk, frontrank);
				
		LinkedList<String> lines = new LinkedList<String>();
		int count = 0;
		Scanner in = new Scanner( new File(filename)).useDelimiter("\n");
		while( in.hasNext() ) {
			String inline = in.next();
			if( inline.length() < 2 ) break;
			lines.add(inline);
			++count;
		}
		inst.size = count;
		inst.k = CENSUS_K;
                DemoPoint.setDimension(CENSUS_D);
		inst.points = new DemoPoint[count];
		inst.clusters = new DemoPoint[CENSUS_K];
		
		Iterator<String> lit = lines.iterator();
		for( int i = 0 ; i < count && lit.hasNext(); ++i) {
			inst.points[i] = new DemoPoint(parseCensusLine( lit.next() ));
		}
		inst.prepareClusters();
		return inst;
	}

	private static double[] parseCensusLine( String line ) {
		double[] ret = new double[CENSUS_D];
		String[] values = line.split(",");
		int[] valuesIndices = {0, //age
							   4, // education-num
							   10,// capital-gain
							   11,// capital loss
							   12 // hours-per-week
							  };
		for( int i = 0 ; i < CENSUS_D; ++i ) {
			String word = values[valuesIndices[i]];
			if( word.contains("?")) {
				System.out.println( "not a number!" );
				ret[i] = Double.NaN;
			}
			ret[i] = Double.parseDouble(values[valuesIndices[i]]);
		}
		return ret;
	}
	/*
	public void prepareData() {
		// TODO Auto-generated method stub
		
	}
	*/
	public static void main(String[] args) throws IOException {
		Comm.init(args);
		Census me = Census.prepareData(Comm.world(), "adult.data", Comm.world().rank());
		if( me == null ) {
			System.err.println( "Check your filename" );
			return;
		}
		int count = 0 ;
		for( DemoPoint p : me.points ) {
			++count;
			if( count % 100 == 0 );
				//System.out.println( Arrays.toString(p));
		}
	}
}
