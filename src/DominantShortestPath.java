import java.util.ArrayList;
import java.util.HashSet;

public class DominantShortestPath extends ArrivalTimeCalculator {
    // Known (departure time, arrival time) pairs for the dominant shortest path
    ArrayList<double[]> points;

    // bestPaths[i] : index of the best path between points i and i+1
    ArrayList<Integer> bestPaths;

    // Complete paths (may be useful to get an itinerary)
    ArrayList<ArrayList<Integer>> paths;

    public DominantShortestPath(ArrayList<ArrayList<Integer>> paths, double[] proposedDepartureTimes,
                                ArrayList<ArrayList<Double>> arrivalTimes) {
        this.paths = paths;

        // We will have at least proposedDepartureTimes.length points
        points = new ArrayList<>(proposedDepartureTimes.length);
        bestPaths = new ArrayList<>(proposedDepartureTimes.length);

        int currentBestPath = 0;
        for(int k = 0; k < proposedDepartureTimes.length - 1; ++k) {
            double currentSlope =
                    (arrivalTimes.get(currentBestPath).get(k + 1) - arrivalTimes.get(currentBestPath).get(k)) /
                    (proposedDepartureTimes[k + 1] - proposedDepartureTimes[k]);

            // Look for a better path which may be different from the last iteration because of the new slopes
            for (int j = 0; j < paths.size(); ++j) {
                if (j != currentBestPath && arrivalTimes.get(j).get(k) <= arrivalTimes.get(currentBestPath).get(k)) {
                    double otherSlope = (arrivalTimes.get(j).get(k + 1) - arrivalTimes.get(j).get(k)) /
                            (proposedDepartureTimes[k + 1] - proposedDepartureTimes[k]);
                    if (arrivalTimes.get(j).get(k) < arrivalTimes.get(currentBestPath).get(k) ||
                            otherSlope < currentSlope) {
                        currentBestPath = j;
                        currentSlope = otherSlope;
                    }
                }
            }

            points.add(new double[] {proposedDepartureTimes[k], arrivalTimes.get(currentBestPath).get(k)});
            bestPaths.add(currentBestPath);

            // Find all intersections between the current point and the next proposed departure time
            while (true) {
                int nextIntersectingPath = -1;
                double nextSlope = Double.MIN_VALUE;
                double nextIntersectionX = proposedDepartureTimes[k + 1] - proposedDepartureTimes[k];

                // Find the next intersection with the current path
                // If multiple paths intersect at the same point, the one with the mellowest slope will be selected
                for (int j = 0; j < paths.size(); ++j) {
                    if (j != currentBestPath) {
                        double otherSlope = (arrivalTimes.get(j).get(k + 1) - arrivalTimes.get(j).get(k)) /
                                (proposedDepartureTimes[k + 1] - proposedDepartureTimes[k]);
                        double intersectionX = (arrivalTimes.get(j).get(k) - arrivalTimes.get(currentBestPath).get(k)) /
                                (currentSlope - otherSlope);
                        if (intersectionX + proposedDepartureTimes[k] > points.get(points.size() - 1)[0] &&
                                (intersectionX < nextIntersectionX ||
                                intersectionX == nextIntersectionX && otherSlope < nextSlope)) {
                            nextIntersectingPath = j;
                            nextSlope = otherSlope;
                            nextIntersectionX = intersectionX;
                        }
                    }
                }

                if (nextIntersectingPath == -1) {
                    // No more intersections before the next proposed departure time
                    break;
                }

                points.add(new double[] {nextIntersectionX + proposedDepartureTimes[k],
                        arrivalTimes.get(nextIntersectingPath).get(k) + nextSlope * nextIntersectionX});
                bestPaths.add(nextIntersectingPath);
                currentBestPath = nextIntersectingPath;
                currentSlope = nextSlope;
            }
        }

        points.add(new double[] {proposedDepartureTimes[proposedDepartureTimes.length - 1],
                arrivalTimes.get(currentBestPath).get(proposedDepartureTimes.length - 1)});
    }

