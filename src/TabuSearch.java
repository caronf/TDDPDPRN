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
        Solution currentSolution;
        Solution nextSolution = startingSolution;
        Solution bestSolution = startingSolution;

        int[] tabuCounters = new int[requests.size()];
        int noImprovementCounter = 0;

        do {
            currentSolution = nextSolution;
            nextSolution = null;
            int requestIndex = -1;

            ++noImprovementCounter;

            for (int i = 0; i < requests.size(); ++i) {
                if (tabuCounters[i] == 0) {
                    Solution temporarySolution = new Solution(currentSolution);
                    temporarySolution.removeRequest(requests.get(i));
                    if (nextSolution == null ||
                            DoubleComparator.lessThan(temporarySolution.getCost(), nextSolution.getCost())) {
                        nextSolution = temporarySolution;
                        requestIndex = i;
                    }
                }
            }

            assert nextSolution != null;
            double costBeforeReinsertion = nextSolution.getCost();
            nextSolution.insertRequest(requests.get(requestIndex));
            if (DoubleComparator.lessThan(nextSolution.getCost(), bestSolution.getCost())) {
                bestSolution = nextSolution;
                noImprovementCounter = 0;
            }

            for (int i = 0; i < requests.size(); ++i) {
                if (tabuCounters[i] > 0) {
                    Solution temporarySolution = new Solution(currentSolution);
                    temporarySolution.removeRequest(requests.get(i));
                    if (DoubleComparator.lessThan(temporarySolution.getCost(), costBeforeReinsertion)) {
                        temporarySolution.insertRequest(requests.get(i));
                        if (DoubleComparator.lessThan(temporarySolution.getCost(), bestSolution.getCost())) {
                            nextSolution = temporarySolution;
                            requestIndex = i;
                            bestSolution = temporarySolution;
                            noImprovementCounter = 0;
                        }
                    }

                    --tabuCounters[i];
                }
            }

            tabuCounters[requestIndex] = tabuTenure;

            ++totalNbIterations;
        } while (noImprovementCounter < noImprovementStoppingCriteria);

        return bestSolution;
    }
}
