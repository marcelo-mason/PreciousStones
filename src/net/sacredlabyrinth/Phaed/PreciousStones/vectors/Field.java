package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import java.util.List;
import java.util.ArrayList;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import net.sacredlabyrinth.Phaed.PreciousStones.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import org.bukkit.Location;

/**
 * A field object
 * @author phaed
 */
public class Field extends AbstractVec
{
    private int radius;
    private int height;
    private float velocity;
    private int typeId;
    private String owner;
    private String name;
    private List<String> allowed = new ArrayList<String>();
    private List<SnitchEntry> snitchList = new ArrayList<SnitchEntry>();
    private boolean dirty;

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param radius
     * @param height
     * @param world
     * @param typeId
     * @param owner
     * @param name
     */
    public Field(int x, int y, int z, int radius, int height, float velocity, String world, int typeId, String owner, String name)
    {
        super(x, y, z, world);

        this.radius = radius;
        this.height = height;
        this.velocity = velocity;
        this.owner = owner;
        this.name = name;
        this.typeId = typeId;
        this.dirty = true;
    }

    /**
     *
     * @param block
     * @param radius
     * @param height
     * @param owner
     * @param name
     */
    public Field(Block block, int radius, int height, String owner)
    {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

        this.radius = radius;
        this.height = height;
        this.owner = owner;
        this.name = "";
        this.typeId = block.getTypeId();
        this.dirty = true;
    }

    /**
     *
     * @param block
     * @param radius
     * @param height
     */
    public Field(Block block, int radius, int height)
    {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

        this.radius = radius;
        this.height = height;
        this.name = "";
        this.owner = "";
        this.typeId = block.getTypeId();
        this.dirty = true;
    }

    /**
     *
     * @param block
     */
    public Field(Block block)
    {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

        this.dirty = true;
    }

    /**
     *
     * @param radius
     */
    public void setRadius(int radius)
    {
        this.radius = radius;
        this.setHeight((this.radius * 2) + 1);
        this.dirty = true;
    }

    /**
     *
     * @return the block type id
     */
    public int getTypeId()
    {
        return this.typeId;
    }

    /**
     *
     * @return the block type name
     */
    public String getType()
    {
        return Material.getMaterial(this.getTypeId()).toString();
    }

    /**
     *
     * @return the radius
     */
    public int getRadius()
    {
        return this.radius;
    }

    /**
     *
     * @return the height
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     *
     * @return the owner
     */
    public String getOwner()
    {
        return this.owner;
    }

    /**
     *
     * @param owner
     */
    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    /**
     *
     * @param playerName
     * @return
     */
    public boolean isOwner(String playerName)
    {
        return owner.equalsIgnoreCase(playerName);
    }

    /**
     * Set the name value
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Set the name of the field, mark for database save
     * @param name
     */
    public void setFieldName(String name)
    {
        this.name = name;
        this.dirty = true;
    }

    /**
     *
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     *
     * @param name
     * @return
     */
    public boolean isName(String name)
    {
        if (name == null)
        {
            return false;
        }

        return this.name.equalsIgnoreCase(name);
    }

    /**
     *
     * @return
     */
    public List<String> getAllAllowed()
    {
        List<String> all = new ArrayList<String>();
        all.add(owner.toLowerCase());
        all.addAll(allowed);
        return all;
    }

    /**
     *
     * @return
     */
    public String getAllowedList()
    {
        String out = "";

        if (allowed.size() > 0)
        {
            for (int i = 0; i < allowed.size(); i++)
            {
                out += ", " + allowed.get(i);
            }
        }
        else
        {
            return null;
        }

        return out.substring(2);
    }

    /**
     *
     * @param allowedName
     * @return
     */
    public boolean isAllowed(String allowedName)
    {
        if (allowedName.equalsIgnoreCase(owner))
        {
            return true;
        }

        return allowed.contains(allowedName.toLowerCase()) || allowed.contains("*");
    }

    /**
     * Whether the player was allowed
     * @param allowedName
     * @param perm
     * @return confirmation
     */
    public boolean addAllowed(String allowedName)
    {
        if (isAllowed(allowedName))
        {
            return false;
        }

        allowed.add(allowedName.toLowerCase());
        this.dirty = true;
        return true;
    }

    /**
     * Whether the player was removed
     * @param allowedName
     * @return confirmation
     */
    public void removeAllowed(String allowedName)
    {
        allowed.remove(allowedName.toLowerCase());
        this.dirty = true;
    }

    /**
     *
     * @return coordinates string
     */
    public String getCoords()
    {
        return super.toString();
    }

    /**
     *
     * @param name
     * @param reason
     * @param details
     */
    public void addIntruder(String name, String reason, String details)
    {
        for (SnitchEntry se : snitchList)
        {
            if (se.getName().equals(name) && se.getReason().equals(reason) && se.getDetails().equals(details))
            {
                se.addCount();
                return;
            }
        }

        snitchList.add(new SnitchEntry(name, reason, details));
        this.dirty = true;
    }

    /**
     * @param snitchList the snitchList to set
     */
    public void setSnitchList(List<SnitchEntry> snitchList)
    {
        this.snitchList = snitchList;
    }

    /**
     *
     * @return
     */
    public List<SnitchEntry> getSnitchList()
    {
        return snitchList;
    }

