import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TDDPDPRN {
    public static void main(String[] args) {
//        InputData data;
//        try {
//            data = new InputData(false);
//        } catch (FileNotFoundException e) {
//            return;
//        }
//        System.out.println(data.averageSpeedDeviation);
//
//        DominantShortestPath.getDominantShortestPaths(data);

        final int[] arrayNbNodes = new int[] {50, 100, 200, 500};
        final int[] arrayNbClients = new int[] {16, 25, 33, 50, 66, 75, 100, 133, 200};
        final double[] arrayCorr = new double[] {0.02, 0.5, 0.98};
        final int[] indices = new int[] {1, 2, 3, 4, 5};
        final String[] timeWindowTypes = new String[] {"NTW", "WTW"};

        double inputDataReadTime = 0.0;
        double[] preprocessingTimes = new double[arrayNbNodes.length];
        double initialSolutionTimeAverage = 0.0;
        double[] tabuSearchTimes = new double[arrayNbClients.length];
        double[] insertionTimes = new double[arrayNbClients.length];
        double[] removalTimes = new double[arrayNbClients.length];
        double[] nbInsertions = new double[arrayNbClients.length];
        double[] nbRemovals = new double[arrayNbClients.length];
        int[] averageSearchIterations = new int[arrayNbClients.length];
        double initialSolutionCost = 0.0;
        double afterTabuCost = 0.0;
        double totalImprovement = 0.0;
        double maxTotalImprovement = 0.0;
        int nbInstances = 0;
        int[] nbInstancesPerNodeCount = new int[arrayNbNodes.length];

        long startTime = System.nanoTime();

        double msMultiplier = 500;
        TabuSearch tabuSearch = new TabuSearch(10.0, 3.0 / 8.0,
                1.0 / 2.0, Integer.MAX_VALUE, false);
        DynamicProblemSolver dynamicProblemSolver = new DynamicProblemSolver(
                5.0, 10.0, msMultiplier, false, true);

        for (int nbClientsIndex = 0; nbClientsIndex < arrayNbClients.length; ++nbClientsIndex) {
            int nbInstancesForNbClients = 0;
            for (int nbNodeIndex = 0; nbNodeIndex < arrayNbNodes.length; ++nbNodeIndex) {
                for (double corr : arrayCorr) {
                    for (int index : indices) {
                        for (String timeWindowType : timeWindowTypes) {
                            Random instanceRandom = new Random(arrayNbClients[nbClientsIndex] * 100000000 +
                                    arrayNbNodes[nbNodeIndex] * 100000 + (int) (corr * 1000) +
                                    index * 100 + timeWindowType.charAt(0));

                            long time = System.nanoTime();
                            InputData inputData;
                            try {
                                inputData = new InputData(arrayNbNodes[nbNodeIndex], arrayNbClients[nbClientsIndex],
                                        corr, index, timeWindowType, instanceRandom, 0.5, 0.25);
                            } catch (FileNotFoundException e) {
                                // Not all parameter combinations exist
                                continue;
                            }
                            inputDataReadTime += (System.nanoTime() - time) / 1000000000.0;

                            time = System.nanoTime();
                            ArrivalTimeFunction[][] arrivalTimeFunctions =
                                    DominantShortestPath.getDominantShortestPaths(inputData,
                                            false, inputData.intervalTimes);
                            double dspTime = (System.nanoTime() - time) / 1000000000.0;

//                            int totalNbArcs = 0;
//                            int nbPaths = 0;
//                            int maxNbArcs = 0;
//                            int size = 0;
//                            double maxTravelTime = 0.0;
//                            double maxTravelTimeFromDepot = 0.0;
//                            for (int i = 0; i < inputData.nbNodes; ++i) {
//                                for (int j = 0; j < inputData.nbNodes; ++j) {
//                                    if (i != j) {
//                                        for (ArrayList<Integer> path :
//                                                ((DominantShortestPath) arrivalTimeFunctions[i][j]).paths) {
//                                            int nbArcs = path.size();
//                                            totalNbArcs += nbArcs;
//                                            ++nbPaths;
//                                            if (nbArcs > maxNbArcs) {
//                                                maxNbArcs = nbArcs;
//                                            }
//                                        }
//
//                                        double travelTime = arrivalTimeFunctions[i][j].getArrivalTime(0.0);
//                                        if (travelTime > maxTravelTime) {
//                                            maxTravelTime = travelTime;
//                                        }
//                                        if (i == 0 && travelTime > maxTravelTimeFromDepot) {
//                                            maxTravelTimeFromDepot = travelTime;
//                                        }
//
//                                        size += arrivalTimeFunctions[i][j].getSize();
//                                    }
//                                }
//                            }
                            //System.out.println(String.format("%f\t%f", maxTravelTime, maxTravelTimeFromDepot));
//                            System.out.println(String.format("%d\t%f\t%d\t%f\t%f", arrayNbNodes[nbNodeIndex],
//                                    (double) totalNbArcs / nbPaths, maxNbArcs, dspTime, (double) size / 1024 / 1024));

//                            double maxTravelTime = 0.0;
//                            for (Request request : inputData.requests) {
//                                double travelTime = arrivalTimeFunctions[0][request.pickupNode].getArrivalTime(0.0);
//                                if (travelTime > maxTravelTime) {
//                                    maxTravelTime = travelTime;
//                                }
//                                travelTime = arrivalTimeFunctions[0][request.pickupNode].getArrivalTime(0.0);
//                                if (travelTime > maxTravelTime) {
//                                    maxTravelTime = travelTime;
//                                }
//                            }
//                            System.out.println(maxTravelTime);

                            Solution solution = dynamicProblemSolver.apply(inputData, instanceRandom, tabuSearch);
                            System.out.println(String.format("%f\t%f\t%f\t%f\t%d",
                                    solution.getCost(), solution.getTravelTime(), solution.getLateness(),
                                    solution.getOvertime(), solution.getNbRoutes()));

                            for (Request request : inputData.requests) {
                                assert solution.getNbStopsForRequest(request) == 2;
                            }

//                            ArrivalTimeFunction[][] atf1 =
//                                    DominantShortestPath.getDominantShortestPaths(inputData, false,
//                                            inputData.intervalTimes);
//                            ArrivalTimeFunction[][] atf2 =
//                                    DominantShortestPath.getDominantShortestPaths(inputData, true,
//                                            new double[] {inputData.intervalTimes[0]});
//
//                            double travelTimeRatio = 0.0;
//                            int nbValues = 0;
//                            for (int i = 0; i < inputData.nbNodes; ++i) {
//                                for (int j = 0; j < inputData.nbNodes; ++j) {
//                                    if (i != j) {
//                                        for (double departureTime = 0.0;
//                                             departureTime <= inputData.endOfTheDay;
//                                             ++departureTime) {
//                                            double travelTime1 =
//                                                    atf1[i][j].getArrivalTime(departureTime) - departureTime;
//                                            double travelTime2 =
//                                                    atf2[i][j].getArrivalTime(departureTime) - departureTime;
//                                            travelTimeRatio += travelTime1 / travelTime2;
//                                            ++nbValues;
//                                        }
//                                    }
//                                }
//                            }
//                            System.out.println(travelTimeRatio / nbValues - 1.0);

//                            time = System.nanoTime();
//                            ArrivalTimeFunction[][] arrivalTimeFunctions =
//                                    DominantShortestPath.getDominantShortestPaths(inputData);
//                            double preprocessingTime = (System.nanoTime() - time) / 1000000000.0;
//                            System.out.println(preprocessingTime);
                            //preprocessingTimes[nbNodeIndex] += preprocessingTime / 1000000000.0;
//
//                            time = System.nanoTime();
//                            Solution initialSolution = new Solution(inputData.nbVehicles, arrivalTimeFunctions,
//                                    inputData.depotTimeWindowUpperBound, latenessWeight, inputData.vehicleCapacity);
//                            initialSolution.insertRequestsBestFirst(inputData.requests);
//                            double initialSolutionTime = (System.nanoTime() - time) / 1000000000.0;
//                            initialSolutionTimeAverage += initialSolutionTime;
//                            initialSolutionCost += initialSolution.getCost();
//
//                            time = System.nanoTime();
//                            Solution solutionAfterTabu =
//                                    tabuSearch.Apply(initialSolution, inputData.requests, instanceRandom);
//                            double tabuSearchTime = (System.nanoTime() - time) / 1000000000.0;
//                            tabuSearchTimes[nbClientsIndex] += tabuSearchTime;
//                            afterTabuCost += solutionAfterTabu.getCost();
//
//                            double improvement = 1.0 - solutionAfterTabu.getCost() / initialSolution.getCost();
//                            totalImprovement += improvement;
//                            if (improvement > maxTotalImprovement) {
//                                maxTotalImprovement = improvement;
//                            }
//
//                            System.out.println(String.format("%f\t%f",
////                                    arrayNbNodes[nbNodeIndex],
////                                    arrayNbClients[nbClientsIndex],
////                                    corr,
////                                    index,
////                                    timeWindowType,
////                                    preprocessingTime,
////                                    initialSolutionTime,
//                                    tabuSearchTime,
////                                    initialSolution.getCost(),
//                                    solutionAfterTabu.getCost()));
//
                            ++nbInstances;
//                            ++nbInstancesForNbClients;
//                            ++nbInstancesPerNodeCount[nbNodeIndex];
                        }
                    }
                }
            }

            tabuSearchTimes[nbClientsIndex] = tabuSearch.getTimePerIteration();
            insertionTimes[nbClientsIndex] = tabuSearch.getTimePerInsertion();
            removalTimes[nbClientsIndex] = tabuSearch.getTimePerRemoval();
            nbInsertions[nbClientsIndex] = tabuSearch.getNbInsertionsPerIteration();
            nbRemovals[nbClientsIndex] = tabuSearch.getNbRemovalsPerIteration();
            averageSearchIterations[nbClientsIndex] = tabuSearch.getAverageIterationsPerSearch();
            tabuSearch.resetSearchIterations();

//            StringBuilder s = new StringBuilder(String.format("Best solution average for %d clients:\n0\t1.00",
//                    arrayNbClients[nbClientsIndex] / 2));
//            int i = 10;
//            for (double percentage : tabuSearch.getBestSolutionPercentages()) {
//                s.append(String.format("\n%d\t%f", i, percentage / nbInstancesForNbClients));
//                i += 10;
//            }
//            System.out.println(s);
        }

//        for (int i = 0; i < preprocessingTimes.length; ++i) {
//            preprocessingTimes[i] /= nbInstancesPerNodeCount[i];
//        }
//
//        inputDataReadTime /= nbInstances;
//        initialSolutionTimeAverage /= nbInstances;
//        initialSolutionCost /= nbInstances;
//        afterTabuCost /= nbInstances;
//        totalImprovement /= nbInstances;
//
//        System.out.println(String.format("inputDataReadTime = %f", inputDataReadTime));
//        System.out.println(String.format("preprocessingTimes = %s", Arrays.toString(preprocessingTimes)));
//        System.out.println(String.format("initialSolutionTimeAverage = %f", initialSolutionTimeAverage));
//        System.out.println(String.format("initialSolutionCost = %f", initialSolutionCost));
//        System.out.println(String.format("afterTabuCost = %f", afterTabuCost));
//        System.out.println(String.format("totalImprovement = %f", totalImprovement));
//        System.out.println(String.format("maxTotalImprovement = %f", maxTotalImprovement));
        System.out.println(String.format("totalTime (s) = %f", (System.nanoTime() - startTime) / 1000000000.0));
        System.out.println(String.format("averageSearchIterations = %s", Arrays.toString(averageSearchIterations)));
        System.out.println("tabuSearchTimes :");
        for (double time : tabuSearchTimes) {
            System.out.println(time);
        }
        System.out.println(String.format("insertionTimes = %s", Arrays.toString(insertionTimes)));
        System.out.println(String.format("removalTimes = %s", Arrays.toString(removalTimes)));
        System.out.println(String.format("nbInsertions = %s", Arrays.toString(nbInsertions)));
        System.out.println(String.format("nbRemovals = %s", Arrays.toString(nbRemovals)));
    }
}
