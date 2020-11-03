public abstract class RouteStop implements Cloneable {
    protected double arrivalTime;
    protected double departureTime;
    protected double loadAtDeparture;

    public void setArrivalTime(double arrivalTime) {
        assert DoubleComparator.greaterOrEqual(arrivalTime, getTimeWindowLowerBound()) &&
                DoubleComparator.lessOrEqual(arrivalTime, getTimeWindowUpperBound());
        this.arrivalTime = arrivalTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    public double getLoadAtDeparture() {
        return loadAtDeparture;
    }

    public double getLateness() {
        return Math.max(0, arrivalTime - getTimeWindowUpperBound());
    }

    public boolean servesRequest(Request request) {
        return false;
    }

    public abstract int getNode();
    public abstract double getTimeWindowLowerBound();
    public abstract double getTimeWindowUpperBound();
    public abstract void setLoadAtArrival(double loadAtArrival);
    public abstract double getServiceTime();
    public abstract RouteStop copy();
}
