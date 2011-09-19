package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * A field object
 *
 * @author phaed
 */
public class Field extends AbstractVec implements Comparable<Field>
{
    private FieldSettings settings;
    private long id = 0;
    private int radius;
    private int height;
    private int maxx;
    private int maxy;
    private int maxz;
    private int minx;
    private int miny;
    private int minz;
    private float velocity;
    private int typeId;
    private String owner;
    private String name;
    private Field parent;
    private List<Field> children = new LinkedList<Field>();
    private List<String> allowed = new ArrayList<String>();
    private List<DirtyFieldReason> dirty = new LinkedList<DirtyFieldReason>();
    private List<GriefBlock> grief = new LinkedList<GriefBlock>();
    private List<SnitchEntry> snitches = new LinkedList<SnitchEntry>();
    private long lastUsed;
    private boolean progress;
    //private boolean progress;

    /**
     * @param x
     * @param y
     * @param z
     * @param minx
     * @param miny
     * @param minz
     * @param maxx
     * @param maxy
     * @param maxz
     * @param velocity
     * @param world
     * @param typeId
     * @param owner
     * @param name
     * @param lastUsed
     */
    public Field(int x, int y, int z, int minx, int miny, int minz, int maxx, int maxy, int maxz, float velocity, String world, int typeId, String owner, String name, long lastUsed)
    {
        super(x, y, z, world);

        this.minx = minx;
        this.miny = miny;
        this.minz = minz;
        this.maxx = maxx;
        this.maxy = maxy;
        this.maxz = maxz;
        this.radius = x - minx;
        this.height = maxy - miny;

        this.velocity = velocity;
        this.owner = owner;
        this.name = name;
        this.typeId = typeId;
        this.lastUsed = lastUsed;
    }

    /**
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

        calculateDimensions();
    }

    /**
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

        calculateDimensions();
    }

    /**
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

        calculateDimensions();
    }

    /**
     * @param block
     */
    public Field(Block block)
    {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
    }

    public Field()
    {
    }

    /**
     * Check if the field has certain certain properties
     *
     * @param flag
     * @return
     */
    public boolean hasFlag(FieldFlag flag)
    {
        if (settings == null)
        {
            return false;
        }

        return settings.hasFlag(flag);
    }

    private void calculateDimensions()
    {
        this.minx = getX() - radius;
        this.maxx = getX() + radius;
        this.minz = getZ() - radius;
        this.maxz = getZ() + radius;
        this.miny = getY() - radius;
        this.maxy = getY() + radius;

        if (height > 0)
        {
            this.miny = getY() - (int) Math.floor(((double) Math.max(height - 1, 0)) / 2);
            this.maxy = getY() + (int) Math.ceil(((double) Math.max(height - 1, 0)) / 2);
        }
    }

    /**
     * @param radius
     */
    public void setRadius(int radius)
    {
        PreciousStones.getInstance().getForceFieldManager().removeSourceField(this);

        this.radius = radius;

        if (height == 0)
        {
            this.height = (this.radius * 2) + 1;
            dirty.add(DirtyFieldReason.HEIGHT);
        }

        dirty.add(DirtyFieldReason.RADIUS);

        PreciousStones.getInstance().getForceFieldManager().addSourceField(this);
    }

    /**
     * Sets the cuboid data
     *
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     */
    public void setCuboidDimensions(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        PreciousStones.getInstance().getForceFieldManager().removeSourceField(this);

        this.minx = minX;
        this.miny = minY;
        this.minz = minZ;
        this.maxx = maxX;
        this.maxy = maxY;
        this.maxz = maxZ;

        this.radius = (maxx - minx) / 2;
        this.height = maxy - miny;

        dirty.add(DirtyFieldReason.DIMENSIONS);

        PreciousStones.getInstance().getForceFieldManager().addSourceField(this);
    }

    /**
     * @return the block type id
     */
    public int getTypeId()
    {
        return this.typeId;
    }

    /**
     * @return the block type name
     */
    public String getType()
    {
        return Material.getMaterial(this.getTypeId()).toString();
    }

    /**
     * @return the radius
     */
    public int getRadius()
    {
        return this.radius;
    }

    /**
     * @return the height
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * @return the owner
     */
    public String getOwner()
    {
        return this.owner;
    }

    /**
     * @param owner
     */
    public void setOwner(String owner)
    {
        this.owner = owner;
        dirty.add(DirtyFieldReason.OWNER);
    }

    /**
     * @param playerName
     * @return
     */
    public boolean isOwner(String playerName)
    {
        return owner.equalsIgnoreCase(playerName);
    }

