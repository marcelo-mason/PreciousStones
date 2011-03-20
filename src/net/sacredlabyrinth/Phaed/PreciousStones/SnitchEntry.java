package net.sacredlabyrinth.Phaed.PreciousStones;

public class SnitchEntry
{
    private String name;
    private String datetime;
    
    public SnitchEntry(String name, String datetime)
    {
	this.name = name;
	this.datetime = datetime;
    }
    
    public String getName()
    {
	return this.name;
    }
    
    public String getDateTime()
    {
	return datetime;
    }
    
    @Override
    public int hashCode()
    {
	return (new String(name)).hashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    {
	if (!(obj instanceof SnitchEntry))
	    return false;
	
	SnitchEntry other = (SnitchEntry) obj;
	return other.name.equals(this.name);
    }
}
