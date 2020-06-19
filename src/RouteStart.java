public class RouteStart extends RouteStop {
    public RouteStart() {
        arrivalTime = 0;
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
    public RouteStart copy() {
        return new RouteStart();
    }
}
