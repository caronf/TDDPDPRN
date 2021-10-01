import java.util.Calendar;
import java.util.Date;

public class ProblemClock {
    // Multiply the time values by this multiplier to obtain milliseconds
    double msMultiplier;
    Date startTime;
    Calendar calendar;

    public ProblemClock(double msMultiplier) {
        this.msMultiplier = msMultiplier;
        startTime = new Date();
        calendar = Calendar.getInstance();
    }

    public double getCurrentProblemTime() {
        return (System.currentTimeMillis() - startTime.getTime()) / msMultiplier;
    }

    public Date convertProblemTimeToDate(double problemTime) {
        calendar.setTime(startTime);
        calendar.add(Calendar.MILLISECOND, (int) (problemTime * msMultiplier));
        return calendar.getTime();
    }

    public long convertProblemTimeToMs(double problemTime) {
        return (long) (problemTime * msMultiplier);
    }
}
