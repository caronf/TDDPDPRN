import java.util.*;

public class DynamicProblemSolver {
    private final double latenessWeight;
    private final double overtimeWeight;

    // Multiply the time values by this multiplier to obtain milliseconds
    private final double msMultiplier;

    private final boolean useAverageTravelTime;
    private final boolean useTabuSearch;

    public DynamicProblemSolver(double latenessWeight, double overtimeWeight, double msMultiplier,
                                boolean useAverageTravelTime, boolean useTabuSearch) {
        this.latenessWeight = latenessWeight;
        this.overtimeWeight = overtimeWeight;
        this.msMultiplier = msMultiplier;
        this.useAverageTravelTime = useAverageTravelTime;
        this.useTabuSearch = useTabuSearch;
    }

    public Solution apply(InputData inputData, Random random, TabuSearch tabuSearch) {
        Timer timer = new Timer();

        double[] departureTimes;
        if (useAverageTravelTime) {
            departureTimes = new double[] {0.0};
        } else {
            departureTimes = inputData.proposedDepartTime;
        }

        ArrivalTimeFunction[][] arrivalTimeFunctions =
                DominantShortestPath.getDominantShortestPaths(inputData, useAverageTravelTime, departureTimes);

        ProblemClock problemClock = new ProblemClock(msMultiplier);

        Solution solution = new Solution(inputData.nbVehicles, arrivalTimeFunctions,
                inputData.endOfTheDay, latenessWeight, overtimeWeight,
                inputData.vehicleCapacity, inputData.returnTime);
        ArrayList<Request> requestsInserted = new ArrayList<>(inputData.requests.size());

        while (requestsInserted.size() < inputData.requests.size()) {
            double nextReleaseTime = Double.MAX_VALUE;
            ArrayList<Request> requestsToInsert = new ArrayList<>();
            for (Request request : inputData.requests) {
                if (!requestsInserted.contains(request)) {
                    if (DoubleComparator.lessOrEqual(request.releaseTime, problemClock.getCurrentProblemTime())) {
                        requestsToInsert.add(request);
                    } else {
                        nextReleaseTime = Math.min(nextReleaseTime, request.releaseTime);
                    }
                }
            }

            solution.setCurrentTime(problemClock.getCurrentProblemTime());
            solution.insertRequestsBestFirst(requestsToInsert);
            requestsInserted.addAll(requestsToInsert);

            // Might seal newly added stops
            solution.setCurrentTime(problemClock.getCurrentProblemTime());

            if (DoubleComparator.greaterThan(nextReleaseTime, problemClock.getCurrentProblemTime())) {
                if (useTabuSearch) {
                    if (nextReleaseTime < Double.MAX_VALUE) {
                        timer.schedule(new InteruptSearchTask(tabuSearch),
                                problemClock.convertProblemTimeToDate(nextReleaseTime));
                    }

                    solution = tabuSearch.Apply(solution, requestsInserted, random, problemClock);
                    timer.purge();
                    tabuSearch.resetInterruption();
                }

                if (nextReleaseTime < Double.MAX_VALUE) {
                    double timeToNextRelease = nextReleaseTime - problemClock.getCurrentProblemTime();
                    if(timeToNextRelease > 0.0) {
                        problemClock.skip(timeToNextRelease);
                    }
                }
            }
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
