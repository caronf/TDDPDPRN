import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;

public class TDDPDPRN {
    public static void main(String[] args) {
        final double latenessWeight = 1.0;
        final int[] arrayNbNodes = new int[] {50, 100, 200};
        final int[] arrayNbClients = new int[] {16, 25, 33, 50, 66, 75, 100, 133};
        final double[] arrayCorr = new double[] {0.02, 0.5, 0.98};
        final int[] indices = new int[] {1, 2, 3, 4, 5};
        final String[] timeWindowTypes = new String[] {"NTW", "WTW"};

        double inputDataReadTime = 0.0;
        double[] preprocessingTimes = new double[arrayNbNodes.length];
        double initialSolutionTime = 0.0;
        double[] tabuSearchTimes = new double[arrayNbClients.length];
        double initialSolutionCost = 0.0;
        double afterTabuCost = 0.0;
        double totalImprovement = 0.0;
        double maxTotalImprovement = 0.0;
        int nbInstances = 0;
        int[] nbInstancesPerNodeCount = new int[arrayNbNodes.length];

        for (int nbClientsIndex = 0; nbClientsIndex < arrayNbClients.length; ++nbClientsIndex) {
            int nbInstancesForNbClients = 0;
            TabuSearch tabuSearch = new TabuSearch(
                    arrayNbClients[nbClientsIndex] * 10,
                    arrayNbClients[nbClientsIndex] * 3 / 8,
                    10,
                    arrayNbClients[nbClientsIndex] / 4);

            for (int nbNodeIndex = 0; nbNodeIndex < arrayNbNodes.length; ++nbNodeIndex) {
                for (double corr : arrayCorr) {
                    for (int index : indices) {
                        for (String timeWindowType : timeWindowTypes) {
                            long time = System.nanoTime();
                            InputData inputData;
                            try {
                                inputData = new InputData(arrayNbNodes[nbNodeIndex], arrayNbClients[nbClientsIndex],
                                        corr, index, timeWindowType);
                            } catch (FileNotFoundException e) {
                                // Not all parameter combinations exist
                                continue;
                            }
                            inputDataReadTime += (System.nanoTime() - time) / 1000000000.0;

                            time = System.nanoTime();
                            ArrivalTimeFunction[][] arrivalTimeFunctions =
                                    DominantShortestPath.getDominantShortestPaths(inputData);
                            preprocessingTimes[nbNodeIndex] += (System.nanoTime() - time) / 1000000000.0;

                            time = System.nanoTime();
                            Solution initialSolution = new Solution(inputData.nbVehicles, arrivalTimeFunctions,
                                    inputData.depotTimeWindowUpperBound, latenessWeight, inputData.vehicleCapacity);
                            initialSolution.insertRequestsBestFirst(inputData.requests);
                            initialSolutionTime += (System.nanoTime() - time) / 1000000000.0;
                            initialSolutionCost += initialSolution.getCost();

                            Random instanceRandom = new Random(arrayNbClients[nbClientsIndex] * 100000000 +
                                    arrayNbNodes[nbNodeIndex] * 100000 + (int) (corr * 1000) +
                                    index * 100 + timeWindowType.charAt(0));
                            time = System.nanoTime();
                            Solution solutionAfterTabu =
                                    tabuSearch.Apply(initialSolution, inputData.requests, instanceRandom);
                            tabuSearchTimes[nbClientsIndex] += (System.nanoTime() - time) / 1000000000.0;
                            afterTabuCost += solutionAfterTabu.getCost();

                            double improvement = 1.0 - solutionAfterTabu.getCost() / initialSolution.getCost();
                            totalImprovement += improvement;
                            if (improvement > maxTotalImprovement) {
                                maxTotalImprovement = improvement;
                            }

                            ++nbInstances;
                            ++nbInstancesForNbClients;
                            ++nbInstancesPerNodeCount[nbNodeIndex];
                        }
                    }
                }
            }

            tabuSearchTimes[nbClientsIndex] /= tabuSearch.getTotalNbIterations();

            StringBuilder s = new StringBuilder(String.format("Best solution average for %d clients:\n0\t1.00",
                    arrayNbClients[nbClientsIndex] / 2));
            int i = 10;
            for (double percentage : tabuSearch.getBestSolutionPercentages()) {
                s.append(String.format("\n%d\t%f", i, percentage / nbInstancesForNbClients));
                i += 10;
            }
            System.out.println(s);
        }

        for (int i = 0; i < preprocessingTimes.length; ++i) {
            preprocessingTimes[i] /= nbInstancesPerNodeCount[i];
        }

        inputDataReadTime /= nbInstances;
        initialSolutionTime /= nbInstances;
        initialSolutionCost /= nbInstances;
        afterTabuCost /= nbInstances;
        totalImprovement /= nbInstances;

        System.out.println(String.format("inputDataReadTime = %f", inputDataReadTime));
        System.out.println(String.format("preprocessingTimes = %s", Arrays.toString(preprocessingTimes)));
        System.out.println(String.format("initialSolutionTime = %f", initialSolutionTime));
        System.out.println(String.format("tabuSearchTimes = %s", Arrays.toString(tabuSearchTimes)));
        System.out.println(String.format("initialSolutionCost = %f", initialSolutionCost));
        System.out.println(String.format("afterTabuCost = %f", afterTabuCost));
        System.out.println(String.format("totalImprovement = %f", totalImprovement));
        System.out.println(String.format("maxTotalImprovement = %f", maxTotalImprovement));
    }
}
