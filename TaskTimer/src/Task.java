package patriker.tasktimer;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Task {
    String project;
    String tz;
    ZonedDateTime taskDate;
    int taskTime;

    public Task(String projectName, int seconds, TaskTimeZone zone) {
        project = projectName;
        taskDate = ZonedDateTime.now(zone.getZoneId());
        taskTime = seconds;
        tz = zone.getName();
    }

    public String getDateStr() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss");
        return taskDate.format(f);
    }

    public String toString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss");
        return taskDate.format(f) + " (" + tz + ") " + "- Project: " + project + " -  " + Integer.toString(taskTime)
                + " sec";
    }
}
