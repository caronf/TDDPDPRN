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
        routeStart = otherRoute.routeStart.copy();
        RouteStop currentStop = routeStart;
        RouteStop stopToCopy = otherRoute.routeStart.getNextStop();
        while (stopToCopy != null) {
            RouteStop newStop = stopToCopy.copy();
            currentStop.setNextStop(newStop);
            newStop.setPreviousStop(currentStop);
            currentStop = newStop;
            stopToCopy = stopToCopy.getNextStop();
        }

        assert currentStop instanceof RouteEnd;
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

        // The first insertion should always be feasible unless the request's load
        // is greater than the vehicle capacity in which case the instance is infeasible
        assert tempRoute.isFeasible();

        Route bestRoute = new Route(tempRoute);

        while (tempRoute.cycleInsertionPoints()) {
            if (tempRoute.isFeasible() && DoubleComparator.lessThan(tempRoute.getCost(), bestRoute.getCost())) {
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

        updateStops(newPickupStop);
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

        updateStops(updateFromStop);
        return true;
    }

    private void updateStops(RouteStop updateFromStop) {
        RouteStop currentStop = updateFromStop.getPreviousStop();
        while (currentStop != routeEnd) {
            RouteStop nextStop = currentStop.getNextStop();

            currentStop.setDepartureTime(Math.max(currentStop.getArrivalTime() + currentStop.getServiceTime(),
                    arrivalTimeFunctions[currentStop.getNode()][nextStop.getNode()].getDepartureTime(
                            nextStop.getTimeWindowLowerBound())));
            nextStop.setArrivalTime(arrivalTimeFunctions[currentStop.getNode()][nextStop.getNode()].getArrivalTime(
                    currentStop.getDepartureTime()));
            nextStop.setLoadAtArrival(currentStop.getLoadAtDeparture());

            currentStop = nextStop;
        }

        assert DoubleComparator.equal(routeEnd.getLoadAtArrival(), 0.0);

        travelTime = -1.0;
        lateness = -1.0;
    }

    private boolean isFeasible() {
        RouteStop currentStop = routeStart;
        while (currentStop != routeEnd) {
            if (DoubleComparator.greaterThan(currentStop.getLoadAtArrival(), vehicleCapacity)) {
                return false;
            }
            currentStop = currentStop.getNextStop();
        }

        return true;
    }
}
