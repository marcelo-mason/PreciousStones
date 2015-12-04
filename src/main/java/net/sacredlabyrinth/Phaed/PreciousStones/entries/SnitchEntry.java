package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import org.bukkit.ChatColor;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 * @author phaed
 */
public class SnitchEntry {
    private String name;
    private String reason;
    private String details;
    private int eventCount;
    private Field field;
    private DateTime age;

    /**
     *
     */
    public SnitchEntry() {
    }

    /**
     * @param name
     * @param reason
     * @param details
     */
    public SnitchEntry(String name, String reason, String details) {
        this.name = name;
        this.reason = reason;
        this.details = details;
        this.eventCount = 1;
        this.age = (new DateTime());
    }

    /**
     * @param field
     * @param name
     * @param reason
     * @param details
     * @param eventCount
     */
    public SnitchEntry(Field field, String name, String reason, String details, int eventCount) {
        this.field = field;
        this.name = name;
        this.reason = reason;
        this.details = details;
        this.eventCount = eventCount;
        this.age = (new DateTime());
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * @return
     */
    public String getReasonDisplay() {
        String out = reason;

        if (reason.equals("Block Break")) {
            out = ChatColor.DARK_RED + reason;
        }
        if (reason.equals("Block Place")) {
            out = ChatColor.DARK_RED + reason;
        }
        if (reason.equals("Entry")) {
            out = ChatColor.BLUE + reason;
        }
        if (reason.equals("Used")) {
            out = ChatColor.GREEN + reason;
        }
        if (reason.equals("Shopped")) {
            out = ChatColor.GREEN + reason;
        }
        if (reason.equals("Ignite")) {
            out = ChatColor.DARK_RED + reason;
        }

        if (getEventCount() > 1) {
            return out + " (" + getEventCount() + ")";
        }

        return out;
    }

    /**
     * @param details the details to set
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * @return
     */
    public String getDetails() {
        return details;
    }

    /**
     *
     */
    public void addCount() {
        setEventCount(getEventCount() + 1);
    }

    /**
     * @return the eventCount
     */
    public int getEventCount() {
        return eventCount;
    }

    /**
     * @param eventCount the eventCount to set
     */
    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    /**
     * @return the field
     */
    public Field getField() {
        return field;
    }

    /**
     * @param field the field to set
     */
    public void setField(Field field) {
        this.field = field;
    }

    /**
     * Returns the number of minutes of age
     *
     * @return
     */
    public int getAgeInSeconds() {
        return Seconds.secondsBetween(new DateTime(), new DateTime(age)).getSeconds();
    }
}
