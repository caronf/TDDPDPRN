public abstract class RouteStop implements Cloneable {
    protected double arrivalTime;
    protected double departureTime;
    protected double loadAtDeparture;

    protected double cumulativeTravelTime;
    protected double cumulativeLateness;
    protected boolean cumulativeFeasibility;

    public void setArrivalTime(double arrivalTime) {
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

    public void setCumulativeTravelTime(double cumulativeTravelTime) {
        this.cumulativeTravelTime = cumulativeTravelTime;
    }

    public double getCumulativeTravelTime() {
        return cumulativeTravelTime;
    }

    public void setCumulativeLateness(double cumulativeLateness) {
        this.cumulativeLateness = cumulativeLateness;
    }

    public double getCumulativeLateness() {
        return cumulativeLateness;
    }

    public void setCumulativeFeasibility(boolean cumulativeFeasibility) {
        this.cumulativeFeasibility = cumulativeFeasibility;
    }

    public boolean getCumulativeFeasibility() {
        return cumulativeFeasibility;
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
