import java.util.Calendar;
import java.util.Date;

public class ProblemClock {
    // Multiply the time values by this multiplier to obtain milliseconds
    double msMultiplier;
    double skippedTime;
    Date startTime;
    Calendar calendar;

    public ProblemClock(double msMultiplier) {
        this.msMultiplier = msMultiplier;
        skippedTime = 0.0;
        startTime = new Date();
        calendar = Calendar.getInstance();
    }

    public double getCurrentProblemTime() {
        return (System.currentTimeMillis() - startTime.getTime()) / msMultiplier + skippedTime;
    }

    public void skip(double problemTime) {
        skippedTime += problemTime;
    }

    public Date convertProblemTimeToDate(double problemTime) {
        calendar.setTime(startTime);
        calendar.add(Calendar.MILLISECOND, (int) (problemTime * msMultiplier));
        return calendar.getTime();
    }
}
