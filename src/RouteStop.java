public abstract class RouteStop implements Cloneable {
    protected double arrivalTime;

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
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
    public abstract RouteStop copy();
}
