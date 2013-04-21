package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.*;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.PermissionsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.*;

/**disabledFlags
 * A field object
 *
 * @author phaed
 */
public class Field extends AbstractVec implements Comparable<Field>
{
    final private Field self;
    private FieldSettings settings;
    private long id = 0;
    private int radius;
    private int height;
    private int maxx;
    private int maxy;
    private int maxz;
    private int minx;
    private int miny;
    private float velocity;
    private int minz;
    private BlockTypeEntry type;
    private String owner;
    private String newOwner;
    private String name;
    private Field parent;
    private List<Field> children = new ArrayList<Field>();
    private List<String> allowed = new ArrayList<String>();
    private List<String> renters = new ArrayList<String>();
    private List<String> blacklistedCommands = new ArrayList<String>();
    private List<BlockTypeEntry> whitelistedBlocks = new ArrayList<BlockTypeEntry>();
    private Set<DirtyFieldReason> dirty = new HashSet<DirtyFieldReason>();
    private List<GriefBlock> grief = new ArrayList<GriefBlock>();
    private List<SnitchEntry> snitches = new ArrayList<SnitchEntry>();
    private List<FieldFlag> flags = new ArrayList<FieldFlag>();
    private List<FieldFlag> disabledFlags = new ArrayList<FieldFlag>();
    private List<FieldFlag> insertedFlags = new ArrayList<FieldFlag>();
    private List<FieldFlag> clearedFlags = new ArrayList<FieldFlag>();
    private List<BlockEntry> fenceBlocks = new ArrayList<BlockEntry>();
    private List<RentEntry> renterEntries = new ArrayList<RentEntry>();
    private long lastUsed;
    private boolean progress;
    private boolean open;
    private int revertSecs;
    private boolean disabled;
    private int disablerId;
    private boolean translocating;
    private int translocationSize;
    private boolean hidden;
    private boolean forested;
    private List<PaymentEntry> payment = new ArrayList<PaymentEntry>();
    private PaymentEntry purchase;
    private boolean singIsClean;
    private int limitSeconds;

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
     * @param type
     * @param owner
     * @param name
     * @param lastUsed
     */
    public Field(int x, int y, int z, int minx, int miny, int minz, int maxx, int maxy, int maxz, float velocity, String world, BlockTypeEntry type, String owner, String name, long lastUsed)
    {
        super(x, y, z, world);

        this.minx = minx;
        this.miny = miny;
        this.minz = minz;
        this.maxx = maxx;
        this.maxy = maxy;
        this.maxz = maxz;
        this.radius = Helper.getWidthFromCoords(x, minx);
        this.height = Helper.getWidthFromCoords(maxy, miny);

        this.velocity = velocity;
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.lastUsed = lastUsed;
        this.self = this;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param radius
     * @param height
     * @param velocity
     * @param world
     * @param type
     * @param owner
     * @param name
     * @param lastUsed
     */
    public Field(int x, int y, int z, int radius, int height, float velocity, String world, BlockTypeEntry type, String owner, String name, long lastUsed)
    {
        super(x, y, z, world);

        this.radius = radius;
        this.height = height;

        this.velocity = velocity;
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.lastUsed = lastUsed;
        this.self = this;

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
        this.type = new BlockTypeEntry(block.getTypeId(), block.getData());
        this.self = this;

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
        this.type = new BlockTypeEntry(block.getTypeId(), block.getData());
        this.self = this;

        calculateDimensions();
    }

    /**
     * @param block
     */
    public Field(Block block)
    {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
        this.self = this;
    }

    public Field()
    {
        this.self = this;
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
            this.miny = getY() - ((height - 1) / 2);
            this.maxy = getY() + ((height - 1) / 2);
        }

        dirty.add(DirtyFieldReason.DIMENSIONS);
    }

    /**
     * Returns the maximum volume this field can take up.
     *
     * @return
     */
    public int getMaxVolume()
    {
        if (settings.getCustomVolume() > 0)
        {
            return settings.getCustomVolume();
        }

        int side = Math.max((settings.getRadius() * 2) + 1, 1);
        int h = side;

        if (height > 0)
        {
            h = height;
        }

        return side * side * h;
    }

    /**
     * Retuns the acutal volume the filed is currently taking up
     *
     * @return
     */
    public int getActualVolume()
    {
        int widthX = Helper.getWidthFromCoords(maxx, minx);
        int widthZ = Helper.getWidthFromCoords(maxz, minz);
        int height = Helper.getWidthFromCoords(maxy, miny);
        return (height * widthX * widthZ);
    }

    /**
     * Returns the volume with a fixed height of 1.
     * Used for size comparisons where height is irrelevant.
     *
     * @return
     */
    public int getFlatVolume()
    {
        int widthX = Helper.getWidthFromCoords(maxx, minx);
        int widthZ = Helper.getWidthFromCoords(maxz, minz);
        return (widthX * widthZ);
    }

    /**
     * @param radius
     */
    public void setRadius(int radius)
    {
        this.radius = radius;

        if (height == 0)
        {
            this.height = (this.radius * 2) + 1;
            dirty.add(DirtyFieldReason.HEIGHT);
        }

        dirty.add(DirtyFieldReason.RADIUS);

        calculateDimensions();
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
        this.minx = minX;
        this.miny = minY;
        this.minz = minZ;
        this.maxx = maxX;
        this.maxy = maxY;
        this.maxz = maxZ;

        this.radius = (((Helper.getWidthFromCoords(maxx, minx) - 1) + (Helper.getWidthFromCoords(maxz, minz) - 1)) / 2) / 2;
        this.height = maxy - miny;

        dirty.add(DirtyFieldReason.DIMENSIONS);
    }

