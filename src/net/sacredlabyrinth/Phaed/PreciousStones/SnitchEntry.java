package net.sacredlabyrinth.Phaed.PreciousStones;

public class SnitchEntry
{
    private String name;
    private String reason;
    private String details;
    private int count;
    
    public SnitchEntry(String name, String reason, String details)
    {
	this.name = name;
	this.reason = reason;
	this.details = details;
	this.count = 1;
    }
    
    public String getName()
    {
	return this.name;
    }
    
    public String getReasonDisplay()
    {
	if(count > 1)
	{
	    return this.reason + " (" + count + ")";
	}
	
	return this.reason;
    }

    public String getReason()
    {
	return this.reason;
    }
    
    public String getDetails()
    {
	return this.details;
    }
    
    public void addCount()
    {
	this.count = this.count + 1;
    }
}
