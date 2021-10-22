public abstract class ArrivalTimeFunction {
    public abstract double getArrivalTime(double departureTime);
    public abstract double getDepartureTime(double arrivalTime);

    // Get the size in bytes of the data in this object
    public abstract int getSize();
}
