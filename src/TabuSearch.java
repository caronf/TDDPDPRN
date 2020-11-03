import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TabuSearch {
    private int totalNbIterations;
    private int stoppingCriteria;
    private int tabuTenure;
    private final int nbDiversificationIterations;
    private int nbRandomMoves;
//    private final double[] bestSolutionPercentages;
//    private final double[] currentSolutionPercentages;

    public TabuSearch(int nbDiversificationIterations) {
        totalNbIterations = 0;
        this.nbDiversificationIterations = nbDiversificationIterations;
//        bestSolutionPercentages = new double[stoppingCriteria * (nbDiversificationIterations + 1) / 10];
//        currentSolutionPercentages = new double[stoppingCriteria * (nbDiversificationIterations + 1) / 10];
    }

    public int getTotalNbIterations() {
        return totalNbIterations;
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

        int[] tabuCounters = new int[requests.size()];
        int pastIterations = 0;

        for (int diversificationCounter = 0;
             diversificationCounter <= nbDiversificationIterations;
             ++diversificationCounter) {
            int nbIterations = 0;
            while (nbIterations < stoppingCriteria) {
                Solution bestTemporarySolution = null;
                Solution bestTabuTemporarySolution = null;
                int bestRequestIndex = -1;
                int bestTabuRequestIndex = -1;

                for (int i = 0; i < requests.size(); ++i) {
                    Solution temporarySolution = new Solution(currentSolution);
                    temporarySolution.removeRequest(requests.get(i));

                    if (tabuCounters[i] == 0) {
                        if (bestTemporarySolution == null ||
                                DoubleComparator.lessThan(temporarySolution.getCost(), bestTemporarySolution.getCost())) {
                            bestTemporarySolution = temporarySolution;
                            bestRequestIndex = i;
                        }
                    } else {
                        if (bestTabuTemporarySolution == null || DoubleComparator.lessThan(temporarySolution.getCost(),
                                bestTabuTemporarySolution.getCost())) {
                            bestTabuTemporarySolution = temporarySolution;
                            bestTabuRequestIndex = i;
                        }

                        --tabuCounters[i];
                    }
                }

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

                ++totalNbIterations;
                ++nbIterations;
//                if ((nbIterations + pastIterations) % 10 == 0) {
//                    bestSolutionPercentages[(nbIterations + pastIterations) / 10 - 1] +=
//                            bestSolution.getCost() / startingSolution.getCost();
//                    currentSolutionPercentages[(nbIterations + pastIterations) / 10 - 1] +=
//                            currentSolution.getCost() / startingSolution.getCost();
//                }
            }

            if (diversificationCounter < nbDiversificationIterations) {
                if (currentSolution == bestSolution) {
                    currentSolution = new Solution(currentSolution);
                }

                for (int i = 0; i < nbRandomMoves; ++i) {
                    int requestIndex;
                    do {
                        requestIndex = random.nextInt(requests.size());
                    } while(currentSolution.removeRequest(requests.get(requestIndex)) == 0);

                    currentSolution.insertRequestAtRandomPosition(requests.get(requestIndex), random);
                    pastIterations += nbIterations;
                }

                Arrays.fill(tabuCounters, 0);
            }
        }

        return bestSolution;
    }
}
