import java.util.ArrayList;

public class Route {
    private final ArrayList<RouteStop> routeStops;
    private final ArrivalTimeCalculator[][] arrivalTimeCalculators;
    private final double latenessWeight;
    private double travelTime;
    private double lateness;
    private int newRequestPickupIndex;
    private int newRequestDeliveryIndex;

    public Route(ArrivalTimeCalculator[][] arrivalTimeCalculators,
                 double depotTimeWindowUpperBound, double latenessWeight) {
        routeStops = new ArrayList<>();
        routeStops.add(new RouteStart());
        routeStops.add(new RouteEnd(depotTimeWindowUpperBound));
        travelTime = -1.0;
        lateness = -1.0;
        newRequestPickupIndex = -1;
        newRequestDeliveryIndex = -1;
        this.arrivalTimeCalculators = arrivalTimeCalculators;
        this.latenessWeight = latenessWeight;
    }

    public Route(Route otherRoute) {
        routeStops = new ArrayList<>(otherRoute.routeStops.size());
        for (RouteStop routeStop : otherRoute.routeStops) {
            routeStops.add(routeStop.copy());
        }

        travelTime = otherRoute.travelTime;
        lateness = otherRoute.lateness;
        newRequestPickupIndex = -1;
        newRequestDeliveryIndex = -1;
        arrivalTimeCalculators = otherRoute.arrivalTimeCalculators;
        latenessWeight = otherRoute.latenessWeight;
    }

    public double getCost() {
        if (travelTime < 0.0 || lateness < 0.0) {
            travelTime = 0.0;
            lateness = 0.0;
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

    private boolean cycleInsertionPoints() {
        int updateFromIndex;
        if (newRequestDeliveryIndex == routeStops.size() - 2) {
            if (newRequestPickupIndex == routeStops.size() - 3)
            {
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

            updateFromIndex = newRequestPickupIndex - 1;
        }
        else {

            // Move the pickup point forward in the route
            RouteStop deliveryRouteStop = routeStops.get(newRequestDeliveryIndex);
            routeStops.set(newRequestDeliveryIndex, routeStops.get(newRequestDeliveryIndex + 1));
            ++newRequestDeliveryIndex;
            routeStops.set(newRequestDeliveryIndex, deliveryRouteStop);

            updateFromIndex = newRequestDeliveryIndex - 1;
        }

        updateArrivalTimes(updateFromIndex);
        return true;
    }

    private void insertRequest(Request request) {
        newRequestPickupIndex = 1;
        newRequestDeliveryIndex = 2;

        routeStops.add(newRequestPickupIndex, new PickupRouteStop(request));
        routeStops.add(newRequestDeliveryIndex, new DeliveryRouteStop(request));

        updateArrivalTimes(newRequestPickupIndex);
    }

    private void updateArrivalTimes(int updateFromIndex) {
        for (int i = updateFromIndex; i < routeStops.size(); ++i) {
            routeStops.get(i).setArrivalTime(
                    arrivalTimeCalculators[routeStops.get(i-1).getNode()][routeStops.get(i).getNode()].getArrivalTime(
                            routeStops.get(i-1).getDepartureTime()));
        }

        travelTime = -1.0;
        lateness = -1.0;
    }

    public Route getRouteAfterInsertion(Request request) {
        Route tempRoute = new Route(this);
        tempRoute.insertRequest(request);

        Route bestRoute = new Route(tempRoute);
        while (tempRoute.cycleInsertionPoints()) {
            if (tempRoute.getCost() < bestRoute.getCost()) {
                bestRoute = new Route(tempRoute);
            }
        }

        return bestRoute;
    }
}
