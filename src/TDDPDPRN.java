import java.io.FileNotFoundException;

public class TDDPDPRN {
    public static void main(String[] args) {
        final double latenessWeight = 2.0;
        final int[] arrayNbNodes = new int[] {50, 100, 200};
        final int[] arrayNbClients = new int[] {16, 25, 33, 50, 66, 75, 100, 133};
        final double[] arrayCorr = new double[] {0.02, 0.5, 0.98};
        final int[] indices = new int[] {1, 2, 3, 4, 5};
        final String[] timeWindowTypes = new String[] {"NTW", "WTW"};

        double inputDataReadTime = 0.0;
        double preprocessingTime = 0.0;
        double solution1Time = 0.0;
        double solution2Time = 0.0;
        double solution3Time = 0.0;
        double solution1Cost = 0.0;
        double solution2Cost = 0.0;
        double solution3Cost = 0.0;
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
                            Solution solution1 = new Solution(inputData.nbVehicles, arrivalTimeCalculators,
                                    inputData.depotTimeWindowUpperBound, latenessWeight);
                            solution1.insertRequests1(inputData.requests);
                            solution1Time += (System.nanoTime() - time) / 1000000000.0;
                            solution1Cost += solution1.getCost();

                            time = System.nanoTime();
                            Solution solution2 = new Solution(inputData.nbVehicles, arrivalTimeCalculators,
                                    inputData.depotTimeWindowUpperBound, latenessWeight);
                            solution2.insertRequests2(inputData.requests);
                            solution2Time += (System.nanoTime() - time) / 1000000000.0;
                            solution2Cost += solution2.getCost();

                            time = System.nanoTime();
                            Solution solution3 = new Solution(inputData.nbVehicles, arrivalTimeCalculators,
                                    inputData.depotTimeWindowUpperBound, latenessWeight);
                            solution3.insertRequests3(inputData.requests);
                            solution3Time += (System.nanoTime() - time) / 1000000000.0;
                            solution3Cost += solution3.getCost();

                            ++nbInstances;
                        }
                    }
                }
            }
        }

        inputDataReadTime /= nbInstances;
        preprocessingTime /= nbInstances;
        solution1Time /= nbInstances;
        solution2Time /= nbInstances;
        solution3Time /= nbInstances;
        solution1Cost /= nbInstances;
        solution2Cost /= nbInstances;
        solution3Cost /= nbInstances;

        System.out.println(String.format("inputDataReadTime = %f", inputDataReadTime));
        System.out.println(String.format("preprocessingTime = %f", preprocessingTime));
        System.out.println(String.format("solution1Time = %f", solution1Time));
        System.out.println(String.format("solution2Time = %f", solution2Time));
        System.out.println(String.format("solution3Time = %f", solution3Time));
        System.out.println(String.format("solution1Cost = %f", solution1Cost));
        System.out.println(String.format("solution2Cost = %f", solution2Cost));
        System.out.println(String.format("solution3Cost = %f", solution3Cost));
    }
}