import java.io.FileNotFoundException;

public class TDDPDPRN {
    public static void main(String[] args) {
        final double latenessWeight = 1.0;
        final int[] arrayNbNodes = new int[] {50, 100, 200};
        final int[] arrayNbClients = new int[] {16, 25, 33, 50, 66, 75, 100, 133};
        final double[] arrayCorr = new double[] {0.02, 0.5, 0.98};
        final int[] indices = new int[] {1, 2, 3, 4, 5};
        final String[] timeWindowTypes = new String[] {"NTW", "WTW"};
        final TabuSearch localDescent = new TabuSearch(1, 0);

        double inputDataReadTime = 0.0;
        double preprocessingTime = 0.0;
        double initialSolutionTime = 0.0;
        double descentTime = 0.0;
        double tabuSearchTime = 0.0;
        double initialSolutionCost = 0.0;
        double afterDescentCost = 0.0;
        double afterTabuCost = 0.0;
        double descentImprovement = 0.0;
        double tabuSearchImprovement = 0.0;
        double maxTabuSearchImprovement = 0.0;
        int nbInstances = 0;

        for (int nbNodes : arrayNbNodes) {
            for (int nbClients : arrayNbClients) {
                TabuSearch tabuSearch = new TabuSearch(
                        nbClients * 10,
                        nbClients * 3 / 8);

                for (double corr : arrayCorr) {
                    for (int index : indices) {
                        for (String timeWindowType : timeWindowTypes) {
                            long time = System.nanoTime();
                            InputData inputData;
                            try {
                                inputData = new InputData(nbNodes, nbClients, corr, index, timeWindowType);
                            } catch (FileNotFoundException e) {
                                // Not all parameter combinations exist
                                continue;
                            }
                            inputDataReadTime += (System.nanoTime() - time) / 1000000000.0;

                            time = System.nanoTime();
                            ArrivalTimeFunction[][] arrivalTimeFunctions =
                                    DominantShortestPath.getDominantShortestPaths(inputData);
                            preprocessingTime += (System.nanoTime() - time) / 1000000000.0;

                            time = System.nanoTime();
                            Solution initialSolution = new Solution(inputData.nbVehicles, arrivalTimeFunctions,
                                    inputData.depotTimeWindowUpperBound, latenessWeight, inputData.vehicleCapacity);
                            initialSolution.insertRequestsBestFirst(inputData.requests);
                            initialSolutionTime += (System.nanoTime() - time) / 1000000000.0;
                            initialSolutionCost += initialSolution.getCost();

                            time = System.nanoTime();
                            Solution solutionAfterDescent = localDescent.Apply(initialSolution, inputData.requests);
                            descentTime += (System.nanoTime() - time) / 1000000000.0;
                            afterDescentCost += solutionAfterDescent.getCost();
                            descentImprovement += 1.0 -
                                    solutionAfterDescent.getCost() / initialSolution.getCost();


                            time = System.nanoTime();
                            Solution solutionAfterTabu = tabuSearch.Apply(solutionAfterDescent, inputData.requests);
                            tabuSearchTime += (System.nanoTime() - time) / 1000000000.0;
                            afterTabuCost += solutionAfterTabu.getCost();

                            double improvement = 1.0 - solutionAfterTabu.getCost() / solutionAfterDescent.getCost();
                            tabuSearchImprovement += improvement;
                            if (improvement > maxTabuSearchImprovement) {
                                maxTabuSearchImprovement = improvement;
                            }

                            ++nbInstances;
                        }
                    }
                }
            }
        }

        inputDataReadTime /= nbInstances;
        preprocessingTime /= nbInstances;
        initialSolutionTime /= nbInstances;
        descentTime /= nbInstances;
        tabuSearchTime /= nbInstances;
        initialSolutionCost /= nbInstances;
        afterDescentCost /= nbInstances;
        afterTabuCost /= nbInstances;
        descentImprovement /= nbInstances;
        tabuSearchImprovement /= nbInstances;

        System.out.println(String.format("inputDataReadTime = %f", inputDataReadTime));
        System.out.println(String.format("preprocessingTime = %f", preprocessingTime));
        System.out.println(String.format("initialSolutionTime = %f", initialSolutionTime));
        System.out.println(String.format("descentTime = %f", descentTime));
        System.out.println(String.format("tabuSearchTime = %f", tabuSearchTime));
        System.out.println(String.format("initialSolutionCost = %f", initialSolutionCost));
        System.out.println(String.format("afterDescentCost = %f", afterDescentCost));
        System.out.println(String.format("afterTabuCost = %f", afterTabuCost));
        System.out.println(String.format("descentImprovement = %f", descentImprovement));
        System.out.println(String.format("tabuSearchImprovement = %f", tabuSearchImprovement));
        System.out.println(String.format("maxTabuSearchImprovement = %f", maxTabuSearchImprovement));
    }
}
