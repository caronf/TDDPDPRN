public class TabuSearch {
    public int totalNbIterations;
    public Solution Apply(Solution startingSolution, Iterable<Request> requests) {
        Solution previousBestSolution;
        Solution bestSolution = startingSolution;

        do {
            previousBestSolution = bestSolution;

            // Search neighborhood for a better solution
            for (Request request : requests) {
                Solution temporarySolution = new Solution(previousBestSolution);
                temporarySolution.removeRequest(request);
                temporarySolution.insertRequest(request);

                assert DoubleComparator.lessOrEqual(temporarySolution.getCost(), previousBestSolution.getCost());
                if (DoubleComparator.lessThan(temporarySolution.getCost(), bestSolution.getCost())) {
                    bestSolution = temporarySolution;
                }
            }

            ++totalNbIterations;
        } while (previousBestSolution != bestSolution);

        return bestSolution;
    }
}
