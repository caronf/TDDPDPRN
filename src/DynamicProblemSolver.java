import java.util.ArrayList;
import java.util.Random;

public class DynamicProblemSolver {
    private final double latenessWeight;

    public DynamicProblemSolver(double latenessWeight) {
        this.latenessWeight = latenessWeight;
    }

    public Solution apply(InputData inputData, Random random, TabuSearch tabuSearch) {
        ArrivalTimeFunction[][] arrivalTimeFunctions =
                DominantShortestPath.getDominantShortestPaths(inputData);
        Solution solution = new Solution(inputData.nbVehicles, arrivalTimeFunctions,
                inputData.depotTimeWindowUpperBound, latenessWeight, inputData.vehicleCapacity);
        double currentTime = 0.0;
        ArrayList<Request> requestsToInsert = new ArrayList<>();

        while (currentTime < Double.MAX_VALUE) {
            double nextReleaseTime = Double.MAX_VALUE;
            for (Request request : inputData.requests) {
                if (request.releaseTime == currentTime) {
                    requestsToInsert.add(request);
                } else if (request.releaseTime > currentTime && request.releaseTime <= nextReleaseTime) {
                    nextReleaseTime = request.releaseTime;
                }
            }

            solution.setCurrentTime(currentTime);
            solution.insertRequestsBestFirst(requestsToInsert);
            requestsToInsert.clear();
//            Solution solutionAfterTabu =
//                    tabuSearch.Apply(solution, inputData.requests, instanceRandom);

            currentTime = nextReleaseTime;
        }

        return solution;
    }
}
