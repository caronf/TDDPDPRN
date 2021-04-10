import java.util.ArrayList;
import java.util.Comparator;

public class PiecewiseArrivalTimeFunction extends ArrivalTimeFunction {
    public ArrayList<double[]> points;

    public PiecewiseArrivalTimeFunction() {
        points = new ArrayList<>();
    }

    public PiecewiseArrivalTimeFunction(double[] stepTimes, double baseTravelTime, double[] speedFactors) {
        points = new ArrayList<>(stepTimes.length * 2 - 1);
        points.add(new double[] {0.0, getNeighborArrivalTime(baseTravelTime, 0, stepTimes, speedFactors)});

        for (int i = 1; i < stepTimes.length; ++i) {
            // Arrival at current step time
            double departureTime = getNeighborDepartureTime(baseTravelTime, i, stepTimes, speedFactors);
            double arrivalTime = stepTimes[i];

            if (departureTime < 0.0) {
                // Adjust point so that the departure time is within the domain
                double adjustment = stepTimes[stepTimes.length - 1] *
                        Math.ceil(-departureTime / stepTimes[stepTimes.length - 1]);
                departureTime += adjustment;
                arrivalTime += adjustment;
            }

            points.add(new double[] {departureTime, arrivalTime});

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

        // The travel time should be the same for the first and last point
        assert DoubleComparator.equal(points.get(0)[1] - points.get(0)[0],
                points.get(points.size() - 1)[1] - points.get(points.size() - 1)[0]);
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
            double arrivalTime = point[1];

            if (departureTime < 0.0) {
                // Adjust point so that the departure time is within the domain
                double adjustment =
                        piecewiseArrivalTimeFunction1.points.get(piecewiseArrivalTimeFunction1.points.size() - 1)[0] *
                                Math.ceil(-departureTime / piecewiseArrivalTimeFunction1.points.get(
                                        piecewiseArrivalTimeFunction1.points.size() - 1)[0]);
                departureTime += adjustment;
                arrivalTime += adjustment;
            }

            points.add(new double[] {departureTime, arrivalTime});
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

        // The travel time should be the same for the first and last point
        assert DoubleComparator.equal(points.get(0)[1] - points.get(0)[0],
                points.get(points.size() - 1)[1] - points.get(points.size() - 1)[0]);
    }

    @Override
    public double getArrivalTime(double departureTime) {
        assert departureTime >= 0.0;
        double departureTimeInDomain = departureTime % points.get(points.size() - 1)[0];

        int previousPoint = 0;
        int nextPoint = points.size() - 1;
        while (previousPoint < nextPoint - 1) {
            int medianPoint = (previousPoint + nextPoint) / 2;
            if (DoubleComparator.lessThan(departureTimeInDomain, points.get(medianPoint)[0])) {
                nextPoint = medianPoint;
            } else {
                previousPoint = medianPoint;
            }
        }

        assert previousPoint + 1 == nextPoint;

        // Perform a linear interpolation between previousPoint and nextPoint
        assert DoubleComparator.greaterThan(points.get(nextPoint)[0], points.get(previousPoint)[0]);
        double arrivalTime = points.get(previousPoint)[1] +
                (departureTimeInDomain - points.get(previousPoint)[0]) *
                (points.get(nextPoint)[1] - points.get(previousPoint)[1]) /
                (points.get(nextPoint)[0] - points.get(previousPoint)[0]);
        arrivalTime += departureTime - departureTimeInDomain;
        assert DoubleComparator.greaterOrEqual(arrivalTime, departureTime);

        return arrivalTime;
    }

    @Override
    public double getDepartureTime(double arrivalTime) {
        assert arrivalTime >= 0.0;
        double codomainSpan = points.get(points.size() - 1)[1] - points.get(0)[1];

        // Should be (arrivalTime - points.get(0)[1]) % codomainSpan + points.get(0)[1]
        // but the extra operations ensure that the modulo doesn't give a negative result
        double arrivalTimeInCodomain =
                ((arrivalTime - points.get(0)[1]) % codomainSpan + codomainSpan) % codomainSpan + points.get(0)[1];

        int previousPoint = 0;
        int nextPoint = points.size() - 1;
        while (previousPoint < nextPoint - 1) {
            int medianPoint = (previousPoint + nextPoint) / 2;
            if (DoubleComparator.lessThan(arrivalTimeInCodomain, points.get(medianPoint)[1])) {
                nextPoint = medianPoint;
            } else {
                previousPoint = medianPoint;
            }
        }

        assert previousPoint + 1 == nextPoint;

        // Perform a linear interpolation between previousPoint and nextPoint
        assert DoubleComparator.greaterThan(points.get(nextPoint)[1], points.get(previousPoint)[1]);
        double departureTime = points.get(previousPoint)[0] +
                (arrivalTimeInCodomain - points.get(previousPoint)[1]) *
                (points.get(nextPoint)[0] - points.get(previousPoint)[0]) /
                        (points.get(nextPoint)[1] - points.get(previousPoint)[1]);
        departureTime += arrivalTime - arrivalTimeInCodomain;
        assert DoubleComparator.lessOrEqual(departureTime, arrivalTime);

        return departureTime;
    }

    private static double getNeighborArrivalTime(double baseTravelTime, int step,
                                                 double[] stepTimes, double[] speedFactors) {
        double relativeTimeTravelled = 0.0;
        int nbCompleteDays = step / (stepTimes.length - 1);
        int currentStep = step % (stepTimes.length - 1);

        // Find the final step in the speed function
        double distanceInCurrentStep = (stepTimes[currentStep + 1] - stepTimes[currentStep]) *
                speedFactors[currentStep];
        while(DoubleComparator.lessThan(relativeTimeTravelled + distanceInCurrentStep, baseTravelTime)){
            relativeTimeTravelled += distanceInCurrentStep;

            if(currentStep == stepTimes.length - 2){
                currentStep = 0;
                ++nbCompleteDays;
            } else {
                ++currentStep;
            }

            distanceInCurrentStep = (stepTimes[currentStep + 1] - stepTimes[currentStep]) *
                    speedFactors[currentStep % speedFactors.length];
        }

        return stepTimes[currentStep] +
                (baseTravelTime - relativeTimeTravelled) / speedFactors[currentStep] +
                nbCompleteDays * stepTimes[stepTimes.length - 1];
    }

    private static double getNeighborDepartureTime(double baseTravelTime, int step,
                                                   double[] stepTimes, double[] speedFactors) {
        double endOfTheDay = stepTimes[stepTimes.length - 1];
        double relativeTimeTravelled = 0.0;
        int nbCompleteDays = 0;
        int currentStep = step;

        // Find the final step in the speed function
        double distanceInCurrentStep = (stepTimes[currentStep] - stepTimes[currentStep - 1]) *
                speedFactors[currentStep - 1];
        while(DoubleComparator.lessThan(relativeTimeTravelled + distanceInCurrentStep, baseTravelTime)){
            relativeTimeTravelled += distanceInCurrentStep;

            if(currentStep == 1){
                currentStep = stepTimes.length - 1;
                ++nbCompleteDays;
            } else {
                --currentStep;
            }

            distanceInCurrentStep = (stepTimes[currentStep] - stepTimes[currentStep - 1]) *
                    speedFactors[currentStep - 1];
        }

        return stepTimes[currentStep] -
                (baseTravelTime - relativeTimeTravelled) / speedFactors[currentStep - 1] -
                nbCompleteDays * endOfTheDay;
    }
}
