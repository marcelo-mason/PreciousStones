package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import com.avaje.ebean.annotation.CacheStrategy;
import com.avaje.ebean.validation.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import net.sacredlabyrinth.Phaed.PreciousStones.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.CloakEntry;

/**
 *
 * @author cc_madelg
 */
@Entity()
@CacheStrategy
@Table(name = "fields", uniqueConstraints = @UniqueConstraint(columnNames = { "x", "y", "z", "world" }))
public class Field extends AbstractVec implements Serializable
{
    @Id
    private Long id;

    private int radius;
    private int height;
    private int typeId;

    @NotNull
    private String owner;

    @NotNull
    private String name;

    @OneToMany(mappedBy = "field", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<SnitchEntry> snitchList = new ArrayList<SnitchEntry>();

    private List<String> allowed = new ArrayList<String>();

    @OneToOne(cascade = CascadeType.ALL)
    private CloakEntry cloakEntry;

    private int chunkX;
    private int chunkZ;

    public Field()
    {
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param radius
     * @param height
     * @param chunkvec
     * @param world
     * @param typeId
     * @param owner
     * @param allowed
     * @param name
     * @param snitchList
     * @param cloakEntry
     */
    public Field(int x, int y, int z, int radius, int height, String world, int typeId, String owner, ArrayList<String> allowed, String name)
    {
	super(x, y, z, world);

        this.chunkX = x >> 4;
        this.chunkZ = z >> 4;
	this.radius = radius;
	this.height = height;
	this.owner = owner;
	this.name = name;
	this.allowed = allowed;
	this.typeId = typeId;
    }

    /**
     *
     * @param block
     * @param radius
     * @param height
     * @param owner
     * @param allowed
     * @param name
     */
    public Field(Block block, int radius, int height, String owner, ArrayList<String> allowed, String name)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

        this.chunkX = block.getX() >> 4;
        this.chunkZ = block.getZ() >> 4;
	this.radius = radius;
	this.height = height;
	this.owner = owner;
	this.name = name;
	this.allowed = allowed;
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

        this.chunkX = block.getX() >> 4;
        this.chunkZ = block.getZ() >> 4;
	this.radius = radius;
	this.height = height;
	this.typeId = block.getTypeId();
    }

    /**
     *
     * @param block
     */
    public Field(Block block)
    {
	super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

        this.chunkX = block.getX() >> 4;
        this.chunkZ = block.getZ() >> 4;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * @return the chunk x
     */
    public int getChunkX()
    {
        return chunkX;
    }

    /**
     * @param ChunkX the chunk x to set
     */
    public void setChunkX(int chunkX)
    {
        this.chunkX = chunkX;
    }

    /**
     * @return the chunk z
     */
    public int getChunkZ()
    {
        return chunkZ;
    }

    /**
     * @param ChunkZ the chunk z to set
     */
    public void setChunkZ(int chunkZ)
    {
        this.chunkZ = chunkZ;
    }

    /**
     *
     * @param radius
     */
    public void setRadius(int radius)
    {
	this.radius = radius;
	this.setHeight((this.radius * 2) + 1);
    }

    /**
     *
     * @return
     */
    public int getTypeId()
    {
	return this.typeId;
    }

    /**
     *
     * @return
     */
    public String getType()
    {
	return Material.getMaterial(this.getTypeId()).toString();
    }

    /**
     *
     * @return
     */
    public ChunkVec getChunkVec()
    {
	return this.getChunkvec();
    }

    /**
     *
     * @return
     */
    public int getRadius()
    {
	return this.radius;
    }

    /**
     *
     * @return
     */
    public int getHeight()
    {
	return this.height;
    }

    /**
     *
     * @return
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
     * @param name
     */
    public void setName(String name)
    {
	this.name = name;
    }

    /**
     *
     * @return
     */
    public String getName()
    {
	if (this.name.length() == 0)
	{
	    return this.getOwner() + "'s domain";
	}

	return this.name;
    }

    /**
     *
     * @param name
     * @return
     */
    public boolean isName(String name)
    {
	if (name == null && this.getName() == null)
	{
	    return true;
	}

	if (name == null || this.getName() == null)
	{
	    return false;
	}

	return this.getName().equals(name);
    }

    /**
     *
     * @return
     */
    public String getStoredName()
    {
	return this.getName();
    }

    /**
     *
     * @return
     */
    public List<String> getAllowed()
    {
	return allowed;
    }

    /**
     *
     * @return
     */
    public List<String> getAllAllowed()
    {
	List<String> all = new ArrayList<String>();
	all.add(getOwner());
	all.addAll(getAllowed());
	return all;
    }

    /**
     *
     * @return
     */
    public String getAllowedList()
    {
	String out = "";

	if (getAllowed().size() > 0)
	{
	    for (int i = 0; i < getAllowed().size(); i++)
	    {
		out += ", " + getAllowed().get(i);
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
     * @param playerName
     * @return
     */
    public boolean isOwner(String playerName)
    {
	return playerName.equals(getOwner());
    }

    /**
     *
     * @param allowedName
     * @return
     */
    public boolean isAllowed(String allowedName)
    {
	if(getAllowed().contains("*"))
	{
	    return true;
	}

	return getAllowed().contains(allowedName);
    }

    /**
     *
     * @param allowedName
     * @return
     */
    public boolean isAllAllowed(String allowedName)
    {
	if(getAllowed().contains("*"))
	{
	    return true;
	}

	return allowedName.equals(getOwner()) || getAllowed().contains(allowedName);
    }

    /**
     *
     * @param allowedName
     * @return
     */
    public boolean addAllowed(String allowedName)
    {
	if (getAllowed().contains(allowedName))
	    return false;

	getAllowed().add(allowedName);
	return true;
    }

    /**
     *
     * @param allowedName
     * @return
     */
    public boolean removeAllowed(String allowedName)
    {
	if (!allowed.contains(allowedName))
	    return false;

	getAllowed().remove(allowedName);
	return true;
    }

    /**
     *
     * @return
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
	for(SnitchEntry se : getSnitchList())
	{
	    if(se.getName().equals(name) && se.getReason().equals(reason) && se.getDetails().equals(details))
	    {
		se.addCount();
		return;
	    }
	}

	getSnitchList().add(new SnitchEntry(name, reason, details));
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
     * @return
     */
    public String getSnitchListString()
    {
	String out = "";

	for (SnitchEntry se : getSnitchList())
	{
	    out += se.toString();
	}

	return out;
    }

    /**
     *
     */
    public void cleanSnitchList()
    {
	getSnitchList().clear();
    }

    /**
     *
     * @return
     */
    public CloakEntry getCloakEntry()
    {
	return cloakEntry;
    }

    /**
     *
     * @param cloakEntry
     */
    public void setCloakEntry(CloakEntry cloakEntry)
    {
	this.cloakEntry = cloakEntry;
    }

    /**
     *
     * @return
     */
    public String getCloakString()
    {
	return getCloakEntry() == null ? "" : getCloakEntry().toString();
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
    }

    /**
     * @param typeId the typeId to set
     */
    public void setTypeId(int typeId)
    {
        this.typeId = typeId;
    }

    /**
     * @param allowed the allowed to set
     */
    public void setAllowed(List<String> allowed)
    {
        this.allowed = allowed;
    }

    /**
     * @return the chunkvec
     */
    public ChunkVec getChunkvec()
    {
        return new ChunkVec(getX() >> 4, getZ() >> 4, getWorld());
    }

    /**
     *
     * @return
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
     *
     * @param field
     * @return
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
     *
     * @param vec
     * @return
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
	    return true;

	return false;
    }

    /**
     *
     * @param field
     * @return
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
	    return true;

	return false;
    }

    /**
     *
     * @param block
     * @return
     */
    public boolean envelops(Block block)
    {
	return envelops(new Vec(block));
    }
}
