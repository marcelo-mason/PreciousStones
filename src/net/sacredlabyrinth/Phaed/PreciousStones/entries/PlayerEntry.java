package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author phaed
 */
public class PlayerEntry
{
    private String name;
    private boolean disabled;
    private boolean online;
    private int density;
    private boolean superduperpickaxe;
    private Location outsideLocation;
    private Map<BlockTypeEntry, Integer> fieldCount = new HashMap<BlockTypeEntry, Integer>();

    /**
     * @param disabled
     */
    public PlayerEntry()
    {
        disabled = PreciousStones.getInstance().getSettingsManager().isOffByDefault();
        density = PreciousStones.getInstance().getSettingsManager().getVisualizeDensity();
    }

    /**
     * Increment the field count of a specific field
     *
     * @param typeid
     */
    public void incrementFieldCount(BlockTypeEntry type)
    {
        if (fieldCount.containsKey(type))
        {
            fieldCount.put(type, fieldCount.get(type) + 1);
        }
        else
        {
            fieldCount.put(type, 1);
        }
    }

    /**
     * Decrement the field count of a specific field
     *
     * @param typeid
     */
    public void decrementFieldCount(BlockTypeEntry type)
    {
        if (fieldCount.containsKey(type))
        {
            fieldCount.put(type, Math.max(fieldCount.get(type) - 1, 0));
        }
    }

    /**
     * @return the fieldCount
     */
    public HashMap<BlockTypeEntry, Integer> getFieldCount()
    {
        HashMap<BlockTypeEntry, Integer> counts = new HashMap<BlockTypeEntry, Integer>();
        counts.putAll(fieldCount);

        return counts;
    }

    /**
     * Get the number of fields the player has placed
     *
     * @param typeid
     * @return
     */
    public int getFieldCount(BlockTypeEntry type)
    {
        if (fieldCount.containsKey(type))
        {
            return fieldCount.get(type);
        }

        return 0;
    }

    /**
     * Get the total number of fields the player has placed
     *
     * @param typeid
     * @return
     */
    public int getTotalFieldCount()
    {
        int total = 0;

        for (int count : fieldCount.values())
        {
            total += count;
        }

        return total;
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
        JSONObject json = new JSONObject();

        // writing the list of flags to json

        if (superduperpickaxe)
        {
            json.put("superduperpickaxe", superduperpickaxe);
        }

        if (disabled)
        {
            json.put("disabled", disabled);
        }


        json.put("density", density);

        return json.toString();
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
            Object obj = JSONValue.parse(flagString);
            JSONObject flags = (JSONObject) obj;

            if (flags != null)
            {
                for (Object flag : flags.keySet())
                {
                    try
                    {
                        // reading the list of flags from json

                        if (flag.equals("disabled"))
                        {
                            disabled = (Boolean) flags.get(flag);
                        }

                        if (flag.equals("superduperpickaxe"))
                        {
                            superduperpickaxe = (Boolean) flags.get(flag);
                        }

                        if (flag.equals("subdivisions"))
                        {
                            density = ((Long) flags.get(flag)).intValue();
                        }
                    }
                    catch (Exception ex)
                    {
                        for (StackTraceElement el : ex.getStackTrace())
                        {
                            System.out.print("Failed reading flag: " + flag);
                            System.out.print("Value: " + flags.get(flag));
                            System.out.print(el.toString());
                        }
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
    public boolean isSuperduperpickaxe()
    {
        return superduperpickaxe;
    }
    public void setSuperduperpickaxe(boolean superduperpickaxe)
    {
        this.superduperpickaxe = superduperpickaxe;
    }
}
