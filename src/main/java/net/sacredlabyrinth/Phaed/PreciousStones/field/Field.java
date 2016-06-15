package net.sacredlabyrinth.Phaed.PreciousStones.field;

import net.sacredlabyrinth.Phaed.PreciousStones.DirtyFieldReason;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.CuboidEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.FieldSign;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.ForesterEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.SignHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.StackHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.modules.*;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.AbstractVec;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.*;

/**
 * disabledFlags
 * A field object
 *
 * @author phaed
 */
public class Field extends AbstractVec implements Comparable<Field> {
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
    private Set<String> allowed = new HashSet<String>();
    private Set<DirtyFieldReason> dirty = new HashSet<DirtyFieldReason>();
    private long lastUsed;
    private boolean progress;
    private boolean open;
    private boolean disabled;
    private int disablerId;

    private FlagsModule flags = new FlagsModule(this);
    private BuyingModule buying = new BuyingModule();
    private RentingModule renting = new RentingModule(this);
    private MaskingModule masking = new MaskingModule(this);
    private HidingModule hiding = new HidingModule(this);
    private ForestingModule foresting = new ForestingModule(this);
    private TranslocatingModule translocating = new TranslocatingModule(this);
    private RevertingModule reverting = new RevertingModule(this);
    private ListingModule listing = new ListingModule(this);
    private SnitchingModule snitching = new SnitchingModule(this);
    private FencingModule fencing = new FencingModule(this);

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
    public Field(int x, int y, int z, int minx, int miny, int minz, int maxx, int maxy, int maxz, float velocity, String world, BlockTypeEntry type, String owner, String name, long lastUsed) {
        super(x, y, z, world);

        this.minx = minx;
        this.miny = miny;
        this.minz = minz;
        this.maxx = maxx;
        this.maxy = maxy;
        this.maxz = maxz;
        this.radius = Helper.getWidthFromCoords(x, minx);
        this.height = 0;

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
    public Field(int x, int y, int z, int radius, int height, float velocity, String world, BlockTypeEntry type, String owner, String name, long lastUsed) {
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
    public Field(Block block, int radius, int height, String owner) {
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
    public Field(Block block, int radius, int height) {
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
    public Field(Block block) {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
    }

    private void calculateDimensions() {
        this.minx = getX() - radius;
        this.maxx = getX() + radius;
        this.minz = getZ() - radius;
        this.maxz = getZ() + radius;
        this.miny = getY() - radius;
        this.maxy = getY() + radius;

        if (height > 0) {
            this.miny = getY() - ((height - 1) / 2);
            this.maxy = getY() + ((height - 1) / 2);
        }

        if (flags.hasFlag(FieldFlag.CUBOID)) {
            dirty.add(DirtyFieldReason.DIMENSIONS);
        }
    }

    /**
     * Returns the maximum volume this field can take up.
     *
     * @return
     */
    public int getMaxVolume() {
        if (settings.getCustomVolume() > 0) {
            return settings.getCustomVolume();
        }

        int side = Math.max((settings.getRadius() * 2) + 1, 1);
        int h = side;

        if (height > 0) {
            h = height;
        }

        return side * side * h;
    }

    /**
     * Retuns the acutal volume the filed is currently taking up
     *
     * @return
     */
    public int getActualVolume() {
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
    public int getFlatVolume() {
        int widthX = Helper.getWidthFromCoords(maxx, minx);
        int widthZ = Helper.getWidthFromCoords(maxz, minz);
        return (widthX * widthZ);
    }

    /**
     * @param radius
     */
    public void setRadius(int radius) {
        this.radius = radius;

        if (height == 0) {
            this.height = (this.radius * 2) + 1;
        }
        calculateDimensions();

        if (flags.hasFlag(FieldFlag.CUBOID)) {
            dirty.add(DirtyFieldReason.DIMENSIONS);
        } else {
            dirty.add(DirtyFieldReason.HEIGHT);
            dirty.add(DirtyFieldReason.RADIUS);
        }
    }

    public int canSetCuboidRadius(int radius) {
        int volume = getMaxVolume();
        int newVolume = (int) Math.pow((radius * 2) + 1, 3.0);

        return newVolume - volume;
    }

    /**
     * Expand cuboid
     *
     * @param num
     * @param dir
     * @return the overflow if any
     */
    public int expand(int num, String dir, boolean bypass) {
        CuboidEntry ce = new CuboidEntry(this, true);
        ce.expand(num, dir);
        int overflow = ce.getOverflow();

        if (overflow <= 0 || bypass) {
            ce.finalizeField();
        }

        return overflow;
    }

    /**
     * Expand cuboid
     *
     * @param u
     * @param d
     * @param n
     * @param s
     * @param e
     * @param w
     * @return the overflow if any
     */
    public int expand(int u, int d, int n, int s, int e, int w, boolean bypass) {
        CuboidEntry ce = new CuboidEntry(this, true);
        ce.expand(u, d, n, s, e, w);
        int overflow = ce.getOverflow();

        if (overflow <= 0 || bypass) {
            ce.finalizeField();
        }

        return overflow;
    }

    /**
     * Contract cuboid
     *
     * @param num
     * @param dir
     * @return the overflow if any
     */
    public void contract(int num, String dir) {
        CuboidEntry ce = new CuboidEntry(this, true);
        ce.contract(num, dir);
        ce.finalizeField();
    }

    /**
     * Contract cuboid
     *
     * @param u
     * @param d
     * @param n
     * @param s
     * @param e
     * @param w
     * @return the overflow if any
     */
    public void contract(int u, int d, int n, int s, int e, int w) {
        CuboidEntry ce = new CuboidEntry(this, true);
        ce.contract(u, d, n, s, e, w);
        ce.finalizeField();
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
    public void setCuboidDimensions(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minx = minX;
        this.miny = minY;
        this.minz = minZ;
        this.maxx = maxX;
        this.maxy = maxY;
        this.maxz = maxZ;

        this.radius = (((Helper.getWidthFromCoords(maxx, minx) - 1) + (Helper.getWidthFromCoords(maxz, minz) - 1)) / 2) / 2;

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
    public void setRelativeCuboidDimensions(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        World world = getBlock().getWorld();

        Location min = new Location(world, minX, minY, minZ);
        min = min.add(getLocation());

        Location max = new Location(world, maxX, maxY, maxZ);
        max = max.add(getLocation());

        setCuboidDimensions(min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
    }

    public Location getRelativeMin() {
        World world = getBlock().getWorld();
        Location min = new Location(world, minx, miny, minz);
        min.subtract(getLocation());
        return min;
    }

    public Location getRelativeMax() {
        World world = getBlock().getWorld();
        Location max = new Location(world, maxx, maxy, maxz);
        max.subtract(getLocation());
        return max;
    }

    /**
     * @return the block type id
     */
    public int getTypeId() {
        return type.getTypeId();
    }

    /**
     * @return the block data
     */
    public short getData() {
        return type.getData();
    }

    /**
     * @return the type entry
     */
    public BlockTypeEntry getTypeEntry() {
        return type;
    }

    /**
     * @return the block type name
     */
    public String getType() {
        return Material.getMaterial(this.getTypeId()).toString();
    }

    /**
     * @return the radius
     */
    public int getRadius() {
        return this.radius;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        if (this.height == 0) {
            return (this.radius * 2) + 1;
        }

        return this.height;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * @param owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
        dirty.add(DirtyFieldReason.OWNER);
    }

    /**
     * @param playerName
     * @return
     */
    public boolean isOwner(String playerName) {
        return owner.equalsIgnoreCase(playerName);
    }

    /**
     * Set the name value
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
        dirty.add(DirtyFieldReason.NAME);
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     * @return
     */
    public boolean isName(String name) {
        if (name == null) {
            return false;
        }

        return this.name.equalsIgnoreCase(name);
    }

    /**
     * @return
     */
    public List<String> getAllAllowed() {
        List<String> all = new ArrayList<String>();
        all.add(owner.toLowerCase());
        all.addAll(allowed);
        all.addAll(renting.getRenters());
        return all;
    }

    public void clearAllowed() {
        allowed.clear();
    }

    /**
     * Check whether a target (name, g:group, c:clan) is in the allowed list on this field
     *
     * @param target
     * @return
     */
    public boolean isInAllowedList(String target) {
        return allowed.contains(target.toLowerCase());
    }

    /**
     * Check whether a target (name, g:group, c:clan) is allowed on this field
     *
     * @param target
     * @return
     */
    public boolean isAllowed(String target) {
        if (target.equalsIgnoreCase(owner)) {
            return true;
        }

        if (allowed.contains("*")) {
            return true;
        }

        if (allowed.contains(target.toLowerCase())) {
            return true;
        }

        if (renting.hasRenter(target.toLowerCase())) {
            return true;
        }

        List<String> groups = PreciousStones.getInstance().getPermissionsManager().getGroups(getWorld(), target);

        for (String group : groups) {
            if (allowed.contains("g:" + group)) {
                return true;
            }
        }

        String clan = PreciousStones.getInstance().getSimpleClansManager().getClan(target);

        if (clan != null) {
            if (allowed.contains("c:" + clan)) {
                return true;
            }
        }

        OfflinePlayer offlinePlayer = PreciousStones.getInstance().getServer().getOfflinePlayer(target);

        if (offlinePlayer != null) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getMainScoreboard();

            Team team = board.getPlayerTeam(offlinePlayer);

            if (team != null) {
                if (allowed.contains("t:" + team.getName().toLowerCase())) {
                    return true;
                }
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
    public boolean addAllowed(String target) {
        if (isAllowed(target)) {
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
    public void removeAllowed(String target) {
        allowed.remove(target.toLowerCase());
        dirty.add(DirtyFieldReason.ALLOWED);
    }

    /**
     * Migrate a player in the allowed list to a new name
     *
     * @param oldPlayerName
     * @param newPlayerName
     * @return
     */
    public boolean migrateAllowed(String oldPlayerName, String newPlayerName) {
        oldPlayerName = oldPlayerName.toLowerCase();
        if (!allowed.remove(oldPlayerName)) {
            return false;
        }

        allowed.add(newPlayerName);
        dirty.add(DirtyFieldReason.ALLOWED);
        return true;
    }

    /**
     * @return coordinates string format [x y z world]
     */
    public String getCoords() {
        return super.toString();
    }

    /**
     * @return coordinates string format x y z
     */
    public String getCleanCoords() {
        return getX() + " " + getY() + " " + getZ();
    }


    @Override
    public String toString() {
        return super.toString() + " [" + getOwner() + "]";
    }

    /**
     * @return vectors of the corners
     */
    public List<Vector> getCorners() {
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
    public Set<ChunkVec> getEnvelopingChunks() {
        HashSet<ChunkVec> envelopingChunks = new HashSet<ChunkVec>();

        for (int x = minx; x <= (maxx + 15); x += 16) {
            for (int z = minz; z <= (maxz + 15); z += 16) {
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
    public Set<Field> getIntersectingFields() {
        Set<ChunkVec> envelopingChunks = getEnvelopingChunks();

        Set<Field> sources = new HashSet<Field>();

        for (ChunkVec ecv : envelopingChunks) {
            List<Field> fields = PreciousStones.getInstance().getForceFieldManager().getSourceFieldsInChunk(ecv, FieldFlag.ALL);

            for (Field field : fields) {
                if (field.equals(this)) {
                    continue;
                }

                if (field.intersects(this)) {
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
    public boolean intersects(Field field) {
        if (!field.getWorld().equals(getWorld())) {
            return false;
        }

        List<Vector> corners = field.getCorners();

        for (Vector vec : corners) {
            if (this.envelops(vec)) {
                return true;
            }
        }

        corners = this.getCorners();

        for (Vector vec : corners) {
            if (field.envelops(vec)) {
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
    public boolean envelops(Vector vec) {
        int px = vec.getBlockX();
        int py = vec.getBlockY();
        int pz = vec.getBlockZ();

        if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz) {
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
    public boolean envelops(AbstractVec field) {
        int px = field.getX();
        int py = field.getY();
        int pz = field.getZ();

        if (px >= minx && px <= maxx && py >= miny && py <= maxy && pz >= minz && pz <= maxz) {
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
    public boolean envelops(Block block) {
        return envelops(new Vec(block));
    }

    /**
     * Whether location is enveloped by the field
     *
     * @param loc
     * @return confirmation
     */
    public boolean envelops(Location loc) {
        return envelops(new Vec(loc));
    }

    /**
     * @return the velocity
     */
    public float getVelocity() {
        return velocity;
    }

    /**
     * @param velocity the velocity to set
     */
    public void setVelocity(float velocity) {
        this.velocity = velocity;
        dirty.add(DirtyFieldReason.VELOCITY);
    }

    /**
     * Mark for deletion
     */
    public void markForDeletion() {
        dirty.add(DirtyFieldReason.DELETE);
    }

    /**
     * @return the allowed
     */
    public List<String> getAllowed() {
        return new ArrayList<String>(allowed);
    }

    /**
     * @return the allowed
     */
    public List<String> getRenters() {
        return renting.getRenters();
    }

    /**
     * @return the if its rented
     */
    public boolean isRented() {
        return renting.hasRenters();
    }

    /**
     * @return the allowed
     */
    public boolean isRenter(String playerName) {
        return renting.hasRenter(playerName.toLowerCase());
    }

    /**
     * @return the packedAllowed
     */
    public String getPackedAllowed() {
        return Helper.toMessage(allowed, "|");
    }

    /**
     * @param packedAllowed the packedAllowed to set
     */
    public void setPackedAllowed(String packedAllowed) {
        this.allowed.clear();
        this.allowed.addAll(Helper.fromArray(packedAllowed.split("[|]")));
    }

    /**
     *
     */
    public void updateLastUsed() {
        lastUsed = (new DateTime()).getMillis();
        dirty.add(DirtyFieldReason.LASTUSED);
    }

    /**
     * Returns the number of days last used
     *
     * @return
     */
    public int getAgeInDays() {
        if (lastUsed <= 0) {
            return 0;
        }

        return Days.daysBetween(new DateTime(lastUsed), new DateTime()).getDays();
    }

    /**
     * @return the settings
     */
    public FieldSettings getSettings() {
        return settings;
    }

    /**
     * @param settings the settings to set
     */
    public void setSettings(FieldSettings settings) {
        //Add all the default flags
        for (FieldFlag flag : settings.getDefaultFlags()) {
            flags.addFlag(flag);
        }
        this.settings = settings;
    }

    /**
     * Whether the item is dirty
     *
     * @param dirtyType
     * @return
     */
    public boolean isDirty(DirtyFieldReason dirtyType) {
        return dirty.contains(dirtyType);
    }


    /**
     * Whether the item is dirty
     *
     * @return
     */
    public boolean isDirty() {
        return !dirty.isEmpty();
    }

    /**
     * Clear dirty items
     */
    public void clearDirty() {
        dirty.clear();
    }

    /**
     * Returns the distance between this field and a location
     *
     * @param loc
     * @return
     */
    public double distance(Location loc) {
        return Math.sqrt(Math.pow(loc.getBlockX() - getX(), 2.0D) + Math.pow(loc.getBlockY() - getY(), 2.0D) + Math.pow(loc.getBlockZ() - getZ(), 2.0D));
    }

    public int compareTo(Field field) throws ClassCastException {
        int c = this.getX() - field.getX();

        if (c == 0) {
            c = this.getZ() - field.getZ();
        }

        if (c == 0) {
            c = this.getY() - field.getY();
        }

        if (c == 0) {
            c = this.getWorld().compareTo(field.getWorld());
        }

        return c;
    }

    public int getMaxx() {
        return maxx;
    }

    public int getMaxy() {
        return maxy;
    }

    public int getMaxz() {
        return maxz;
    }

    public int getMinx() {
        return minx;
    }

    public int getMiny() {
        return miny;
    }

    public int getMinz() {
        return minz;
    }

    public String getDimensionString() {
        return String.format("minx: %s maxx: %s miny: %s maxy: %s minz: %s maxz: %s", minx, maxx, miny, maxy, minz, maxz);
    }

    public boolean isProgress() {
        return progress;
    }

    public void setProgress(boolean progress) {
        this.progress = progress;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Field> getChildren() {
        return children;
    }

    public Set<Field> getFamily() {
        Set<Field> out = new HashSet<Field>();
        out.addAll(children);
        out.add(this);
        return out;
    }

    public void clearChildren() {
        this.children.clear();
    }

    public void clearParent() {
        parent = null;
    }

    public void addChild(Field field) {
        children.add(field);
    }

    public Field getParent() {
        return parent;
    }

    public void setParent(Field parent) {
        this.parent = parent;
    }

    public boolean isParent() {
        return !children.isEmpty();
    }

    public boolean isChild() {
        return parent != null;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public FieldSign getAttachedFieldSign() {
        return SignHelper.getAttachedFieldSign(getBlock());
    }

    /**
     * Whether the field is disabled
     *
     * @return
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Disables the field
     *
     * @param disabled
     */
    public void setDisabled(boolean disabled) {
        setDisabled(disabled, null, false);
    }

    /**
     * Disables the field
     *
     * @param disabled
     */
    public void setDisabled(boolean disabled, boolean skipMask) {
        setDisabled(disabled, null, true);
    }

    /**
     * Disables the field but skips masking, used on world load operations
     *
     * @param disabled
     */
    public void setDisabledNoMask(boolean disabled) {
        setDisabled(disabled, null, true);
    }

    /**
     * Disables the field
     *
     * @param disabled
     */
    public boolean setDisabled(boolean disabled, Player player) {
        return setDisabled(disabled, player, false);
    }

    /**
     * Disables the field
     *
     * @param disabled
     */
    public boolean setDisabled(boolean disabled, Player player, boolean skipMask) {
        PreciousStones plugin = PreciousStones.getInstance();

        if (disabled != this.disabled) {
            this.disabled = disabled;

            if (disabled) {
                if (!skipMask) {
                    if (flags.hasFlag(FieldFlag.MASK_ON_DISABLED)) {
                        masking.mask();
                    }

                    if (flags.hasFlag(FieldFlag.MASK_ON_ENABLED)) {
                        masking.unmask();
                    }
                }

                if (flags.hasFlag(FieldFlag.BREAKABLE_ON_DISABLED)) {
                    flags.setBreakable();
                }

                if (flags.hasFlag(FieldFlag.TRANSLOCATION)) {
                    if (isNamed()) {
                        plugin.getTranslocationManager().applyTranslocation(this);
                    }
                }

                plugin.getEntryManager().removeAllPlayers(this);
            } else {
                if (settings.getPayToEnable() > 0) {
                    if (player == null) {
                        this.disabled = true;
                        return false;
                    }

                    if (!plugin.getForceFieldManager().purchase(player, settings.getPayToEnable())) {
                        this.disabled = true;
                        return false;
                    }
                }

                if (!skipMask) {
                    if (flags.hasFlag(FieldFlag.MASK_ON_DISABLED)) {
                        masking.unmask();
                    }

                    if (flags.hasFlag(FieldFlag.MASK_ON_ENABLED)) {
                        masking.mask();
                    }
                }

                startDisabler();

                if (flags.hasFlag(FieldFlag.BREAKABLE_ON_DISABLED)) {
                    flags.unsetBreakable();
                }

                if (flags.hasFlag(FieldFlag.TRANSLOCATION)) {
                    if (isNamed()) {
                        plugin.getTranslocationManager().clearTranslocation(this);
                    }
                }

                if (flags.hasFlag(FieldFlag.FORESTER) && foresting.hasForesterUse() && !foresting.isForesting()) {
                    if (player != null) {
                        ForesterEntry fe = new ForesterEntry(this, player);
                    }
                }

                if (flags.hasFlag(FieldFlag.TELEPORT_PLAYERS_ON_ENABLE) ||
                        flags.hasFlag(FieldFlag.TELEPORT_MOBS_ON_ENABLE) ||
                        flags.hasFlag(FieldFlag.TELEPORT_VILLAGERS_ON_ENABLE) ||
                        flags.hasFlag(FieldFlag.TELEPORT_ANIMALS_ON_ENABLE)) {
                    List<Entity> entities = Bukkit.getServer().getWorld(this.getWorld()).getEntities();

                    for (Entity entity : entities) {
                        if (envelops(entity.getLocation())) {
                            if (flags.hasFlag(FieldFlag.TELEPORT_MOBS_ON_ENABLE)) {
                                if (entity instanceof Monster || entity instanceof Golem || entity instanceof WaterMob) {
                                    plugin.getTeleportationManager().teleport(entity, this);
                                }
                            }

                            if (flags.hasFlag(FieldFlag.TELEPORT_VILLAGERS_ON_ENABLE)) {
                                if (entity instanceof Villager) {
                                    plugin.getTeleportationManager().teleport(entity, this);
                                }
                            }

                            if (flags.hasFlag(FieldFlag.TELEPORT_ANIMALS_ON_ENABLE)) {
                                if (entity instanceof Ageable) {
                                    plugin.getTeleportationManager().teleport(entity, this);
                                }
                            }

                            if (flags.hasFlag(FieldFlag.TELEPORT_PLAYERS_ON_ENABLE)) {
                                if (entity instanceof Player) {
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
    public boolean startDisabler() {
        if (settings != null && settings.getAutoDisableTime() > 0) {
            Player player = Bukkit.getServer().getPlayerExact(owner);
            final String theOwner = owner;
            final Field thisField = this;

            if (player != null) {
                ChatHelper.send(player, "fieldWillDisable", settings.getTitle(), settings.getAutoDisableTime());
            }

            if (disablerId > 0) {
                Bukkit.getServer().getScheduler().cancelTask(disablerId);
            }

            disablerId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PreciousStones.getInstance(), new Runnable() {
                public void run() {
                    if (!thisField.isDisabled()) {
                        Player player = Bukkit.getServer().getPlayerExact(theOwner);

                        if (player != null) {
                            ChatHelper.send(player, "fieldHasDisabled", settings.getTitle());
                        }

                        thisField.setDisabled(true);
                        thisField.getFlagsModule().dirtyFlags("startDisabler");

                        PreciousStones.getInstance().getEntryManager().actOnInhabitantsOnDisableToggle(thisField);
                    }
                }
            }, 20L * settings.getAutoDisableTime());

            return true;
        }
        return false;
    }

    public void changeOwner() {
        setOwner(newOwner);
        setNewOwner(null);
    }

    public String getNewOwner() {
        return newOwner;
    }

    public void setNewOwner(String newOwner) {
        this.newOwner = newOwner;
    }

    public void addDirty(DirtyFieldReason reason) {
        dirty.add(reason);
    }

    /**
     * check if the area a field may cover has players in it
     *
     * @param playerName
     */
    public boolean containsPlayer(String playerName) {
        Player player = Bukkit.getServer().getPlayerExact(playerName);

        if (player != null) {
            if (envelops(player.getLocation())) {
                return true;
            }
        }

        return false;
    }

    /**
     * If the block matches the field type stored on the db
     *
     * @return
     */
    public boolean matchesBlockType() {
        Block block = getBlock();
        return block.getTypeId() == getTypeId();
    }

    /**
     * If the block is missing
     *
     * @return
     */
    public boolean missingBlock() {
        Block block = getBlock();
        return block.getTypeId() == 0;
    }

    /**
     * Whether any of the allowed players are online and in the world
     *
     * @return
     */
    public boolean hasOnlineAllowed() {
        World world = Bukkit.getWorld(getWorld());

        if (world != null) {
            List<String> allAllowed = getAllAllowed();

            for (String allowed : allAllowed) {
                Player player = Bukkit.getServer().getPlayerExact(allowed);

                if (player != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNamed() {
        return getName().length() > 0;
    }

    public String getDetails() {
        return "[" + getType() + "|" + getX() + " " + getY() + " " + getZ() + "]";
    }

    public boolean take(Player player) {
        Block block = getBlock();

        if (block.getTypeId() != type.getTypeId()) {
            return false;
        }

        PreciousStones.getInstance().getForceFieldManager().refundField(player, this);
        PreciousStones.getInstance().getForceFieldManager().releaseWipe(this);

        ItemStack is = new ItemStack(type.getTypeId(), 1, (short) 0, type.getData());

        if (settings.hasMetaName()) {
            StackHelper.setItemMeta(is, settings);
        }

        StackHelper.give(player, is);
        return true;
    }

    public boolean hasFlag(FieldFlag flag) {
        return flags.hasFlag(flag);
    }

    public boolean hasFlag(String flagStr) {
        return flags.hasFlag(flagStr);
    }

    public RentingModule getRentingModule() {
        return renting;
    }

    public MaskingModule getMaskingModule() {
        return masking;
    }

    public HidingModule getHidingModule() {
        return hiding;
    }

    public ForestingModule getForestingModule() {
        return foresting;
    }

    public TranslocatingModule getTranslocatingModule() {
        return translocating;
    }

    public RevertingModule getRevertingModule() {
        return reverting;
    }

    public ListingModule getListingModule() {
        return listing;
    }

    public SnitchingModule getSnitchingModule() {
        return snitching;
    }

    public FencingModule getFencingModule() {
        return fencing;
    }

    public FlagsModule getFlagsModule() {
        return flags;
    }

    public BuyingModule getBuyingModule() {
        return buying;
    }
}