import java.util.ArrayList;
import java.util.Random;

public class Route {
    private final ArrayList<RouteStop> routeStops;
    private final RouteEnd routeEnd;
    private final ArrivalTimeFunction[][] arrivalTimeFunctions;
    private final double latenessWeight;
    private final double overtimeWeight;
    private final double vehicleCapacity;
    private final double returnTime;
    private double currentTime;

    // Stops become sealed when the vehicle is on its way and remain sealed afterwards
    private int nbSealedStops;

    private int newRequestPickupIndex;
    private int newRequestDeliveryIndex;

    // Previous insertion points are forbidden to avoid reinserting at the same place.
    private int previousPickupIndex;
    private int previousDeliveryIndex;

    public Route(ArrivalTimeFunction[][] arrivalTimeFunctions, double depotTimeWindowUpperBound,
                 double latenessWeight, double overtimeWeight, double vehicleCapacity, double returnTime) {
        routeStops = new ArrayList<>();
        routeStops.add(new RouteStart());
        routeEnd = new RouteEnd(depotTimeWindowUpperBound);
        routeStops.add(routeEnd);

        currentTime = 0.0;
        nbSealedStops = 1;
        newRequestPickupIndex = -1;
        newRequestDeliveryIndex = -1;
        previousPickupIndex = -1;
        previousDeliveryIndex = -1;
        this.arrivalTimeFunctions = arrivalTimeFunctions;
        this.latenessWeight = latenessWeight;
        this.overtimeWeight = overtimeWeight;
        this.vehicleCapacity = vehicleCapacity;
        this.returnTime = returnTime;

        updateStops(1);
    }

    public Route(Route otherRoute) {
        routeStops = new ArrayList<>(otherRoute.routeStops.size());
        for (RouteStop routeStop : otherRoute.routeStops) {
            routeStops.add(routeStop.copy());
        }
        routeEnd = (RouteEnd) routeStops.get(routeStops.size() - 1);

        currentTime = otherRoute.currentTime;
        nbSealedStops = otherRoute.nbSealedStops;
        newRequestPickupIndex = -1;
        newRequestDeliveryIndex = -1;
        previousPickupIndex = otherRoute.previousPickupIndex;
        previousDeliveryIndex = otherRoute.previousDeliveryIndex;
        arrivalTimeFunctions = otherRoute.arrivalTimeFunctions;
        latenessWeight = otherRoute.latenessWeight;
        overtimeWeight = otherRoute.overtimeWeight;
        vehicleCapacity = otherRoute.vehicleCapacity;
        returnTime = otherRoute.returnTime;
    }

    public double getCost() {
        return routeEnd.getCumulativeTravelTime() +
                latenessWeight * routeEnd.getCumulativeLateness() +
                overtimeWeight * routeEnd.getOvertime();
    }

    public double getTravelTime() {
        return routeEnd.getCumulativeTravelTime();
    }

    public double getLateness() {
        return routeEnd.getCumulativeLateness();
    }

    public int getNbUnsealedStops() {
        // The route end does not count
        return Math.max(routeStops.size() - nbSealedStops - 1, 0);
    }

    public Route getRouteAfterInsertion(Request request) {
        assert nbSealedStops < routeStops.size();
        Route bestRoute = null;

        if (previousPickupIndex != nbSealedStops ||
                previousDeliveryIndex != nbSealedStops + 1 ||
                routeStops.size() != nbSealedStops + 1) {
            Route tempRoute = new Route(this);
            tempRoute.insertRequest(request);

            do {
                if (tempRoute.isFeasible() &&
                        (bestRoute == null || DoubleComparator.lessThan(tempRoute.getCost(), bestRoute.getCost()))) {
                    bestRoute = new Route(tempRoute);
                }
            } while (tempRoute.cycleInsertionPoints());

            if (bestRoute != null) {
                bestRoute.previousPickupIndex = -1;
                bestRoute.previousDeliveryIndex = -1;
            }
        }

        previousPickupIndex = -1;
        previousDeliveryIndex = -1;
        return bestRoute;
    }

