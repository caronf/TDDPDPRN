import java.util.ArrayList;
import java.util.Collection;

public class Solution {
    private final ArrayList<Route> routes;
    private double cost;
    private double travelTime;
    private double lateness;

    // It's not necessary to test insertion in all routes when many of them are still empty
    private int lastRouteToTest;

    public Solution(int nbRoutes, ArrivalTimeFunction[][] arrivalTimeFunctions,
                    double depotTimeWindowUpperBound, double latenessWeight, double vehicleCapacity) {
        routes = new ArrayList<>(nbRoutes);
        cost = 0.0;
        lastRouteToTest = 0;

        for (int i = 0; i < nbRoutes; ++i) {
            routes.add(new Route(arrivalTimeFunctions, depotTimeWindowUpperBound, latenessWeight, vehicleCapacity));
            cost += routes.get(i).getCost();
        }
    }

    public void insertRequestsAnyOrder(Iterable<Request> requests) {
        for (Request request : requests) {
            double bestCostIncrease = Double.MAX_VALUE;
            Route bestRoute = null;
            int bestRouteIndex = -1;

            for (int i = 0; i <= lastRouteToTest; ++i) {
                Route newRoute = routes.get(i).getRouteAfterInsertion(request);
                double costIncrease = newRoute.getCost() - routes.get(i).getCost();
                if (costIncrease < bestCostIncrease) {
                    bestCostIncrease = costIncrease;
                    bestRoute = newRoute;
                    bestRouteIndex = i;
                }
            }

            assert bestRoute != null;
            travelTime += bestRoute.getTravelTime() - routes.get(bestRouteIndex).getTravelTime();
            lateness += bestRoute.getLateness() - routes.get(bestRouteIndex).getLateness();
            routes.set(bestRouteIndex, bestRoute);
            cost += bestCostIncrease;
            if (bestRouteIndex == lastRouteToTest && bestRouteIndex < routes.size() - 1) {
                ++lastRouteToTest;
            }
        }
    }

    public void insertRequestsBestFirst(Collection<Request> requests) {
        ArrayList<Request> requestsToInsert = new ArrayList<>(requests);
        ArrayList<ArrayList<Double>> costIncreaseMatrix = new ArrayList<>(requests.size());
        ArrayList<ArrayList<Route>> newRoutesMatrix = new ArrayList<>(requests.size());

        // Calculate cost increase for each pair of request/route
        for (Request request : requests) {
            ArrayList<Double> costIncreasesForRequest = new ArrayList<>(routes.size());
            ArrayList<Route> newRoutesForRequest = new ArrayList<>(routes.size());

            for (Route route : routes) {
                Route newRoute = route.getRouteAfterInsertion(request);
                costIncreasesForRequest.add(newRoute.getCost() - route.getCost());
                newRoutesForRequest.add(newRoute);
            }

            costIncreaseMatrix.add(costIncreasesForRequest);
            newRoutesMatrix.add(newRoutesForRequest);
        }

        while (!requestsToInsert.isEmpty()) {
            int bestRequestIndex = 0;
            int bestRouteIndex = 0;

            // Find the smallest increase in cost
            for (int i = 0; i < requestsToInsert.size(); ++i) {
                for (int j = 0; j < routes.size(); ++j) {
                    if (costIncreaseMatrix.get(i).get(j) <
                            costIncreaseMatrix.get(bestRequestIndex).get(bestRouteIndex)) {
                        bestRequestIndex = i;
                        bestRouteIndex = j;
                    }
                }
            }

            // Perform the insertion with minimum cost increase
            travelTime += newRoutesMatrix.get(bestRequestIndex).get(bestRouteIndex).getTravelTime() -
                    routes.get(bestRouteIndex).getTravelTime();
            lateness += newRoutesMatrix.get(bestRequestIndex).get(bestRouteIndex).getLateness() -
                    routes.get(bestRouteIndex).getLateness();
            routes.set(bestRouteIndex, newRoutesMatrix.get(bestRequestIndex).get(bestRouteIndex));
            cost += costIncreaseMatrix.get(bestRequestIndex).get(bestRouteIndex);
            costIncreaseMatrix.remove(bestRequestIndex);
            newRoutesMatrix.remove(bestRequestIndex);
            requestsToInsert.remove(bestRequestIndex);

            // Update the insertion cost of every remaining request for this route
            for (int i = 0; i < requestsToInsert.size(); ++i) {
                Route newRoute = routes.get(bestRouteIndex).getRouteAfterInsertion(requestsToInsert.get(i));
                costIncreaseMatrix.get(i).set(bestRouteIndex,
                        newRoute.getCost() - routes.get(bestRouteIndex).getCost());
                newRoutesMatrix.get(i).set(bestRouteIndex, newRoute);
            }
        }
    }

    public double getCost() {
        return cost;
    }
}
