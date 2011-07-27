package kmeansmr;


import org.apache.hadoop.mapreduce.Job;



public class KMeansEntryPoint {

	public static void main(String args[]) throws Exception
	{
		int k = 3;
		Job job = KMeansDriver.startJob("", k);
		
		while(!job.isComplete());
		
		System.out.println("After Iteration:");
		
		for(int i = 0; i < k; i++)
		{
			System.out.print("Cluster " + i + ": ");
			
			for(String str : job.getConfiguration().getStrings("Cluster" + i))
			{
				System.out.print(str + ", ");
			}
			
			System.out.println("\n");
		}
	}
	
}
