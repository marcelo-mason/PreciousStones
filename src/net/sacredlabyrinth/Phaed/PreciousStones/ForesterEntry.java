package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

/**
 *
 * @author phaed
 */
public class ForesterEntry
{
    private Field field;
    private int count;
    private String playerName;
    private boolean landPrepared;
    private boolean processing;

    /**
     *
     * @param field
     * @param count
     */
    public ForesterEntry(Field field, String playerName)
    {
        this.field = field;
        this.playerName = playerName;
        this.count = 0;
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
     * Increment count
     */
    public void addCount()
    {
        count = count + 1 ;
    }

    @Override
    public int hashCode()
    {
	return ((new Integer(field.getX())).hashCode() >> 13) ^ ((new Integer(field.getY())).hashCode() >> 7) ^ ((new Integer(field.getZ())).hashCode()) ^ ((field.getWorld()).hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
	if (!(obj instanceof ForesterEntry))
	    return false;

	ForesterEntry other = (ForesterEntry) obj;
	return other.field.getX() == field.getX() && other.field.getY() == field.getY() && other.field.getZ() == field.getZ() && other.field.getWorld().equals(field.getWorld());
    }

    /**
     * @return the playerName
     */
    public String getPlayerName()
    {
        return playerName;
    }

    /**
     * @param playerName the playerName to set
     */
    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
    }

    /**
     * @return the landPrepared
     */
    public boolean isLandPrepared()
    {
        return landPrepared;
    }

    /**
     * @param landPrepared the landPrepared to set
     */
    public void setLandPrepared(boolean landPrepared)
    {
        this.landPrepared = landPrepared;
    }

    /**
     * @return the processing
     */
    public boolean isProcessing()
    {
        return processing;
    }

    /**
     * @param processing the processing to set
     */
    public void setProcessing(boolean processing)
    {
        this.processing = processing;
    }

}
