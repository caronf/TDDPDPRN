import java.util.ArrayList;
import java.util.Collection;

public class Solution {
    private final ArrayList<Request> requests;
    private final ArrayList<Route> routes;
    private double travelTime;
    private double lateness;
    private double cost;
    private int maxNbRoutes;

    public Solution(int maxNbRoutes, ArrivalTimeFunction[][] arrivalTimeFunctions,
                    double depotTimeWindowUpperBound, double latenessWeight, double vehicleCapacity) {
        requests = new ArrayList<>();
        routes = new ArrayList<>();
        routes.add(new Route(arrivalTimeFunctions, depotTimeWindowUpperBound, latenessWeight, vehicleCapacity));

        travelTime = routes.get(0).getTravelTime();
        lateness = routes.get(0).getLateness();
        cost = routes.get(0).getCost();
        this.maxNbRoutes = maxNbRoutes;
    }

    public Solution(Solution otherSolution) {
        requests = new ArrayList<>(otherSolution.requests);
        routes = new ArrayList<>(otherSolution.routes.size());
        for (Route route : otherSolution.routes) {
            routes.add(new Route(route));
        }

        travelTime = otherSolution.travelTime;
        lateness = otherSolution.lateness;
        cost = otherSolution.cost;
        maxNbRoutes = otherSolution.maxNbRoutes;
    }

    public void insertRequest(Request request) {
        double bestCostIncrease = Double.MAX_VALUE;
        Route bestRoute = null;
        int bestRouteIndex = -1;

        for (int i = 0; i < routes.size(); ++i) {
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
        cost += bestCostIncrease;

        if (bestRouteIndex == routes.size() - 1 && routes.size() < maxNbRoutes) {
            routes.add(bestRouteIndex, bestRoute);
        }
        else
        {
            routes.set(bestRouteIndex, bestRoute);
        }

        this.requests.add(request);
    }

    public void insertRequestsAnyOrder(Iterable<Request> requests) {
        for (Request request : requests) {
            insertRequest(request);
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
            cost += costIncreaseMatrix.get(bestRequestIndex).get(bestRouteIndex);

            boolean addRoute = bestRouteIndex == routes.size() - 1 && routes.size() < maxNbRoutes;
            if (addRoute) {
                routes.add(bestRouteIndex, newRoutesMatrix.get(bestRequestIndex).get(bestRouteIndex));
            }
            else
            {
                routes.set(bestRouteIndex, newRoutesMatrix.get(bestRequestIndex).get(bestRouteIndex));
            }

            costIncreaseMatrix.remove(bestRequestIndex);
            newRoutesMatrix.remove(bestRequestIndex);
            this.requests.add(requestsToInsert.remove(bestRequestIndex));

            // Update the insertion cost of every remaining request for this route
            for (int i = 0; i < requestsToInsert.size(); ++i) {
                Route newRoute = routes.get(bestRouteIndex).getRouteAfterInsertion(requestsToInsert.get(i));
                if (addRoute) {
                    costIncreaseMatrix.get(i).add(bestRouteIndex,
                            newRoute.getCost() - routes.get(bestRouteIndex).getCost());
                    newRoutesMatrix.get(i).add(bestRouteIndex, newRoute);
                }
                else {
                    costIncreaseMatrix.get(i).set(bestRouteIndex,
                            newRoute.getCost() - routes.get(bestRouteIndex).getCost());
                    newRoutesMatrix.get(i).set(bestRouteIndex, newRoute);
                }
            }
        }
    }

    public void removeRequest(Request request) {
        for (Route route : routes) {
            travelTime -= route.getTravelTime();
            lateness -= route.getLateness();
            cost -= route.getCost();

            route.removeRequest(request);

            travelTime += route.getTravelTime();
            lateness += route.getLateness();
            cost += route.getCost();
        }
    }

    public double getCost() {
        return cost;
    }
}