    /**
     * Sets the cuboid data using relative dimensions
     *
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     */
    public void setRelativeCuboidDimensions(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        World world = getBlock().getWorld();

        Location min = new Location(world, minX, minY, minZ);
        min = min.add(getLocation());

        Location max = new Location(world, maxX, maxY, maxZ);
        max = max.add(getLocation());

        setCuboidDimensions(min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
    }

    public Location getRelativeMin()
    {
        World world = getBlock().getWorld();
        Location min = new Location(world, minx, miny, minz);
        min.subtract(getLocation());
        return min;
    }

    public Location getRelativeMax()
    {
        World world = getBlock().getWorld();
        Location max = new Location(world, maxx, maxy, maxz);
        max.subtract(getLocation());
        return max;
    }

    /**
     * @return the block type id
     */
    public int getTypeId()
    {
        return type.getTypeId();
    }

    /**
     * @return the block data
     */
    public byte getData()
    {
        return type.getData();
    }

    /**
     * @return the type entry
     */
    public BlockTypeEntry getTypeEntry()
    {
        return type;
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
     * returns the computed height, whether custom height was used or not
     *
     * @return
     */
    public int getComputedHeight()
    {
        if (this.height > 0)
        {
            return this.height;
        }

        return (this.radius * 2) + 1;
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
        all.addAll(renters);
        return all;
    }

    /**
     * Check whether a target (name, g:group, c:clan) is in the allowed list on this field
     *
     * @param target
     * @return
     */
    public boolean isInAllowedList(String target)
    {
        return allowed.contains(target.toLowerCase());
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

        if (renters.contains(target.toLowerCase()))
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
     * @return coordinates string format [x y z world]
     */
    public String getCoords()
    {
        return super.toString();
    }

    /**
     * @return coordinates string format x y z
     */
    public String getCleanCoords()
    {
        return getX() + " " + getY() + " " + getZ();
    }


    @Override
    public String toString()
    {
        return super.toString() + " [" + getOwner() + "]";
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

        for (int x = minx; x <= (maxx + 15); x += 16)
        {
            for (int z = minz; z <= (maxz + 15); z += 16)
            {
                envelopingChunks.add(new ChunkVec(x >> 4, z >> 4, getWorld()));
            }
        }

        return envelopingChunks;
    }

    /**
     * Returns all the fields that overlap this field
     *
     * @return
     */
    public Set<Field> getIntersectingFields()
    {
        Set<ChunkVec> envelopingChunks = getEnvelopingChunks();

        Set<Field> sources = new HashSet<Field>();

        for (ChunkVec ecv : envelopingChunks)
        {
            List<Field> fields = PreciousStones.getInstance().getForceFieldManager().getSourceFieldsInChunk(ecv, FieldFlag.ALL);

            for (Field field : fields)
            {
                if (field.equals(this))
                {
                    continue;
                }

                if (field.intersects(this))
                {
                    sources.add(field);
                }
            }
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
     * @return the allowed
     */
    public List<String> getRenters()
    {
        return Collections.unmodifiableList(renters);
    }

    /**
     * @return the if its rented
     */
    public boolean isRented()
    {
        return !renters.isEmpty();
    }

    /**
     * @return if it has been bought
     */
    public boolean isBought()
    {
        return purchase != null;
    }

    /**
     * @return the allowed
     */
    public boolean isRenter(String playerName)
    {
        return renters.contains(playerName.toLowerCase());
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
        if (!grief.contains(gb))
        {
            grief.add(gb);
        }
        dirty.add(DirtyFieldReason.GRIEF_BLOCKS);
    }


    /**
     * @return the grief
     */
    public Queue<GriefBlock> getGrief()
    {
        Queue<GriefBlock> g = new LinkedList<GriefBlock>();
        g.addAll(grief);
        grief.clear();
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
     *
     */
    public void updateLastUsed()
    {
        lastUsed = (new DateTime()).getMillis();
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

        return Days.daysBetween(new DateTime(lastUsed), new DateTime()).getDays();
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
        //Add all the default flags
        for (FieldFlag flag : settings.getDefaultFlags())
        {
            flags.add(flag);
        }
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
     * Whether the item is dirty
     *
     * @return
     */
    public boolean isDirty()
    {
        return !dirty.isEmpty();
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
            c = this.getWorld().compareTo(field.getWorld());
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

    public String getDimensionString()
    {
        return String.format("minx: %s maxx: %s miny: %s maxy: %s minz: %s maxz: %s", minx, maxx, miny, maxy, minz, maxz);
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

    public boolean isOpen()
    {
        return open;
    }

    public void setOpen(boolean open)
    {
        this.open = open;
    }

    /**
     * Check if the field has certain certain properties
     *
     * @param flag
     * @return
     */
    public boolean hasFlag(FieldFlag flag)
    {
        boolean ret = flags.contains(flag);

        if (!ret)
        {
            ret = insertedFlags.contains(flag);
        }

        if (disabledFlags.contains(flag))
        {
            ret = false;
        }

        return ret;
    }

    /**
     * Check if the field has certain certain properties
     *
     * @param flagStr
     * @return
     */
    public boolean hasFlag(String flagStr)
    {
        return hasFlag(Helper.toFieldFlag(flagStr));
    }

    /**
     * Return the list of flags and their data as a json string
     *
     * @return the flags
     */
    public String getFlagsAsString()
    {
        JSONObject json = new JSONObject();

        // writing the list of flags to json

        JSONArray disabledFlags = new JSONArray();
        disabledFlags.addAll(getDisabledFlagsStringList());

        JSONArray clearedFlags = new JSONArray();
        clearedFlags.addAll(getClearedFlagsStringList());

        JSONArray insertedFlags = new JSONArray();
        insertedFlags.addAll(getInsertedFlagsStringList());

        JSONArray renterList = new JSONArray();
        renterList.addAll(getRentersString());

        JSONArray paymentList = new JSONArray();
        paymentList.addAll(getPaymentString());

        JSONArray blacklistedCommandsList = new JSONArray();
        blacklistedCommandsList.addAll(blacklistedCommands);

        JSONArray whitelistedBlocksList = new JSONArray();
        whitelistedBlocksList.addAll(whitelistedBlocks);

        if (!paymentList.isEmpty())
        {
            json.put("payments", paymentList);
        }

        if (!disabledFlags.isEmpty())
        {
            json.put("disabledFlags", disabledFlags);
        }

        if (!insertedFlags.isEmpty())
        {
            json.put("insertedFlags", insertedFlags);
        }

        if (!clearedFlags.isEmpty())
        {
            json.put("clearedFlags", clearedFlags);
        }

        if (!blacklistedCommandsList.isEmpty())
        {
            json.put("blacklistedCommands", blacklistedCommandsList);
        }

        if (!renterList.isEmpty())
        {
            json.put("renters", renterList);
        }

        if (revertSecs > 0)
        {
            json.put("revertSecs", revertSecs);
        }

        if (limitSeconds > 0)
        {
            json.put("limitSeconds", limitSeconds);
        }

        if (disabled)
        {
            json.put("disabled", disabled);
        }

        if (hidden)
        {
            json.put("hidden", hidden);
        }

        if (purchase != null)
        {
            json.put("purchase", purchase);
        }

        return json.toString();
    }

    public ArrayList<String> getPaymentString()
    {
        ArrayList<String> ll = new ArrayList<String>();
        for (PaymentEntry entry : payment)
        {
            ll.add(entry.toString());
        }
        return ll;
    }

    public ArrayList<String> getDisabledFlagsStringList()
    {
        ArrayList<String> ll = new ArrayList<String>();
        for (FieldFlag flag : disabledFlags)
        {
            ll.add(Helper.toFlagStr(flag));
        }
        return ll;
    }

    public ArrayList<String> getInsertedFlagsStringList()
    {
        ArrayList<String> ll = new ArrayList<String>();
        for (FieldFlag flag : insertedFlags)
        {
            ll.add(Helper.toFlagStr(flag));
        }
        return ll;
    }

    public ArrayList<String> getClearedFlagsStringList()
    {
        ArrayList<String> ll = new ArrayList<String>();
        for (FieldFlag flag : clearedFlags)
        {
            ll.add(Helper.toFlagStr(flag));
        }
        return ll;
    }

    public ArrayList<String> getRentersString()
    {
        ArrayList<String> ll = new ArrayList<String>();
        for (RentEntry entry : renterEntries)
        {
            ll.add(entry.serialize());
        }
        return ll;
    }

    /**
     * Returns inserted flags
     *
     * @return
     */
    public List<FieldFlag> getInsertedFlags()
    {
        return insertedFlags;
    }

    /**
     * Returns inserted flags
     *
     * @return
     */
    public List<FieldFlag> getClearedFlags()
    {
        return clearedFlags;
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
            JSONObject flags = (JSONObject) JSONValue.parse(flagString);

            if (flags != null)
            {
                for (Object flag : flags.keySet())
                {
                    try
                    {
                        // reading the list of flags from json
                        if (flag.equals("disabledFlags"))
                        {
                            JSONArray disabledFlags = (JSONArray) flags.get(flag);

                            for (Object flagStr : disabledFlags)
                            {
                                // do no toggle of no-toggle flags

                                if (flagStr.toString().equalsIgnoreCase("dynmap-area") || flagStr.toString().equalsIgnoreCase("dynmap-marker"))
                                {
                                    if (hasFlag(FieldFlag.DYNMAP_NO_TOGGLE))
                                    {
                                        continue;
                                    }
                                }

                                disableFlag(flagStr.toString());
                            }
                        }
                        else if (flag.equals("insertedFlags"))
                        {
                            JSONArray localFlags = (JSONArray) flags.get(flag);

                            for (Object flagStr : localFlags)
                            {
                                insertFieldFlag(flagStr.toString());
                            }
                        }
                        else if (flag.equals("clearedFlags"))
                        {
                            JSONArray localFlags = (JSONArray) flags.get(flag);

                            for (Object flagStr : localFlags)
                            {
                                clearFieldFlag(flagStr.toString());
                            }
                        }
                        else if (flag.equals("renters"))
                        {
                            JSONArray renterList = (JSONArray) flags.get(flag);

                            renterEntries.clear();
                            renters.clear();
                            for (Object flagStr : renterList)
                            {
                                RentEntry entry = new RentEntry(flagStr.toString());
                                renters.add(entry.getPlayerName().toLowerCase());
                                renterEntries.add(entry);
                            }
                        }
                        else if (flag.equals("blacklistedCommands"))
                        {
                            JSONArray blacklistedCommandsList = (JSONArray) flags.get(flag);

                            for (Object flagStr : blacklistedCommandsList)
                            {
                                blacklistedCommands.add(flagStr.toString());
                            }
                        }
                        else if (flag.equals("whitelistedBlocks"))
                        {
                            JSONArray whitelistedBlocksList = (JSONArray) flags.get(flag);

                            for (Object flagStr : whitelistedBlocksList)
                            {
                                whitelistedBlocks.add(new BlockTypeEntry(flagStr.toString()));
                            }
                        }
                        else if (flag.equals("revertSecs"))
                        {
                            revertSecs = ((Long) flags.get(flag)).intValue();
                        }
                        else if (flag.equals("limitSeconds"))
                        {
                            limitSeconds = ((Long) flags.get(flag)).intValue();
                        }
                        else if (flag.equals("disabled"))
                        {
                            setDisabled(((Boolean) flags.get(flag)));
                        }
                        else if (flag.equals("hidden"))
                        {
                            hidden = (Boolean) flags.get(flag);
                        }
                        else if (flag.equals("purchase"))
                        {
                            purchase = new PaymentEntry(flags.get(flag).toString());
                        }
                        else if (flag.equals("payments"))
                        {
                            JSONArray paymentList = (JSONArray) flags.get(flag);

                            paymentList.clear();
                            for (Object flagStr : paymentList)
                            {
                                payment.add(new PaymentEntry(flagStr.toString()));
                            }
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

    /**
     * Enable a flag
     *
     * @param flagStr
     */
    public void enableFlag(String flagStr)
    {
        boolean canEnable = false;

        for (Iterator iter = disabledFlags.iterator(); iter.hasNext(); )
        {
            FieldFlag flag = (FieldFlag) iter.next();

            if (Helper.toFlagStr(flag).equals(flagStr))
            {
                //remove from the disableFlags list
                iter.remove();
                canEnable = true;
            }
        }

        if (canEnable && !flags.contains(Helper.toFieldFlag(flagStr)))
        {
            flags.add(Helper.toFieldFlag(flagStr));
            dirty.add(DirtyFieldReason.FLAGS);
        }
    }

    /**
     * Disabled a flag.
     *
     * @param flagStr
     */
    public void disableFlag(String flagStr)
    {
        boolean hasFlag = false;

        for (Iterator iter = flags.iterator(); iter.hasNext(); )
        {
            FieldFlag flag = (FieldFlag) iter.next();
            if (Helper.toFlagStr(flag).equals(flagStr))
            {
                iter.remove();
                hasFlag = true;
            }
        }

        if (hasFlag && !disabledFlags.contains(Helper.toFieldFlag(flagStr)))
        {
            disabledFlags.add(Helper.toFieldFlag(flagStr));
            dirty.add(DirtyFieldReason.FLAGS);
        }
    }

    /**
     * Whether it has the disabled flag string
     *
     * @param flagStr
     * @return
     */
    public boolean hasDisabledFlag(String flagStr)
    {
        for (FieldFlag flag : disabledFlags)
        {
            if (Helper.toFlagStr(flag).equals(flagStr))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether it has the disabled flag
     *
     * @param flag
     * @return
     */
    public boolean hasDisabledFlag(FieldFlag flag)
    {
        return disabledFlags.contains(flag);
    }

    /**
     * Returns the disabled flags
     *
     * @return
     */
    public List<FieldFlag> getDisabledFlags()
    {
        return Collections.unmodifiableList(disabledFlags);
    }

    /**
     * Toggles a field flag.  returns its state.
     *
     * @param flagStr
     */
    public boolean toggleFieldFlag(String flagStr)
    {
        boolean hasFlag = hasFlag(flagStr);

        if (hasFlag)
        {
            disableFlag(flagStr);
            return false;
        }
        else
        {
            enableFlag(flagStr);
            return true;
        }
    }

    /**
     * Revert all the flags back to default
     */
    public void RevertFlags()
    {
        //Revert all the flags back to the default
        insertedFlags.clear();
        disabledFlags.clear();
        flags.clear();
        for (FieldFlag flag : settings.getDefaultFlags())
        {
            flags.add(flag);
        }
        dirty.add(DirtyFieldReason.FLAGS);
    }

    /**
     * Returns all the flags
     *
     * @return
     */
    public List<FieldFlag> getFlags()
    {
        return Collections.unmodifiableList(flags);
    }

    /**
     * Clear a field flag from the field
     *
     * @param flagStr
     */
    public boolean clearFieldFlag(String flagStr)
    {
        boolean cleared = false;

        if (insertedFlags.contains(Helper.toFieldFlag(flagStr)))
        {
            insertedFlags.remove(Helper.toFieldFlag(flagStr));
            cleared = true;
        }

        if (disabledFlags.contains(Helper.toFieldFlag(flagStr)))
        {
            disabledFlags.remove(Helper.toFieldFlag(flagStr));
            cleared = true;
        }

        if (flags.contains(Helper.toFieldFlag(flagStr)))
        {
            flags.remove(Helper.toFieldFlag(flagStr));
            cleared = true;
        }

        clearedFlags.add(Helper.toFieldFlag(flagStr));

        return cleared;
    }

    /**
     * Insert a field flag into the field
     *
     * @param flagStr
     */
    public boolean insertFieldFlag(String flagStr)
    {
        if (!insertedFlags.contains(Helper.toFieldFlag(flagStr)))
        {
            insertedFlags.add(Helper.toFieldFlag(flagStr));

            if (clearedFlags.contains(Helper.toFieldFlag(flagStr)))
            {
                clearedFlags.remove(Helper.toFieldFlag(flagStr));
            }

            return true;
        }

        return false;
    }

    /**
     * Force insert a flag if it doesn't exist.
     * This can suck
     *
     * @param flagStr
     */
    public boolean insertFlag(String flagStr)
    {
        if (!flags.contains(Helper.toFieldFlag(flagStr)))
        {
            flags.add(Helper.toFieldFlag(flagStr));
            dirty.add(DirtyFieldReason.FLAGS);
            PreciousStones.getInstance().getStorageManager().offerField(this);
            return true;
        }
        return false;
    }

    /**
     * Imports a collection of field flags to this field
     *
     * @param flags
     */
    public void importFlags(List<FieldFlag> flags)
    {
        for (FieldFlag flag : flags)
        {
            insertFieldFlag(Helper.toFlagStr(flag));
        }
    }

    /**
     * Gets the amount of seconds between each automatic grief revert
     *
     * @return
     */
    public int getRevertSecs()
    {
        return revertSecs;
    }

    /**
     * Sets the amount of seconds between each automatic grief revert
     *
     * @param revertSecs
     */
    public void setRevertSecs(int revertSecs)
    {
        this.revertSecs = revertSecs;
        dirty.add(DirtyFieldReason.FLAGS);
    }

    /**
     * Whether the field is disabled
     *
     * @return
     */
    public boolean isDisabled()
    {
        return disabled;
    }

    /**
     * Disables the field
     *
     * @param disabled
     */
    public void setDisabled(boolean disabled)
    {
        setDisabled(disabled, null);
    }

    /**
     * Disables the field
     *
     * @param disabled
     */
    public boolean setDisabled(boolean disabled, Player player)
    {
        PreciousStones plugin = PreciousStones.getInstance();

        if (disabled != this.disabled)
        {
            this.disabled = disabled;

            if (disabled)
            {
                if (hasFlag(FieldFlag.MASK_ON_DISABLED))
                {
                    mask();
                }

                if (hasFlag(FieldFlag.MASK_ON_ENABLED))
                {
                    unmask();
                }

                if (hasFlag(FieldFlag.BREAKABLE_ON_DISABLED))
                {
                    if (!flags.contains(FieldFlag.BREAKABLE))
                    {
                        if (!insertedFlags.contains(FieldFlag.BREAKABLE))
                        {
                            insertedFlags.add(FieldFlag.BREAKABLE);
                        }
                    }
                }

                plugin.getEntryManager().removeAllPlayers(this);
            }
            else
            {
                if (settings.getPayToEnable() > 0)
                {
                    if (player == null)
                    {
                        this.disabled = true;
                        return false;
                    }

                    if (!plugin.getForceFieldManager().purchase(player, settings.getPayToEnable()))
                    {
                        this.disabled = true;
                        return false;
                    }
                }

                if (hasFlag(FieldFlag.MASK_ON_DISABLED))
                {
                    unmask();
                }

                if (hasFlag(FieldFlag.MASK_ON_ENABLED))
                {
                    mask();
                }

                startDisabler();

                if (hasFlag(FieldFlag.BREAKABLE_ON_DISABLED))
                {
                    if (!flags.contains(FieldFlag.BREAKABLE))
                    {
                        if (insertedFlags.contains(FieldFlag.BREAKABLE))
                        {
                            insertedFlags.remove(FieldFlag.BREAKABLE);
                        }
                    }
                }

                if (hasFlag(FieldFlag.FORESTER) && !forested)
                {
                    if (player != null)
                    {
                        ForesterEntry fe = new ForesterEntry(this, player.getName());
                    }
                }

                if (hasFlag(FieldFlag.TELEPORT_PLAYERS_ON_ENABLE) ||
                        hasFlag(FieldFlag.TELEPORT_MOBS_ON_ENABLE) ||
                        hasFlag(FieldFlag.TELEPORT_VILLAGERS_ON_ENABLE) ||
                        hasFlag(FieldFlag.TELEPORT_ANIMALS_ON_ENABLE) ||
                        hasFlag(FieldFlag.TELEPORT_ANIMALS_ON_ENABLE))
                {
                    List<Entity> entities = Bukkit.getServer().getWorld(this.getWorld()).getEntities();

                    for (Entity entity : entities)
                    {
                        if (envelops(entity.getLocation()))
                        {
                            if (hasFlag(FieldFlag.TELEPORT_MOBS_ON_ENABLE))
                            {
                                if (entity instanceof Monster || entity instanceof Golem || entity instanceof WaterMob)
                                {
                                    plugin.getTeleportationManager().teleport(entity, this);
                                }
                            }

                            if (hasFlag(FieldFlag.TELEPORT_VILLAGERS_ON_ENABLE))
                            {
                                if (entity instanceof Villager)
                                {
                                    plugin.getTeleportationManager().teleport(entity, this);
                                }
                            }

                            if (hasFlag(FieldFlag.TELEPORT_ANIMALS_ON_ENABLE))
                            {
                                if (entity instanceof Ageable)
                                {
                                    plugin.getTeleportationManager().teleport(entity, this);
                                }
                            }

                            if (hasFlag(FieldFlag.TELEPORT_PLAYERS_ON_ENABLE))
                            {
                                if (entity instanceof Player)
                                {
                                    plugin.getTeleportationManager().teleport(entity, this);
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Starts the disabling process for auto disable fields
     */
    public boolean startDisabler()
    {
        if (settings != null && settings.getAutoDisableSeconds() > 0)
        {
            Player player = Bukkit.getServer().getPlayerExact(owner);
            final String theOwner = owner;
            final Field thisField = this;

            if (player != null)
            {
                ChatBlock.send(player, "fieldWillDisable", settings.getTitle(), settings.getAutoDisableSeconds());
            }

            if (disablerId > 0)
            {
                Bukkit.getServer().getScheduler().cancelTask(disablerId);
            }

            disablerId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PreciousStones.getInstance(), new Runnable()
            {
                public void run()
                {
                    if (!thisField.isDisabled())
                    {
                        Player player = Bukkit.getServer().getPlayerExact(theOwner);

                        if (player != null)
                        {
                            ChatBlock.send(player, "fieldHasDisabled", settings.getTitle());
                        }

                        thisField.setDisabled(true);
                        thisField.dirtyFlags();
                    }
                }
            }, 20L * settings.getAutoDisableSeconds());

            return true;
        }
        return false;
    }


    /**
     * Generate fence around the field
     */
    public void generateFence(int item)
    {
        PreciousStones plugin = PreciousStones.getInstance();

        World world = Bukkit.getServer().getWorld(this.getWorld());

        if (world == null)
        {
            return;
        }

        int minx = getX() - getRadius() - 1;
        int maxx = getX() + getRadius() + 1;
        int minz = getZ() - getRadius() - 1;
        int maxz = getZ() + getRadius() + 1;
        int miny = getY() - (Math.max(getHeight() - 1, 0) / 2) - 1;
        int maxy = getY() + (Math.max(getHeight() - 1, 0) / 2) + 1;

        int mid = getY();

        if (hasFlag(FieldFlag.CUBOID))
        {
            minx = getMinx() - 1;
            maxx = getMaxx() + 1;
            minz = getMinz() - 1;
            maxz = getMaxz() + 1;
            miny = getMiny() - 1;
            maxy = getMaxy() + 1;
        }

        int limity = Math.min(plugin.getSettingsManager().getFenceMaxDepth(), miny);

        // traveling the z length

        for (int z = minz; z <= maxz; z++)
        {
            int sideOneMidId = world.getBlockTypeIdAt(minx, mid, z);

            if (plugin.getSettingsManager().isNaturalThroughType(sideOneMidId))
            {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--)
                {
                    int sideOne = world.getBlockTypeIdAt(minx, y, z);

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideOne))
                    {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor)
                {
                    for (int y = mid; y >= limity; y--)
                    {
                        int sideOne = world.getBlockTypeIdAt(minx, y, z);

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideOne))
                        {
                            continue;
                        }

                        Block block = world.getBlockAt(minx, y, z);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setTypeId(item);
                    }
                }
            }

            int sideTwoMidId = world.getBlockTypeIdAt(maxx, mid, z);

            if (plugin.getSettingsManager().isNaturalThroughType(sideTwoMidId))
            {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--)
                {
                    int sideTwo = world.getBlockTypeIdAt(maxx, y, z);

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo))
                    {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor)
                {
                    for (int y = mid; y >= limity; y--)
                    {
                        int sideTwo = world.getBlockTypeIdAt(maxx, y, z);

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo))
                        {
                            continue;
                        }

                        Block block = world.getBlockAt(maxx, y, z);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setTypeId(item);
                    }
                }
            }
        }

        // traveling the x length

        for (int x = minx; x <= maxx; x++)
        {
            int sideOneMidId = world.getBlockTypeIdAt(x, mid, minz);

            if (plugin.getSettingsManager().isNaturalThroughType(sideOneMidId))
            {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--)
                {
                    int sideOne = world.getBlockTypeIdAt(x, y, minz);

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideOne))
                    {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor)
                {
                    for (int y = mid; y >= limity; y--)
                    {
                        int sideOne = world.getBlockTypeIdAt(x, y, minz);

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideOne))
                        {
                            continue;
                        }

                        Block block = world.getBlockAt(x, y, minz);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setTypeId(item);
                    }
                }
            }

            int sideTwoMidId = world.getBlockTypeIdAt(x, mid, maxz);

            if (plugin.getSettingsManager().isNaturalThroughType(sideTwoMidId))
            {
                // if the midId is through type then travel downwards

                boolean hasFloor = false;

                for (int y = mid; y >= limity; y--)
                {
                    int sideTwo = world.getBlockTypeIdAt(x, y, maxz);

                    if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo))
                    {
                        hasFloor = true;
                        break;
                    }
                }

                if (hasFloor)
                {
                    for (int y = mid; y >= limity; y--)
                    {
                        int sideTwo = world.getBlockTypeIdAt(x, y, maxz);

                        if (!plugin.getSettingsManager().isNaturalThroughType(sideTwo))
                        {
                            continue;
                        }

                        Block block = world.getBlockAt(x, y, maxz);
                        fenceBlocks.add(new BlockEntry(block));
                        block.setTypeId(item);
                    }
                }
            }
        }
    }

    /**
     * Remove fence from around the field
     */
    public void clearFence()
    {

    }

    public void changeOwner()
    {
        setOwner(newOwner);
        setNewOwner(null);
    }

    public String getNewOwner()
    {
        return newOwner;
    }

    public void setNewOwner(String newOwner)
    {
        this.newOwner = newOwner;
    }

    public void dirtyFlags()
    {
        dirty.add(DirtyFieldReason.FLAGS);
        PreciousStones.getInstance().getStorageManager().offerField(this);
    }

    /**
     * check if the area a field may cover has players in it
     *
     * @param playerName
     */
    public boolean containsPlayer(String playerName)
    {
        Player player = Bukkit.getServer().getPlayerExact(playerName);

        if (player != null)
        {
            if (envelops(player.getLocation()))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isTranslocating()
    {
        return translocating;
    }

    public void setTranslocating(boolean translocating)
    {
        this.translocating = translocating;
    }

    public void mask()
    {
        mask(null);
    }

    public void unmask()
    {
        unmask(null);
    }

    public void mask(Player actor)
    {
        Set<Player> fieldInhabitants = new HashSet<Player>();

        if (actor != null)
        {
            fieldInhabitants.add(actor);
        }
        else
        {
            fieldInhabitants = PreciousStones.getInstance().getForceFieldManager().getFieldInhabitants(this);
        }

        Entity[] entities = getBlock().getChunk().getEntities();

        for (Entity entity : entities)
        {
            if (entity instanceof Player)
            {
                fieldInhabitants.add((Player) entity);
            }
        }

        for (Player player : fieldInhabitants)
        {
            if (hasFlag(FieldFlag.MASK_ON_ENABLED))
            {
                player.sendBlockChange(getLocation(), settings.getMaskOnEnabledBlock(), (byte) 0);
            }
            else
            {
                player.sendBlockChange(getLocation(), settings.getMaskOnDisabledBlock(), (byte) 0);
            }
        }
    }

    public void unmask(Player actor)
    {
        Set<Player> fieldInhabitants = new HashSet<Player>();

        if (actor != null)
        {
            fieldInhabitants.add(actor);
        }
        else
        {
            fieldInhabitants = PreciousStones.getInstance().getForceFieldManager().getFieldInhabitants(this);
        }

        Entity[] entities = getBlock().getChunk().getEntities();

        for (Entity entity : entities)
        {
            if (entity instanceof Player)
            {
                fieldInhabitants.add((Player) entity);
            }
        }

        for (Player player : fieldInhabitants)
        {
            player.sendBlockChange(getLocation(), getTypeId(), getData());
        }
    }

    /**
     * If the block matches the field type stored on the db
     *
     * @return
     */
    public boolean matchesBlockType()
    {
        Block block = getBlock();
        return block.getTypeId() == getTypeId();
    }

    /**
     * If the block is missing
     *
     * @return
     */
    public boolean missingBlock()
    {
        Block block = getBlock();
        return block.getTypeId() == 0;
    }

    public void hide()
    {
        if (!isHidden())
        {
            hidden = true;
            dirtyFlags();

            BlockTypeEntry maskType = findMaskType();
            Block block = getBlock();
            block.setTypeId(maskType.getTypeId());
            block.setData(maskType.getData());

            PreciousStones.getInstance().getStorageManager().offerField(this);
        }

        if (isParent())
        {
            for (Field child : children)
            {
                if (!child.isHidden())
                {
                    child.hide();
                }
            }
        }

        if (isChild())
        {
            if (!getParent().isHidden())
            {
                getParent().hide();
            }
        }
    }

    /**
     * Unhides the field block turning it back to its normal block type
     */
    public void unHide()
    {
        if (isHidden())
        {
            hidden = false;
            dirtyFlags();

            Block block = getBlock();
            block.setTypeId(getTypeId());
            block.setData(getData());

            PreciousStones.getInstance().getStorageManager().offerField(this);
        }

        if (isParent())
        {
            for (Field child : children)
            {
                if (child.isHidden())
                {
                    child.unHide();
                }
            }
        }

        if (isChild())
        {
            if (getParent().isHidden())
            {
                getParent().unHide();
            }
        }
    }

    public boolean isHidden()
    {
        /*
        // fix any discrepencies

        if (hidden)
        {
            if (matchesBlockType())
            {
                hidden = false;
                dirtyFlags();
            }
        }
        else
        {
            if (!matchesBlockType())
            {
                hidden = true;
                dirtyFlags();
            }
        }*/

        return hidden;
    }

    private BlockTypeEntry findMaskType()
    {
        List<Vec> vecs = new ArrayList<Vec>();

        Vec center = new Vec(getBlock());
        vecs.add(center.add(1, 0, 0));
        vecs.add(center.add(-1, 0, 0));
        vecs.add(center.add(0, 0, 1));
        vecs.add(center.add(0, 0, -1));
        vecs.add(center.add(-1, -1, 0));
        vecs.add(center.add(0, -1, 1));
        vecs.add(center.add(0, 1, 0));

        for (Vec vec : vecs)
        {
            Block relative = vec.getBlock();

            if (relative.getTypeId() != 0)
            {
                BlockTypeEntry entry = new BlockTypeEntry(relative);

                if (PreciousStones.getInstance().getSettingsManager().isHidingMaskType(entry))
                {
                    return entry;
                }
            }
        }

        return PreciousStones.getInstance().getSettingsManager().getFirstHidingMask();
    }

    /**
     * Whether any of the allowed players are online and in the world
     *
     * @return
     */
    public boolean hasOnlineAllowed()
    {
        World world = Bukkit.getWorld(getWorld());

        if (world != null)
        {
            List<String> allAllowed = getAllAllowed();

            for (String allowed : allAllowed)
            {
                Player player = Bukkit.getServer().getPlayerExact(allowed);

                if (player != null)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNamed()
    {
        return getName().length() > 0;
    }

    public void setTranslocationSize(int translocationSize)
    {
        this.translocationSize = translocationSize;
    }

    public boolean isOverRedstoneMax()
    {
        return translocationSize > PreciousStones.getInstance().getSettingsManager().getMaxSizeTranslocationForRedstone();
    }

    public boolean isOverTranslocationMax()
    {
        return isOverTranslocationMax(0);
    }

    public boolean isOverTranslocationMax(int extra)
    {
        return translocationSize + extra > PreciousStones.getInstance().getSettingsManager().getMaxSizeTranslocation();
    }

    public int getTranslocationSize()
    {
        return translocationSize;
    }

    public String getDetails()
    {
        return "[" + getType() + "|" + getX() + " " + getY() + " " + getZ() + "]";
    }

    public boolean isForested()
    {
        return forested;
    }

    public void setForested(boolean forested)
    {
        this.forested = forested;
    }

    public RentEntry getRenter(Player player)
    {
        for (RentEntry entry : renterEntries)
        {
            if (entry.getPlayerName().equals(player.getName()))
            {
                return entry;
            }
        }

        return null;
    }

    public void addRent(Player player)
    {
        FieldSign s = getAttachedFieldSign();

        if (s != null)
        {
            int seconds = SignHelper.periodToSeconds(s.getPeriod());

            if (seconds == 0)
            {
                ChatBlock.send(player, "fieldSignRentError");
                return;
            }

            RentEntry renter = getRenter(player);

            if (renter != null)
            {
                renter.addSeconds(seconds);

                ChatBlock.send(player, "fieldSignRentRented", SignHelper.secondsToPeriods(renter.getPeriodSeconds()));
            }
            else
            {
                renterEntries.add(new RentEntry(player.getName(), seconds));
                renters.add(player.getName().toLowerCase());

                if (renterEntries.size() == 1)
                {
                    scheduleNextRentUpdate();
                }
                ChatBlock.send(player, "fieldSignRentRented", s.getPeriod());

                PreciousStones.getInstance().getEntryManager().leaveField(player, this);
                PreciousStones.getInstance().getEntryManager().enterField(player, this);
            }

            dirtyFlags();
            PreciousStones.getInstance().getStorageManager().offerField(this);
        }
    }

    public void removeRenter(RentEntry entry)
    {
        renterEntries.remove(entry);
        renters.remove(entry.getPlayerName().toLowerCase());

        dirtyFlags();
        PreciousStones.getInstance().getStorageManager().offerField(this);
    }

    public boolean clearRents()
    {
        if (isRented())
        {
            renterEntries.clear();
            renters.clear();
            purchase = null;
            cleanFieldSign();

            dirtyFlags();
            PreciousStones.getInstance().getStorageManager().offerField(this);
            return true;
        }
        return false;
    }

    public boolean removeRents()
    {
        FieldSign s = getAttachedFieldSign();

        if (s != null)
        {
            s.eject();

            renterEntries.clear();
            renters.clear();

            if (purchase != null)
            {
                removeAllowed(purchase.getPlayer());
                purchase = null;
            }

            payment.clear();

            dirtyFlags();
            PreciousStones.getInstance().getStorageManager().offerField(this);
            return true;
        }

        return false;
    }

    public void scheduleNextRentUpdate()
    {
        if (!renterEntries.isEmpty())
        {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PreciousStones.getInstance(), new Update(), 20);
        }
    }

    public List<RentEntry> getRenterEntries()
    {
        return Collections.unmodifiableList(renterEntries);
    }

    public void abandonRent(Player player)
    {
        for (RentEntry entry : renterEntries)
        {
            if (entry.getPlayerName().equals(player.getName()))
            {
                removeRenter(entry);
                cleanFieldSign();
                return;
            }
        }
    }

    public FieldSign getAttachedFieldSign()
    {
        return SignHelper.getAttachedFieldSign(getBlock());
    }

    public void cleanFieldSign()
    {
        if (!isRented())
        {
            FieldSign s = getAttachedFieldSign();

            if (s != null)
            {
                s.setAvailableColor();
                s.cleanRemainingTime();
            }
        }
    }

    public void addPurchase(String playerName, String fieldName, BlockTypeEntry item, int amount)
    {
        purchase = new PaymentEntry(playerName, fieldName, item, amount);

        dirtyFlags();
        PreciousStones.getInstance().getStorageManager().offerField(this);
    }

    public void addPayment(String playerName, String fieldName, BlockTypeEntry item, int amount)
    {
        boolean added = false;

        for (PaymentEntry entry : payment)
        {
            if (entry.getPlayer().equals(playerName) && (item == null || entry.getItem().equals(item)))
            {
                entry.setAmount(entry.getAmount() + amount);
                added = true;
            }
        }

        if (!added)
        {
            payment.add(new PaymentEntry(playerName, fieldName, item, amount));
        }

        dirtyFlags();
        PreciousStones.getInstance().getStorageManager().offerField(this);
    }

    public boolean rent(Player player, FieldSign s)
    {
        Field field = s.getField();

        if (field.getLimitSeconds() > 0)
        {
            RentEntry renter = getRenter(player);

            if (renter != null)
            {
                int seconds = SignHelper.periodToSeconds(s.getPeriod());

                if (renter.getPeriodSeconds() + seconds > field.getLimitSeconds())
                {
                    ChatBlock.send(player, "limitReached");
                    return false;
                }
            }
        }

        if (s.getItem() == null)
        {
            if (PreciousStones.getInstance().getPermissionsManager().hasEconomy())
            {
                if (PermissionsManager.hasMoney(player, s.getPrice()))
                {
                    PreciousStones.getInstance().getPermissionsManager().playerCharge(player, s.getPrice());

                    addPayment(player.getName(), s.getField().getName(), null, s.getPrice());
                    addRent(player);

                    PreciousStones.getInstance().getCommunicationManager().logPayment(getOwner(), player.getName(), s);
                    return true;
                }

                ChatBlock.send(player, "economyNotEnoughMoney");
            }
        }
        else
        {
            if (StackHelper.hasItems(player, s.getItem(), s.getPrice()))
            {
                StackHelper.remove(player, s.getItem(), s.getPrice());

                addPayment(player.getName(), s.getField().getName(), s.getItem(), s.getPrice());
                addRent(player);

                PreciousStones.getInstance().getCommunicationManager().logPayment(getOwner(), player.getName(), s);
                return true;
            }

            ChatBlock.send(player, "economyNotEnoughItems");
        }
        return false;
    }

    public boolean hasPendingPayments()
    {
        return !payment.isEmpty();
    }

    public boolean buy(Player player, FieldSign s)
    {
        if (s.getItem() == null)
        {
            if (PreciousStones.getInstance().getPermissionsManager().hasEconomy())
            {
                if (PermissionsManager.hasMoney(player, s.getPrice()))
                {
                    PreciousStones.getInstance().getPermissionsManager().playerCharge(player, s.getPrice());

                    addPurchase(player.getName(), s.getField().getName(), null, s.getPrice());

                    PreciousStones.getInstance().getCommunicationManager().logPurchase(getOwner(), player.getName(), s);
                    return true;
                }

                ChatBlock.send(player, "economyNotEnoughMoney");
            }
        }
        else
        {
            if (StackHelper.hasItems(player, s.getItem(), s.getPrice()))
            {
                StackHelper.remove(player, s.getItem(), s.getPrice());

                addPurchase(player.getName(), s.getField().getName(), s.getItem(), s.getPrice());

                PreciousStones.getInstance().getCommunicationManager().logPurchase(getOwner(), player.getName(), s);
                return true;
            }

            ChatBlock.send(player, "economyNotEnoughItems");
        }
        return false;
    }

    public boolean hasPendingPurchase()
    {
        return purchase != null;
    }

    public boolean isBuyer(Player player)
    {
        return purchase != null && purchase.getPlayer().equals(player.getName());
    }

    public void retrievePayment(Player player)
    {
        for (PaymentEntry entry : payment)
        {
            if (entry.isItemPayment())
            {
                StackHelper.give(player, entry.getItem(), entry.getAmount());

                if (entry.getFieldName().isEmpty())
                {
                    ChatBlock.send(player, "fieldSignItemPaymentReceivedNoName", entry.getAmount(), entry.getItem().getFriendly(), entry.getPlayer());
                }
                else
                {
                    ChatBlock.send(player, "fieldSignItemPaymentReceived", entry.getAmount(), entry.getItem().getFriendly(), entry.getPlayer(), entry.getFieldName());
                }
            }
            else
            {
                PreciousStones.getInstance().getPermissionsManager().playerCredit(player, entry.getAmount());

                if (entry.getFieldName().isEmpty())
                {
                    ChatBlock.send(player, "fieldSignPaymentReceivedNoName", entry.getAmount(), entry.getPlayer());
                }
                else
                {
                    ChatBlock.send(player, "fieldSignPaymentReceived", entry.getAmount(), entry.getPlayer(), entry.getFieldName());
                }
            }
        }

        PreciousStones.getInstance().getCommunicationManager().logPaymentCollect(getOwner(), player.getName(), getAttachedFieldSign());

        payment.clear();
        dirtyFlags();
        PreciousStones.getInstance().getStorageManager().offerField(this);
    }

    public void retrievePurchase(Player player)
    {
        setOwner(purchase.getPlayer());
        allowed.clear();

        if (purchase.isItemPayment())
        {
            StackHelper.give(player, purchase.getItem(), purchase.getAmount());

            if (purchase.getFieldName().isEmpty())
            {
                ChatBlock.send(player, "fieldSignItemPaymentReceivedNoName", purchase.getAmount(), purchase.getItem().getFriendly(), purchase.getPlayer());
            }
            else
            {
                ChatBlock.send(player, "fieldSignItemPaymentReceived", purchase.getAmount(), purchase.getItem().getFriendly(), purchase.getPlayer(), purchase.getFieldName());
            }
        }
        else
        {
            PreciousStones.getInstance().getPermissionsManager().playerCredit(player, purchase.getAmount());

            if (purchase.getFieldName().isEmpty())
            {
                ChatBlock.send(player, "fieldSignPaymentReceivedNoName", purchase.getAmount(), purchase.getPlayer());
            }
            else
            {
                ChatBlock.send(player, "fieldSignPaymentReceived", purchase.getAmount(), purchase.getPlayer(), purchase.getFieldName());
            }
        }

        PreciousStones.getInstance().getCommunicationManager().logPurchaseCollect(getOwner(), player.getName(), getAttachedFieldSign());

        purchase = null;
        dirtyFlags();
        PreciousStones.getInstance().getStorageManager().offerField(this);
    }

    public int getLimitSeconds()
    {
        return limitSeconds;
    }

    public void setLimitSeconds(int limitSeconds)
    {
        this.limitSeconds = limitSeconds;

        dirtyFlags();
        PreciousStones.getInstance().getStorageManager().offerField(this);
    }

    private class Update implements Runnable
    {
        public void run()
        {
            if (isRented())
            {
                FieldSign s = getAttachedFieldSign();

                if (s != null)
                {
                    if (s.isRentable() && s.isValid())
                    {
                        boolean foundSomeone = false;

                        if (PreciousStones.getInstance().getEntryManager().hasInhabitants(self))
                        {
                            for (RentEntry entry : renterEntries)
                            {
                                s.updateRemainingTime(entry.remainingRent());
                                foundSomeone = true;
                            }
                        }

                        if (!foundSomeone && !singIsClean)
                        {
                            s.cleanRemainingTime();
                            singIsClean = true;
                        }
                    }
                }
            }
            for (Iterator iter = renterEntries.iterator(); iter.hasNext(); )
            {
                RentEntry entry = (RentEntry) iter.next();

                if (entry.isDone())
                {
                    renters.remove(entry.getPlayerName().toLowerCase());
                    iter.remove();

                    dirtyFlags();
                    PreciousStones.getInstance().getStorageManager().offerField(self);

                    if (getName().isEmpty())
                    {
                        ChatBlock.send(entry.getPlayerName(), "fieldSignRentExpiredNoName");
                    }
                    else
                    {
                        ChatBlock.send(entry.getPlayerName(), "fieldSignRentExpired", getName());
                    }
                }
            }

            scheduleNextRentUpdate();
        }
    }

    public int getFencePrice()
    {
        return fenceBlocks.size() * settings.getFenceItemPrice();
    }

    public void addBlacklistedCommand(String command)
    {
        if (!blacklistedCommands.contains(command))
        {
            blacklistedCommands.add(command);
        }
        dirtyFlags();
        PreciousStones.getInstance().getStorageManager().offerField(this);
    }

    public void clearBlacklistedCommands()
    {
        blacklistedCommands.clear();
        dirtyFlags();
        PreciousStones.getInstance().getStorageManager().offerField(this);
    }

    public boolean isBlacklistedCommand(String command)
    {
        if (hasFlag(FieldFlag.COMMAND_BLACKLISTING))
        {
            command = command.replace("/", "");

            int i = command.indexOf(' ');

            if (i > -1)
            {
                command = command.substring(0, i);
            }

            PreciousStones.debug(command);

            return blacklistedCommands.contains(command);
        }
        return false;
    }

    public boolean hasBlacklistedComands()
    {
        return blacklistedCommands.size() > 0;
    }

    public String getBlacklistedCommandsList()
    {
        String out = "";

        for (String cmd : blacklistedCommands)
        {
            out += cmd + ", ";
        }

        return Helper.stripTrailing(out, ", ");
    }

    public void addWhitelistedBlock(BlockTypeEntry type)
    {
        if (!whitelistedBlocks.contains(type))
        {
            whitelistedBlocks.add(type);
        }
        dirtyFlags();
        PreciousStones.getInstance().getStorageManager().offerField(this);
    }

    public void deleteWhitelistedBlock(BlockTypeEntry type)
    {
        whitelistedBlocks.remove(type);
        dirtyFlags();
        PreciousStones.getInstance().getStorageManager().offerField(this);
    }
}