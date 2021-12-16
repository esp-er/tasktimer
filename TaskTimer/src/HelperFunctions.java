package patriker.tasktimer;

public class HelperFunctions{
  public static String sectoHrMinSec(int seconds) {
    int hours = seconds / 60 / 60;
    int minutes = (seconds / 60) - (hours * 60);
    int secs = seconds % 60;
    return String.format("%02d:%02d:%02d", hours, minutes, secs);
  }

  public static float sectoHr(int seconds) {
    int hours = seconds / 60 / 60;
    int secs = 60 * ((seconds / 60) - (hours * 60)) + (seconds % 60);
    float frac = (float) hours + (((float) secs) / (float) 3600.0);
    return frac;
  }

  public static float sectoMin(int seconds) {
    return (float) (seconds / 60.0);
  }

  public static String secondsToMinSec(int seconds) {
    int minutes = seconds / 60;
    int secs = seconds - (minutes * 60);
    return String.format("%02d:%02d", minutes, secs);
  }

  public static int minSectoSeconds(String s) {
    String[] split = s.split(":");
    int mins = Integer.parseInt(split[0]);
    int secs = Integer.parseInt(split[1]);

    return (mins * 60) + secs;
  }

  public static String hrStr(int total){ return String.format("%.3f", sectoHr(total)) + " 시간"; }
  public static String minStr(int total){ return String.format("%.1f", sectoMin(total)) + " 분"; }
 
}
