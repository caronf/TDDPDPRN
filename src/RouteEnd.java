public class RouteEnd extends RouteStop {
    double timeWindowUpperBound;

    public RouteEnd(double timeWindowUpperBound) {
        this.timeWindowUpperBound = timeWindowUpperBound;
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
        return timeWindowUpperBound;
    }

    @Override
    public RouteEnd copy() {
        RouteEnd routeEnd = new RouteEnd(timeWindowUpperBound);
        routeEnd.setArrivalTime(getArrivalTime());
        return routeEnd;
    }
}