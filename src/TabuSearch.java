import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class TabuSearch {
    private int searchIterations;
    private int searchCount;
    private double searchTime;
    private int nbIterationsPerPhase;
    private int tabuTenure;
    private final int nbDiversificationIterations;
    private int nbRandomMoves;
    private final AtomicBoolean isInterrupted;

//    private final double[] bestSolutionPercentages;
//    private final double[] currentSolutionPercentages;

    public TabuSearch(int nbDiversificationIterations) {
        searchIterations = 0;
        searchCount = 0;
        searchTime = 0;
        this.nbDiversificationIterations = nbDiversificationIterations;
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

    public void setNbIterationsPerPhase(int nbIterationsPerPhase) {
        this.nbIterationsPerPhase = nbIterationsPerPhase;
    }

    public void setTabuTenure(int tabuTenure) {
        this.tabuTenure = tabuTenure;
    }

    public void setNbRandomMoves(int nbRandomMoves) {
        this.nbRandomMoves = nbRandomMoves;
    }

    public Solution Apply(Solution startingSolution, List<Request> requests, Random random) {
        ++searchCount;
        Solution currentSolution = startingSolution;
        Solution bestSolution = startingSolution;

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
            while (nbIterations < nbIterationsPerPhase * (diversificationCounter + 1)) {
                if (isInterrupted.get()) {
                    searchTime += (System.nanoTime() - phaseStartTime) / 1000000000.0;
                    return bestSolution;
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
                    } else if (tabuCounters[requestIndex] == 0) {
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

                assert bestTemporarySolution != null;
                bestTemporarySolution.insertRequest(requests.get(bestRequestIndex));
                currentSolution = bestTemporarySolution;
                if (DoubleComparator.lessThan(bestTemporarySolution.getCost(), bestTemporarySolution.getCost())) {
                    nbIterations = 0;
                    bestSolution = bestTemporarySolution;
                }

                if (bestTabuTemporarySolution != null) {
                    bestTabuTemporarySolution.insertRequest(requests.get(bestTabuRequestIndex));
                    if (DoubleComparator.lessThan(bestTabuTemporarySolution.getCost(), bestSolution.getCost())) {
                        nbIterations = 0;
                        currentSolution = bestTabuTemporarySolution;
                        bestSolution = bestTabuTemporarySolution;
                        bestRequestIndex = bestTabuRequestIndex;
                    }
                }

                tabuCounters[bestRequestIndex] = tabuTenure;

//                if ((nbIterations + pastIterations) % 10 == 0) {
//                    bestSolutionPercentages[(nbIterations + pastIterations) / 10 - 1] +=
//                            bestSolution.getCost() / startingSolution.getCost();
//                    currentSolutionPercentages[(nbIterations + pastIterations) / 10 - 1] +=
//                            currentSolution.getCost() / startingSolution.getCost();
//                }
            }

            searchTime += (System.nanoTime() - phaseStartTime) / 1000000000.0;

            if (diversificationCounter < nbDiversificationIterations) {
                if (currentSolution == bestSolution) {
                    currentSolution = new Solution(currentSolution);
                }

                for (int i = 0; i < nbRandomMoves; ++i) {
                    if (isInterrupted.get()) {
                        return bestSolution;
                    }

                    int requestIndexIndex = random.nextInt(validRequestIndices.size());
                    if (currentSolution.removeRequest(requests.get(validRequestIndices.get(requestIndexIndex))) > 0) {
                        currentSolution.insertRequestAtRandomPosition(
                                requests.get(validRequestIndices.get(requestIndexIndex)), random);

                        if (DoubleComparator.lessThan(currentSolution.getCost(), bestSolution.getCost())) {
                            bestSolution = currentSolution;
                        }
                    } else {
                        validRequestIndices.remove(requestIndexIndex);
                    }
                }

                //pastIterations += nbIterations;
                Arrays.fill(tabuCounters, 0);
            }
        }

        return bestSolution;
    }
}
