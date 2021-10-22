import java.util.ArrayList;
import java.util.Comparator;

public class PiecewiseArrivalTimeFunction extends ArrivalTimeFunction {
    // Break points between which the arrival time evolves linearly
    protected final ArrayList<double[]> points;

    public PiecewiseArrivalTimeFunction() {
        points = new ArrayList<>();
    }

    public PiecewiseArrivalTimeFunction(double[] stepTimes, double baseTravelTime, double[] speedFactors) {
        assert stepTimes.length == speedFactors.length + 1;

        points = new ArrayList<>(stepTimes.length * 2 - 1);
        points.add(new double[] {0.0, getNeighborArrivalTime(baseTravelTime, 0, stepTimes, speedFactors)});

        for (int i = 1; i < stepTimes.length - 1; ++i) {
            // Arrival at current step time
            double departureTime = getNeighborDepartureTime(baseTravelTime, i, stepTimes, speedFactors);
            if (DoubleComparator.greaterThan(departureTime, 0.0)) {
                points.add(new double[] {departureTime, stepTimes[i]});
            }

            // Departure at current step time
            points.add(new double[] {stepTimes[i], getNeighborArrivalTime(baseTravelTime, i, stepTimes, speedFactors)});
        }

        points.sort(Comparator.comparingDouble(pt -> pt[0]));

        // Remove duplicate points
        int i = 1;
        while (i < points.size()) {
            if (DoubleComparator.equal(points.get(i)[0], points.get(i - 1)[0])) {
                assert DoubleComparator.equal(points.get(i)[1], points.get(i - 1)[1]);
                points.remove(i);
            } else {
                ++i;
            }
        }
    }

    // Concatenate two piecewise arrival time functions
    public PiecewiseArrivalTimeFunction(PiecewiseArrivalTimeFunction piecewiseArrivalTimeFunction1,
                                        PiecewiseArrivalTimeFunction piecewiseArrivalTimeFunction2) {
        points = new ArrayList<>(piecewiseArrivalTimeFunction1.points.size() +
                piecewiseArrivalTimeFunction2.points.size());

        for (double[] point : piecewiseArrivalTimeFunction1.points) {
            points.add(new double[] {point[0], piecewiseArrivalTimeFunction2.getArrivalTime(point[1])});
        }

        for (double[] point : piecewiseArrivalTimeFunction2.points) {
            double departureTime = piecewiseArrivalTimeFunction1.getDepartureTime(point[0]);
            if (DoubleComparator.greaterThan(departureTime, 0.0)) {
                points.add(new double[] {departureTime, point[1]});
            }
        }

        points.sort(Comparator.comparingDouble(a -> a[0]));

        // Remove duplicate points
        int i = 1;
        while (i < points.size()) {
            if (DoubleComparator.equal(points.get(i)[0], points.get(i - 1)[0])) {
                assert DoubleComparator.equal(points.get(i)[1], points.get(i - 1)[1]);
                points.remove(i);
            } else {
                ++i;
            }
        }
    }

    @Override
    public double getArrivalTime(double departureTime) {
        assert departureTime >= 0.0;

        if (DoubleComparator.greaterOrEqual(departureTime, points.get(points.size() - 1)[0])) {
            // The function continues towards infinity with a slope of 1 after the last break point
            return points.get(points.size() - 1)[1] + departureTime - points.get(points.size() - 1)[0];
        }

        int previousPoint = 0;
        int nextPoint = points.size() - 1;
        while (previousPoint < nextPoint - 1) {
            int medianPoint = (previousPoint + nextPoint) / 2;
            if (DoubleComparator.lessThan(departureTime, points.get(medianPoint)[0])) {
                nextPoint = medianPoint;
            } else {
                previousPoint = medianPoint;
            }
        }

        assert previousPoint + 1 == nextPoint;

        // Perform a linear interpolation between previousPoint and nextPoint
        assert DoubleComparator.greaterThan(points.get(nextPoint)[0], points.get(previousPoint)[0]);
        double arrivalTime = points.get(previousPoint)[1] + (departureTime - points.get(previousPoint)[0]) *
                (points.get(nextPoint)[1] - points.get(previousPoint)[1]) /
                (points.get(nextPoint)[0] - points.get(previousPoint)[0]);
        assert DoubleComparator.greaterOrEqual(arrivalTime, departureTime);
        return arrivalTime;
    }

