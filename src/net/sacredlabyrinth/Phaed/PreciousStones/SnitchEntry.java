package net.sacredlabyrinth.Phaed.PreciousStones;

public class SnitchEntry
{
    private String name;
    private String reason;
    private String details;
    
    public SnitchEntry(String name, String reason, String details)
    {
	this.name = name;
	this.reason = reason;
	this.details = details;
    }
    
    public String getName()
    {
	return this.name;
    }
    
    public String getReason()
    {
	return this.reason;
    }

    public String getDetails()
    {
	return this.details;
    }
}
