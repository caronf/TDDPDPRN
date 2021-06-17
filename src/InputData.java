import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class InputData {
 	//static final int depotIndex = 0;
	public final int nbNodes;
    public final double[][] distanceMatrix;
    public final int[][] speedFunctionMatrix;
    public final ArrayList<Request> requests;
	public final ArrayList<double[][]> speedFunctionList;
	//public final ArrivalTimeFunction[][] arcArrivalTimeFunctions;
	public final HashMap<Integer, HashMap<Integer, ArrivalTimeFunction>> arcArrivalTimeFunctions;
    public final double[] proposedDepartTime;
    public final double endOfTheDay;
    public final int nbVehicles;
    public final double vehicleCapacity;
    public final double returnTime;

    public InputData(int nbNodes, int nbClients, double corr, int index, String tw, Random random, double dynamismRatio)
			throws FileNotFoundException {
    	this.nbNodes = nbNodes;
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

		// The first line is irrelevant
		sc.nextLine();
		while(sc.hasNextLine()) {
		    line = sc.nextLine().trim().split("\\s+");
		    travelTimes[Integer.parseInt(line[0])][Integer.parseInt(line[1])] = Double.parseDouble(line[2]);
		}
		sc.close();

		sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/instances/LL-%d_%d_corr%.2f_%d_%s.txt",
				nbNodes, nbClients, corr, index, tw)));

		line = sc.nextLine().trim().split("\\s+");

		nbVehicles = Integer.parseInt(line[0]);
		vehicleCapacity = Double.parseDouble(line[1]);

		line = sc.nextLine().trim().split("\\s+");
		assert Integer.parseInt(line[0]) == 0;
		endOfTheDay = Double.parseDouble(line[5]);

		int nbRequests = nbClients / 2;
		requests = new ArrayList<>(nbRequests);
		ArrayList<Integer> staticRequestIndices = new ArrayList<>(nbRequests);
		for (int i = 0; i < nbRequests; ++i) {
			staticRequestIndices.add(i);
		}
		for (int i = 0; i < Math.round(nbRequests * dynamismRatio); ++i) {
			staticRequestIndices.remove(random.nextInt(staticRequestIndices.size()));
		}

		double maxArrivalTime = 0.0;
		while(sc.hasNextLine()) {
			line = sc.nextLine().trim().split("\\s+");
			if (!sc.hasNextLine()) {
				break;
			}

			int node1 = Integer.parseInt(line[0]);
			double load = Double.parseDouble(line[3]);
			double timeWindowLowerBound1 = Double.parseDouble(line[4]);
			double timeWindowUpperBound1 = Double.parseDouble(line[5]);

			line = sc.nextLine().trim().split("\\s+");

			int node2 = Integer.parseInt(line[0]);
			load += Double.parseDouble(line[3]);
			double timeWindowLowerBound2 = Double.parseDouble(line[4]);
			double timeWindowUpperBound2 = Double.parseDouble(line[5]);

			assert timeWindowLowerBound1 < endOfTheDay &&
					timeWindowLowerBound2 < endOfTheDay &&
					timeWindowUpperBound1 < endOfTheDay &&
					timeWindowUpperBound2 < endOfTheDay;

			// Select the earliest time window end (or start in case of equality) as the pickup point
			double arrivalTime;
			if (timeWindowUpperBound1 < timeWindowUpperBound2 ||
					timeWindowUpperBound1 == timeWindowUpperBound2 && timeWindowLowerBound1 <= timeWindowLowerBound2) {
				arrivalTime = random.nextDouble() * timeWindowLowerBound1;
				requests.add(new Request(node1, node2, load,
						timeWindowLowerBound1, timeWindowUpperBound1, 0.0,
						timeWindowLowerBound2, timeWindowUpperBound2, 0.0,
						staticRequestIndices.contains(requests.size()) ? 0.0 : arrivalTime));
			} else {
				arrivalTime = random.nextDouble() * timeWindowLowerBound2;
				requests.add(new Request(node2, node1, load,
						timeWindowLowerBound2, timeWindowUpperBound2, 0.0,
						timeWindowLowerBound1, timeWindowUpperBound1, 0.0,
						staticRequestIndices.contains(requests.size()) ? 0.0 : arrivalTime));
			}

			maxArrivalTime = Math.max(maxArrivalTime, arrivalTime);
		}

		returnTime = (maxArrivalTime + endOfTheDay) / 2.0;
		sc.close();

		sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/arcTypes/LL-%d_%d.txt", nbNodes, index)));
	    
		line = sc.nextLine().trim().split("\\s+");

		int nbIntervals = Integer.parseInt(line[0]);
		int nbTypes = Integer.parseInt(line[1]);
		double[][] speedFactors = new double[nbTypes][nbIntervals];
		proposedDepartTime = new double[nbIntervals + 1];

		line = sc.nextLine().trim().split("\\s+");

		for (int i = 0; i < nbIntervals; ++i) {
			proposedDepartTime[i + 1] = Double.parseDouble(line[i]) * endOfTheDay;
		}

		for (int j = 0; j < nbTypes; ++j) {
			line = sc.nextLine().trim().split("\\s+");

		    for (int i = 0; i < nbIntervals; ++i) {
				speedFactors[j][i] = Double.parseDouble(line[i]);
			}
		}

		double[][] fct;
		speedFunctionList = new ArrayList<>();
		arcArrivalTimeFunctions = new HashMap<>();
		while(sc.hasNextLine()) {
			line = sc.nextLine().trim().split("\\s+");
		    
		    int from = Integer.parseInt(line[0]);
		    int to = Integer.parseInt(line[1]);
		    int typ = Integer.parseInt(line[2]);

		    fct = new double[nbIntervals][2];

		    for (int i = 0; i < nbIntervals; ++i) {
			  	fct[i][0] = proposedDepartTime[i + 1];
			  	fct[i][1] = (distanceMatrix[from][to] / travelTimes[from][to]) * speedFactors[typ][i];
		  	}

		    if (!arcArrivalTimeFunctions.containsKey(from)) {
		    	arcArrivalTimeFunctions.put(from, new HashMap<>());
			}

		    assert !arcArrivalTimeFunctions.get(from).containsKey(to);
			arcArrivalTimeFunctions.get(from).put(to,
					new PiecewiseArrivalTimeFunction(proposedDepartTime, travelTimes[from][to], speedFactors[typ]));
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

	public InputData(boolean bigFile) throws FileNotFoundException {
		distanceMatrix = null;
		speedFunctionMatrix = null;
		requests = null;
		speedFunctionList = null;
		proposedDepartTime = null;
		endOfTheDay = 0.0;
		nbVehicles = 0;
		vehicleCapacity = 0.0;
		returnTime = 0.0;

		String filename;
		String separator;
		int yearIndex;
		int trailingCells;
		if (bigFile) {
			filename = "Maha_Gmira_data/filtered_more5.csv";
			separator = ";";
			yearIndex = 0;
			trailingCells = 0;
		} else {
			filename = "Maha_Gmira_data/df_vector_cl.csv";
			separator = ",";
			yearIndex = 1;
			trailingCells = 2;
		}

		double maxDeviation = 0.0;
		int maxDeviationSegment = -1;
		nbNodes = 51317;
		//HashMap<Integer, Integer> nodes = new HashMap<>(nbNodes);
		HashSet<Integer> toNodes = new HashSet<>();
		arcArrivalTimeFunctions = new HashMap<>();
		Scanner sc = new Scanner(new File(filename));
		int nbLines = 0;
		sc.nextLine();
		while(sc.hasNextLine()) {
			String[] line = sc.nextLine().split(separator);
			//if (Integer.parseInt(line[yearIndex]) == 2014 &&
					//Integer.parseInt(line[yearIndex + 1]) == 1 && Integer.parseInt(line[yearIndex + 2]) == 1) {
				int i = Integer.parseInt(line[yearIndex + 6]);
				int j = Integer.parseInt(line[yearIndex + 7]);

				if (!arcArrivalTimeFunctions.containsKey(i)) {
					arcArrivalTimeFunctions.put(i, new HashMap<>());
				}

				if (!arcArrivalTimeFunctions.get(i).containsKey(j)) {
					arcArrivalTimeFunctions.get(i).put(j, new PiecewiseArrivalTimeFunction(
							new double[]{0.0, 100.0}, Double.parseDouble(line[yearIndex + 8]), new double[]{1.0}));
					//toNodes.add(j);
				}

				if (!arcArrivalTimeFunctions.containsKey(j)) {
					arcArrivalTimeFunctions.put(j, new HashMap<>());
				}

				if (!arcArrivalTimeFunctions.get(j).containsKey(i)) {
					arcArrivalTimeFunctions.get(j).put(i, new PiecewiseArrivalTimeFunction(
							new double[]{0.0, 100.0}, Double.parseDouble(line[yearIndex + 8]), new double[]{1.0}));
				}

				double averageSpeed = 0.0;
				for (int k = yearIndex + 8; k < line.length - trailingCells; ++k) {
					averageSpeed += Double.parseDouble(line[k]);
				}
				averageSpeed /= line.length - trailingCells - (yearIndex + 8);

				double deviation = 0.0;
				for (int k = yearIndex + 8; k < line.length - trailingCells; ++k) {
					deviation += Math.abs(averageSpeed - Double.parseDouble(line[k]));
				}

				if (deviation > maxDeviation) {
					maxDeviation = deviation;
					maxDeviationSegment = Integer.parseInt(line[yearIndex + 5]);
				}

				nbLines++;
			//}
		}

//		HashSet<Integer> nodesOnlyFrom = new HashSet<>(arcArrivalTimeFunctions2.keySet());
//		nodesOnlyFrom.removeAll(toNodes);
//
//		HashSet<Integer> nodesOnlyTo = new HashSet<>(toNodes);
//		nodesOnlyTo.removeAll(arcArrivalTimeFunctions2.keySet());

//		assert nodes.size() == nbNodes;
//
//		for (int i = 0; i < nbNodes; i++) {
//			assert arcArrivalTimeFunctions2.containsKey(i);
//			assert arcArrivalTimeFunctions2.get(i).size() > 0;
//		}

//		HashSet<Integer> visitedNodes = new HashSet<>();
//		Queue<Integer> nodesToVisit = new ArrayDeque<>();
//		for (int i : arcArrivalTimeFunctions2.keySet()) {
//			nodesToVisit.add(i);
//			break;
//		}
//		nodesToVisit.add(164839);

//		while (!nodesToVisit.isEmpty()) {
//			int i = nodesToVisit.remove();
//			if (!visitedNodes.contains(i)) {
//				visitedNodes.add(i);
//				if (arcArrivalTimeFunctions2.containsKey(i)) {
//					nodesToVisit.addAll(arcArrivalTimeFunctions2.get(i).keySet());
//				}
//			}
//		}
//
//		HashSet<Integer> unvisitedNodes = new HashSet<>(arcArrivalTimeFunctions2.keySet());
//		unvisitedNodes.removeAll(visitedNodes);

		return;
	}
}
