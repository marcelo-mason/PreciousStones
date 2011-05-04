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
 * @author cc_madelg
 */
@Entity()
@CacheStrategy
@Table(name = "snitch_entries")
public class SnitchEntry implements Serializable
{
    @NotNull
    private String name;

    @NotNull
    private String reason;

    @NotNull
    private String details;

    private int count;

    @Id
    private Long id;

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
	this.count = 1;
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
	if (getCount() > 1)
	{
	    return this.getReason() + " (" + getCount() + ")";
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
     * @return the count
     */
    public int getCount()
    {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count)
    {
        this.count = count;
    }

    /**
     *
     */
    public void addCount()
    {
	this.setCount(this.getCount() + 1);
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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
}
