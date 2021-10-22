public class ImmediateArrivalTimeFunction extends ArrivalTimeFunction {
    @Override
    public double getArrivalTime(double departureTime) {
        return departureTime;
    }

    @Override
    public double getDepartureTime(double arrivalTime) {
        return arrivalTime;
    }

    @Override
    public int getSize() {
        return 0;
    }
}
