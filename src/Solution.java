import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class Solution {
    private final ArrayList<Route> routes;
    private final int maxNbRoutes;
    private double travelTime;
    private double lateness;
    private double overtime;
    private double cost;

    // routeIndexForNextInsertion is used when the last request removed had
    // a sealed pickup and indicates in which route the delivery must be reinserted
    private int routeIndexForNextInsertion;

    public Solution(int maxNbRoutes, ArrivalTimeFunction[][] arrivalTimeFunctions, double depotTimeWindowUpperBound,
                    double latenessWeight, double overtimeWeight, double vehicleCapacity, double returnTime) {
        routes = new ArrayList<>();
        routes.add(new Route(arrivalTimeFunctions, depotTimeWindowUpperBound,
                latenessWeight, overtimeWeight, vehicleCapacity, returnTime));

        this.maxNbRoutes = maxNbRoutes;
        travelTime = routes.get(0).getTravelTime();
        lateness = routes.get(0).getLateness();
        overtime = routes.get(0).getOvertime();
        cost = routes.get(0).getCost();

        routeIndexForNextInsertion = -1;
    }

    public Solution(Solution otherSolution) {
        routes = new ArrayList<>(otherSolution.routes.size());
        for (Route route : otherSolution.routes) {
            routes.add(new Route(route));
        }

        maxNbRoutes = otherSolution.maxNbRoutes;
        travelTime = otherSolution.travelTime;
        lateness = otherSolution.lateness;
        overtime = otherSolution.overtime;
        cost = otherSolution.cost;

        routeIndexForNextInsertion = otherSolution.routeIndexForNextInsertion;
    }

    public int getNbUnsealedStops() {
        int nbUnsealedSlot = 0;
        for (Route route : routes) {
            nbUnsealedSlot += route.getNbUnsealedStops();
        }
        return nbUnsealedSlot;
    }

    public void insertRequestAtRandomPosition(Request request, Random random) {
        int routeIndex;
        boolean insertPickupStop;
        if (routeIndexForNextInsertion >= 0) {
            routeIndex = routeIndexForNextInsertion;
            routeIndexForNextInsertion = -1;
            insertPickupStop = false;
        } else {
            ArrayList<Integer> validRouteIndices = new ArrayList<>();
            for (int i = 0; i < routes.size(); ++i) {
                if (routes.get(i).isInsertionPossible()) {
                    validRouteIndices.add(i);
                }
            }
            routeIndex = validRouteIndices.get(random.nextInt(validRouteIndices.size()));
            insertPickupStop = true;
        }

        Route routeAfterInsertion =
                routes.get(routeIndex).getRouteAfterRandomInsertion(request, random, insertPickupStop);
        if (routeIndex == routes.size() - 1 && routes.size() < maxNbRoutes) {
            routes.add(routeIndex, routeAfterInsertion);
        }
        else
        {
            travelTime -= routes.get(routeIndex).getTravelTime();
            lateness -= routes.get(routeIndex).getLateness();
            overtime -= routes.get(routeIndex).getOvertime();
            cost -= routes.get(routeIndex).getCost();

            routes.set(routeIndex, routeAfterInsertion);
        }

        travelTime += routeAfterInsertion.getTravelTime();
        lateness += routeAfterInsertion.getLateness();
        overtime += routeAfterInsertion.getOvertime();
        cost += routeAfterInsertion.getCost();
    }

    public void insertRequest(Request request) {
        double bestCostIncrease = Double.MAX_VALUE;
        Route bestRoute = null;
        int routeIndex = -1;
        if (routeIndexForNextInsertion >= 0) {
            routeIndex = routeIndexForNextInsertion;
            int nbTries = 0;
            do {
                assert nbTries <= 1;
                bestRoute = routes.get(routeIndex).getRouteAfterInsertingDelivery(request);
                ++nbTries;
                // If the insertion fails the first time, the delivery will
                // be inserted at its previous location the second time
            } while (bestRoute == null);
            routeIndexForNextInsertion = -1;
        } else {
            int nbTries = 0;
            do {
                assert nbTries <= 1;
                for (int i = 0; i < routes.size(); ++i) {
                    if (routes.get(i).isInsertionPossible()) {
                        Route newRoute = routes.get(i).getRouteAfterInsertion(request);
                        if (newRoute != null) {
                            double costIncrease = newRoute.getCost() - routes.get(i).getCost();
                            if (costIncrease < bestCostIncrease) {
                                bestCostIncrease = costIncrease;
                                bestRoute = newRoute;
                                routeIndex = i;
                            }
                        }
                    }
                }
                ++nbTries;
                // If the insertion fails the first time, the service points
                // will be inserted at its previous location the second time
            } while (bestRoute == null);
        }

        if (routeIndex == routes.size() - 1 && routes.size() < maxNbRoutes) {
            routes.add(routeIndex, bestRoute);
        }
        else
        {
            travelTime -= routes.get(routeIndex).getTravelTime();
            lateness -= routes.get(routeIndex).getLateness();
            overtime -= routes.get(routeIndex).getOvertime();
            cost -= routes.get(routeIndex).getCost();

            routes.set(routeIndex, bestRoute);
        }

        travelTime += bestRoute.getTravelTime();
        lateness += bestRoute.getLateness();
        overtime += bestRoute.getOvertime();
        cost += bestRoute.getCost();
    }

    public void insertRequestsAnyOrder(Iterable<Request> requests) {
        assert routeIndexForNextInsertion == -1;

        for (Request request : requests) {
            insertRequest(request);
        }
    }

    public void insertRequestsBestFirst(Collection<Request> requests) {
        assert routeIndexForNextInsertion == -1;

        ArrayList<Request> requestsToInsert = new ArrayList<>(requests);
        ArrayList<ArrayList<Double>> costIncreaseMatrix = new ArrayList<>(requests.size());
        ArrayList<ArrayList<Route>> newRoutesMatrix = new ArrayList<>(requests.size());

        // Calculate cost increase for each pair of request/route
        for (Request request : requests) {
            ArrayList<Double> costIncreasesForRequest = new ArrayList<>(routes.size());
            ArrayList<Route> newRoutesForRequest = new ArrayList<>(routes.size());

            for (Route route : routes) {
                Route newRoute = null;
                if (route.isInsertionPossible()) {
                    newRoute = route.getRouteAfterInsertion(request);
                }
                costIncreasesForRequest.add(newRoute == null ? Double.MAX_VALUE :
                        newRoute.getCost() - route.getCost());
                newRoutesForRequest.add(newRoute);
            }

            costIncreaseMatrix.add(costIncreasesForRequest);
            newRoutesMatrix.add(newRoutesForRequest);
        }

        while (!requestsToInsert.isEmpty()) {
            int bestRequestIndex = -1;
            int bestRouteIndex = -1;

            // Find the smallest increase in cost
            for (int i = 0; i < requestsToInsert.size(); ++i) {
                for (int j = 0; j < routes.size(); ++j) {
                    if (costIncreaseMatrix.get(i).get(j) < Double.MAX_VALUE &&
                            (bestRequestIndex == -1 || costIncreaseMatrix.get(i).get(j) <
                                    costIncreaseMatrix.get(bestRequestIndex).get(bestRouteIndex))) {
                        bestRequestIndex = i;
                        bestRouteIndex = j;
                    }
                }
            }

            // Perform the insertion with minimum cost increase
            assert bestRequestIndex != -1;
            assert routes.get(bestRouteIndex) != null;
            travelTime += newRoutesMatrix.get(bestRequestIndex).get(bestRouteIndex).getTravelTime() -
                    routes.get(bestRouteIndex).getTravelTime();
            lateness += newRoutesMatrix.get(bestRequestIndex).get(bestRouteIndex).getLateness() -
                    routes.get(bestRouteIndex).getLateness();
            overtime += newRoutesMatrix.get(bestRequestIndex).get(bestRouteIndex).getOvertime() -
                    routes.get(bestRouteIndex).getOvertime();
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
            requestsToInsert.remove(bestRequestIndex);

            // Update the insertion cost of every remaining request for this route
            for (int i = 0; i < requestsToInsert.size(); ++i) {
                Route newRoute = routes.get(bestRouteIndex).getRouteAfterInsertion(requestsToInsert.get(i));
                double costIncrease = newRoute == null ? Double.MAX_VALUE :
                        newRoute.getCost() - routes.get(bestRouteIndex).getCost();
                if (addRoute) {
                    costIncreaseMatrix.get(i).add(bestRouteIndex, costIncrease);
                    newRoutesMatrix.get(i).add(bestRouteIndex, newRoute);
                }
                else {
                    costIncreaseMatrix.get(i).set(bestRouteIndex, costIncrease);
                    newRoutesMatrix.get(i).set(bestRouteIndex, newRoute);
                }
            }
        }
    }

    public int removeRequest(Request request) {
        routeIndexForNextInsertion = -1;

        for (int i = 0; i < routes.size(); ++i) {
            travelTime -= routes.get(i).getTravelTime();
            lateness -= routes.get(i).getLateness();
            overtime -= routes.get(i).getOvertime();
            cost -= routes.get(i).getCost();

            int nbRemoved = routes.get(i).removeRequest(request);

            travelTime += routes.get(i).getTravelTime();
            lateness += routes.get(i).getLateness();
            overtime += routes.get(i).getOvertime();
            cost += routes.get(i).getCost();

            if (nbRemoved > 0) {
                if (nbRemoved == 1) {
                    routeIndexForNextInsertion = i;
                } else if (routes.get(i).isEmpty()) {
                    // Only keep one empty route at the end of the list
                    assert routes.get(i).getCost() == 0.0;
                    Route emptyRoute = routes.remove(i);
                    if (!routes.get(routes.size() - 1).isEmpty()) {
                        routes.add(emptyRoute);
                    }
                }

                return nbRemoved;
            }
        }

        return 0;
    }

    public double getTravelTime() {
        return travelTime;
    }

    public double getLateness() {
        return lateness;
    }

    public double getOvertime() {
        return overtime;
    }

    public double getCost() {
        return cost;
    }

    public void setCurrentTime(double time) {
        for (Route route : routes) {
            route.setCurrentTime(time);
        }
    }

    public int getNbStopsForRequest(Request request) {
        int nbStops = 0;
        for (Route route : routes) {
            nbStops += route.getNbStopsForRequest(request);
        }

        return nbStops;
    }

    public double getNextDepartureTime() {
        double nextDepartureTime = Double.MAX_VALUE;
        for (Route route : routes) {
            nextDepartureTime = Math.min(nextDepartureTime, route.getNextDepartureTime());
        }

        return nextDepartureTime;
    }

    public int getNbRoutes() {
        int nbRoutes = routes.size();
        if (routes.get(routes.size() - 1).isEmpty()) {
            --nbRoutes;
        }

        return nbRoutes;
    }
}
