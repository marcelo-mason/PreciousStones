package net.sacredlabyrinth.Phaed.PreciousStones;

/**
 *
 * @author phaed
 */
public class SnitchEntry
{
    private String name;
    private String reason;
    private String details;
    private int eventCount;

    /**
     *
     */
    public SnitchEntry()
    {
    }

    /**
     *
     * @param name
     * @param reason
     * @param details
     */
    public SnitchEntry(String name, String reason, String details)
    {
        this.name = name;
        this.reason = reason;
        this.details = details;
        this.eventCount = 1;
    }

    /**
     *
     * @param name
     * @param reason
     * @param details
     */
    public SnitchEntry(String packed)
    {
        String[] split = packed.split("[#]");

        if (split.length == 4)
        {
            this.name = split[0];
            this.reason = split[1];
            this.details = split[2];
            this.eventCount = Integer.parseInt(split[3]);
        }
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason)
    {
        this.reason = reason;
    }

    /**
     *
     * @return
     */
    public String getReason()
    {
        return this.reason;
    }

    /**
     *
     * @return
     */
    public String getReasonDisplay()
    {
        if (getEventCount() > 1)
        {
            return this.getReason() + " (" + getEventCount() + ")";
        }

        return this.getReason();
    }

    /**
     * @param details the details to set
     */
    public void setDetails(String details)
    {
        this.details = details;
    }

    /**
     *
     * @return
     */
    public String getDetails()
    {
        return this.details;
    }

    /**
     *
     */
    public void addCount()
    {
        this.setEventCount(this.getEventCount() + 1);
    }

    /**
     * @return the eventCount
     */
    public int getEventCount()
    {
        return eventCount;
    }

    /**
     * @param eventCount the eventCount to set
     */
    public void setEventCount(int eventCount)
    {
        this.eventCount = eventCount;
    }

    @Override
    public String toString()
    {
        return name + "#" + reason + "#" + details + "#" + eventCount;
    }
}