    /**
     * Set the name value
     *
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
        dirty.add(DirtyFieldReason.NAME);
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
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
     * Check whether a target (name, g:group, c:clan) is allowed on this field
     *
     * @param target
     * @return
     */
    public boolean isAllowed(String target)
    {
        if (target.equalsIgnoreCase(owner))
        {
            return true;
        }

        if (allowed.contains("*"))
        {
            return true;
        }

        if (allowed.contains(target.toLowerCase()))
        {
            return true;
        }

        List<String> groups = PreciousStones.getInstance().getPermissionsManager().getGroups(getWorld(), target);

        for (String group : groups)
        {
            if (allowed.contains("g:" + group))
            {
                return true;
            }
        }

        String clan = PreciousStones.getInstance().getSimpleClansManager().getClan(target);

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
     * Allow a target (name, g:group, c:clan) into this field
     *
     * @param target
     * @return confirmation
     */
    public boolean addAllowed(String target)
    {
        if (isAllowed(target))
        {
            return false;
        }

        allowed.add(target.toLowerCase());
        dirty.add(DirtyFieldReason.ALLOWED);
        return true;
    }

    /**
     * Disallow a target (name, g:group, c:clan) from this field
     *
     * @param target
     */
    public void removeAllowed(String target)
    {
        allowed.remove(target.toLowerCase());
        dirty.add(DirtyFieldReason.ALLOWED);
    }

    /**
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
        PreciousStones.getInstance().getForceFieldManager().removeSourceField(this);

        this.height = height;
        dirty.add(DirtyFieldReason.HEIGHT);

        PreciousStones.getInstance().getForceFieldManager().addSourceField(this);
    }

    /**
     * @param typeId the typeId to set
     */
    public void setTypeId(int typeId)
    {
        this.typeId = typeId;
    }

    /**
     * @return vectors of the corners
     */
    public List<Vector> getCorners()
    {
        List<Vector> corners = new ArrayList<Vector>();

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
     * Get all the chunks this field envelops
     *
     * @return
     */
    public Set<ChunkVec> getEnvelopingChunks()
    {
        HashSet<ChunkVec> envelopingChunks = new HashSet<ChunkVec>();

        for (int x = minx; x <= maxx; x += 16)
        {
            for (int z = minz; z <= maxz; z += 16)
            {
                envelopingChunks.add(new ChunkVec(x >> 4, z >> 4, getWorld()));
            }
        }

        envelopingChunks.add(new ChunkVec(maxx >> 4, maxz >> 4, getWorld()));

        return envelopingChunks;
    }

    /**
     * Returns all the fields that overlap this field
     *
     * @return
     */
    public Set<Field> getOverlappingFields()
    {
        Set<ChunkVec> envelopingChunks = getEnvelopingChunks();

        Set<Field> sources = new HashSet<Field>();

        for (ChunkVec ecv : envelopingChunks)
        {
            sources.addAll(PreciousStones.getInstance().getForceFieldManager().getSourceFields(ecv.getBlock().getChunk(), FieldFlag.ALL));
        }

        return sources;
    }

    /**
     * Whether the fields intersect
     *
     * @param field
     * @return confirmation
     */
    public boolean intersects(Field field)
    {
        if (!field.getWorld().equals(getWorld()))
        {
            return false;
        }

        List<Vector> corners = field.getCorners();

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
     *
     * @param vec
     * @return confirmation
     */
    public boolean envelops(Vector vec)
    {
        int px = vec.getBlockX();
        int py = vec.getBlockY();
        int pz = vec.getBlockZ();

        if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
        {
            return true;
        }

        return false;
    }

    /**
     * Whether field block is enveloped by the field
     *
     * @param field
     * @return confirmation
     */
    public boolean envelops(AbstractVec field)
    {
        int px = field.getX();
        int py = field.getY();
        int pz = field.getZ();

        if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz)
        {
            return true;
        }

        return false;
    }

    /**
     * Whether block is enveloped by the field
     *
     * @param block
     * @return confirmation
     */
    public boolean envelops(Block block)
    {
        return envelops(new Vec(block));
    }

    /**
     * Whether location is enveloped by the field
     *
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
     *
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
    public Queue<GriefBlock> getGrief()
    {
        Queue<GriefBlock> g = new LinkedList<GriefBlock>();
        g.addAll(grief);
        return g;
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
     *
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
     *
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
     *
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


    public int getMaxx()
    {
        return maxx;
    }

    public int getMaxy()
    {
        return maxy;
    }

    public int getMaxz()
    {
        return maxz;
    }

    public int getMinx()
    {
        return minx;
    }

    public int getMiny()
    {
        return miny;
    }

    public int getMinz()
    {
        return minz;
    }

    public boolean isProgress()
    {
        return progress;
    }

    public void setProgress(boolean progress)
    {
        this.progress = progress;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public List<Field> getChildren()
    {
        return children;
    }

    public Set<Field> getFamily()
    {
        Set<Field> out = new HashSet<Field>();
        out.addAll(children);
        out.add(this);
        return out;
    }

    public void clearChildren()
    {
        this.children.clear();
    }

    public void clearParent()
    {
        parent = null;
    }

    public void addChild(Field field)
    {
        children.add(field);
    }

    public Field getParent()
    {
        return parent;
    }

    public void setParent(Field parent)
    {
        this.parent = parent;
    }

    public boolean isParent()
    {
        return !children.isEmpty();
    }

    public boolean isChild()
    {
        return parent != null;
    }


}
