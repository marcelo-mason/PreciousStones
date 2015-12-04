package net.sacredlabyrinth.Phaed.PreciousStones.blocks;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;

public class RelativeBlock implements Comparable {
    private int rx;
    private int ry;
    private int rz;

    public RelativeBlock(Vec centerVec, Vec vec) {
        Vec v = vec.subtract(centerVec);

        this.rx = v.getX();
        this.ry = v.getY();
        this.rz = v.getZ();
    }

    public RelativeBlock(int rx, int ry, int rz) {
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    /**
     * Gets the absolute location
     *
     * @param vec
     * @return
     */
    public Vec getAbsoluteVec(Vec vec) {
        return getRelativeVec(vec.getWorld()).add(vec);
    }

    /**
     * Get the relative vec
     *
     * @return
     */
    public Vec getRelativeVec(String world) {
        return new Vec(rx, ry, rz, world);
    }

    /**
     * Returns a vec of the relative location
     *
     * @param centerVec
     * @return
     */
    public Vec toVec(Vec centerVec) {
        return new Vec(getAbsoluteVec(centerVec));
    }

    public int getRx() {
        return rx;
    }

    public int getRy() {
        return ry;
    }

    public int getRz() {
        return rz;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RelativeBlock)) {
            return false;
        }

        RelativeBlock other = (RelativeBlock) obj;
        return other.getRx() == this.getRx() && other.getRy() == this.getRy() && other.getRz() == this.getRz();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.getRx();
        hash = 47 * hash + this.getRy();
        hash = 47 * hash + this.getRz();
        return hash;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj instanceof RelativeBlock) {
            RelativeBlock other = (RelativeBlock) obj;

            if (this.getRx() < other.getRx() ||
                    this.getRx() == other.getRx() && this.getRy() < other.getRy() ||
                    this.getRx() == other.getRx() && this.getRy() == other.getRy() && this.getRz() < other.getRz()) {
                return -1;
            } else if (this.getRx() > other.getRx() ||
                    this.getRx() == other.getRx() && this.getRy() > other.getRy() ||
                    this.getRx() == other.getRx() && this.getRy() == other.getRy() && this.getRz() > other.getRz()) {
                return 1;
            } else {
                return 0;
            }
        } else {
            throw new IllegalArgumentException("obj must be an instance of a RelativeBlock object.");
        }
    }
}
