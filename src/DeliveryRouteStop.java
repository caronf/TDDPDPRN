public class DeliveryRouteStop extends RouteStop {
    Request request;

    public DeliveryRouteStop(Request request) {
        this.request = request;
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
    public DeliveryRouteStop copy() {
        DeliveryRouteStop deliveryRouteStop = new DeliveryRouteStop(request);
        deliveryRouteStop.setArrivalTime(arrivalTime);
        return deliveryRouteStop;
    }
}