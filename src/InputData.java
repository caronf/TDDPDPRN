import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class InputData {
    //static final int depotIndex = 0;
    public final int nbNodes;
    public final ArrayList<Request> requests;
    public final HashMap<Integer, HashMap<Integer, ArrivalTimeFunction>> arcArrivalTimeFunctions;
    public final HashMap<Integer, HashMap<Integer, Double>> averageTravelTimes;
    public final double averageSpeedDeviation;
    public final double[] proposedDepartTime;
    public final double endOfTheDay;
    public final int nbVehicles;
    public final double vehicleCapacity;
    public final double returnTime;

    public InputData(int nbNodes, int nbClients, double corr, int index, String tw,
                     Random random, double dynamismRatio, double vehiclesPerRequest)
            throws FileNotFoundException {
        this.nbNodes = nbNodes;
        String[] line;
        double[][] travelTimes = new double[nbNodes][nbNodes];

        Scanner sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/travelTimes/LL-%d_%d_corr%.2f_%d.t",
                nbNodes, nbClients, corr, index)));

        // The first line is irrelevant
        sc.nextLine();
        while(sc.hasNextLine()) {
            line = sc.nextLine().trim().split("\\s+");
            travelTimes[Integer.parseInt(line[0])][Integer.parseInt(line[1])] = Double.parseDouble(line[2]);
        }
        sc.close();

        boolean fileExists = true;
        try {
            sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/instances/LL-%d_%d_corr%.2f_%d_%s.txt",
                    nbNodes, nbClients, corr, index, tw)));
        } catch (FileNotFoundException e) {
            if (nbClients != 200) {
                throw e;
            }
            fileExists = false;
        }

        int nbRequests = nbClients / 2;
        nbVehicles = (int) (nbRequests * vehiclesPerRequest);

        double maxArrivalTime = 0.0;
        if (fileExists) {
            line = sc.nextLine().trim().split("\\s+");
            vehicleCapacity = Double.parseDouble(line[1]);

            line = sc.nextLine().trim().split("\\s+");
            assert Integer.parseInt(line[0]) == 0;
            endOfTheDay = Double.parseDouble(line[5]);

            requests = new ArrayList<>(nbRequests);
            ArrayList<Integer> staticRequestIndices = new ArrayList<>(nbRequests);
            for (int i = 0; i < nbRequests; ++i) {
                staticRequestIndices.add(i);
            }
            for (int i = 0; i < Math.round(nbRequests * dynamismRatio); ++i) {
                staticRequestIndices.remove(random.nextInt(staticRequestIndices.size()));
            }

            while (sc.hasNextLine()) {
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

            assert requests.size() == nbRequests;

            sc.close();
        } else {
            vehicleCapacity = 200.0;
            endOfTheDay = 100.0;
            requests = new ArrayList<>(nbRequests);
            for (int i = 0; i < nbRequests; ++i) {
                double load = random.nextDouble() * 40.0 + 20.0;

                int node1 = random.nextInt(nbNodes);
                int node2 = random.nextInt(nbNodes);
                double timeWindowLowerBound1 = random.nextDouble() * 85.0;
                double timeWindowLowerBound2 = random.nextDouble() * 85.0;
                double timeWindowUpperBound1;
                double timeWindowUpperBound2;
                if (tw.equals("NTW")) {
                    timeWindowUpperBound1 = timeWindowLowerBound1 + 3.0 + random.nextDouble();
                    timeWindowUpperBound2 = timeWindowLowerBound2 + 3.0 + random.nextDouble();
                } else {
                    assert tw.equals("WTW");
                    timeWindowUpperBound1 = timeWindowLowerBound1 + 8.0 + random.nextDouble() * 7.0;
                    timeWindowUpperBound2 = timeWindowLowerBound2 + 8.0 + random.nextDouble() * 7.0;
                }

                // Select the earliest time window end (or start in case of equality) as the pickup point
                double arrivalTime;
                if (timeWindowUpperBound1 < timeWindowUpperBound2 ||
                        timeWindowUpperBound1 == timeWindowUpperBound2 &&
                                timeWindowLowerBound1 <= timeWindowLowerBound2) {
                    arrivalTime = i < Math.round(nbRequests * dynamismRatio) ?
                            random.nextDouble() * timeWindowLowerBound1 : 0.0;
                    requests.add(new Request(node1, node2, load,
                            timeWindowLowerBound1, timeWindowUpperBound1, 0.0,
                            timeWindowLowerBound2, timeWindowUpperBound2, 0.0, arrivalTime));
                } else {
                    arrivalTime = i < Math.round(nbRequests * dynamismRatio) ?
                            random.nextDouble() * timeWindowLowerBound2 : 0.0;
                    requests.add(new Request(node2, node1, load,
                            timeWindowLowerBound2, timeWindowUpperBound2, 0.0,
                            timeWindowLowerBound1, timeWindowUpperBound1, 0.0, arrivalTime));
                }

                maxArrivalTime = Math.max(maxArrivalTime, arrivalTime);
            }
        }

        returnTime = (maxArrivalTime + endOfTheDay) / 2.0;

        fileExists = true;
        try {
            sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/arcTypes/LL-%d_%d.txt", nbNodes, index)));
        } catch (FileNotFoundException e) {
            fileExists = false;
        }

        double speedDeviationSum = 0.0;
        int nbArcs = 0;
        arcArrivalTimeFunctions = new HashMap<>();
        averageTravelTimes = new HashMap<>();
        if (fileExists) {
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

            while (sc.hasNextLine()) {
                line = sc.nextLine().trim().split("\\s+");

                int from = Integer.parseInt(line[0]);
                int to = Integer.parseInt(line[1]);
                int typ = Integer.parseInt(line[2]);

                double averageSpeedFactor = 0.0;
                for (int i = 0; i < nbIntervals; ++i) {
                    averageSpeedFactor += speedFactors[typ][i] * (proposedDepartTime[i + 1] - proposedDepartTime[i]);
                }
                averageSpeedFactor /= (proposedDepartTime[nbIntervals] - proposedDepartTime[0]);

                for (int i = 0; i < nbIntervals; ++i) {
                    speedDeviationSum += Math.abs(speedFactors[typ][i] - averageSpeedFactor) / averageSpeedFactor *
                            (proposedDepartTime[i + 1] - proposedDepartTime[i]);
                }

                if (!arcArrivalTimeFunctions.containsKey(from)) {
                    arcArrivalTimeFunctions.put(from, new HashMap<>());
                    averageTravelTimes.put(from, new HashMap<>());
                }

                assert !arcArrivalTimeFunctions.get(from).containsKey(to);
                arcArrivalTimeFunctions.get(from).put(to,
                        new PiecewiseArrivalTimeFunction(proposedDepartTime, travelTimes[from][to], speedFactors[typ]));
                averageTravelTimes.get(from).put(to, averageSpeedFactor * travelTimes[from][to]);

                ++nbArcs;
            }
            sc.close();
            averageSpeedDeviation =
                    speedDeviationSum / (proposedDepartTime[nbIntervals] - proposedDepartTime[0]) / nbArcs;
        } else {
            double[][] speedFactors = new double[][] {
                    {1.5, 1.0, 1.67, 1.17, 1.33},
                    {1.17, 0.67, 1.33, 0.83, 1},
                    {1, 0.33, 0.67, 0.5, 0.83}
            };
            proposedDepartTime = new double[] {0.0, 20.0, 30.0, 70.0, 80.0, 100.0};

            for (int from = 0; from < nbNodes; ++from) {
                for (int to = 0; to < nbNodes; ++to) {
                    if (travelTimes[from][to] > 0.0) {
                        int typ = random.nextInt(speedFactors.length);
                        double averageSpeedFactor = 0.0;
                        for (int i = 0; i < proposedDepartTime.length - 1; ++i) {
                            averageSpeedFactor += speedFactors[typ][i] * (proposedDepartTime[i + 1] - proposedDepartTime[i]);
                        }
                        averageSpeedFactor /= 100.0;

                        for (int i = 0; i < 5; ++i) {
                            speedDeviationSum += Math.abs(speedFactors[typ][i] - averageSpeedFactor) /
                                    averageSpeedFactor * (proposedDepartTime[i + 1] - proposedDepartTime[i]);
                        }

                        if (!arcArrivalTimeFunctions.containsKey(from)) {
                            arcArrivalTimeFunctions.put(from, new HashMap<>());
                            averageTravelTimes.put(from, new HashMap<>());
                        }

                        assert !arcArrivalTimeFunctions.get(from).containsKey(to);
                        arcArrivalTimeFunctions.get(from).put(to,
                                new PiecewiseArrivalTimeFunction(proposedDepartTime, travelTimes[from][to], speedFactors[typ]));
                        averageTravelTimes.get(from).put(to, averageSpeedFactor * travelTimes[from][to]);

                        ++nbArcs;
                    }
                }
            }
            averageSpeedDeviation = speedDeviationSum / 100.0 / nbArcs;
        }
    }

    public InputData(boolean bigFile) throws FileNotFoundException {
        requests = null;
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
        double speedDeviationSum = 0.0;
        nbNodes = 51317;
        //HashMap<Integer, Integer> nodes = new HashMap<>(nbNodes);
        HashSet<Integer> toNodes = new HashSet<>();
        arcArrivalTimeFunctions = new HashMap<>();
        averageTravelTimes = new HashMap<>();
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
            averageSpeed /= line.length - trailingCells - yearIndex - 8;

            for (int k = yearIndex + 8; k < line.length - trailingCells; ++k) {
                speedDeviationSum += Math.abs(Double.parseDouble(line[k]) - averageSpeed) / averageSpeed /
                        (line.length - trailingCells - yearIndex - 8);
            }

            nbLines++;
            //}
        }

        sc.close();
        averageSpeedDeviation = speedDeviationSum / nbLines;

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
