public class RouteEnd extends RouteStop {
    double endOfTheDay;

    public RouteEnd(double endOfTheDay) {
        this.endOfTheDay = endOfTheDay;
    }

    public double getOvertime() {
        return Math.max(0, arrivalTime - endOfTheDay);
    }
    @Override
    public int getNode() {
        return 0;
    }

    @Override
    public double getTimeWindowLowerBound() {
        return endOfTheDay;
    }

    @Override
    public double getTimeWindowUpperBound() {
        return Double.MAX_VALUE;
    }

    @Override
    public void setLoadAtArrival(double loadAtArrival) {
        loadAtDeparture = loadAtArrival;
    }

    @Override
    public double getServiceTime() {
        return 0.0;
    }

    @Override
    public RouteEnd copy() {
        RouteEnd routeEnd = new RouteEnd(endOfTheDay);
        routeEnd.setArrivalTime(getArrivalTime());
        routeEnd.setDepartureTime(getDepartureTime());
        routeEnd.loadAtDeparture = loadAtDeparture;
        routeEnd.setCumulativeTravelTime(cumulativeTravelTime);
        routeEnd.setCumulativeLateness(cumulativeLateness);
        routeEnd.setCumulativeFeasibility(cumulativeFeasibility);
        return routeEnd;
    }
}
