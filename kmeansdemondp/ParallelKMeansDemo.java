package kmeansdemondp;
/*
 * 
 */


import java.io.IOException;

import edu.rit.pj.Comm;
import edu.rit.util.Range;
import edu.rit.mp.DoubleBuf;
import edu.rit.pj.reduction.DoubleOp;
import edu.rit.mp.IntegerBuf;
import edu.rit.pj.reduction.IntegerOp;
import edu.rit.mp.BooleanBuf;
import edu.rit.util.Random;

public class ParallelKMeansDemo extends KMeansDemo {
	Comm world;
	int rank, worldsize;
	
	Range[] ranges;
	Range ir;
	
	public ParallelKMeansDemo( Comm world ) {
		super();
		this.world = world;
		this.rank = world.rank();
		this.worldsize = world.size();
		Range[] ranges = new Range(0, size-1).subranges(size);
                Range ir = ranges[rank];
                setStart(ir.lb());
                setEnd(ir.ub());
	}
	
	public ParallelKMeansDemo( Comm world, String[] args ) {
		super( args );
		this.world = world;
		this.rank = world.rank();
		this.worldsize = world.size();
		ranges = new Range(0, size-1).subranges(size);
                ir = ranges[rank];
                setStart(ir.lb());
                setEnd(ir.ub());
	}
	
	public ParallelKMeansDemo( Comm world, int keyser, int k, int d ) {
		super( keyser, k, d, System.currentTimeMillis() );
		this.world = world;
		this.rank = world.rank();
		this.worldsize = world.size();
		ranges = new Range(0, this.size-1).subranges(worldsize);
                ir = ranges[rank];
                setStart(ir.lb());
                setEnd(ir.ub());
	}
	
	@Override
	public void prepareClusters() {
		prepareClusters(true);
	}
	
	public void prepareClusters(boolean parallel) {
		for(int i = 0; i < points.length; i++) {
                        DemoPoint dp = points[i];
			int c = (int)(k*rng.nextDouble());
			//System.out.println(dp + " assigned to clu " + c);
			if( dp != null ) 
				dp.setCluster(c);
		}
		if( parallel ) {
			updateStep();
		}
		else {
			System.out.println("datasize " + size);
			System.out.println("new Range(0, " + (size-1) + ").subranges("+size+");");
			Range[] ranges = new Range(0, size-1).subranges(worldsize);
			Range ir = ranges[rank];
			this.setStart(ir.lb());
			this.setEnd(ir.ub());
			System.out.println("my ranges : " + ir.lb() + " to " + ir.ub());
			
			updateStep(false);
		}
	}
    
    protected void broadcastClusters() throws IOException {
    	double[][] assignments = new double[k][d];
        
        if( rank == 0 ) assignments = this.clusterCenters();
        DoubleBuf dbuf = DoubleBuf.buffer(assignments);
        world.broadcast(0, dbuf);
        // System.out.println("rank " + rank + " & after broadcast " + Arrays.toString(assignments[0]));
        
    	// System.out.println("rank: " + this.rank);
        for( int i = 0 ; i < k ; ++i ) {
        	clusters[i].setCoords( assignments[i]  );
        }
    }
    
    /**
     * Executes the non-parallel update step method
     * @param flag
     */
    public void updateStep( boolean flag ) {
    	super.updateStep();
    }
    
    @Override
    public void updateStep() {
    	double[][] totals = new double[k][d];
    	
    	// init all totals to 0
    	for( int i = 0 ; i < k; ++i ) 
            for( int j = 0; j < d; ++j )
                totals[i][j] = 0;
            
        int[] count = new int[k];
        //System.out.println("updatestep - getStart " + getStart() + " getEnd " + getEnd());
        for( int i = getStart(); i < getEnd(); ++i ) {
            DemoPoint p = points[i];
            double[] coords = p.getCoords();
            int c = p.getCluster();
            for( int j = 0; j < d; ++j ) {
                totals[c][j] += coords[j];
            }
            ++count[c];
        }
        
        for( int i = 0 ; i < k; ++i ) {
        	try {
        		world.reduce(0, IntegerBuf.buffer(count[i]), IntegerOp.SUM );
        		for( int j = 0; j < d; ++j )
        			world.reduce( 0, DoubleBuf.buffer(totals[i][j]), DoubleOp.SUM );
        	} catch( IOException e ) {
        		throw new RuntimeException( e );
        	}
        }
        if( rank == 0 ) {
	        for( int i = 0; i < k; ++i ) {
	            for( int j = 0; j < d; ++j ) {
	                //System.out.println(String.format("cluster:%d dimension:%d sum=%f count=%d",
	                //        i,j,totals[i][j], count[i]));
	                totals[i][j] = totals[i][j] / count[i];
	            }
	        }
	        
	        
	        // for each cluster, set his position to the average of his points
	        for( int i = 0; i < k; ++i ) {
	            if( clusters[i] == null ) {
	                clusters[i] = new DemoPoint( totals[i], i );
	                changedFlag = true;
	                continue;
	            }
	            double[] before = clusters[i].getCoords();
	            for( int j = 0; j < d; ++j ) {
	                if( before[j] != totals[i][j] ) {
	                    clusters[i].setCoords(totals[i] );
	                    changedFlag = true;
	                    break;
	                }
	            }
	        }
        }
        try {
        	broadcastClusters();
        	world.broadcast( 0, BooleanBuf.buffer( changedFlag ) );
        } catch( IOException e ) {
    		throw new RuntimeException( e );
    	}
    }
    
    
    public static void main( String[] args ) throws IOException {
    	Comm.init(args);
    	
    	ParallelKMeansDemo me = new ParallelKMeansDemo( Comm.world(), args );
    	me.prepareData("stuff");
    	me.prepareClusters(false);

		int count = 0;      
		me.printStatus(null);
		while(count < 15) {	
			me.assignmentStep();
			me.updateStep();
			if( me.rank == 0 ) {
				System.out.println("Iteration " + count);
				me.printStatus(null);
			}

			++count;
		}
    }
}
