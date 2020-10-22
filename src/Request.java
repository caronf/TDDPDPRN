public class Request {
    public final int pickupNode;
    public final int deliveryNode;
    public final double load;
    public final double pickupTimeWindowLowerBound;
    public final double pickupTimeWindowUpperBound;
    public final double pickupServiceTime;
    public final double deliveryTimeWindowLowerBound;
    public final double deliveryTimeWindowUpperBound;
    public final double deliveryServiceTime;
    public final double releaseTime;

    public Request(int pickupNode, int deliveryNode, double load, double pickupTimeWindowLowerBound,
                   double pickupTimeWindowUpperBound, double pickupServiceTime, double deliveryTimeWindowLowerBound,
                   double deliveryTimeWindowUpperBound, double deliveryServiceTime, double releaseTime) {
        this.pickupNode = pickupNode;
        this.deliveryNode = deliveryNode;
        this.load = load;
        this.pickupTimeWindowLowerBound = pickupTimeWindowLowerBound;
        this.pickupTimeWindowUpperBound = pickupTimeWindowUpperBound;
        this.pickupServiceTime = pickupServiceTime;
        this.deliveryTimeWindowLowerBound = deliveryTimeWindowLowerBound;
        this.deliveryTimeWindowUpperBound = deliveryTimeWindowUpperBound;
        this.deliveryServiceTime = deliveryServiceTime;
        this.releaseTime = releaseTime;
    }
}