    @Override
    public double getArrivalTime(double departureTime) {
        int k = 1;
        double departureTimeRemainder = departureTime % points.get(points.size() - 1)[0];
        while (points.get(k)[0] <= departureTimeRemainder) {
            ++k;
        }

        // Perform a linear interpolation between points k-1 and k
        double arrivalTime = points.get(k-1)[1] + (departureTimeRemainder - points.get(k - 1)[0]) *
                (points.get(k)[1] - points.get(k - 1)[1]) / (points.get(k)[0] - points.get(k - 1)[0]);
        arrivalTime += departureTime - departureTimeRemainder;
        assert arrivalTime >= departureTime;
        return arrivalTime;
    }

    public static ArrivalTimeCalculator[][] getDominantShortestPaths(InputData inputData) {
        int nbNodes = inputData.distanceMatrix.length;
        ArrivalTimeCalculator[][] dominantShortestPaths = new ArrivalTimeCalculator[nbNodes][nbNodes];

        for (int departureNode = 0; departureNode < nbNodes; ++departureNode) {
            // pathsPerDestination[i][j] : Path j to destination i
            ArrayList<ArrayList<ArrayList<Integer>>> pathsPerDestination = new ArrayList<>(nbNodes);
            // arrivalTimesPerPath[i][j][k] : Arrival time at destination i using path j leaving at proposedDepartTime[k]
            ArrayList<ArrayList<ArrayList<Double>>> arrivalTimesPerPath = new ArrayList<>(nbNodes);
            for (int i = 0; i < nbNodes; ++i) {
                pathsPerDestination.add(new ArrayList<>());
                arrivalTimesPerPath.add(new ArrayList<>());
            }

            // Add the path from departure node to departure node
            pathsPerDestination.get(departureNode).add(new ArrayList<>());
            pathsPerDestination.get(departureNode).get(0).add(departureNode);

            // arrivalTimes[i][j][k] : Arrival time at destination i using path j leaving at proposedDepartTime[k]
            //double[][][] arrivalTimes = new double[nbNodes][inputData.proposedDepartTime.length][inputData.proposedDepartTime.length];
            double[] arrivalTimes = new double[nbNodes];
            int[] previousNodes = new int[nbNodes];
            int[] pathIndices = new int[nbNodes];
            HashSet<Integer> nodesToVisit = new HashSet<>(nbNodes);

            for (double departureTime : inputData.proposedDepartTime) {
                // Calculate the best path from departureNode to every other node
                // when leaving at departureTime using Dijkstra's algorithm
                for (int i = 0; i < nbNodes; i++) {
                    arrivalTimes[i] = Double.MAX_VALUE;
                    previousNodes[i] = -1;
                    nodesToVisit.add(i);
                }

                nodesToVisit.remove(departureNode);
                arrivalTimes[departureNode] = departureTime;
                ArrayList<Integer> departureNodePath = new ArrayList<>();
                departureNodePath.add(departureNode);
                pathsPerDestination.get(departureNode).add(departureNodePath);
                pathIndices[departureNode] = 0;

                int nextNode = departureNode;
                while (!nodesToVisit.isEmpty()) {
                    if (nextNode == -1) {
                        // Find the node to visit next
                        for (int i : nodesToVisit) {
                            if (nextNode < 0 || arrivalTimes[i] < arrivalTimes[nextNode]) {
                                nextNode = i;
                            }
                        }

                        // Save the path to the next node
                        nodesToVisit.remove(nextNode);
                        int previousNode = previousNodes[nextNode];
                        ArrayList<Integer> nextNodePath = new ArrayList<>(
                                pathsPerDestination.get(previousNode).get(pathIndices[previousNode]));
                        nextNodePath.add(nextNode);

                        pathIndices[nextNode] = pathsPerDestination.get(nextNode).indexOf(nextNodePath);
                        if (pathIndices[nextNode] == -1) {
                            pathsPerDestination.get(nextNode).add(nextNodePath);
                            pathIndices[nextNode] = pathsPerDestination.get(nextNode).size() - 1;
                            arrivalTimesPerPath.get(nextNode).add(new ArrayList<>(inputData.proposedDepartTime.length));

                            // Calculate the arrival time when using the path at other times
                            for (double proposedDepartureTime : inputData.proposedDepartTime) {
                                double arrivalTime;
                                if (proposedDepartureTime == departureTime) {
                                    arrivalTime = arrivalTimes[nextNode];
                                }
                                else {
                                    arrivalTime = proposedDepartureTime;
                                    for (int i = 0; i < nextNodePath.size() - 1; ++i) {
                                        arrivalTime = getNeighborArrivalTime(
                                                inputData.distanceMatrix[nextNodePath.get(i)][nextNodePath.get(i + 1)],
                                                arrivalTime,
                                                inputData.speedFunctionList.get(
                                                        inputData.speedFunctionMatrix[nextNodePath.get(i)][nextNodePath.get(i + 1)] - 1));
                                    }
                                }

                                arrivalTimesPerPath.get(nextNode).get(pathIndices[nextNode]).add(arrivalTime);
                            }
                        }
                    }

                    // Update the arrival times of unvisited neighbors
                    for (int i : nodesToVisit) {
                        if(inputData.distanceMatrix[nextNode][i] > 0) {
                            double arrivalTime = getNeighborArrivalTime(inputData.distanceMatrix[nextNode][i],
                                    arrivalTimes[nextNode],
                                    inputData.speedFunctionList.get(inputData.speedFunctionMatrix[nextNode][i] - 1));

                            if (arrivalTime < arrivalTimes[i]) {
                                arrivalTimes[i] = arrivalTime;
                                previousNodes[i] = nextNode;
                            }
                        }
                    }

                    nextNode = -1;
                }
            }

            for (int arrivalNode = 0; arrivalNode < nbNodes; ++arrivalNode) {
                if (departureNode == arrivalNode) {
                    dominantShortestPaths[departureNode][arrivalNode] = new ImmediateArrivalTimeCalculator();
                }
                else {
                    dominantShortestPaths[departureNode][arrivalNode] = new DominantShortestPath(
                            pathsPerDestination.get(arrivalNode), inputData.proposedDepartTime,
                            arrivalTimesPerPath.get(arrivalNode));
                }
            }
        }

        return dominantShortestPaths;
    }

