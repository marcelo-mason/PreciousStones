package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * A field object
 * @author phaed
 */
public class Field extends AbstractVec implements Comparable<Field>
{
    private FieldSettings settings;
    private int radius;
    private int height;
    private float velocity;
    private int typeId;
    private String owner;
    private String name;
    private List<String> allowed = new ArrayList<String>();
    private List<DirtyFieldReason> dirty = new ArrayList<DirtyFieldReason>();
    private List<GriefBlock> grief = new ArrayList<GriefBlock>();
    private List<SnitchEntry> snitches = new LinkedList<SnitchEntry>();
    private long lastUsed;

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param radius
     * @param height
     * @param velocity
     * @param world
     * @param typeId
     * @param owner
     * @param name
     * @param lastUsed
     */
    public Field(int x, int y, int z, int radius, int height, float velocity, String world, int typeId, String owner, String name, long lastUsed)
    {
        super(x, y, z, world);

        this.radius = radius;
        this.height = height;
        this.velocity = velocity;
        this.owner = owner;
        this.name = name;
        this.typeId = typeId;
        this.lastUsed = lastUsed;
    }

    /**
     *
     * @param block
     * @param radius
     * @param height
     * @param owner
     */
    public Field(Block block, int radius, int height, String owner)
    {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

        this.radius = radius;
        this.height = height;
        this.owner = owner;
        this.name = "";
        this.typeId = block.getTypeId();
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
    }

    /**
     *
     * @param block
     */
    public Field(Block block)
    {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
    }

    /**
     *
     * @param radius
     */
    public void setRadius(int radius)
    {
        this.radius = radius;
        this.setHeight((this.radius * 2) + 1);
        dirty.add(DirtyFieldReason.RADIUS);
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
        dirty.add(DirtyFieldReason.OWNER);
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
        dirty.add(DirtyFieldReason.NAME);
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

        if (allowed.contains("*"))
        {
            return true;
        }

        if (allowed.contains(allowedName.toLowerCase()))
        {
            return true;
        }

        List<String> groups = PreciousStones.getInstance().getPermissionsManager().getGroups(getWorld(), allowedName);

        for (String group : groups)
        {
            if (allowed.contains("g:" + group))
            {
                return true;
            }
        }

        String clan = PreciousStones.getInstance().getSimpleClansManager().getClan(allowedName);

        if (clan != null)
        {
            if (allowed.contains("c:" + clan))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether the player was allowed
     * @param allowedName
     * @return confirmation
     */
    public boolean addAllowed(String allowedName)
    {
        if (isAllowed(allowedName))
        {
            return false;
        }

        allowed.add(allowedName.toLowerCase());
        dirty.add(DirtyFieldReason.ALLOWED);
        return true;
    }

    /**
     * Whether the player was removed
     * @param allowedName
     */
    public void removeAllowed(String allowedName)
    {
        allowed.remove(allowedName.toLowerCase());
        dirty.add(DirtyFieldReason.ALLOWED);
    }

    /**
     *
     * @return coordinates string
     */
    public String getCoords()
    {
        return super.toString();
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
        dirty.add(DirtyFieldReason.HEIGHT);
    }

    /**
     * @param typeId the typeId to set
     */
    public void setTypeId(int typeId)
    {
        this.typeId = typeId;
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
        int miny = getY() - (int) Math.floor(((double) Math.max(getHeight() - 1, 0)) / 2);
        int maxy = getY() + (int) Math.ceil(((double) Math.max(getHeight() - 1, 0)) / 2);

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
        int miny = getY() - (int) Math.floor(((double) Math.max(getHeight() - 1, 0)) / 2);
        int maxy = getY() + (int) Math.ceil(((double) Math.max(getHeight() - 1, 0)) / 2);

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
        int miny = getY() - (int) Math.floor(((double) Math.max(getHeight() - 1, 0)) / 2);
        int maxy = getY() + (int) Math.ceil(((double) Math.max(getHeight() - 1, 0)) / 2);

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
        dirty.add(DirtyFieldReason.VELOCITY);
    }

    /**
     * Mark for deletion
     */
    public void markForDeletion()
    {
        dirty.add(DirtyFieldReason.DELETE);
    }

    /**
     * @return the allowed
     */
    public List<String> getAllowed()
    {
        return Collections.unmodifiableList(allowed);
    }

    /**
     * @return the packedAllowed
     */
    public String getPackedAllowed()
    {
        return Helper.toMessage(allowed, "|");
    }

    /**
     * @param packedAllowed the packedAllowed to set
     */
    public void setPackedAllowed(String packedAllowed)
    {
        this.allowed = Helper.fromArray(packedAllowed.split("[|]"));
    }

    /**
     * ADd a grief block to the collection
     * @param gb
     */
    public void addGriefBlock(GriefBlock gb)
    {
        grief.add(gb);
        dirty.add(DirtyFieldReason.GRIEF_BLOCKS);
    }

    /**
     * Clear grief blocks
     */
    public void clearGrief()
    {
        grief.clear();
    }

    /**
     * @return the grief
     */
    public List<GriefBlock> getGrief()
    {
        return Collections.unmodifiableList(grief);
    }

    /**
     * Clear snitch list
     */
    public void clearSnitch()
    {
        snitches.clear();
    }

    /**
     * @return the snitches
     */
    public List<SnitchEntry> getSnitches()
    {
        return Collections.unmodifiableList(snitches);
    }

    /**
     * @param snitches the snitches to set
     */
    public void setSnitches(List<SnitchEntry> snitches)
    {
        this.snitches.clear();
        this.snitches.addAll(snitches);
    }

    /**
     *
     */
    public void updateLastUsed()
    {
        lastUsed = (new Date()).getTime();
        dirty.add(DirtyFieldReason.LASTUSED);
    }

    /**
     * Returns the number of days last used
     * @return
     */
    public int getAgeInDays()
    {
        if (lastUsed <= 0)
        {
            return 0;
        }

        return (int) Dates.differenceInDays(new Date(), new Date(lastUsed));
    }

    /**
     * @return the settings
     */
    public FieldSettings getSettings()
    {
        return settings;
    }

    /**
     * @param settings the settings to set
     */
    public void setSettings(FieldSettings settings)
    {
        this.settings = settings;
    }

    /**
     * Whether the item is dirty
     * @param dirtyType
     * @return
     */
    public boolean isDirty(DirtyFieldReason dirtyType)
    {
        return dirty.contains(dirtyType);
    }

    /**
     * Clear dirty items
     */
    public void clearDirty()
    {
        dirty.clear();
    }

    /**
     * Returns the distance between this field and a location
     * @param loc
     * @return
     */
    public double distance(Location loc)
    {
        return Math.sqrt(Math.pow(loc.getBlockX() - getX(), 2.0D) + Math.pow(loc.getBlockY() - getY(), 2.0D) + Math.pow(loc.getBlockZ() - getZ(), 2.0D));
    }

    public int compareTo(Field field) throws ClassCastException
    {
        int c = this.getX() - field.getX();

        if (c == 0)
        {
            c = this.getZ() - field.getZ();
        }

        if (c == 0)
        {
            c = this.getY() - field.getY();
        }

        if (c == 0)
        {
            this.getWorld().compareTo(field.getWorld());
        }

        return c;
    }
}
