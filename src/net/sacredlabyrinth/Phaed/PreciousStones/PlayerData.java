package net.sacredlabyrinth.Phaed.PreciousStones;

import org.bukkit.Location;
import org.stringtree.json.JSONReader;
import org.stringtree.json.JSONValidatingReader;
import org.stringtree.json.JSONWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author phaed
 */
public class PlayerData
{
    private String name;
    private boolean disabled;
    private boolean online;
    private int density;
    private Location outsideLocation;
    private Map<Integer, Integer> fieldCount = new HashMap<Integer, Integer>();

    /**
     * @param disabled
     */
    public PlayerData()
    {

        disabled = PreciousStones.getInstance().getSettingsManager().isOffByDefault();
        density = PreciousStones.getInstance().getSettingsManager().getVisualizeDensity();
    }

    /**
     * Increment the field count of a specific field
     *
     * @param typeid
     */
    public void incrementFieldCount(int typeid)
    {
        if (fieldCount.containsKey(typeid))
        {
            fieldCount.put(typeid, fieldCount.get(typeid) + 1);
        }
        else
        {
            fieldCount.put(typeid, 1);
        }
    }

    /**
     * Decrement the field count of a specific field
     *
     * @param typeid
     */
    public void decrementFieldCount(int typeid)
    {
        if (fieldCount.containsKey(typeid))
        {
            fieldCount.put(typeid, Math.max(fieldCount.get(typeid) - 1, 0));
        }
    }

    /**
     * @return the fieldCount
     */
    public HashMap<Integer, Integer> getFieldCount()
    {
        HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
        counts.putAll(fieldCount);

        return counts;
    }

    /**
     * Get the number of fields the player has placed
     *
     * @param typeid
     * @return
     */
    public int getFieldCount(int typeid)
    {
        if (fieldCount.containsKey(typeid))
        {
            return fieldCount.get(typeid);
        }

        return 0;
    }

    /**
     * @return
     */
    public boolean isDisabled()
    {
        return this.disabled;
    }

    /**
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

    /**
     * Return the list of flags and their data as a json string
     *
     * @return the flags
     */
    public String getFlags()
    {
        HashMap<String, Object> flags = new HashMap<String, Object>();

        // writing the list of flags to json

        flags.put("disabled", disabled);
        flags.put("density", density);

        return (new JSONWriter()).write(flags);
    }

    /**
     * Read the list of flags in from a json string
     *
     * @param flagString the flags to set
     */
    public void setFlags(String flagString)
    {
        if (flagString != null && !flagString.isEmpty())
        {
            JSONReader reader = new JSONValidatingReader();
            HashMap<String, Object> flags = (HashMap<String, Object>) reader.read(flagString);

            if (flags != null)
            {
                for (String flag : flags.keySet())
                {
                    // reading the list of flags from json

                    if (flag.equals("disabled"))
                    {
                        disabled = (Boolean) flags.get(flag);
                    }

                    if (flag.equals("subdivisions"))
                    {
                        density = ((Long) flags.get(flag)).intValue();
                    }
                }
            }
        }
    }

    public int getDensity()
    {
        return Math.max(density, 1);
    }

    public void setDensity(int density)
    {
        this.density = density;
    }
}
