import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class InputData {
 	//static final int depotIndex = 0;
    public final double[][] distanceMatrix;
    public final int[][] speedFunctionMatrix;
    public final ArrayList<Request> requests;
	public final ArrayList<double[][]> speedFunctionList;
	public final ArrivalTimeFunction[][] arcArrivalTimeFunctions;
    public final double[] proposedDepartTime;
    public final double depotTimeWindowUpperBound;
    public final int nbVehicles;
    public final double vehicleCapacity;

    public InputData(int nbNodes, int nbClients, double corr, int index, String tw) throws FileNotFoundException {
		distanceMatrix = new double[nbNodes][nbNodes];
		speedFunctionMatrix = new int[nbNodes][nbNodes];
		String[] line;

		Scanner sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/distances/LL-%d_%d_corr%.2f_%d.d",
				nbNodes, nbClients, corr, index)));

		// The first line is not important
		sc.nextLine();
		while(sc.hasNextLine())
		{
			line = sc.nextLine().trim().split("\\s+");
		    distanceMatrix[Integer.parseInt(line[0])][Integer.parseInt(line[1])] = Double.parseDouble(line[2]);
		}
		sc.close();

		double[][] travelTimes = new double[nbNodes][nbNodes];

		sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/travelTimes/LL-%d_%d_corr%.2f_%d.t",
				nbNodes, nbClients, corr, index)));

		// The first line is not important
		sc.nextLine();
		while(sc.hasNextLine()) {
		    line = sc.nextLine().trim().split("\\s+");
		    travelTimes[Integer.parseInt(line[0])][Integer.parseInt(line[1])] = Double.parseDouble(line[2]);
		}
		sc.close();

		sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/instances/LL-%d_%d_corr%.2f_%d_%s.txt",
				nbNodes, nbClients, corr, index, tw)));

		line = sc.nextLine().trim().split("\\s+");

		nbVehicles = Integer.parseInt(line[1]);
		vehicleCapacity  = Double.parseDouble(line[2]);

		line = sc.nextLine().trim().split("\\s+");
		assert Integer.parseInt(line[0]) == 0;
		depotTimeWindowUpperBound = Double.parseDouble(line[5]);

		requests = new ArrayList<>(nbClients / 2);

		while(sc.hasNextLine()) {
			line = sc.nextLine().trim().split("\\s+");
			if (!sc.hasNextLine()) {
				break;
			}

			//double  dem =Float.parseFloat(temp[3]);
			int node1 = Integer.parseInt(line[0]);
			double load = Double.parseDouble(line[3]);
			double timeWindowLowerBound1 = Double.parseDouble(line[4]);
			double timeWindowUpperBound1 = Double.parseDouble(line[5]);

			line = sc.nextLine().trim().split("\\s+");

			int node2 = Integer.parseInt(line[0]);
			load += Double.parseDouble(line[3]);
			double timeWindowLowerBound2 = Double.parseDouble(line[4]);
			double timeWindowUpperBound2 = Double.parseDouble(line[5]);

			// Select the earliest time window end (or start in case of equality) as the pickup point
			if (timeWindowUpperBound1 < timeWindowUpperBound2 ||
					timeWindowUpperBound1 == timeWindowUpperBound2 && timeWindowLowerBound1 <= timeWindowLowerBound2) {
				requests.add(new Request(node1, node2, load,
						timeWindowLowerBound1, timeWindowUpperBound1, timeWindowLowerBound2, timeWindowUpperBound2));
			} else {
				requests.add(new Request(node2, node1, load,
						timeWindowLowerBound2, timeWindowUpperBound2, timeWindowLowerBound1, timeWindowUpperBound1));
			}
		}
		sc.close();

		sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/arcTypes/LL-%d_%d.txt", nbNodes, index)));
	    
		line = sc.nextLine().trim().split("\\s+");

		int nbIntervals = Integer.parseInt(line[0]);
		int nbTypes = Integer.parseInt(line[1]);
		double[][] speedFactors = new double[nbTypes][nbIntervals];
		proposedDepartTime = new double[nbIntervals + 1];

		line = sc.nextLine().trim().split("\\s+");

		for (int i = 0; i < nbIntervals; ++i) {
			proposedDepartTime[i + 1] = Double.parseDouble(line[i]) * depotTimeWindowUpperBound;
		}

		for (int j = 0; j < nbTypes; ++j) {
			line = sc.nextLine().trim().split("\\s+");

		    for (int i = 0; i < nbIntervals; ++i) {
				speedFactors[j][i] = Double.parseDouble(line[i]);
			}
		}

		double[][] fct;
		speedFunctionList = new ArrayList<>();
		arcArrivalTimeFunctions = new ArrivalTimeFunction[nbNodes][nbNodes];
		while(sc.hasNextLine()) {
			line = sc.nextLine().trim().split("\\s+");
		    
		    int from =Integer.parseInt(line[0]);
		    int to =Integer.parseInt(line[1]);
		    int typ = Integer.parseInt(line[2]);

		    fct = new double[nbIntervals][2];

		    for (int i = 0; i < nbIntervals; ++i) {
			  	fct[i][0] = proposedDepartTime[i + 1];
			  	fct[i][1] = (distanceMatrix[from][to] / travelTimes[from][to]) * speedFactors[typ][i];
		  	}

			arcArrivalTimeFunctions[from][to] =
					new PiecewiseArrivalTimeFunction(proposedDepartTime, travelTimes[from][to], speedFactors[typ]);
//			for (double departureTime = 0.0; departureTime <= proposedDepartTime[proposedDepartTime.length - 1] * 3;
//				 departureTime += 10.0) {
//				double arrivalTime1 = DominantShortestPath.getNeighborArrivalTime(distanceMatrix[from][to], departureTime, fct);
//				double arrivalTime2 = arrivalTimeFunctions[from][to].getArrivalTime(departureTime);
//				assert Math.abs(arrivalTime1 - arrivalTime2) < 0.0001;
//			}

			int functionIndex = speedFunctionList.indexOf(fct);
		    if (functionIndex == -1) {
				speedFunctionList.add(fct);
				functionIndex = speedFunctionList.size() - 1;
			}

		    // We add 1 because 0 is used for nonexistent arcs
			speedFunctionMatrix[from][to] = functionIndex + 1;
		}
		sc.close();
    }
}
