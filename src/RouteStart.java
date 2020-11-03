public class RouteStart extends RouteStop {
    public RouteStart() {
        arrivalTime = 0.0;
        departureTime = 0.0;
        loadAtDeparture = 0.0;
    }

    @Override
    public int getNode() {
        return 0;
    }

    @Override
    public double getTimeWindowLowerBound() {
        return 0.0;
    }

    @Override
    public double getTimeWindowUpperBound() {
        return Double.MAX_VALUE;
    }

    @Override
    public void setLoadAtArrival(double loadAtArrival) {
        throw new RuntimeException("The load at arrival of the route start cannot be modified.");
    }

    @Override
    public double getServiceTime() {
        return 0.0;
    }

    @Override
    public RouteStart copy() {
        RouteStart routeStart = new RouteStart();
        assert arrivalTime == 0.0;
        routeStart.setDepartureTime(departureTime);
        assert loadAtDeparture == 0.0;
        return routeStart;
    }
}
