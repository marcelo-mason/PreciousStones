package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.SnitchEntry;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private TimeUnit timeUnit;
    private BlockTypeEntry type;
    private String owner;
    private String newOwner;
    private String name;
    private Field parent;
    private List<Field> children = new ArrayList<Field>();
    private List<String> allowed = new ArrayList<String>();
    private Set<DirtyFieldReason> dirty = new HashSet<DirtyFieldReason>();
    private List<GriefBlock> grief = new ArrayList<GriefBlock>();
    private List<SnitchEntry> snitches = new ArrayList<SnitchEntry>();
    private List<FieldFlag> flags = new ArrayList<FieldFlag>();
    private List<FieldFlag> disabledFlags = new ArrayList<FieldFlag>();
    private List<FieldFlag> insertedFlags = new ArrayList<FieldFlag>();
    private List<BlockEntry> fenceBlocks = new ArrayList<BlockEntry>();
    private long lastUsed;
    private boolean progress;
    private boolean open;
    private int revertSecs;
    private boolean disabled;
    private int disablerId;
    private boolean translocating;
    private int translocationSize;

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
        this.radius = x - minx;
        this.height = maxy - miny;

        this.velocity = velocity;
        this.owner = owner;
        this.name = name;
        this.type = type;
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

        this.radius = ((maxx - minx) - 1) / 2;
        this.height = maxy - miny;

        dirty.add(DirtyFieldReason.DIMENSIONS);

        PreciousStones.getInstance().getForceFieldManager().addSourceField(this);
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
        PreciousStones.getInstance().getForceFieldManager().removeSourceField(this);

        World world = getBlock().getWorld();

        Location min = new Location(world, minX, minY, minZ);
        min = min.add(getLocation());

        Location max = new Location(world, maxX, maxY, maxZ);
        max = max.add(getLocation());

        PreciousStones.debug(min.toVector().toBlockVector().toString());
        PreciousStones.debug(max.toVector().toBlockVector().toString());

        this.minx = min.getBlockX();
        this.miny = min.getBlockY();
        this.minz = min.getBlockZ();

        this.maxx = max.getBlockX();
        this.maxy = max.getBlockY();
        this.maxz = max.getBlockZ();

        this.radius = ((maxx - minx) - 1) / 2;
        this.height = maxy - miny;

        dirty.add(DirtyFieldReason.DIMENSIONS);

        PreciousStones.getInstance().getForceFieldManager().addSourceField(this);
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

        // allows all allies into each others fields

        /*
        if (PreciousStones.getInstance().getSimpleClansManager().isAllyOwner(owner, target))
        {
            return true;
        }
        */

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
    public Set<Field> getOverlappingFields()
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

                World world = field.getBlock().getWorld();

                List<Vector> corners = field.getCorners();

                for (Vector corner : corners)
                {
                    if (this.envelops(corner.toLocation(world)))
                    {
                        sources.add(field);
                    }
                }

                corners = this.getCorners();

                for (Vector corner : corners)
                {
                    if (field.envelops(corner.toLocation(world)))
                    {
                        sources.add(field);
                    }
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

    public int getVolume()
    {
        if (settings.getCustomVolume() > 0)
        {
            return settings.getCustomVolume();
        }

        int maxWidth = (settings.getRadius() * 2) + 1;
        int maxHeight = settings.getHeight();

        return (maxHeight * maxWidth * maxWidth);
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

        JSONArray insertedFlags = new JSONArray();
        insertedFlags.addAll(getInsertedFlagsStringList());

        if (!disabledFlags.isEmpty())
        {
            json.put("disabledFlags", disabledFlags);
        }

        if (!insertedFlags.isEmpty())
        {
            json.put("insertedFlags", insertedFlags);
        }

        if (revertSecs > 0)
        {
            json.put("revertSecs", revertSecs);
        }

        if (disabled)
        {
            json.put("disabled", disabled);
        }

        return json.toString();
    }

    public ArrayList<String> getDisabledFlagsStringList()
    {
        ArrayList<String> ll = new ArrayList();
        for (Iterator iter = disabledFlags.iterator(); iter.hasNext(); )
        {
            FieldFlag flag = (FieldFlag) iter.next();
            ll.add(Helper.toFlagStr(flag));
        }
        return ll;
    }

    public ArrayList<String> getInsertedFlagsStringList()
    {
        ArrayList<String> ll = new ArrayList();
        for (Iterator iter = insertedFlags.iterator(); iter.hasNext(); )
        {
            FieldFlag flag = (FieldFlag) iter.next();
            ll.add(Helper.toFlagStr(flag));
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
                        else if (flag.equals("revertSecs"))
                        {
                            revertSecs = ((Long) flags.get(flag)).intValue();
                        }
                        else if (flag.equals("disabled"))
                        {
                            setDisabled(((Boolean) flags.get(flag)));
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
     * Insert a field flag into the field
     *
     * @param flagStr
     */
    public boolean insertFieldFlag(String flagStr)
    {
        if (!insertedFlags.contains(Helper.toFieldFlag(flagStr)))
        {
            dirty.add(DirtyFieldReason.FLAGS);
            insertedFlags.add(Helper.toFieldFlag(flagStr));
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

                PreciousStones.getInstance().getEntryManager().removeAllPlayers(this);
            }
            else
            {
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
            }
        }

        dirty.add(DirtyFieldReason.FLAGS);
    }

    /**
     * Starts the disabling process for auto disable fields
     */
    public boolean startDisabler()
    {
        if (settings != null && settings.getAutoDisableSeconds() > 0)
        {
            Player player = Helper.matchSinglePlayer(owner);
            final String theOwner = owner;
            final Field thisField = this;

            if (player != null)
            {
                ChatBlock.sendMessage(player, ChatColor.YELLOW + Helper.capitalize(settings.getTitle()) + " field will disable itself after " + settings.getAutoDisableSeconds() + Helper.plural(settings.getAutoDisableSeconds(), " second", "s"));
            }

            if (disablerId > 0)
            {
                PreciousStones.getInstance().getServer().getScheduler().cancelTask(disablerId);
            }

            disablerId = PreciousStones.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(PreciousStones.getInstance(), new Runnable()
            {
                public void run()
                {
                    if (!thisField.isDisabled())
                    {
                        Player player = Helper.matchSinglePlayer(theOwner);

                        if (player != null)
                        {
                            ChatBlock.sendMessage(player, ChatColor.YELLOW + Helper.capitalize(settings.getTitle()) + " field has been disabled");
                        }

                        thisField.setDisabled(true);
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
    public void generateFence()
    {
        PreciousStones plugin = PreciousStones.getInstance();

        World world = PreciousStones.getInstance().getServer().getWorld(this.getWorld());

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

        // traveling the z length

        for (int z = minz; z <= maxz; z++)
        {
            boolean hasSideOne = false;
            boolean hasSideTwo = false;

            if (!hasSideOne)
            {
                int sideOneMidId = world.getBlockTypeIdAt(minx, mid, z);

                if (plugin.getSettingsManager().isThroughType(sideOneMidId))
                {
                    // if the midId is through type then travel downwards

                    for (int y = mid; y >= miny; y--)
                    {
                        int sideOne = world.getBlockTypeIdAt(minx, y, z);

                        if (!plugin.getSettingsManager().isThroughType(sideOne))
                        {
                            // once we reach a solid record the block above
                            hasSideOne = true;
                            Block block = world.getBlockAt(minx, y + 1, z);
                            fenceBlocks.add(new BlockEntry(block));
                            block.setType(Material.FENCE);
                            break;
                        }
                    }
                }
                else
                {
                    // if the midId is solid then travel upwards

                    for (int y = mid; y >= maxy; y++)
                    {
                        int sideOne = world.getBlockTypeIdAt(minx, y, z);

                        if (plugin.getSettingsManager().isThroughType(sideOne))
                        {
                            // once we find a through type record the block
                            hasSideOne = true;
                            Block block = world.getBlockAt(minx, y, z);
                            fenceBlocks.add(new BlockEntry(block));
                            block.setType(Material.FENCE);
                            break;
                        }
                    }
                }
            }

            if (!hasSideTwo)
            {
                int sideTwoMidId = world.getBlockTypeIdAt(maxx, mid, z);

                if (plugin.getSettingsManager().isThroughType(sideTwoMidId))
                {
                    // if the midId is through type then travel downwards

                    for (int y = mid; y >= miny; y--)
                    {
                        int sideTwo = world.getBlockTypeIdAt(maxx, y, z);

                        if (!plugin.getSettingsManager().isThroughType(sideTwo))
                        {
                            // once we reach a solid record the block above
                            hasSideTwo = true;
                            Block block = world.getBlockAt(maxx, y + 1, z);
                            fenceBlocks.add(new BlockEntry(block));
                            block.setType(Material.FENCE);
                            break;
                        }
                    }
                }
                else
                {
                    // if the midId is solid then travel upwards

                    for (int y = mid; y >= maxy; y++)
                    {
                        int sideTwo = world.getBlockTypeIdAt(maxx, y, z);

                        if (plugin.getSettingsManager().isThroughType(sideTwo))
                        {
                            // once we find a through type record the block
                            hasSideTwo = true;
                            Block block = world.getBlockAt(maxx, y, z);
                            fenceBlocks.add(new BlockEntry(block));
                            block.setType(Material.FENCE);
                            break;
                        }
                    }
                }
            }
        }

        // traveling the z length

        for (int x = minz; x <= maxz; x++)
        {
            boolean hasSideOne = false;
            boolean hasSideTwo = false;

            if (!hasSideOne)
            {
                int sideOneMidId = world.getBlockTypeIdAt(x, mid, minz);

                if (plugin.getSettingsManager().isThroughType(sideOneMidId))
                {
                    // if the midId is through type then travel downwards

                    for (int y = mid; y >= miny; y--)
                    {
                        int sideOne = world.getBlockTypeIdAt(x, y, minz);

                        if (!plugin.getSettingsManager().isThroughType(sideOne))
                        {
                            // once we reach a solid record the block above
                            hasSideOne = true;
                            Block block = world.getBlockAt(x, y + 1, minz);
                            fenceBlocks.add(new BlockEntry(block));
                            block.setType(Material.FENCE);
                            break;
                        }
                    }
                }
                else
                {
                    // if the midId is solid then travel upwards

                    for (int y = mid; y >= maxy; y++)
                    {
                        int sideOne = world.getBlockTypeIdAt(x, y, minz);

                        if (plugin.getSettingsManager().isThroughType(sideOne))
                        {
                            // once we find a through type record the block
                            hasSideOne = true;
                            Block block = world.getBlockAt(x, y, minz);
                            fenceBlocks.add(new BlockEntry(block));
                            block.setType(Material.FENCE);
                            break;
                        }
                    }
                }
            }

            if (!hasSideTwo)
            {
                int sideTwoMidId = world.getBlockTypeIdAt(x, mid, maxz);

                if (plugin.getSettingsManager().isThroughType(sideTwoMidId))
                {
                    // if the midId is through type then travel downwards

                    for (int y = mid; y >= miny; y--)
                    {
                        int sideTwo = world.getBlockTypeIdAt(x, y, maxz);

                        if (!plugin.getSettingsManager().isThroughType(sideTwo))
                        {
                            // once we reach a solid record the block above
                            hasSideTwo = true;
                            Block block = world.getBlockAt(x, y + 1, maxz);
                            fenceBlocks.add(new BlockEntry(block));
                            block.setType(Material.FENCE);
                            break;
                        }
                    }
                }
                else
                {
                    // if the midId is solid then travel upwards

                    for (int y = mid; y >= maxy; y++)
                    {
                        int sideTwo = world.getBlockTypeIdAt(x, y, maxz);

                        if (plugin.getSettingsManager().isThroughType(sideTwo))
                        {
                            // once we find a through type record the block
                            hasSideTwo = true;
                            Block block = world.getBlockAt(x, y, maxz);
                            fenceBlocks.add(new BlockEntry(block));
                            block.setType(Material.FENCE);
                            break;
                        }
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
        Player player = Helper.matchSinglePlayer(playerName);

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
}