    public Route getRouteAfterRandomInsertion(Request request, Random random, boolean insertPickupStop) {
        assert nbSealedStops < routeStops.size();

        Route tempRoute;
        do {
            tempRoute = new Route(this);

            int updateFromIndex;
            int deliveryIndex = random.nextInt(routeStops.size() - nbSealedStops) + nbSealedStops;

            if (insertPickupStop) {
                int pickupIndex = random.nextInt(routeStops.size() - nbSealedStops) + nbSealedStops;
                if (deliveryIndex < pickupIndex) {
                    int i = pickupIndex;
                    pickupIndex = deliveryIndex;
                    deliveryIndex = i;
                }
                tempRoute.routeStops.add(pickupIndex, new PickupRouteStop(request));
                ++deliveryIndex;
                updateFromIndex = pickupIndex;
            } else {
                updateFromIndex = deliveryIndex;
            }

            tempRoute.routeStops.add(deliveryIndex, new DeliveryRouteStop(request));
            tempRoute.updateStops(updateFromIndex);
        } while (!tempRoute.isFeasible());

        // There is always at least one feasible insertion :
        // At the end of the route for a pickup + delivery
        // Or at its previous location for a delivery only since it can only be reinserted in the same route

        return tempRoute;
    }

    public Route getRouteAfterInsertingDelivery(Request request) {
        assert previousPickupIndex == -1;
        assert nbSealedStops < routeStops.size();

        Route tempRoute = new Route(this);
        DeliveryRouteStop deliveryRouteStop = new DeliveryRouteStop(request);
        tempRoute.routeStops.add(nbSealedStops, deliveryRouteStop);
        tempRoute.updateStops(nbSealedStops);

        Route bestRoute = null;
        if (nbSealedStops != previousDeliveryIndex && tempRoute.isFeasible()) {
            bestRoute = new Route(tempRoute);
        }

        for (int i = nbSealedStops; i < routeStops.size() - 1; ++i) {
            tempRoute.routeStops.set(i, tempRoute.routeStops.get(i + 1));
            tempRoute.routeStops.set(i + 1, deliveryRouteStop);
            tempRoute.updateStops(i);

            if (i + 1 != previousDeliveryIndex && tempRoute.isFeasible() &&
                    (bestRoute == null || DoubleComparator.lessThan(tempRoute.getCost(), bestRoute.getCost()))) {
                bestRoute = new Route(tempRoute);
            }
        }

        previousDeliveryIndex = -1;
        return bestRoute;
    }

    public int removeRequest(Request request) {
        previousPickupIndex = -1;
        previousDeliveryIndex = -1;

        for (int i = nbSealedStops; i < routeStops.size() - 1; ++i) {
            if (routeStops.get(i).servesRequest(request)) {
                if (previousPickupIndex == -1) {
                    previousPickupIndex = i;
                } else {
                    previousDeliveryIndex = i;
                    routeStops.remove(previousDeliveryIndex);
                    routeStops.remove(previousPickupIndex);
                    updateStops(previousPickupIndex);
                    return 2;
                }
            }
        }

        if (previousPickupIndex != -1) {
            previousDeliveryIndex = previousPickupIndex;
            previousPickupIndex = -1;
            routeStops.remove(previousDeliveryIndex);
            updateStops(previousDeliveryIndex);
            return 1;
        } else {
            return 0;
        }
    }

    private void insertRequest(Request request) {
        newRequestPickupIndex = nbSealedStops;
        if (previousPickupIndex == nbSealedStops && previousDeliveryIndex == nbSealedStops + 1) {
            assert nbSealedStops + 2 <= routeStops.size() ;
            newRequestDeliveryIndex = nbSealedStops + 2;
        } else {
            newRequestDeliveryIndex = nbSealedStops + 1;
        }

        routeStops.add(newRequestPickupIndex, new PickupRouteStop(request));
        routeStops.add(newRequestDeliveryIndex, new DeliveryRouteStop(request));

        updateStops(newRequestPickupIndex);
    }

