import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class InputData {
    public final int nbNodes;
    public final ArrayList<Request> requests;
    public final HashMap<Integer, HashMap<Integer, ArrivalTimeFunction>> arcArrivalTimeFunctions;
    public final HashMap<Integer, HashMap<Integer, Double>> averageTravelTimes;
    public final double averageSpeedDeviation;
    public final double[] intervalTimes;
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
        endOfTheDay = 100.0;
        double instanceEndOfTheDay;
        if (fileExists) {
            line = sc.nextLine().trim().split("\\s+");
            vehicleCapacity = Double.parseDouble(line[1]);

            line = sc.nextLine().trim().split("\\s+");
            assert Integer.parseInt(line[0]) == 0;
            instanceEndOfTheDay = Double.parseDouble(line[5]);

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
                double timeWindowLowerBound1 = Double.parseDouble(line[4]) / instanceEndOfTheDay * endOfTheDay;
                double timeWindowUpperBound1 = Double.parseDouble(line[5]) / instanceEndOfTheDay * endOfTheDay;

                line = sc.nextLine().trim().split("\\s+");

                int node2 = Integer.parseInt(line[0]);
                load += Double.parseDouble(line[3]);
                double timeWindowLowerBound2 = Double.parseDouble(line[4]) / instanceEndOfTheDay * endOfTheDay;
                double timeWindowUpperBound2 = Double.parseDouble(line[5]) / instanceEndOfTheDay * endOfTheDay;

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
            instanceEndOfTheDay = 250.0;
            double maxTimeWindowLowerBound = 0.85 * endOfTheDay;
            double minTimeWindowLength = tw.equals("NTW") ? 0.03 * endOfTheDay : 0.08 * endOfTheDay;
            double maxAdditionalTimeWindowLength = tw.equals("NTW") ? 0.01 * endOfTheDay : 0.07 * endOfTheDay;
            requests = new ArrayList<>(nbRequests);
            for (int i = 0; i < nbRequests; ++i) {
                double load = random.nextDouble() * 40.0 + 20.0;

                int node1 = random.nextInt(nbNodes);
                int node2 = random.nextInt(nbNodes);
                double timeWindowLowerBound1 = random.nextDouble() * maxTimeWindowLowerBound;
                double timeWindowLowerBound2 = random.nextDouble() * maxTimeWindowLowerBound;
                double timeWindowUpperBound1 = timeWindowLowerBound1 + minTimeWindowLength +
                        random.nextDouble() * maxAdditionalTimeWindowLength;
                double timeWindowUpperBound2 = timeWindowLowerBound2 + minTimeWindowLength +
                        random.nextDouble() * maxAdditionalTimeWindowLength;

                // Select the earliest time window end (or start in case of equality) as the pickup point
                double arrivalTime;
                if (timeWindowUpperBound1 < timeWindowUpperBound2 ||
                        timeWindowUpperBound1 == timeWindowUpperBound2 &&
                                timeWindowLowerBound1 <= timeWindowLowerBound2) {
                    arrivalTime = random.nextDouble() * timeWindowLowerBound1;
                    requests.add(new Request(node1, node2, load,
                            timeWindowLowerBound1, timeWindowUpperBound1, 0.0,
                            timeWindowLowerBound2, timeWindowUpperBound2, 0.0,
                            i >= Math.round(nbRequests * dynamismRatio) ? 0.0 : arrivalTime));
                } else {
                    arrivalTime = random.nextDouble() * timeWindowLowerBound2;
                    requests.add(new Request(node2, node1, load,
                            timeWindowLowerBound2, timeWindowUpperBound2, 0.0,
                            timeWindowLowerBound1, timeWindowUpperBound1, 0.0,
                            i >= Math.round(nbRequests * dynamismRatio) ? 0.0 : arrivalTime));
                }

                if (i < Math.round(nbRequests * dynamismRatio)) {
                    maxArrivalTime = Math.max(maxArrivalTime, arrivalTime);
                }
            }
        }

        returnTime = (maxArrivalTime + endOfTheDay) / 2.0;
        for (int i = 0; i < travelTimes.length; ++i) {
            for (int j = 0; j < travelTimes[i].length; ++j) {
                travelTimes[i][j] = travelTimes[i][j] / instanceEndOfTheDay * endOfTheDay;
            }
        }

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
            intervalTimes = new double[nbIntervals + 1];

            line = sc.nextLine().trim().split("\\s+");

            for (int i = 0; i < nbIntervals; ++i) {
                intervalTimes[i + 1] = Double.parseDouble(line[i]) * endOfTheDay;
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
                    averageSpeedFactor += speedFactors[typ][i] * (intervalTimes[i + 1] - intervalTimes[i]);
                }
                averageSpeedFactor /= (intervalTimes[nbIntervals] - intervalTimes[0]);

                for (int i = 0; i < nbIntervals; ++i) {
                    speedDeviationSum += Math.abs(speedFactors[typ][i] - averageSpeedFactor) / averageSpeedFactor *
                            (intervalTimes[i + 1] - intervalTimes[i]);
                }

                if (!arcArrivalTimeFunctions.containsKey(from)) {
                    arcArrivalTimeFunctions.put(from, new HashMap<>());
                    averageTravelTimes.put(from, new HashMap<>());
                }

                assert !arcArrivalTimeFunctions.get(from).containsKey(to);
                arcArrivalTimeFunctions.get(from).put(to,
                        new PiecewiseArrivalTimeFunction(intervalTimes, travelTimes[from][to], speedFactors[typ]));
                averageTravelTimes.get(from).put(to, averageSpeedFactor * travelTimes[from][to]);

                ++nbArcs;
            }
            sc.close();
            averageSpeedDeviation =
                    speedDeviationSum / (intervalTimes[nbIntervals] - intervalTimes[0]) / nbArcs;
        } else {
            double[][] speedFactors = new double[][] {
                    {1.5, 1.0, 1.67, 1.17, 1.33},
                    {1.17, 0.67, 1.33, 0.83, 1},
                    {1, 0.33, 0.67, 0.5, 0.83}
            };
            intervalTimes = new double[] {0.0, 0.2 * endOfTheDay, 0.3 * endOfTheDay,
                    0.7 * endOfTheDay, 0.8 * endOfTheDay, endOfTheDay};

            for (int from = 0; from < nbNodes; ++from) {
                for (int to = 0; to < nbNodes; ++to) {
                    if (travelTimes[from][to] > 0.0) {
                        int typ = random.nextInt(speedFactors.length);
                        double averageSpeedFactor = 0.0;
                        for (int i = 0; i < intervalTimes.length - 1; ++i) {
                            averageSpeedFactor += speedFactors[typ][i] * (intervalTimes[i + 1] - intervalTimes[i]);
                        }
                        averageSpeedFactor /= 100.0;

                        for (int i = 0; i < intervalTimes.length - 1; ++i) {
                            speedDeviationSum += Math.abs(speedFactors[typ][i] - averageSpeedFactor) /
                                    averageSpeedFactor * (intervalTimes[i + 1] - intervalTimes[i]);
                        }

                        if (!arcArrivalTimeFunctions.containsKey(from)) {
                            arcArrivalTimeFunctions.put(from, new HashMap<>());
                            averageTravelTimes.put(from, new HashMap<>());
                        }

                        assert !arcArrivalTimeFunctions.get(from).containsKey(to);
                        arcArrivalTimeFunctions.get(from).put(to,
                                new PiecewiseArrivalTimeFunction(intervalTimes, travelTimes[from][to], speedFactors[typ]));
                        averageTravelTimes.get(from).put(to, averageSpeedFactor * travelTimes[from][to]);

                        ++nbArcs;
                    }
                }
            }
            averageSpeedDeviation = speedDeviationSum / 100.0 / nbArcs;
        }
    }
}
