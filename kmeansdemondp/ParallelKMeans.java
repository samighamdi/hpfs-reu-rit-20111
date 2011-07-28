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
import edu.rit.mp.DoubleBuf;
import edu.rit.mp.IntegerBuf;
import edu.rit.mp.ByteBuf;
import edu.rit.pj.CommStatus;
import edu.rit.util.Range;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import edu.rit.util.Random;
import java.io.FileOutputStream;
import java.io.File;

public class ParallelKMeans extends KMeans {
	public static final String DEFAULT_IN = "KMeansArray";
    Comm world;
    int rank, worldsize;
    Range[] ranges;
    Range ir;
	
    protected String infile;
	
    public int frontRank = 0;

    public ParallelKMeans(Comm world, int frontRank) {
        super();
        this.world = world;
        this.rank = world.rank();
        this.worldsize = world.size();
        this.frontRank = frontRank;
        Range[] ranges = new Range(0, size - 1).subranges(size);
        Range ir = ranges[rank];
        setStart(ir.lb());
        setEnd(ir.ub());
    }

    public ParallelKMeans(Comm world, int frontRank, String[] args) {
        super(args);
        this.world = world;
        this.rank = world.rank();
        this.worldsize = world.size();
        this.frontRank = frontRank;
        ranges = new Range(0, size - 1).subranges(size);
        ir = ranges[rank];
        setStart(ir.lb());
        setEnd(ir.ub());
    }

    public ParallelKMeans(Comm world, int frontRank, int keyser, int k, int d) {
        super(keyser, k, d, System.currentTimeMillis());
        this.world = world;
        this.rank = world.rank();
        this.worldsize = world.size();
        this.frontRank = frontRank;
        ranges = new Range(0, this.size - 1).subranges(worldsize);
        ir = ranges[rank];
        setStart(ir.lb());
        setEnd(ir.ub());
    }
    
    public void prepareData( String infile ) {
		DemoPoint.setDimension(this.d);
		try {
			ArrayDoubleFS myfs = new ArrayDoubleFS( new File(infile + ".adfs") );
			//System.out.println( "myfs.size() = " + myfs.size());
			ranges = new Range(0, (int)myfs.size() - 1).subranges(this.worldsize);
//			System.out.println( "HEY ASSHOLE\n\n\n\n\n array index (rank) = " + rank + 
//					"\n in an array of length " + ranges.length + 
//					"\n based on worldsize  " + this.worldsize );
	        ir = ranges[rank];
	        setStart(ir.lb());
	        setEnd(ir.ub());
			this.d = myfs.innerSize() - 1;
			DemoPoint.setDimension(d);
			
			myfs.seek(start);
			this.points = DemoPointArray.toDemoPoints(myfs.getNext( this.size ));
			this.clusters = new DemoPoint[k];
			/*
			for( int i = 0 ; i < this.size; ++i ) {
				for( int  j = 0 ; j < d ; ++j ) {
					if( points[i][j] > maxPoints[j] ) {
						maxPoints[j] = points[i][j];
					}
				}
			}*/
			
			//assignMaxima();	
		} catch( IOException e) {
			throw new RuntimeException(e); // gotta make it out somehow
		} catch( java.text.ParseException e ) {
			throw new RuntimeException(e);
		}
	}

    protected void assignMaxima() throws IOException {
    	for( int i = 0 ; i < d ; ++i ) {
    		DoubleBuf buf = DoubleBuf.buffer( maxPoints[i] );
    		world.allReduce(buf, DoubleOp.MAXIMUM);
    	}
	}

	@Override
    public void prepareClusters() {
        prepareClusters(true);
    }

    public void prepareClusters(boolean parallel) {
        for (int i = 0; i < points.length; i++) {
            DemoPoint dp = points[i];
            int c = (int) (k * rng.nextDouble());
            //System.out.println(dp + " assigned to clu " + c);
            if (dp != null) {
                dp.setCluster(c);
            }
        }
        if (parallel) {
            updateStep();
        } else {
            //System.out.println("datasize " + size);
            //System.out.println("new Range(0, " + (size - 1) + ").subranges(" + size + ");");
            Range[] ranges = new Range(0, size - 1).subranges(worldsize);
            Range ir = ranges[rank];
            this.setStart(ir.lb());
            this.setEnd(ir.ub());
            //System.out.println("my ranges : " + ir.lb() + " to " + ir.ub());

            updateStep(false);
        }
    }
    // */

    protected void broadcastClusters() throws IOException {
        double[][] assignments = new double[k][d];

        if (rank == 0) {
            assignments = this.clusterCenters();
        }
        DoubleBuf dbuf = DoubleBuf.buffer(assignments);
        world.broadcast(0, dbuf);
        // System.out.println("rank " + rank + " & after broadcast " + Arrays.toString(assignments[0]));

        // System.out.println("rank: " + this.rank);
        for (int i = 0; i < k; ++i) {
            clusters[i].setCoords(assignments[i]);
        }
    }

    /**
     * Executes the non-parallel update step method
     * @param flag
     */
    public void updateStep(boolean flag) {
        super.updateStep();
    }

