import java.util.*;

public class DynamicProblemSolver {
    private final double iterationsMultiplier;
    private final double tabuTenureMultiplier;
    private final double randomMovesMultiplier;
    private final double latenessWeight;
    private final double overtimeWeight;

    // Multiply the time values by this multiplier to obtain milliseconds
    private final double msMultiplier;

    public DynamicProblemSolver(double iterationsMultiplier, double tabuTenureMultiplier, double randomMovesMultiplier,
                                double latenessWeight, double overtimeWeight, double msMultiplier) {
        this.iterationsMultiplier = iterationsMultiplier;
        this.tabuTenureMultiplier = tabuTenureMultiplier;
        this.randomMovesMultiplier = randomMovesMultiplier;
        this.latenessWeight = latenessWeight;
        this.overtimeWeight = overtimeWeight;
        this.msMultiplier = msMultiplier;
    }

    public Solution apply(InputData inputData, Random random, TabuSearch tabuSearch) {
        Date startTime = new Date();
        Calendar calendar = Calendar.getInstance();
        Timer timer = new Timer();

        ArrivalTimeFunction[][] arrivalTimeFunctions =
                DominantShortestPath.getDominantShortestPaths(inputData);
        Solution solution = new Solution(inputData.nbVehicles, arrivalTimeFunctions,
                inputData.endOfTheDay, latenessWeight, overtimeWeight,
                inputData.vehicleCapacity, inputData.returnTime);
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

            // Might seal newly added stops
            solution.setCurrentTime(currentTime);

            int nbUnsealedStops = solution.getNbUnsealedStops();
            tabuSearch.setNbIterationsPerPhase((int) (nbUnsealedStops * iterationsMultiplier));
            tabuSearch.setTabuTenure((int) (nbUnsealedStops * tabuTenureMultiplier));
            tabuSearch.setNbRandomMoves((int) (nbUnsealedStops * randomMovesMultiplier));

            double nextDepartureTime = solution.getNextDepartureTime();
            while (DoubleComparator.lessThan(nextDepartureTime, nextReleaseTime) && nbUnsealedStops > 0) {
                calendar.setTime(startTime);
                calendar.add(Calendar.MILLISECOND, (int) (nextDepartureTime * msMultiplier));

                // If the calendar time is passed, the task will be scheduled as soon as possible
                timer.schedule(new InteruptSearchTask(tabuSearch), calendar.getTime());
                solution = tabuSearch.Apply(solution, requestsInserted, random);
                tabuSearch.resetInterruption();

                solution.setCurrentTime(nextDepartureTime);

                nbUnsealedStops = solution.getNbUnsealedStops();
                tabuSearch.setNbIterationsPerPhase((int) (nbUnsealedStops * iterationsMultiplier));
                tabuSearch.setTabuTenure((int) (nbUnsealedStops * tabuTenureMultiplier));
                tabuSearch.setNbRandomMoves((int) (nbUnsealedStops * randomMovesMultiplier));

                nextDepartureTime = solution.getNextDepartureTime();
            }

            if (nextReleaseTime < Double.MAX_VALUE && nbUnsealedStops > 0) {
                calendar.setTime(startTime);
                calendar.add(Calendar.MILLISECOND, (int) (nextReleaseTime * msMultiplier));

                // If the calendar time is passed, the task will be scheduled as soon as possible
                timer.schedule(new InteruptSearchTask(tabuSearch), calendar.getTime());
                solution = tabuSearch.Apply(solution, requestsInserted, random);
                tabuSearch.resetInterruption();
            }

            currentTime = nextReleaseTime;
        }

        timer.cancel();
        return solution;
    }
}

class InteruptSearchTask extends TimerTask {
    private final TabuSearch tabuSearch;

    public InteruptSearchTask(TabuSearch tabuSearch) {
        this.tabuSearch = tabuSearch;
    }

    @Override
    public void run() {
        tabuSearch.interrupt();
    }
}
