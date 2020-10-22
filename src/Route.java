import java.util.ArrayList;
import java.util.Random;

public class Route {
    private final ArrayList<RouteStop> routeStops;
    private final ArrivalTimeFunction[][] arrivalTimeFunctions;
    private final double latenessWeight;
    private final double vehicleCapacity;
    private double travelTime;
    private double lateness;
    private double currentTime;

    // Stops become sealed when the vehicle is on its way and remain sealed afterwards
    private int nbSealedStops;

    private int newRequestPickupIndex;
    private int newRequestDeliveryIndex;

    // Previous insertion points are forbidden to avoid reinserting at the same place.
    private int previousPickupIndex;
    private int previousDeliveryIndex;

    public Route(ArrivalTimeFunction[][] arrivalTimeFunctions, double depotTimeWindowUpperBound,
                 double latenessWeight, double vehicleCapacity) {
        routeStops = new ArrayList<>();
        routeStops.add(new RouteStart());
        routeStops.add(new RouteEnd(depotTimeWindowUpperBound));

        travelTime = -1.0;
        lateness = -1.0;
        currentTime = 0.0;
        nbSealedStops = 1;
        newRequestPickupIndex = -1;
        newRequestDeliveryIndex = -1;
        previousPickupIndex = -1;
        previousDeliveryIndex = -1;
        this.arrivalTimeFunctions = arrivalTimeFunctions;
        this.latenessWeight = latenessWeight;
        this.vehicleCapacity = vehicleCapacity;
    }

    public Route(Route otherRoute) {
        routeStops = new ArrayList<>(otherRoute.routeStops.size());
        for (RouteStop routeStop : otherRoute.routeStops) {
            routeStops.add(routeStop.copy());
        }

        travelTime = otherRoute.travelTime;
        lateness = otherRoute.lateness;
        currentTime = otherRoute.currentTime;
        nbSealedStops = otherRoute.nbSealedStops;
        newRequestPickupIndex = -1;
        newRequestDeliveryIndex = -1;
        previousPickupIndex = otherRoute.previousPickupIndex;
        previousDeliveryIndex = otherRoute.previousDeliveryIndex;
        arrivalTimeFunctions = otherRoute.arrivalTimeFunctions;
        latenessWeight = otherRoute.latenessWeight;
        vehicleCapacity = otherRoute.vehicleCapacity;
    }

    public double getCost() {
        if (travelTime < 0.0 || lateness < 0.0) {
            travelTime = 0.0;
            lateness = routeStops.get(0).getLateness();
            for (int i = 1; i < routeStops.size(); ++i) {
                travelTime += routeStops.get(i).getArrivalTime() - routeStops.get(i - 1).getDepartureTime();
                lateness += routeStops.get(i).getLateness();
            }
        }

        return travelTime + latenessWeight * lateness;
    }

    public double getTravelTime() {
        return travelTime;
    }

    public double getLateness() {
        return lateness;
    }

    public Route getRouteAfterInsertion(Request request) {
        if (previousPickupIndex == nbSealedStops &&
                previousDeliveryIndex == nbSealedStops + 1 &&
                routeStops.size() == nbSealedStops + 1) {
            previousPickupIndex = -1;
            previousDeliveryIndex = -1;
            return null;
        }

        Route tempRoute = new Route(this);
        tempRoute.insertRequest(request);
        Route bestRoute = null;

        do {
            if (tempRoute.isFeasible() &&
                    (bestRoute == null || DoubleComparator.lessThan(tempRoute.getCost(), bestRoute.getCost()))) {
                bestRoute = new Route(tempRoute);
            }
        } while (tempRoute.cycleInsertionPoints());

        assert bestRoute != null;
        previousPickupIndex = -1;
        previousDeliveryIndex = -1;
        bestRoute.previousPickupIndex = -1;
        bestRoute.previousDeliveryIndex = -1;
        return bestRoute;
    }

    public Route getRouteAfterRandomInsertion(Request request, Random random) {
        Route tempRoute;

        do {
            tempRoute = new Route(this);
            int pickupIndex = random.nextInt(routeStops.size() - nbSealedStops) + nbSealedStops;
            int deliveryIndex = random.nextInt(routeStops.size() - nbSealedStops) + nbSealedStops;
            if (deliveryIndex < pickupIndex) {
                int i = pickupIndex;
                pickupIndex = deliveryIndex;
                deliveryIndex = i;
            }

            tempRoute.routeStops.add(pickupIndex, new PickupRouteStop(request));
            ++deliveryIndex;
            tempRoute.routeStops.add(deliveryIndex, new DeliveryRouteStop(request));

            tempRoute.updateStops(pickupIndex);
        } while (!tempRoute.isFeasible());

        return tempRoute;
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
            return 1;
        } else {
            return 0;
        }
    }

    private void insertRequest(Request request) {
        newRequestPickupIndex = nbSealedStops;
        if (previousPickupIndex == nbSealedStops && newRequestDeliveryIndex == nbSealedStops + 1) {
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
            if (newRequestDeliveryIndex == routeStops.size() - 2) {
                if (newRequestPickupIndex == routeStops.size() - 3) {
                    return false;
                }

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

        for (int i = updateFromIndex; i < routeStops.size(); ++i) {
            routeStops.get(i-1).setDepartureTime(Math.max(currentTime, Math.max(
                    routeStops.get(i-1).getArrivalTime() + routeStops.get(i-1).getServiceTime(),
                    arrivalTimeFunctions[routeStops.get(i-1).getNode()][routeStops.get(i).getNode()].getDepartureTime(
                            routeStops.get(i).getTimeWindowLowerBound()))));

            routeStops.get(i).setArrivalTime(arrivalTimeFunctions[routeStops.get(i-1).getNode()][routeStops.get(i).getNode()].getArrivalTime(
                    routeStops.get(i-1).getDepartureTime()));

            routeStops.get(i).setLoadAtArrival(routeStops.get(i-1).getLoadAtDeparture());
        }

        assert DoubleComparator.equal(routeStops.get(routeStops.size() - 1).getLoadAtArrival(), 0.0);

        travelTime = -1.0;
        lateness = -1.0;
    }

    private boolean isFeasible() {
        for (RouteStop routeStop : routeStops) {
            if (DoubleComparator.greaterThan(routeStop.getLoadAtArrival(), vehicleCapacity)) {
                return false;
            }
        }

        return true;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
        while (nbSealedStops < routeStops.size() - 1 && routeStops.get(nbSealedStops - 1).getDepartureTime() < currentTime) {
            ++nbSealedStops;
        }
    }
}