    @Override
    public void updateStep() {
        double[][] totals = new double[k][d];
        int[] count = new int[k];
        
        // init all totals to 0
        for (int i = 0; i < k; ++i) {
            for (int j = 0; j < d; ++j) {
                totals[i][j] = 0;
            }
            count[i] = 0;
        }

        //System.out.println("updatestep - getStart " + getStart() + " getEnd " + getEnd());
        for (int i = getStart(); i < getEnd(); ++i) {
            DemoPoint p = points[i];
            double[] coords = p.getCoords();
            int c = p.getCluster();
            for (int j = 0; j < d; ++j) {
                totals[c][j] += coords[j];
            }
            ++count[c];
        }
        //System.out.println ("Parallel K Means updateStep before PJ, rank = " + rank);
        //System.out.println( "rank " + rank +" k,d = " + k +","+d);
        for (int i = 0; i < k; ++i) {
        	
            try {
            	//if( rank == 2 ) System.out.println( "rank 2 start of iteration " +i );
                world.reduce(0, IntegerBuf.buffer(count[i]), IntegerOp.SUM);
                //if( rank == 2 ) System.out.println( "done with 1st reduce");
                for (int j = 0; j < d; ++j) {
                	if( rank == 2 ) {
                		//System.out.println( "rank 2 reducing "+i + " " + j);
                	}
                    world.reduce(0, DoubleBuf.buffer(totals[i][j]), DoubleOp.SUM);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //System.out.println( "update step done with pj rank = " + rank);
        if (rank == 0) {
            for (int i = 0; i < k; ++i) {
                for (int j = 0; j < d; ++j) {
                    //System.out.println(String.format("cluster:%d dimension:%d sum=%f count=%d",
                    //        i,j,totals[i][j], count[i]));
                    totals[i][j] = totals[i][j] / count[i];
                }
            }


            // for each cluster, set his position to the average of his points
            for (int i = 0; i < k; ++i) {
                if (clusters[i] == null) {
                    clusters[i] = new DemoPoint(totals[i], i);
                    changedFlag = true;
                    continue;
                }
                double[] before = clusters[i].getCoords();
                for (int j = 0; j < d; ++j) {
                    if (before[j] != totals[i][j]) {
                        clusters[i].setCoords(totals[i]);
                        changedFlag = true;
                        break;
                    }
                }
            }
        }
        //System.out.println("about to broadcast clusters rank = " + rank);
        try {
            broadcastClusters();
            //world.broadcast(0, BooleanBuf.buffer(changedFlag));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startKMeans(String datafile, int maxiter) throws IOException {    	
        prepareData(datafile);
        //System.out.println("done preparing data");
        prepareClusters(false);
        //System.out.println("done with clusters");
        int count = 0;
        //printStatus(null);
        while (count < maxiter) {
            //*			
        	//System.out.println("welcum 2 while loop count = " + count);
            assignmentStep();
            //System.out.println("done with assignment step rank = " + rank);
            updateStep();
            //System.out.println("done with update step");

            ImgGen generator = new ImgGen(10, 10, (rank == frontRank) ? true : false, k);
            //System.out.println( "Hi it's rank != fromrank" );
            generator.generatePoints(pointsData());
            if (rank != frontRank) {
                BufferedImage image = generator.getPointsImg();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                //System.out.println("preparing to imageio.write");
                ImageIO.write(image, "png", bytes);
                byte[] buffer = bytes.toByteArray();
                int[] imageSize = {buffer.length};
                //System.out.println( "about to send integerbuf (imagesize)");
                world.send(frontRank, count, IntegerBuf.buffer(imageSize));
                //System.out.println( "about to send bytebuf (image)");
                world.send(frontRank, count, ByteBuf.buffer(buffer));
                //System.out.println("done sending bytebuf");
            } else {
                byte[] tracker = new byte[worldsize];
                int[] sizes = new int[worldsize];
                for (int i = 0; i < worldsize; i++) {
                    if (rank != i) {
                        int[] imageSize = new int[1];
                        IntegerBuf imgSizeBuf = IntegerBuf.buffer(imageSize);
                        //System.out.println("about to receive image size buf from rank " + i);
                        CommStatus status = world.receive(i, count, imgSizeBuf);
                        if (tracker[status.fromRank] == 0) {
                        	//System.out.println("passed if fromrank == 0");
                            byte[] buffer = new byte[imageSize[0]];
                            ByteBuf buf = ByteBuf.buffer(buffer);
                            //System.out.println("about to receive bytebuf from rank 0");
                            world.receive(i, count, buf);
                            BufferedImage imagePart = ImageIO.read(new ByteArrayInputStream(buffer));
                            //System.out.println("done with imageio.read");
                            if (imagePart != null) {
                                generator.addPointsImage(imagePart);
                            }
                            tracker[status.fromRank]++;
                        }
                    }
                }
                BufferedImage image = generator.getFullImg();
                addArchivedImage(image);
            }

            //System.out.println("Iteration " + count);
            //printStatus(null);

            ++count;
        }
    }
}
