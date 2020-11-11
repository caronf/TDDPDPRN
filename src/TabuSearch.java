import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TabuSearch {
    private int searchIterations;
    private double searchTime;
    private int stoppingCriteria;
    private int tabuTenure;
    private final int nbDiversificationIterations;
    private int nbRandomMoves;
//    private final double[] bestSolutionPercentages;
//    private final double[] currentSolutionPercentages;

    public TabuSearch(int nbDiversificationIterations) {
        searchIterations = 0;
        searchTime = 0;
        this.nbDiversificationIterations = nbDiversificationIterations;
//        bestSolutionPercentages = new double[stoppingCriteria * (nbDiversificationIterations + 1) / 10];
//        currentSolutionPercentages = new double[stoppingCriteria * (nbDiversificationIterations + 1) / 10];
    }

    public double getTimePerIteration() {
        return searchTime / searchIterations;
    }

    public void resetSearchIterations() {
        searchIterations = 0;
        searchTime = 0.0;
    }

//    public double[] getBestSolutionPercentages() {
//        return bestSolutionPercentages;
//    }
//
//    public double[] getCurrentSolutionPercentages() {
//        return currentSolutionPercentages;
//    }

    public void setStoppingCriteria(int stoppingCriteria) {
        this.stoppingCriteria = stoppingCriteria;
    }

    public void setTabuTenure(int tabuTenure) {
        this.tabuTenure = tabuTenure;
    }

    public void setNbRandomMoves(int nbRandomMoves) {
        this.nbRandomMoves = nbRandomMoves;
    }

    public Solution Apply(Solution startingSolution, List<Request> requests, Random random) {
        Solution currentSolution = startingSolution;
        Solution bestSolution = startingSolution;

        ArrayList<Integer> validRequestIndices = new ArrayList<>(requests.size());
        for (int i = 0; i < requests.size(); ++i) {
            validRequestIndices.add(i);
        }

        int[] tabuCounters = new int[requests.size()];
        int pastIterations = 0;

        for (int diversificationCounter = 0;
             diversificationCounter <= nbDiversificationIterations;
             ++diversificationCounter) {
            int nbIterations = 0;
            double searchStartTime = System.nanoTime();
            while (nbIterations < stoppingCriteria) {
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

                assert bestTemporarySolution != null;
                bestTemporarySolution.insertRequest(requests.get(bestRequestIndex));
                currentSolution = bestTemporarySolution;
                if (DoubleComparator.lessThan(bestTemporarySolution.getCost(), bestTemporarySolution.getCost())) {
                    bestSolution = bestTemporarySolution;
                }

                if (bestTabuTemporarySolution != null) {
                    bestTabuTemporarySolution.insertRequest(requests.get(bestTabuRequestIndex));
                    if (DoubleComparator.lessThan(bestTabuTemporarySolution.getCost(), bestSolution.getCost())) {
                        currentSolution = bestTabuTemporarySolution;
                        bestSolution = bestTabuTemporarySolution;
                        bestRequestIndex = bestTabuRequestIndex;
                    }
                }

                tabuCounters[bestRequestIndex] = tabuTenure;

                ++nbIterations;
//                if ((nbIterations + pastIterations) % 10 == 0) {
//                    bestSolutionPercentages[(nbIterations + pastIterations) / 10 - 1] +=
//                            bestSolution.getCost() / startingSolution.getCost();
//                    currentSolutionPercentages[(nbIterations + pastIterations) / 10 - 1] +=
//                            currentSolution.getCost() / startingSolution.getCost();
//                }
            }

            searchIterations += nbIterations;
            searchTime += (System.nanoTime() - searchStartTime) / 1000000000.0;

            if (diversificationCounter < nbDiversificationIterations) {
                if (currentSolution == bestSolution) {
                    currentSolution = new Solution(currentSolution);
                }

                for (int i = 0; i < nbRandomMoves; ++i) {
                    int requestIndexIndex = random.nextInt(validRequestIndices.size());
                    currentSolution.removeRequest(requests.get(validRequestIndices.get(requestIndexIndex)));
                    currentSolution.insertRequestAtRandomPosition(
                            requests.get(validRequestIndices.get(requestIndexIndex)), random);
                    pastIterations += nbIterations;
                }

                Arrays.fill(tabuCounters, 0);
            }
        }

        return bestSolution;
    }
}
