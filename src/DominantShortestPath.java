import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DominantShortestPath extends PiecewiseArrivalTimeFunction {
    // bestPaths[i] : index of the best path between points i and i+1
    ArrayList<Integer> bestPaths;

    // Complete paths (may be useful to get an itinerary)
    ArrayList<ArrayList<Integer>> paths;

    public DominantShortestPath(ArrayList<ArrayList<Integer>> paths,
                                HashMap<Integer, HashMap<Integer, ArrivalTimeFunction>> arcArrivalTimeFunctions) {
        this.paths = paths;
        bestPaths = new ArrayList<>();

        PiecewiseArrivalTimeFunction[] arrivalTimeFunctions = new PiecewiseArrivalTimeFunction[paths.size()];
        int bestPathIndex = -1;
        double bestPathSlope = 0.0;

        for (int i = 0; i < paths.size(); ++i) {
            ArrayList<Integer> path = paths.get(i);
            assert path.size() >= 2;
            arrivalTimeFunctions[i] =
                    (PiecewiseArrivalTimeFunction) arcArrivalTimeFunctions.get(path.get(0)).get(path.get(1));
            for (int j = 2; j < path.size(); ++j) {
                arrivalTimeFunctions[i] = new PiecewiseArrivalTimeFunction(arrivalTimeFunctions[i],
                        (PiecewiseArrivalTimeFunction) arcArrivalTimeFunctions.get(path.get(j - 1)).get(path.get(j)));
            }

            assert DoubleComparator.equal(arrivalTimeFunctions[i].points.get(0)[0], 0.0);
            double pathSlope = (arrivalTimeFunctions[i].points.get(1)[1] - arrivalTimeFunctions[i].points.get(0)[1]) /
                    arrivalTimeFunctions[i].points.get(1)[0];
            if (bestPathIndex == -1 ||
                    DoubleComparator.lessThan(arrivalTimeFunctions[i].points.get(0)[1],
                            arrivalTimeFunctions[bestPathIndex].points.get(0)[1]) ||
                    DoubleComparator.equal(arrivalTimeFunctions[i].points.get(0)[1],
                            arrivalTimeFunctions[bestPathIndex].points.get(0)[1]) && pathSlope < bestPathSlope) {
                bestPathIndex = i;
                bestPathSlope = pathSlope;
            }
        }

        points.add(arrivalTimeFunctions[bestPathIndex].points.get(0));
        bestPaths.add(bestPathIndex);

        int[] stepPerPath = new int[paths.size()];
        double bestPathOrigin = arrivalTimeFunctions[bestPathIndex].points.get(0)[1] -
                bestPathSlope * arrivalTimeFunctions[bestPathIndex].points.get(0)[0];

        while (stepPerPath[bestPathIndex] < arrivalTimeFunctions[bestPathIndex].points.size() - 1) {
            int nextIntersectingPath = -1;
            double nextIntersectionX =
                    arrivalTimeFunctions[bestPathIndex].points.get(stepPerPath[bestPathIndex] + 1)[0];
            double nextSlope = Double.MIN_VALUE;
            double nextOrigin = 0.0;
            int nextStep = -1;

            if (stepPerPath[bestPathIndex] < arrivalTimeFunctions[bestPathIndex].points.size() - 2) {
                nextSlope = (arrivalTimeFunctions[bestPathIndex].points.get(stepPerPath[bestPathIndex] + 2)[1] -
                        arrivalTimeFunctions[bestPathIndex].points.get(stepPerPath[bestPathIndex] + 1)[1]) /
                        (arrivalTimeFunctions[bestPathIndex].points.get(stepPerPath[bestPathIndex] + 2)[0] -
                                arrivalTimeFunctions[bestPathIndex].points.get(stepPerPath[bestPathIndex] + 1)[0]);
                nextOrigin = arrivalTimeFunctions[bestPathIndex].points.get(stepPerPath[bestPathIndex] + 1)[1] -
                        nextSlope * arrivalTimeFunctions[bestPathIndex].points.get(stepPerPath[bestPathIndex] + 1)[0];
            }

            // Find the next intersection with the current path
            // If multiple paths intersect at the same point, the one with the mellowest slope will be selected
            for (int i = 0; i < paths.size(); ++i) {
                if (i != bestPathIndex) {
                    while (DoubleComparator.lessThan(arrivalTimeFunctions[i].points.get(stepPerPath[i] + 1)[0],
                            points.get(points.size() - 1)[0])) {
                        ++stepPerPath[i];
                    }

                    for (int j = stepPerPath[i]; j < arrivalTimeFunctions[i].points.size() - 1 &&
                            DoubleComparator.lessOrEqual(arrivalTimeFunctions[i].points.get(j)[0], nextIntersectionX);
                         ++j) {
                        double slope = (arrivalTimeFunctions[i].points.get(j + 1)[1] -
                                        arrivalTimeFunctions[i].points.get(j)[1]) /
                                (arrivalTimeFunctions[i].points.get(j + 1)[0] -
                                        arrivalTimeFunctions[i].points.get(j)[0]);
                        double origin = arrivalTimeFunctions[i].points.get(j)[1] -
                                slope * arrivalTimeFunctions[i].points.get(j)[0];
                        double intersectionX = -1.0;

                        if (!DoubleComparator.equal(bestPathSlope, slope)) {
                            intersectionX = (origin - bestPathOrigin) / (bestPathSlope - slope);
                        } else if (DoubleComparator.equal(bestPathOrigin, origin)) {
                            intersectionX =
                                    arrivalTimeFunctions[bestPathIndex].points.get(stepPerPath[bestPathIndex] + 1)[0];
                        }

                        if (DoubleComparator.greaterThan(intersectionX, points.get(points.size() - 1)[0]) &&
                                DoubleComparator.greaterOrEqual(intersectionX,
                                        arrivalTimeFunctions[i].points.get(j)[0]) &&
                                DoubleComparator.lessThan(intersectionX,
                                        arrivalTimeFunctions[i].points.get(j + 1)[0]) &&
                                (DoubleComparator.lessThan(intersectionX, nextIntersectionX) &&
                                                DoubleComparator.lessOrEqual(slope, bestPathSlope) ||
                                        DoubleComparator.equal(intersectionX, nextIntersectionX) &&
                                                DoubleComparator.lessThan(slope, nextSlope))) {
                            nextIntersectingPath = i;
                            nextSlope = slope;
                            nextOrigin = origin;
                            nextIntersectionX = intersectionX;
                            nextStep = j;
                        }
                    }
                }
            }

            if (nextIntersectingPath == -1) {
                // No intersection was found before the next point
                points.add(arrivalTimeFunctions[bestPathIndex].points.get(stepPerPath[bestPathIndex] + 1));
                ++stepPerPath[bestPathIndex];
            } else {
                bestPathIndex = nextIntersectingPath;
                points.add(new double[] {nextIntersectionX,
                        arrivalTimeFunctions[bestPathIndex].getArrivalTime(nextIntersectionX)});
                stepPerPath[bestPathIndex] = nextStep;
            }

            bestPaths.add(bestPathIndex);
            bestPathSlope = nextSlope;
            bestPathOrigin = nextOrigin;

            for (int i = 0; i < paths.size(); ++i) {
                assert DoubleComparator.lessOrEqual(
                        arrivalTimeFunctions[bestPathIndex].getArrivalTime(nextIntersectionX),
                        arrivalTimeFunctions[i].getArrivalTime(nextIntersectionX));
            }
        }

        // The arrival time should correspond to the best path for any departure time
//        for (double departureTime = 0.0;
//             DoubleComparator.lessOrEqual(departureTime, points.get(points.size() - 1)[0] * 3);
//             departureTime += ThreadLocalRandom.current().nextDouble(1.0, 20.0)) {
//            double arrivalTime1 = Double.MAX_VALUE;
//            for (int i = 0; i < paths.size(); ++i) {
//                arrivalTime1 = Math.min(arrivalTime1, arrivalTimeFunctions[i].getArrivalTime(departureTime));
//            }
//            double arrivalTime2 = getArrivalTime(departureTime);
//            assert DoubleComparator.equal(arrivalTime1, arrivalTime2);
//        }
    }

    @Override
    public int getSize() {
        int pathsSize = 0;
        for (ArrayList<Integer> path : paths) {
            pathsSize += path.size() * Integer.BYTES;
        }

        return super.getSize() + bestPaths.size() * Integer.BYTES + pathsSize;
    }

    public static ArrivalTimeFunction[][] getDominantShortestPaths(InputData inputData, boolean useAverageTravelTimes,
                                                                   double[] departureTimes) {
        ArrivalTimeFunction[][] dominantShortestPaths = new ArrivalTimeFunction[inputData.nbNodes][inputData.nbNodes];

        for (int departureNode = 0; departureNode < inputData.nbNodes; ++departureNode) {
            // pathsPerDestination[i][j] : Path j to destination i
            ArrayList<ArrayList<ArrayList<Integer>>> pathsPerDestination = new ArrayList<>(inputData.nbNodes);
            for (int i = 0; i < inputData.nbNodes; ++i) {
                pathsPerDestination.add(new ArrayList<>());
            }

            // Add the path from departure node to departure node
            pathsPerDestination.get(departureNode).add(new ArrayList<>());
            pathsPerDestination.get(departureNode).get(0).add(departureNode);

            double[] arrivalTimes = new double[inputData.nbNodes];
            int[] previousNodes = new int[inputData.nbNodes];
            int[] pathIndices = new int[inputData.nbNodes];
            HashSet<Integer> nodesToVisit = new HashSet<>(inputData.nbNodes);

            for (double departureTime : departureTimes) {
                // Calculate the best path from departureNode to every other node
                // when leaving at departureTime using Dijkstra's algorithm
                for (int i = 0; i < inputData.nbNodes; i++) {
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
                            if (nextNode < 0 || DoubleComparator.lessThan(arrivalTimes[i], arrivalTimes[nextNode])) {
                                nextNode = i;
                            }
                        }
                        assert arrivalTimes[nextNode] < Double.MAX_VALUE;

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
                        }
                    }

                    // Update the arrival times of unvisited neighbors
                    if (inputData.arcArrivalTimeFunctions.containsKey(nextNode)) {
                        for (HashMap.Entry<Integer, ArrivalTimeFunction> entry :
                                inputData.arcArrivalTimeFunctions.get(nextNode).entrySet()) {
                            if (nodesToVisit.contains(entry.getKey())) {
                                double arrivalTime;
                                if (useAverageTravelTimes) {
                                    arrivalTime = arrivalTimes[nextNode] +
                                            inputData.averageTravelTimes.get(nextNode).get(entry.getKey());
                                } else {
                                    arrivalTime = entry.getValue().getArrivalTime(arrivalTimes[nextNode]);
                                }

                                if (DoubleComparator.lessThan(arrivalTime, arrivalTimes[entry.getKey()])) {
                                    arrivalTimes[entry.getKey()] = arrivalTime;
                                    previousNodes[entry.getKey()] = nextNode;
                                }
                            }
                        }
                    }

                    nextNode = -1;
                }
            }

            for (int arrivalNode = 0; arrivalNode < inputData.nbNodes; ++arrivalNode) {
                if (departureNode == arrivalNode) {
                    dominantShortestPaths[departureNode][arrivalNode] = new ImmediateArrivalTimeFunction();
                } else {
                    dominantShortestPaths[departureNode][arrivalNode] = new DominantShortestPath(
                            pathsPerDestination.get(arrivalNode), inputData.arcArrivalTimeFunctions);
                }
            }
        }

        return dominantShortestPaths;
    }
}
