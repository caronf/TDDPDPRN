import java.util.List;

public class TabuSearch {
    private final int noImprovementStoppingCriteria;
    private final int tabuTenure;

    public TabuSearch(int noImprovementStoppingCriteria, int tabuTenure) {
        this.noImprovementStoppingCriteria = noImprovementStoppingCriteria;
        this.tabuTenure = tabuTenure;
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
                Solution temporarySolution = new Solution(currentSolution);
                temporarySolution.removeRequest(requests.get(i));
                temporarySolution.insertRequest(requests.get(i));

                if (nextSolution == null ||
                        DoubleComparator.lessThan(temporarySolution.getCost(), nextSolution.getCost())) {
                    if (tabuCounters[i] == 0) {
                        nextSolution = temporarySolution;
                        tabuIndex = i;
                    }

                    if (DoubleComparator.lessThan(temporarySolution.getCost(), bestSolution.getCost())) {
                        nextSolution = temporarySolution;
                        tabuIndex = i;
                        bestSolution = temporarySolution;
                        noImprovementCounter = 0;
                    }
                }

                if (tabuCounters[i] > 0) {
                    --tabuCounters[i];
                }
            }

            tabuCounters[tabuIndex] = tabuTenure;
        } while (noImprovementCounter < noImprovementStoppingCriteria);

        return bestSolution;
    }
}
