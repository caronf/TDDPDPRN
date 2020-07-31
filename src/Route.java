import java.util.ArrayList;

public class Route {
    RouteStart routeStart;
    RouteEnd routeEnd;
    private final ArrivalTimeFunction[][] arrivalTimeFunctions;
    private final double latenessWeight;
    private final double vehicleCapacity;
    private double travelTime;
    private double lateness;
    private PickupRouteStop newPickupStop;
    private DeliveryRouteStop newDeliveryStop;

    public Route(ArrivalTimeFunction[][] arrivalTimeFunctions, double depotTimeWindowUpperBound,
                 double latenessWeight, double vehicleCapacity) {
        routeStart = new RouteStart();
        routeEnd = new RouteEnd(depotTimeWindowUpperBound);
        routeStart.setNextStop(routeEnd);
        routeEnd.setPreviousStop(routeStart);

        travelTime = -1.0;
        lateness = -1.0;
        newPickupStop = null;
        newDeliveryStop = null;
        this.arrivalTimeFunctions = arrivalTimeFunctions;
        this.latenessWeight = latenessWeight;
        this.vehicleCapacity = vehicleCapacity;
    }

    public Route(Route otherRoute) {
        routeStart = new RouteStart();
        RouteStop currentStop = routeStart;
        RouteStop stopToCopy = otherRoute.routeStart.getNextStop();
        while (stopToCopy != null) {
            RouteStop newStop = stopToCopy.copy();
            currentStop.setNextStop(newStop);
            newStop.setPreviousStop(currentStop);
            currentStop = newStop;
            stopToCopy = stopToCopy.getNextStop();
        }
        routeEnd = (RouteEnd) currentStop;

        travelTime = otherRoute.travelTime;
        lateness = otherRoute.lateness;
        newPickupStop = null;
        newDeliveryStop = null;
        arrivalTimeFunctions = otherRoute.arrivalTimeFunctions;
        latenessWeight = otherRoute.latenessWeight;
        vehicleCapacity = otherRoute.vehicleCapacity;
    }

    public double getCost() {
        if (travelTime < 0.0 || lateness < 0.0) {
            RouteStop currentStop = routeStart;
            travelTime = 0.0;
            lateness = currentStop.getLateness();
            while (currentStop != routeEnd) {
                RouteStop nextStop = currentStop.getNextStop();
                travelTime += nextStop.getArrivalTime() - currentStop.getDepartureTime();
                lateness += nextStop.getLateness();
                currentStop = nextStop;
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

    private void insertRequest(Request request) {
        newPickupStop = new PickupRouteStop(request);
        newDeliveryStop = new DeliveryRouteStop(request);
        newDeliveryStop.setNextStop(routeStart.getNextStop());
        routeStart.setNextStop(newPickupStop);
        newPickupStop.setPreviousStop(routeStart);
        newPickupStop.setNextStop(newDeliveryStop);
        newDeliveryStop.setPreviousStop(newPickupStop);

        updateArrivalTimes(newPickupStop);
    }

    private boolean cycleInsertionPoints() {
        RouteStop updateFromStop;
        if (newDeliveryStop.getNextStop() == routeEnd) {
            if (newPickupStop.getNextStop() == newDeliveryStop)
            {
                return false;
            }

            // Move the pickup after the following stop and reset the delivery right after the pickup

            newPickupStop.getPreviousStop().setNextStop(newPickupStop.getNextStop());
            newPickupStop.getNextStop().setPreviousStop(newPickupStop.getPreviousStop());

            newDeliveryStop.getPreviousStop().setNextStop(newDeliveryStop.getNextStop());
            newDeliveryStop.getNextStop().setPreviousStop(newDeliveryStop.getPreviousStop());

            newDeliveryStop.setNextStop(newPickupStop.getNextStop().getNextStop());
            newPickupStop.getNextStop().getNextStop().setPreviousStop(newDeliveryStop);
            newPickupStop.setPreviousStop(newPickupStop.getNextStop());
            newPickupStop.getNextStop().setNextStop(newPickupStop);
            newPickupStop.setNextStop(newDeliveryStop);
            newDeliveryStop.setPreviousStop(newPickupStop);

            updateFromStop = newPickupStop.getPreviousStop();
        }
        else {
            // Move the delivery after the following stop

            newDeliveryStop.getPreviousStop().setNextStop(newDeliveryStop.getNextStop());
            newDeliveryStop.getNextStop().setPreviousStop(newDeliveryStop.getPreviousStop());

            newDeliveryStop.setPreviousStop(newDeliveryStop.getNextStop());
            newDeliveryStop.getNextStop().getNextStop().setPreviousStop(newDeliveryStop);
            newDeliveryStop.setNextStop(newDeliveryStop.getNextStop().getNextStop());
            newDeliveryStop.getPreviousStop().setNextStop(newDeliveryStop);

            updateFromStop = newDeliveryStop.getPreviousStop();
        }

        updateArrivalTimes(updateFromStop);
        return true;
    }

    private void updateArrivalTimes(RouteStop updateFromStop) {
        RouteStop currentStop = updateFromStop.getPreviousStop();
        while (currentStop != routeEnd) {
            RouteStop nextStop = currentStop.getNextStop();
            nextStop.setArrivalTime(arrivalTimeFunctions[currentStop.getNode()][nextStop.getNode()].getArrivalTime(
                    currentStop.getDepartureTime()));
            currentStop = nextStop;
        }

        travelTime = -1.0;
        lateness = -1.0;
    }
}
