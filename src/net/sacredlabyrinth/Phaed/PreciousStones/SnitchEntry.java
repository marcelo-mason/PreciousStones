package net.sacredlabyrinth.Phaed.PreciousStones;

import com.avaje.ebean.annotation.CacheStrategy;
import com.avaje.ebean.validation.NotNull;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

/**
 *
 * @author phaed
 */
@Entity()
@CacheStrategy
@Table(name = "ps_snitch_entries")
public class SnitchEntry implements Serializable
{
    @Id
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String reason;

    @NotNull
    private String details;

    private int eventCount;

    @ManyToOne
    private Field field;

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
     * Table identity column
     * @return the id
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Set the table identity column
     * @param id the id
     */
    public void setId(Long id)
    {
        this.id = id;
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
     * @return the field
     */
    public Field getField()
    {
        return field;
    }

    /**
     * @param field the field to set
     */
    public void setField(Field field)
    {
        this.field = field;
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
}
