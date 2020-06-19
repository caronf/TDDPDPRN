public class Request {
    public final int pickupNode;
    public final int deliveryNode;
    public final double pickupTimeWindowLowerBound;
    public final double pickupTimeWindowUpperBound;
    public final double deliveryTimeWindowLowerBound;
    public final double deliveryTimeWindowUpperBound;

    public Request(int pickupNode, int deliveryNode,
                   double pickupTimeWindowLowerBound, double pickupTimeWindowUpperBound,
                   double deliveryTimeWindowLowerBound, double deliveryTimeWindowUpperBound) {
        this.pickupNode = pickupNode;
        this.deliveryNode = deliveryNode;
        this.pickupTimeWindowLowerBound = pickupTimeWindowLowerBound;
        this.pickupTimeWindowUpperBound = pickupTimeWindowUpperBound;
        this.deliveryTimeWindowLowerBound = deliveryTimeWindowLowerBound;
        this.deliveryTimeWindowUpperBound = deliveryTimeWindowUpperBound;
    }
}