    private static double getNeighborArrivalTime(double distance, double departureTime, double [][] speedFunction){
        double endOfTheDay = speedFunction[speedFunction.length - 1][0];
        double currentTime = departureTime % endOfTheDay;
        double distanceTravelled = 0.0;
        int nbCompleteDays = (int) (departureTime / endOfTheDay);
        int currentStep = 0;

        // Find the initial step in the speed function
        while(currentTime > speedFunction[currentStep][0]) {
            ++currentStep;
        }

        // Find the final step in the speed function
        double distanceInCurrentStep = (speedFunction[currentStep][0] - currentTime) * speedFunction[currentStep][1];
        while(distanceTravelled + distanceInCurrentStep < distance){
            distanceTravelled = distanceTravelled + distanceInCurrentStep;

            if(currentStep == speedFunction.length - 1){
                currentTime = 0.0;
                currentStep = 0;
                ++nbCompleteDays;
            } else {
                currentTime = speedFunction[currentStep][0];
                ++currentStep;
            }

            distanceInCurrentStep = (speedFunction[currentStep][0] - currentTime) * speedFunction[currentStep][1];
        }

        currentTime += (distance - distanceTravelled) / speedFunction[currentStep][1];
        currentTime += nbCompleteDays * endOfTheDay;
        assert currentTime >= departureTime;
        return currentTime;
    }
}
