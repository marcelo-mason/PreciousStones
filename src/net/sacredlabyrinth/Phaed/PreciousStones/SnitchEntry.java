package net.sacredlabyrinth.Phaed.PreciousStones;

/**
 *
 * @author cc_madelg
 */
public class SnitchEntry
{
    private String name;
    private String reason;
    private String details;
    private int count;

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
	this.count = 1;
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
     *
     * @return
     */
    public String getReasonDisplay()
    {
	if (count > 1)
	{
	    return this.reason + " (" + count + ")";
	}

	return this.reason;
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
    public String getDetails()
    {
	return this.details;
    }

    /**
     *
     */
    public void addCount()
    {
	this.count = this.count + 1;
    }

    @Override
    public String toString()
    {
	return (";" + name + "@" +  reason + "#" + details).replace("�", "?");
    }
}
