public class PickupRouteStop extends RouteStop {
    Request request;

    public PickupRouteStop(Request request) {
        this.request = request;
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
    public PickupRouteStop copy() {
        PickupRouteStop pickupRouteStop = new PickupRouteStop(request);
        pickupRouteStop.setArrivalTime(arrivalTime);
        return pickupRouteStop;
    }
}
