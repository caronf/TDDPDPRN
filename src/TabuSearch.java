import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class TabuSearch {
    private int searchIterations;
    private int searchCount;
    private double searchTime;

    private final double iterationsMultiplier;
    private final double tabuTenureMultiplier;
    private final double randomMovesMultiplier;
    private final int nbDiversificationIterations;
    private final boolean testReinsertion;

    // Multiply the time values by this multiplier to obtain milliseconds
    private final double msMultiplier;

    private final AtomicBoolean isInterrupted;

//    private final double[] bestSolutionPercentages;
//    private final double[] currentSolutionPercentages;

    public TabuSearch(double iterationsMultiplier, double tabuTenureMultiplier, double randomMovesMultiplier,
                      int nbDiversificationIterations, boolean testReinsertion, double msMultiplier) {
        searchIterations = 0;
        searchCount = 0;
        searchTime = 0;

        this.iterationsMultiplier = iterationsMultiplier;
        this.tabuTenureMultiplier = tabuTenureMultiplier;
        this.randomMovesMultiplier = randomMovesMultiplier;
        this.nbDiversificationIterations = nbDiversificationIterations;
        this.testReinsertion = testReinsertion;
        this.msMultiplier = msMultiplier;

        isInterrupted = new AtomicBoolean(false);
//        bestSolutionPercentages = new double[stoppingCriteria * (nbDiversificationIterations + 1) / 10];
//        currentSolutionPercentages = new double[stoppingCriteria * (nbDiversificationIterations + 1) / 10];
    }

    public int getAverageIterationsPerSearch() {
        return searchCount == 0 ? 0 : searchIterations / searchCount;
    }

    public double getTimePerIteration() {
        return searchIterations == 0 ? 0 : searchTime / searchIterations;
    }

    public void resetSearchIterations() {
        searchIterations = 0;
        searchCount = 0;
        searchTime = 0.0;
    }

    public void interrupt() {
        isInterrupted.set(true);
    }

    public void resetInterruption() {
        isInterrupted.set(false);
    }

//    public double[] getBestSolutionPercentages() {
//        return bestSolutionPercentages;
//    }
//
//    public double[] getCurrentSolutionPercentages() {
//        return currentSolutionPercentages;
//    }

    public Solution Apply(Solution startingSolution, List<Request> requests, Random random, long startTime) {
        ++searchCount;
        Solution currentSolution = startingSolution;
        Solution bestSolution = startingSolution;
        double nextVehicleDeparture = startingSolution.getNextDepartureTime();
        int nbUnsealedStops = startingSolution.getNbUnsealedStops();
        if (nbUnsealedStops == 0) {
            return startingSolution;
        }

        ArrayList<Integer> validRequestIndices = new ArrayList<>(requests.size());
        for (int i = 0; i < requests.size(); ++i) {
            validRequestIndices.add(i);
        }

        int[] tabuCounters = new int[requests.size()];
        //int pastIterations = 0;

        for (int diversificationCounter = 0;
             diversificationCounter <= nbDiversificationIterations;
             ++diversificationCounter) {
            int nbIterations = 0;
            long phaseStartTime = System.nanoTime();
            while (nbIterations < (int) (iterationsMultiplier * nbUnsealedStops) /* * (diversificationCounter + 1)*/) {
                if (isInterrupted.get()) {
                    searchTime += (System.nanoTime() - phaseStartTime) / 1000000000.0;
                    return bestSolution;
                }

                double currentTime = (System.currentTimeMillis() - startTime) / msMultiplier;
                if (DoubleComparator.greaterOrEqual(currentTime, nextVehicleDeparture)) {
                    bestSolution.setCurrentTime(currentTime);
                    nextVehicleDeparture = bestSolution.getNextDepartureTime();
                    nbUnsealedStops = bestSolution.getNbUnsealedStops();
                    if (nbUnsealedStops == 0) {
                        searchTime += (System.nanoTime() - phaseStartTime) / 1000000000.0;
                        return bestSolution;
                    }

                    currentSolution = bestSolution;
                    Arrays.fill(tabuCounters, 0);
                    nbIterations = 0;
                }

                Solution bestTemporarySolution = null;
                Solution bestTabuTemporarySolution = null;
                int bestRequestIndex = -1;
                int bestTabuRequestIndex = -1;

                ArrayList<Integer> requestIndicesToRemove = new ArrayList<>();
                for (int requestIndex : validRequestIndices) {
                    Solution temporarySolution = new Solution(currentSolution);
                    int nbRemoved = temporarySolution.removeRequest(requests.get(requestIndex));

                    if (nbRemoved == 0) {
                        requestIndicesToRemove.add(requestIndex);
                        continue;
                    }

                    if (testReinsertion) {
                        temporarySolution.insertRequest(requests.get(requestIndex));
                    }

                    if (tabuCounters[requestIndex] == 0) {
                        if (bestTemporarySolution == null ||
                                DoubleComparator.lessThan(temporarySolution.getCost(),
                                        bestTemporarySolution.getCost())) {
                            bestTemporarySolution = temporarySolution;
                            bestRequestIndex = requestIndex;
                        }
                    } else {
                        if (bestTabuTemporarySolution == null ||
                                DoubleComparator.lessThan(temporarySolution.getCost(),
                                        bestTabuTemporarySolution.getCost())) {
                            bestTabuTemporarySolution = temporarySolution;
                            bestTabuRequestIndex = requestIndex;
                        }

                        --tabuCounters[requestIndex];
                    }
                }
                validRequestIndices.removeAll(requestIndicesToRemove);

                ++nbIterations;
                ++searchIterations;

                if (bestTemporarySolution != null) {
                    if (!testReinsertion) {
                        bestTemporarySolution.insertRequest(requests.get(bestRequestIndex));
                    }

                    currentSolution = bestTemporarySolution;
                    if (DoubleComparator.lessThan(bestTemporarySolution.getCost(), bestTemporarySolution.getCost())) {
                        nbIterations = 0;
                        bestSolution = bestTemporarySolution;
                        nextVehicleDeparture = bestSolution.getNextDepartureTime();
                    }
                } else if (bestTabuTemporarySolution == null) {
                    searchTime += (System.nanoTime() - phaseStartTime) / 1000000000.0;
                    return bestSolution;
                }

                if (bestTabuTemporarySolution != null) {
                    assert bestTemporarySolution != null;

                    if (!testReinsertion) {
                        bestTabuTemporarySolution.insertRequest(requests.get(bestTabuRequestIndex));
                    }

                    if (DoubleComparator.lessThan(bestTabuTemporarySolution.getCost(), bestSolution.getCost())) {
                        nbIterations = 0;
                        currentSolution = bestTabuTemporarySolution;
                        bestSolution = bestTabuTemporarySolution;
                        nextVehicleDeparture = bestSolution.getNextDepartureTime();
                        bestRequestIndex = bestTabuRequestIndex;
                    }
                }

                tabuCounters[bestRequestIndex] = (int) (tabuTenureMultiplier * nbUnsealedStops);

//                if ((nbIterations + pastIterations) % 10 == 0) {
//                    bestSolutionPercentages[(nbIterations + pastIterations) / 10 - 1] +=
//                            bestSolution.getCost() / startingSolution.getCost();
//                    currentSolutionPercentages[(nbIterations + pastIterations) / 10 - 1] +=
//                            currentSolution.getCost() / startingSolution.getCost();
//                }
            }

            searchTime += (System.nanoTime() - phaseStartTime) / 1000000000.0;

            if (diversificationCounter < nbDiversificationIterations) {
                Arrays.fill(tabuCounters, 0);

                if (currentSolution == bestSolution) {
                    currentSolution = new Solution(currentSolution);
                }

                for (int i = 0; i < randomMovesMultiplier * nbUnsealedStops; ++i) {
                    if (isInterrupted.get()) {
                        return bestSolution;
                    }

                    double currentTime = (System.currentTimeMillis() - startTime) / msMultiplier;
                    if (DoubleComparator.greaterOrEqual(currentTime, nextVehicleDeparture)) {
                        bestSolution.setCurrentTime(currentTime);
                        nextVehicleDeparture = bestSolution.getNextDepartureTime();
                        nbUnsealedStops = bestSolution.getNbUnsealedStops();
                        if (nbUnsealedStops == 0) {
                            return bestSolution;
                        }

                        currentSolution = new Solution(bestSolution);
                        break;
                    }

                    int requestIndexIndex = random.nextInt(validRequestIndices.size());
                    int nbRemoved = currentSolution.removeRequest(
                            requests.get(validRequestIndices.get(requestIndexIndex)));
                    assert nbRemoved > 0;
                    currentSolution.insertRequestAtRandomPosition(
                            requests.get(validRequestIndices.get(requestIndexIndex)), random);

                    if (DoubleComparator.lessThan(currentSolution.getCost(), bestSolution.getCost())) {
                        bestSolution = new Solution(currentSolution);
                        nextVehicleDeparture = bestSolution.getNextDepartureTime();
                    }
                }

                //pastIterations += nbIterations;
            }
        }

        return bestSolution;
    }
}
