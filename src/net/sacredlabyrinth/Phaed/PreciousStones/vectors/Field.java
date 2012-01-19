package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.stringtree.json.JSONReader;
import net.stringtree.json.JSONValidatingReader;
import net.stringtree.json.JSONWriter;
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
    private byte data;
    private String owner;
    private String name;
    private Field parent;
    private List<Field> subFields = new LinkedList<Field>();
    private List<Field> children = new LinkedList<Field>();
    private List<String> allowed = new ArrayList<String>();
    private Set<DirtyFieldReason> dirty = new HashSet<DirtyFieldReason>();
    private List<GriefBlock> grief = new LinkedList<GriefBlock>();
    private List<SnitchEntry> snitches = new LinkedList<SnitchEntry>();
    private List<FieldFlag> flags = new LinkedList<FieldFlag>();
    private List<FieldFlag> disabledFlags = new LinkedList<FieldFlag>();
    private List<FieldFlag> insertedFlags = new LinkedList<FieldFlag>();
    private long lastUsed;
    private boolean progress;
    private boolean open;
    private int revertSecs;
    private boolean disabled;

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
    public Field(int x, int y, int z, int minx, int miny, int minz, int maxx, int maxy, int maxz, float velocity, String world, int typeId, byte data, String owner, String name, long lastUsed)
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
        this.data = data;
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
    public Field(int x, int y, int z, int radius, int height, float velocity, String world, int typeId, byte data, String owner, String name, long lastUsed)
    {
        super(x, y, z, world);

        this.radius = radius;
        this.height = height;

        this.velocity = velocity;
        this.owner = owner;
        this.name = name;
        this.typeId = typeId;
        this.data = data;
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
        this.data = block.getData();

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
        this.data = block.getData();

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
     * @return the block type id
     */
    public int getTypeId()
    {
        return this.typeId;
    }

    /**
     * @return the raw type id
     */
    public int getRawTypeId()
    {
        if (typeId == 35)
        {
            if (data == 0)
            {
                return 35;
            }

            String str = Byte.toString(data);

            if (str.length() == 1)
            {
                str = "0" + str;
            }

            str = "35" + str;

            return Integer.parseInt(str);
        }

        return typeId;
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
            List<Field> fields = PreciousStones.getInstance().getForceFieldManager().getSourceFields(ecv, FieldFlag.ALL);
            sources.addAll(fields);
            sources.remove(this);
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
     * Return the list of flags and their data as a json string
     *
     * @return the flags
     */
    public String getFlagsAsString()
    {
        HashMap<String, Object> flags = new HashMap<String, Object>();

        // writing the list of flags to json

        flags.put("disabledFlags", getDisabledFlagsStringList());
        flags.put("insertedFlags", getInsertedFlagsStringList());
        flags.put("revertSecs", revertSecs);
        flags.put("disabled", disabled);

        JSONWriter jw = new JSONWriter();
        return jw.write(flags);

    }

    public LinkedList<String> getDisabledFlagsStringList()
    {
        LinkedList<String> ll = new LinkedList();
        for (Iterator iter = disabledFlags.iterator(); iter.hasNext(); )
        {
            FieldFlag flag = (FieldFlag) iter.next();
            ll.add(Helper.toFlagStr(flag));
        }
        return ll;
    }

    public LinkedList<String> getInsertedFlagsStringList()
    {
        LinkedList<String> ll = new LinkedList();
        for (Iterator iter = insertedFlags.iterator(); iter.hasNext(); )
        {
            FieldFlag flag = (FieldFlag) iter.next();
            ll.add(Helper.toFlagStr(flag));
        }
        return ll;
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
            JSONReader reader = new JSONValidatingReader();
            HashMap<String, Object> flags = (HashMap<String, Object>) reader.read(flagString);

            if (flags != null)
            {
                for (String flag : flags.keySet())
                {
                    // reading the list of flags from json
                    if (flag.equals("disabledFlags"))
                    {
                        List<String> disabledFlags = (List<String>) flags.get(flag);

                        for (String flagStr : disabledFlags)
                        {
                            disableFlag(flagStr);
                        }
                    }
                    else if (flag.equals("insertedFlags"))
                    {
                        List<String> localFlags = (List<String>) flags.get(flag);
                        for (String flagStr : localFlags)
                        {
                            insertFieldFlag(flagStr);
                        }
                    }
                    else if (flag.equals("revertSecs"))
                    {
                        revertSecs = ((Long) flags.get(flag)).intValue();
                    }
                    else if (flag.equals("disabled"))
                    {
                        disabled = ((Boolean) flags.get(flag));
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
        for (Iterator iter = disabledFlags.iterator(); iter.hasNext(); )
        {
            FieldFlag flag = (FieldFlag) iter.next();

            if (Helper.toFlagStr(flag).equals(flagStr))
            {
                //remove from the disableFlags list
                iter.remove();
            }
        }
        if (!flags.contains(Helper.toFieldFlag(flagStr)))
        {
            flags.add(Helper.toFieldFlag(flagStr));
        }
    }

    /**
     * Disabled a flag.
     *
     * @param flagStr
     */
    public void disableFlag(String flagStr)
    {
        for (Iterator iter = flags.iterator(); iter.hasNext(); )
        {
            FieldFlag flag = (FieldFlag) iter.next();
            if (Helper.toFlagStr(flag).equals(flagStr))
            {
                iter.remove();
            }
        }
        if (!disabledFlags.contains(Helper.toFieldFlag(flagStr)))
        {
            disabledFlags.add(Helper.toFieldFlag(flagStr));
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
     * @param flagStr
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

        dirty.add(DirtyFieldReason.FLAGS);
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
                PreciousStones.getInstance().getForceFieldManager().removeSourceField(this);
            }
            else
            {
                PreciousStones.getInstance().getForceFieldManager().addSourceField(this);
            }
        }
    }

    public byte getData()
    {
        return data;
    }
}
