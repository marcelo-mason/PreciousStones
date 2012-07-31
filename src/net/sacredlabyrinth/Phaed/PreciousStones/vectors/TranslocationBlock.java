package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * @author phaed
 */
public class TranslocationBlock extends AbstractVec
{
    private BlockTypeEntry type;
    private String signText = "";
    private boolean empty = false;
    private int rx;
    private int ry;
    private int rz;


    /**
     * @param x
     * @param y
     * @param z
     * @param world
     * @param typeId
     * @param data
     */
    public TranslocationBlock(int x, int y, int z, String world, BlockTypeEntry type)
    {
        super(x, y, z, world);
        this.type = type;
    }

    /**
     * @param loc
     * @param typeId
     * @param data
     */
    public TranslocationBlock(Location loc, BlockTypeEntry type)
    {
        super(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
        this.type = type;
    }

    /**
     * @param block
     */
    public TranslocationBlock(Block block)
    {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
        this.type = new BlockTypeEntry(block.getTypeId(), block.getData());
    }

    /**
     * @param block
     */
    public TranslocationBlock(Field field, Block block)
    {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
        this.type = new BlockTypeEntry(block.getTypeId(), block.getData());
        setRelativeCoords(field);
    }

    /**
     * @param block
     */
    public TranslocationBlock(BlockState state)
    {
        super(state.getX(), state.getY(), state.getZ(), state.getWorld().getName());
        this.type = new BlockTypeEntry(state.getTypeId(), state.getRawData());
        this.empty = true;
    }

    /**
     * @return the typeId
     */
    public int getTypeId()
    {
        return type.getTypeId();
    }

    /**
     * @return the data
     */
    public byte getData()
    {
        return type.getData();
    }

    /**
     * @return the data
     */
    public void setData(byte data)
    {
        type.setData(data);
    }

    /**
     * @return the signText
     */
    public String getSignText()
    {
        return signText;
    }

    /**
     * @param signText the signText to set
     */
    public void setSignText(String signText)
    {
        this.signText = signText;
    }

    public boolean isEmpty()
    {
        return empty;
    }

    public void setEmpty(boolean empty)
    {
        this.empty = empty;
    }

    public TranslocationBlock(String packed)
    {
        super(Helper.locationFromPacked(packed).getBlockX(), Helper.locationFromPacked(packed).getBlockY(), Helper.locationFromPacked(packed).getBlockZ(), Helper.locationFromPacked(packed).getWorld().getName());

        String[] unpacked = packed.split("[|]");

        this.type = new BlockTypeEntry(Integer.parseInt(unpacked[0]), Byte.parseByte(unpacked[1]));
    }

    public String serialize()
    {
        return getTypeId() + "|" + getData() + "|" + getLocation().getBlockX() + "|" + getLocation().getBlockY() + "|" + getLocation().getBlockZ() + "|" + getLocation().getWorld();
    }

    public void setRelativeCoords(int x, int y, int z)
    {
        this.rx = x;
        this.ry = y;
        this.rz = z;
    }

    public void setRelativeCoords(Field field)
    {
        Location location = getLocation();
        location = location.subtract(field.getLocation());

        this.rx = location.getBlockX();
        this.ry = location.getBlockY();
        this.rz = location.getBlockZ();
    }

    public Location getRelativeLocation()
    {
        World world = PreciousStones.getInstance().getServer().getWorld(getWorld());
        Location location = new Location(world, rx, ry, rz);
        return location;
    }

    public Location getRelativeLocation(Field currentField)
    {
        World world = PreciousStones.getInstance().getServer().getWorld(getWorld());

        Location location = new Location(world, rx, ry, rz);
        location = location.add(currentField.getLocation());

        return location;
    }

    public int getRx()
    {
        return rx;
    }

    public void setRx(int rx)
    {
        this.rx = rx;
    }

    public int getRy()
    {
        return ry;
    }

    public void setRy(int ry)
    {
        this.ry = ry;
    }

    public int getRz()
    {
        return rz;
    }

    public void setRz(int rz)
    {
        this.rz = rz;
    }
}
