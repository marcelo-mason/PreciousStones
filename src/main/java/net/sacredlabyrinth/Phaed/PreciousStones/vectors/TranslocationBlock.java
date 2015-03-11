package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.ItemStackEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.List;

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
    private List<ItemStackEntry> contents = new ArrayList<ItemStackEntry>();


    /**
     * @param x
     * @param y
     * @param z
     * @param world
     * @param type
     */
    public TranslocationBlock(int x, int y, int z, String world, BlockTypeEntry type)
    {
        super(x, y, z, world);
        this.type = type;
    }

    /**
     * @param loc
     * @param type
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
     * @param state
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
        type = new BlockTypeEntry(type.getTypeId(), data);
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
        World world = Bukkit.getServer().getWorld(getWorld());
        Location location = new Location(world, rx, ry, rz);
        return location;
    }

    public Location getRelativeLocation(Field currentField)
    {
        World world = Bukkit.getServer().getWorld(getWorld());

        Location location = new Location(world, rx, ry, rz);
        location = location.add(currentField.getLocation());

        return location;
    }

    public int getRx()
    {
        return rx;
    }

    public int getRy()
    {
        return ry;
    }

    public int getRz()
    {
        return rz;
    }

    private void extractContents()
    {

    }

    public ItemStack[] getItemStacks()
    {
        List<ItemStack> out = new ArrayList<ItemStack>();

        for (ItemStackEntry entry : contents)
        {
            out.add(entry.toItemStack());
        }

        return out.toArray(new ItemStack[]{});
    }

    public boolean hasItemStacks()
    {
        return !contents.isEmpty();
    }

    public void setContents(ItemStack[] stacks)
    {
        contents.clear();
        for (ItemStack stack : stacks)
        {
            if(stack == null)
            {
                contents.add(new ItemStackEntry(new ItemStack(Material.AIR)));
                continue;
            }
            contents.add(new ItemStackEntry(stack));
        }
    }

    public String getContents()
    {
        JSONArray out = new JSONArray();

        for (ItemStackEntry entry : contents)
        {
            out.add(entry.serialize());
        }

        return out.toString();
    }

    public void setContents(String contents)
    {
        if (contents.length() == 0)
        {
            return;
        }

        JSONArray in = (JSONArray) JSONValue.parse(contents);

        this.contents.clear();
        for (Object item : in)
        {
            this.contents.add(new ItemStackEntry((JSONObject) item));
        }
    }
}
