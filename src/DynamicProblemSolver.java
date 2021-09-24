import java.util.*;

public class DynamicProblemSolver {
    private final double latenessWeight;
    private final double overtimeWeight;

    // Multiply the time values by this multiplier to obtain milliseconds
    private final double msMultiplier;

    private boolean useAverageTravelTime;
    private boolean useTabuSearch;

    public DynamicProblemSolver(double latenessWeight, double overtimeWeight, double msMultiplier,
                                boolean useAverageTravelTime, boolean useTabuSearch) {
        this.latenessWeight = latenessWeight;
        this.overtimeWeight = overtimeWeight;
        this.msMultiplier = msMultiplier;
        this.useAverageTravelTime = useAverageTravelTime;
        this.useTabuSearch = useTabuSearch;
    }

    public Solution apply(InputData inputData, Random random, TabuSearch tabuSearch) {
        Date startTime = new Date();
        Calendar calendar = Calendar.getInstance();
        Timer timer = new Timer();

        double[] departureTimes;
        if (useAverageTravelTime) {
            departureTimes = new double[] {0.0};
        } else {
            departureTimes = inputData.proposedDepartTime;
        }

        ArrivalTimeFunction[][] arrivalTimeFunctions =
                DominantShortestPath.getDominantShortestPaths(inputData, useAverageTravelTime, departureTimes);
        Solution solution = new Solution(inputData.nbVehicles, arrivalTimeFunctions,
                inputData.endOfTheDay, latenessWeight, overtimeWeight,
                inputData.vehicleCapacity, inputData.returnTime);
        double currentTime = 0.0;
        ArrayList<Request> requestsInserted = new ArrayList<>(inputData.requests.size());

        while (currentTime < Double.MAX_VALUE) {
            double nextReleaseTime = Double.MAX_VALUE;
            ArrayList<Request> requestsToInsert = new ArrayList<>();
            for (Request request : inputData.requests) {
                if (DoubleComparator.equal(request.releaseTime, currentTime)) {
                    requestsToInsert.add(request);
                } else if (DoubleComparator.greaterThan(request.releaseTime, currentTime) &&
                        DoubleComparator.lessThan(request.releaseTime, nextReleaseTime)) {
                    nextReleaseTime = request.releaseTime;
                }
            }

            solution.setCurrentTime(currentTime);
            solution.insertRequestsBestFirst(requestsToInsert);
            requestsInserted.addAll(requestsToInsert);

            // Might seal newly added stops
            solution.setCurrentTime(currentTime);

            if (useTabuSearch) {
                if (nextReleaseTime < Double.MAX_VALUE) {
                    calendar.setTime(startTime);
                    calendar.add(Calendar.MILLISECOND, (int) (nextReleaseTime * msMultiplier));

                    // If the calendar time is passed, the task will be scheduled as soon as possible
                    timer.schedule(new InteruptSearchTask(tabuSearch), calendar.getTime());
                }

                solution = tabuSearch.Apply(solution, requestsInserted, random, startTime.getTime());
                timer.purge();
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