    /**
     *
     */
    public void cleanSnitchList()
    {
        snitchList.clear();
        this.dirty = true;
    }

    @Override
    public String toString()
    {
        return super.toString() + " [" + getOwner() + "]";
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height)
    {
        this.height = height;
        this.dirty = true;
    }

    /**
     * @param typeId the typeId to set
     */
    public void setTypeId(int typeId)
    {
        this.typeId = typeId;
    }

    /**
     * @return the chunkvec
     */
    public ChunkVec toChunkVec()
    {
        return new ChunkVec(getX() >> 4, getZ() >> 4, getWorld());
    }

    /**
     * @return the vec
     */
    public Vec toVec()
    {
        return new Vec(this);
    }

    /**
     *
     * @return vectors of the corners
     */
    public ArrayList<Vector> getCorners()
    {
        ArrayList<Vector> corners = new ArrayList<Vector>();

        int minx = getX() - getRadius();
        int maxx = getX() + getRadius();
        int minz = getZ() - getRadius();
        int maxz = getZ() + getRadius();
        int miny = getY() - (int) Math.floor(((double) getHeight()) / 2);
        int maxy = getY() + (int) Math.ceil(((double) getHeight()) / 2);

        corners.add(new Vector(minx, miny, minz));
        corners.add(new Vector(minx, miny, maxz));
        corners.add(new Vector(minx, maxy, minz));
        corners.add(new Vector(minx, maxy, maxz));
        corners.add(new Vector(maxx, miny, minz));
        corners.add(new Vector(maxx, miny, maxz));
        corners.add(new Vector(maxx, maxy, minz));
        corners.add(new Vector(maxx, maxy, maxz));

        return corners;
    }

    /**
     * Whether the fields intersect
     * @param field
     * @return confirmation
     */
    public boolean intersects(Field field)
    {
        if (!field.getWorld().equals(getWorld()))
        {
            return false;
        }

        ArrayList<Vector> corners = field.getCorners();

        for (Vector vec : corners)
        {
            if (this.envelops(vec))
            {
                return true;
            }
        }

        corners = this.getCorners();

        for (Vector vec : corners)
        {
            if (field.envelops(vec))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether vector is enveloped by the field
     * @param vec
     * @return confirmation
     */
    public boolean envelops(Vector vec)
    {
        int px = vec.getBlockX();
        int py = vec.getBlockY();
        int pz = vec.getBlockZ();

        int minx = getX() - getRadius();
        int maxx = getX() + getRadius();
        int minz = getZ() - getRadius();
        int maxz = getZ() + getRadius();
        int miny = getY() - (int) Math.floor(((double) getHeight()) / 2);
        int maxy = getY() + (int) Math.ceil(((double) getHeight()) / 2);

        if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
        {
            return true;
        }

        return false;
    }

    /**
     * Whether field block is enveloped by the field
     * @param field
     * @return confirmation
     */
    public boolean envelops(AbstractVec field)
    {
        int px = field.getX();
        int py = field.getY();
        int pz = field.getZ();

        int minx = getX() - getRadius();
        int maxx = getX() + getRadius();
        int minz = getZ() - getRadius();
        int maxz = getZ() + getRadius();
        int miny = getY() - (int) Math.floor(((double) getHeight()) / 2);
        int maxy = getY() + (int) Math.ceil(((double) getHeight()) / 2);

        if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
        {
            return true;
        }

        return false;
    }

    /**
     * Whether block is enveloped by the field
     * @param block
     * @return confirmation
     */
    public boolean envelops(Block block)
    {
        return envelops(new Vec(block));
    }

    /**
     * Whether location is enveloped by the field
     * @param loc
     * @return confirmation
     */
    public boolean envelops(Location loc)
    {
        return envelops(new Vec(loc));
    }

    /**
     * @return the dirty
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * @param dirty the dirty to set
     */
    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
    }

    /**
     * @return the velocity
     */
    public float getVelocity()
    {
        return velocity;
    }

    /**
     * @param velocity the velocity to set
     */
    public void setVelocity(float velocity)
    {
        this.velocity = velocity;
        this.dirty = true;
    }

    /**
     * @return the allowed
     */
    public List<String> getAllowed()
    {
        return allowed;
    }

    /**
     * @param allowed the allowed to set
     */
    public void setAllowed(List<String> allowed)
    {
        this.allowed = allowed;
    }

    /**
     * @return the packedAllowed
     */
    public String getPackedAllowed()
    {
        return Helper.toMessage(allowed, "|");
    }

    /**
     * @return the packedSnitchList
     */
    public String getPackedSnitchList()
    {
        String packed = "";

        for (SnitchEntry se : snitchList)
        {
            packed += se + "|";
        }

        return Helper.stripTrailing(packed, "|");
    }

    /**
     * @param packedAllowed the packedAllowed to set
     */
    public void setPackedAllowed(String packedAllowed)
    {
        this.allowed = Helper.fromArray(packedAllowed.split("[|]"));
    }

    /**
     * @param packedSnitchList the packedSnitchList to set
     */
    public void setPackedSnitchList(String packedSnitchList)
    {
        List<String> packedSnitchLists = Helper.fromArray(packedSnitchList.split("[|]"));

        for (String packed : packedSnitchLists)
        {
            snitchList.add(new SnitchEntry(packed));
        }
    }
}
