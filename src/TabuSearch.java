import java.util.List;

public class TabuSearch {
    private int totalNbIterations;
    private final int noImprovementStoppingCriteria;
    private final int tabuTenure;

    public TabuSearch(int noImprovementStoppingCriteria, int tabuTenure) {
        totalNbIterations = 0;
        this.noImprovementStoppingCriteria = noImprovementStoppingCriteria;
        this.tabuTenure = tabuTenure;
    }

    public int getTotalNbIterations() {
        return totalNbIterations;
    }

    public Solution Apply(Solution startingSolution, List<Request> requests) {
        Solution currentSolution = startingSolution;
        Solution bestSolution = startingSolution;

        int[] tabuCounters = new int[requests.size()];
        int noImprovementCounter = 0;

        do {
            Solution bestTemporarySolution = null;
            Solution bestTabuTemporarySolution = null;
            int bestRequestIndex = -1;
            int bestTabuRequestIndex = -1;

            ++noImprovementCounter;

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
                noImprovementCounter = 0;
            }

            if (bestTabuTemporarySolution != null) {
                bestTabuTemporarySolution.insertRequest(requests.get(bestTabuRequestIndex));
                if (DoubleComparator.lessThan(bestTabuTemporarySolution.getCost(), bestSolution.getCost())) {
                    currentSolution = bestTabuTemporarySolution;
                    bestSolution = bestTabuTemporarySolution;
                    bestRequestIndex = bestTabuRequestIndex;
                    noImprovementCounter = 0;
                }
            }

            tabuCounters[bestRequestIndex] = tabuTenure;

            ++totalNbIterations;
        } while (noImprovementCounter < noImprovementStoppingCriteria);

        return bestSolution;
    }
}