    @Override
    public double getDepartureTime(double arrivalTime) {
        if (DoubleComparator.lessThan(arrivalTime, points.get(0)[1])) {
            // Any negative departure time will indicate the impossibility of this arrival time
            return -1.0;
        }

        if (DoubleComparator.greaterOrEqual(arrivalTime, points.get(points.size() - 1)[1])) {
            // The function continues towards infinity with a slope of 1 after the last break point
            return points.get(points.size() - 1)[0] + arrivalTime - points.get(points.size() - 1)[1];
        }

        int previousPoint = 0;
        int nextPoint = points.size() - 1;
        while (previousPoint < nextPoint - 1) {
            int medianPoint = (previousPoint + nextPoint) / 2;
            if (DoubleComparator.lessThan(arrivalTime, points.get(medianPoint)[1])) {
                nextPoint = medianPoint;
            } else {
                previousPoint = medianPoint;
            }
        }

        assert previousPoint + 1 == nextPoint;

        // Perform a linear interpolation between previousPoint and nextPoint
        assert DoubleComparator.greaterThan(points.get(nextPoint)[1], points.get(previousPoint)[1]);
        double departureTime = points.get(previousPoint)[0] + (arrivalTime - points.get(previousPoint)[1]) *
                (points.get(nextPoint)[0] - points.get(previousPoint)[0]) /
                        (points.get(nextPoint)[1] - points.get(previousPoint)[1]);
        assert DoubleComparator.lessOrEqual(departureTime, arrivalTime);
        return departureTime;
    }

    @Override
    public int getSize() {
        return points.size() * Double.BYTES * 2;
    }

    // Give the arrival time when departing at the start of the specified step
    private static double getNeighborArrivalTime(double baseTravelTime, int step,
                                                 double[] stepTimes, double[] speedFactors) {
        // The relative time travelled is the distance travelled multiplied
        // by the base travel time divided by the arc length
        double relativeTimeTravelled = 0.0;
        int currentStep = step;

        // Find the arrival step in the speed function
        // The speed of the last step is kept at the end of the day
        // so stepTimes.length - 2 is the start of the last step
        double distanceInCurrentStep =
                (stepTimes[currentStep + 1] - stepTimes[currentStep]) * speedFactors[currentStep];
        while (currentStep < stepTimes.length - 2 &&
                DoubleComparator.lessThan(relativeTimeTravelled + distanceInCurrentStep, baseTravelTime)) {
            relativeTimeTravelled += distanceInCurrentStep;
            ++currentStep;

            distanceInCurrentStep = (stepTimes[currentStep + 1] - stepTimes[currentStep]) * speedFactors[currentStep];
        }

        return stepTimes[currentStep] + (baseTravelTime - relativeTimeTravelled) / speedFactors[currentStep];
    }

    // Give the departure time to arrive at the start of the specified step
    private static double getNeighborDepartureTime(double baseTravelTime, int step,
                                                   double[] stepTimes, double[] speedFactors) {
        // The relative time travelled is the distance travelled multiplied
        // by the base travel time divided by the arc length
        double relativeTimeTravelled = 0.0;
        int currentStep = step - 1;

        // Find the departure step in the speed function
        assert currentStep >= 0;
        double distanceInCurrentStep =
                (stepTimes[currentStep + 1] - stepTimes[currentStep]) * speedFactors[currentStep];
        while(DoubleComparator.lessThan(relativeTimeTravelled + distanceInCurrentStep, baseTravelTime)){
            relativeTimeTravelled += distanceInCurrentStep;
            --currentStep;
            if (currentStep < 0) {
                // Any negative departure time will indicate the impossibility of this arrival time
                return -1.0;
            }

            distanceInCurrentStep = (stepTimes[currentStep + 1] - stepTimes[currentStep]) * speedFactors[currentStep];
        }

        return stepTimes[currentStep + 1] - (baseTravelTime - relativeTimeTravelled) / speedFactors[currentStep];
    }
}
