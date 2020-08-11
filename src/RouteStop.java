public abstract class RouteStop implements Cloneable {
    protected double arrivalTime;
    protected double loadAtArrival;
    protected RouteStop previousStop;
    protected RouteStop nextStop;

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setLoadAtArrival(double loadAtArrival) {
        this.loadAtArrival = loadAtArrival;
    }

    public double getLoadAtArrival() {
        return loadAtArrival;
    }

    public void setPreviousStop(RouteStop previousStop) {
        this.previousStop = previousStop;
    }

    public RouteStop getPreviousStop() {
        return previousStop;
    }

    public void setNextStop(RouteStop nextStop) {
        this.nextStop = nextStop;
    }

    public RouteStop getNextStop() {
        return nextStop;
    }

    public double getDepartureTime() {
        return Math.max(arrivalTime, getTimeWindowLowerBound());
    }

    public double getLateness() {
        return Math.max(0, arrivalTime - getTimeWindowUpperBound());
    }

    public abstract int getNode();
    public abstract double getTimeWindowLowerBound();
    public abstract double getTimeWindowUpperBound();
    public abstract double getLoadAtDeparture();
    public abstract RouteStop copy();
}
