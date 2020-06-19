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
        // This should not cause any lateness since the arrival time is 0
        return 0.0;
    }

    @Override
    public RouteStart copy() {
        return new RouteStart();
    }
}
