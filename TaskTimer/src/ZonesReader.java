package patriker.tasktimer;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.net.URISyntaxException;
import java.net.URI;


public class ZonesReader {
    private TaskTimeZone[] zones;

    public ZonesReader(){
      Stream<String> l = 
        new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("timezones.txt"))).lines();
      zones = l.map(s -> new TaskTimeZone(ZoneId.of(s.split(" ", 2)[0]), s.split(" ", 2)[1])).toArray(TaskTimeZone[]::new);
    }

    public int getNumZones(){
        return zones.length;
    }

    public TaskTimeZone[] getZones(){
        return zones;
    }
}

