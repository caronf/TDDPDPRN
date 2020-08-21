public class PickupRouteStop extends RouteStop {
    Request request;

    public PickupRouteStop(Request request) {
        this.request = request;
    }

    @Override
    public boolean servesRequest(Request request) {
        return this.request == request;
    }

    @Override
    public int getNode() {
        return request.pickupNode;
    }

    @Override
    public double getTimeWindowLowerBound() {
        return request.pickupTimeWindowLowerBound;
    }

    @Override
    public double getTimeWindowUpperBound() {
        return request.pickupTimeWindowUpperBound;
    }

    @Override
    public double getLoadAtDeparture() {
        return loadAtArrival + request.load;
    }

    @Override
    public double getServiceTime() {
        return request.pickupServiceTime;
    }

    @Override
    public PickupRouteStop copy() {
        PickupRouteStop pickupRouteStop = new PickupRouteStop(request);
        pickupRouteStop.setArrivalTime(arrivalTime);
        pickupRouteStop.setDepartureTime(departureTime);
        pickupRouteStop.setLoadAtArrival(loadAtArrival);
        return pickupRouteStop;
    }
}
