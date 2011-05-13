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
@Table(name = "ps_allowed_players")
public class AllowedEntry implements Serializable
{
    @Id
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String perm;

    @ManyToOne
    private Field field;

    /**
     *
     * @param name
     * @param perm
     */
    public AllowedEntry(String name, String perm)
    {
        this.name = name;
        this.perm = perm;
    }

    /**
     *
     */
    public AllowedEntry()
    {

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
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the perm
     */
    public String getPerm()
    {
        return perm;
    }

    /**
     * @param perm the perm to set
     */
    public void setPerm(String perm)
    {
        this.perm = perm;
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

    @Override
    public int hashCode()
    {
	return ((name).hashCode() >> 13);
    }

    @Override
    public boolean equals(Object obj)
    {
	if (!(obj instanceof AllowedEntry))
	    return false;

	AllowedEntry other = (AllowedEntry) obj;
	return other.name.equals(this.name);
    }

    @Override
    public String toString()
    {
	return name + (perm.equals("all") ? "" : "(" + perm + ")");
    }
}
