package patriker.tasktimer;

import java.time.ZonedDateTime;
import java.time.ZoneId;

public class TaskTimeZone{
    private ZoneId id;
    private String name;

    public TaskTimeZone(ZoneId z, String s){
        id = z;
        name = s;
    }
    public String getName(){
        return name;
    }
    public void setName(String n){
        name = n;
    }
    public ZoneId getZoneId(){
        return id;
    }

    @Override
    public String toString(){
        return name;
    }
    
}