public class DoubleComparator {
    private static final double EPSILON = 0.000005;

    public static boolean equal(double d1, double d2) {
        return Math.abs(d1 - d2) <= EPSILON;
    }

    public static boolean greaterThan(double d1, double d2) {
        return d1 - d2 > EPSILON;
    }

    public static boolean greaterOrEqual(double d1, double d2) {
        return d1 - d2 >= -EPSILON;
    }

    public static boolean lessThan(double d1, double d2) {
        return d1 - d2 < -EPSILON;
    }

    public static boolean lessOrEqual(double d1, double d2) {
        return d1 - d2 <= EPSILON;
    }
}
