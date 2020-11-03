public class DeliveryRouteStop extends RouteStop {
    Request request;

    public DeliveryRouteStop(Request request) {
        this.request = request;
    }

    @Override
    public boolean servesRequest(Request request) {
        return this.request == request;
    }

    @Override
    public int getNode() {
        return request.deliveryNode;
    }

    @Override
    public double getTimeWindowLowerBound() {
        return request.deliveryTimeWindowLowerBound;
    }

    @Override
    public double getTimeWindowUpperBound() {
        return request.deliveryTimeWindowUpperBound;
    }

    @Override
    public void setLoadAtArrival(double loadAtArrival) {
        loadAtDeparture = loadAtArrival - request.load;
    }

    @Override
    public double getServiceTime() {
        return request.deliveryServiceTime;
    }

    @Override
    public DeliveryRouteStop copy() {
        DeliveryRouteStop deliveryRouteStop = new DeliveryRouteStop(request);
        deliveryRouteStop.setArrivalTime(arrivalTime);
        deliveryRouteStop.setDepartureTime(departureTime);
        deliveryRouteStop.loadAtDeparture = loadAtDeparture;
        return deliveryRouteStop;
    }
}
