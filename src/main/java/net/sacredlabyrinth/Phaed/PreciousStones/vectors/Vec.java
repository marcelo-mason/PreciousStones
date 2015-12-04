package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * @author phaed
 */
public class Vec extends AbstractVec {
    /**
     * @param x
     * @param y
     * @param world
     * @param z
     */
    public Vec(int x, int y, int z, String world) {
        super(x, y, z, world);
    }

    public Vec(String serialized) {
        String[] unpacked = serialized.split("[:]");

        this.setX(Integer.parseInt(unpacked[0]));
        this.setY(Integer.parseInt(unpacked[1]));
        this.setZ(Integer.parseInt(unpacked[2]));
        this.setWorld(unpacked[3]);
    }

    /**
     * /**
     *
     * @param vec
     */
    public Vec(Vec vec) {
        super(vec.getX(), vec.getY(), vec.getZ(), vec.getWorld());
    }

    /**
     * @param block
     */
    public Vec(Block block) {
        super(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
    }

    /**
     * @param av
     */
    public Vec(AbstractVec av) {
        super(av.getX(), av.getY(), av.getZ(), av.getWorld());
    }

    /**
     * @param loc
     */
    public Vec(Location loc) {
        super(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
    }

    /**
     * @param vec
     * @return
     */
    public double distance(Vec vec) {
        return Math.sqrt(Math.pow(vec.getX() - getX(), 2.0D) + Math.pow(vec.getY() - getY(), 2.0D) + Math.pow(vec.getZ() - getZ(), 2.0D));
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Vec add(int x, int y, int z) {
        return new Vec(this.getX() + x, this.getY() + y, this.getZ() + z, this.getWorld());
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Vec subtract(int x, int y, int z) {
        return new Vec(this.getX() - x, this.getY() - y, this.getZ() - z, this.getWorld());
    }

    /**
     * @param vec
     * @return
     */
    public Vec add(Vec vec) {
        return new Vec(this.getX() + vec.getX(), this.getY() + vec.getY(), this.getZ() + vec.getZ(), this.getWorld());
    }

    /**
     * @param vec
     * @return
     */
    public Vec subtract(Vec vec) {
        return new Vec(this.getX() - vec.getX(), this.getY() - vec.getY(), this.getZ() - vec.getZ(), this.getWorld());
    }

    /**
     * @param m the int to multiply by
     * @return
     */
    public Vec multiply(int m) {
        return new Vec(this.getX() * m, this.getY() * m, this.getZ() * m, this.getWorld());
    }

    /**
     * Serializes the block into a string
     *
     * @return
     */
    public String serialize() {
        return this.getX() + ":" + this.getY() + ":" + this.getZ() + ":" + this.getWorld();
    }

    /**
     * Returns a bukkit world
     *
     * @return
     */
    public World toWorld() {
        return Bukkit.getWorld(this.getWorld());
    }
}
