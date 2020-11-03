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
        ArrayList<Request> requestsInserted = new ArrayList<>(inputData.requests.size());

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
            requestsInserted.addAll(requestsToInsert);
            requestsToInsert.clear();

            int nbUnsealedStops = solution.getNbUnsealedStops();
            tabuSearch.setStoppingCriteria(nbUnsealedStops * 10);
            tabuSearch.setTabuTenure(nbUnsealedStops * 3 / 8);
            tabuSearch.setNbRandomMoves(nbUnsealedStops / 2);
            solution = tabuSearch.Apply(solution, requestsInserted, random);

            currentTime = nextReleaseTime;
        }

        return solution;
    }
}
