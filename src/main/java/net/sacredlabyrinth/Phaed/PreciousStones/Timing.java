package net.sacredlabyrinth.Phaed.PreciousStones;

import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Timing {
    private String title;
    private Map<String, Long> times = new HashMap<>();

    public Timing(String title) {
        this.title = title;
        poll("start");
    }

    public void poll(String name) {
        times.put(name, System.currentTimeMillis());
    }

    public void last(String name) {
        poll(name);
        printResults();
    }

    public void printResults() {
        Long previousTime = -9999L;

        PreciousStones.debug("----------------------------------------------------------------------");
        PreciousStones.debug(title + " Timings");
        PreciousStones.debug("----------------------------------------------------------------------");

        for ( Entry<String, Long> timeObj : times.entrySet()) {
            Long time = timeObj.getValue();

            if (previousTime == -9999L) {
                previousTime = time;
                continue;
            }

            long duration = Math.max(time - previousTime, 0);
            String friendlyDuration = DurationFormatUtils.formatDuration(duration, "ss.SSS");

            PreciousStones.debug(friendlyDuration + "  " + timeObj.getKey());
            previousTime = time;
        }

        PreciousStones.debug("----------------------------------------------------------------------");
        times.clear();
    }

    @Override
    public void finalize() throws Throwable {
        if (!times.isEmpty()) {
            printResults();
        }

        super.finalize();
    }
}
