public class RouteStart extends RouteStop {
    public RouteStart() {
        arrivalTime = 0.0;
        loadAtArrival = 0.0;
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
    public double getLoadAtDeparture() {
        return 0.0;
    }

    @Override
    public RouteStart copy() {
        return new RouteStart();
    }
}
