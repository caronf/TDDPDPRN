public class ImmediateArrivalTimeCalculator extends ArrivalTimeCalculator {
    @Override
    public double getArrivalTime(double departureTime) {
        return departureTime;
    }
}
