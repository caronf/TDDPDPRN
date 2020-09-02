import java.util.List;

public class TabuSearch {
    public int totalNbIterations;
    private int noImprovementStoppingCriteria;
    private boolean useTabus;

    public TabuSearch(int noImprovementStoppingCriteria, boolean useTabus) {
        totalNbIterations = 0;
        this.noImprovementStoppingCriteria = noImprovementStoppingCriteria;
        this.useTabus = useTabus;
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
            int tabuIndex = -1;

            ++noImprovementCounter;

            // Search for the best solution in the neighborhood
            for (int i = 0; i < requests.size(); ++i) {
                if (tabuCounters[i] > 0) {
                    --tabuCounters[i];
                } else {
                    Solution temporarySolution = new Solution(currentSolution);
                    temporarySolution.removeRequest(requests.get(i));
                    temporarySolution.insertRequest(requests.get(i));

                    if (nextSolution == null ||
                            DoubleComparator.lessThan(temporarySolution.getCost(), nextSolution.getCost())) {
                        nextSolution = temporarySolution;
                        tabuIndex = i;
                        if (DoubleComparator.lessThan(temporarySolution.getCost(), bestSolution.getCost())) {
                            bestSolution = temporarySolution;
                            noImprovementCounter = 0;
                        }
                    }
                }
            }

            if (useTabus) {
                tabuCounters[tabuIndex] += requests.size() / 2;
            }

            ++totalNbIterations;
        } while (noImprovementCounter < noImprovementStoppingCriteria);

        return bestSolution;
    }
}