    private boolean cycleInsertionPoints() {
        int updateFromIndex = Integer.MAX_VALUE;

        do {
            if (newRequestPickupIndex == routeStops.size() - 3) {
                return false;
            }

            if (newRequestDeliveryIndex == routeStops.size() - 2) {
                // Move the pickup point forward in the route
                RouteStop pickupRouteStop = routeStops.get(newRequestPickupIndex);
                routeStops.set(newRequestPickupIndex, routeStops.get(newRequestPickupIndex + 1));
                ++newRequestPickupIndex;
                routeStops.set(newRequestPickupIndex, pickupRouteStop);

                // Reset the delivery point to be right after the pickup
                newRequestDeliveryIndex = newRequestPickupIndex + 1;
                routeStops.add(newRequestDeliveryIndex, routeStops.remove(routeStops.size() - 2));

                updateFromIndex = Math.min(updateFromIndex, newRequestPickupIndex - 1);
            } else {
                // Move the delivery after the following stop
                RouteStop deliveryRouteStop = routeStops.get(newRequestDeliveryIndex);
                routeStops.set(newRequestDeliveryIndex, routeStops.get(newRequestDeliveryIndex + 1));
                ++newRequestDeliveryIndex;
                routeStops.set(newRequestDeliveryIndex, deliveryRouteStop);

                updateFromIndex = Math.min(updateFromIndex, newRequestDeliveryIndex - 1);
            }
        } while (previousPickupIndex == newRequestPickupIndex && previousDeliveryIndex == newRequestDeliveryIndex);

        updateStops(updateFromIndex);
        return true;
    }

    private void updateStops(int updateFromIndex) {
        assert updateFromIndex >= nbSealedStops;
        assert routeStops.get(routeStops.size() - 1) == routeEnd;

        for (int i = updateFromIndex; i < routeStops.size(); ++i) {
            RouteStop previousStop = routeStops.get(i - 1);
            RouteStop currentStop = routeStops.get(i);

            double waitEndTime = arrivalTimeFunctions[previousStop.getNode()][currentStop.getNode()].getDepartureTime(
                    currentStop.getTimeWindowLowerBound());
            if (currentStop == routeEnd && DoubleComparator.lessThan(returnTime, waitEndTime)) {
                waitEndTime = returnTime;
            }

            double departureTime = Math.max(currentTime,
                    Math.max(previousStop.getArrivalTime() + previousStop.getServiceTime(), waitEndTime));
            double arrivalTime =
                    arrivalTimeFunctions[previousStop.getNode()][currentStop.getNode()].getArrivalTime(departureTime);

            previousStop.setDepartureTime(departureTime);
            currentStop.setArrivalTime(arrivalTime);
            currentStop.setLoadAtArrival(previousStop.getLoadAtDeparture());
            currentStop.setCumulativeTravelTime(previousStop.getCumulativeTravelTime() + arrivalTime - departureTime);
            currentStop.setCumulativeLateness(previousStop.getCumulativeLateness() + currentStop.getLateness());
            currentStop.setCumulativeFeasibility(previousStop.getCumulativeFeasibility() &&
                    DoubleComparator.lessOrEqual(currentStop.getLoadAtDeparture(), vehicleCapacity));
        }

        // The only situation where the load at the end of the route is not equal
        // to zero is when a delivery was removed and is about to be reinserted
        assert DoubleComparator.equal(routeStops.get(routeStops.size() - 2).getLoadAtDeparture(), 0.0) ||
                previousPickupIndex < 0 && previousDeliveryIndex >= 0;
    }

    private boolean isFeasible() {
        return routeEnd.getCumulativeFeasibility();
    }

    public void setCurrentTime(double currentTime) {
        assert currentTime >= this.currentTime;
        this.currentTime = currentTime;
        while (nbSealedStops < routeStops.size() &&
                (DoubleComparator.lessOrEqual(routeStops.get(nbSealedStops - 1).getDepartureTime(), currentTime))) {
            ++nbSealedStops;
        }
    }

    public int getNbStopsForRequest(Request request) {
        int nbStops = 0;
        for (RouteStop routeStop : routeStops) {
            if (routeStop.servesRequest(request)) {
                ++nbStops;
            }
        }

        return nbStops;
    }

    public double getNextDepartureTime() {
        return nbSealedStops < routeStops.size() ?
                routeStops.get(nbSealedStops - 1).getDepartureTime() :
                Double.MAX_VALUE;
    }

    public boolean isInsertionPossible() {
        return nbSealedStops < routeStops.size();
    }
}
