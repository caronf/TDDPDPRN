import java.io.FileNotFoundException;

public class TDDPDPRN {
    public static void main(String[] args) {
        final double latenessWeight = 1.0;
        final int[] arrayNbNodes = new int[] {50, 100, 200};
        final int[] arrayNbClients = new int[] {16, 25, 33, 50, 66, 75, 100, 133};
        final double[] arrayCorr = new double[] {0.02, 0.5, 0.98};
        final int[] indices = new int[] {1, 2, 3, 4, 5};
        final String[] timeWindowTypes = new String[] {"NTW", "WTW"};

        double inputDataReadTime = 0.0;
        double preprocessingTime = 0.0;
        double anyOrderTime = 0.0;
        double bestFirstTime = 0.0;
        double anyOrderCost = 0.0;
        double bestFirstCost = 0.0;
        int nbInstances = 0;

        for (int nbNodes : arrayNbNodes) {
            for (int nbClients : arrayNbClients) {
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
                            ArrivalTimeCalculator[][] arrivalTimeCalculators =
                                    DominantShortestPath.getDominantShortestPaths(inputData);
                            preprocessingTime += (System.nanoTime() - time) / 1000000000.0;

                            time = System.nanoTime();
                            Solution solutionAnyOrder = new Solution(inputData.nbVehicles, arrivalTimeCalculators,
                                    inputData.depotTimeWindowUpperBound, latenessWeight);
                            solutionAnyOrder.insertRequestsAnyOrder(inputData.requests);
                            anyOrderTime += (System.nanoTime() - time) / 1000000000.0;
                            anyOrderCost += solutionAnyOrder.getCost();

                            time = System.nanoTime();
                            Solution solutionBestFirst = new Solution(inputData.nbVehicles, arrivalTimeCalculators,
                                    inputData.depotTimeWindowUpperBound, latenessWeight);
                            solutionBestFirst.insertRequestsBestFirst(inputData.requests);
                            bestFirstTime += (System.nanoTime() - time) / 1000000000.0;
                            bestFirstCost += solutionBestFirst.getCost();

                            ++nbInstances;
                        }
                    }
                }
            }
        }

        inputDataReadTime /= nbInstances;
        preprocessingTime /= nbInstances;
        anyOrderTime /= nbInstances;
        bestFirstTime /= nbInstances;
        anyOrderCost /= nbInstances;
        bestFirstCost /= nbInstances;

        System.out.println(String.format("inputDataReadTime = %f", inputDataReadTime));
        System.out.println(String.format("preprocessingTime = %f", preprocessingTime));
        System.out.println(String.format("anyOrderTime = %f", anyOrderTime));
        System.out.println(String.format("bestFirstTime = %f", bestFirstTime));
        System.out.println(String.format("anyOrderCost = %f", anyOrderCost));
        System.out.println(String.format("bestFirstCost = %f", bestFirstCost));
    }
}