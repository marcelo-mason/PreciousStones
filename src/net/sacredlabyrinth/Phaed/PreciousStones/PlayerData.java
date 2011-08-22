package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.HashMap;
import org.bukkit.Location;

/**
 *
 * @author phaed
 */
public class PlayerData
{
    private String name;
    private boolean disabled;
    private boolean online;
    private Location outsideLocation;
    private HashMap<Integer, Integer> fieldCount = new HashMap<Integer, Integer>();

    /**
     *
     * @param disabled
     */
    public PlayerData(boolean disabled)
    {
        this.disabled = disabled;
    }

    /**
     * Increment the field count of a specific field
     * @param title
     */
    public void incrementFieldCount(int typeid)
    {
        if (getFieldCount().containsKey(typeid))
        {
            getFieldCount().put(typeid, getFieldCount().get(typeid) + 1);
        }
        else
        {
            getFieldCount().put(typeid, 1);
        }
    }

    /**
     * Decrement the field count of a specific field
     * @param title
     */
    public void decrementFieldCount(int typeid)
    {
        if (getFieldCount().containsKey(typeid))
        {
            getFieldCount().put(typeid, Math.max(getFieldCount().get(typeid) - 1, 0));
        }
    }

    /**
     * @return the fieldCount
     */
    public HashMap<Integer, Integer> getFieldCount()
    {
        return fieldCount;
    }

    /**
     * Get the number of fields the player has placed
     * @param title
     * @return
     */
    public int getFieldCount(int typeid)
    {
        if (getFieldCount().containsKey(typeid))
        {
            return getFieldCount().get(typeid);
        }

        return 0;
    }

    /**
     *
     * @return
     */
    public boolean isDisabled()
    {
        return this.disabled;
    }

    /**
     *
     * @param disabled
     */
    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    /**
     * @return the online
     */
    public boolean isOnline()
    {
        return online;
    }

    /**
     * @param online the online to set
     */
    public void setOnline(boolean online)
    {
        this.online = online;
    }

    /**
     * @return the outsideLocation
     */
    public Location getOutsideLocation()
    {
        return outsideLocation;
    }

    /**
     * @param outsideLocation the outsideLocation to set
     */
    public void setOutsideLocation(Location outsideLocation)
    {
        this.outsideLocation = outsideLocation;
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
}
