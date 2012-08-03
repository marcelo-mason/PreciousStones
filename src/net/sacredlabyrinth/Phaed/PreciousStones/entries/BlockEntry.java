package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * @author phaed
 */
public class BlockEntry
{
    private final int typeId;
    private final byte data;
    private final Location location;

    /**
     * @param block
     */
    public BlockEntry(Block block)
    {
        this.typeId = block.getTypeId();
        this.data = block.getData();
        this.location = block.getLocation();
    }

    /**
     * @param block
     */
    public BlockEntry(Location loc, int typeId, byte data)
    {
        this.typeId = typeId;
        this.data = data;
        this.location = loc;
    }

    /**
     * @return the typeId
     */
    public int getTypeId()
    {
        return typeId;
    }

    /**
     * @return the data
     */
    public byte getData()
    {
        return data;
    }

    /**
     * @return the location
     */
    public Location getLocation()
    {
        return location;
    }

    /**
     * @return the block
     */
    public Block getBlock()
    {
        return location.getWorld().getBlockAt(location);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof BlockEntry))
        {
            return false;
        }

        BlockEntry other = (BlockEntry) obj;
        return other.getTypeId() == this.getTypeId() && other.getData() == this.getData() && Helper.isSameBlock(this.getLocation(), other.getLocation());
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 47 * hash + this.getTypeId();
        hash = 47 * hash + this.getData();
        hash = 47 * hash + this.getLocation().getBlockX() + this.getLocation().getBlockY() + this.getLocation().getBlockZ();
        return hash;
    }

    @Override
    public String toString()
    {
        return "[" + getTypeId() + ":" + getData() + " " + Helper.toLocationString(location) + "]";
    }

    public BlockEntry(String packed)
    {
        String[] unpacked = packed.split("[|]");

        this.typeId = Integer.parseInt(unpacked[0]);
        this.data = Byte.parseByte(unpacked[1]);

        int x = Integer.parseInt(unpacked[2]);
        int y = Integer.parseInt(unpacked[3]);
        int z = Integer.parseInt(unpacked[4]);
        String world = unpacked[5].toString();

        World w = PreciousStones.getInstance().getServer().getWorld(world);

        this.location = new Location(w, x, y, z);
    }

    public String serialize()
    {
        return getTypeId() + "|" + getData() + "|" + location.getBlockX() + "|" + location.getBlockY() + "|" + location.getBlockZ() + "|" + location.getWorld();
    }

    public boolean isValid()
    {
        Material material = Material.getMaterial(getTypeId());
        return material != null;
    }
}
