package net.sacredlabyrinth.Phaed.PreciousStones.vectors;

/**
 *
 * @author cc_madelg
 */

public class Vector
{
    protected final double x;
    protected final double y;
    protected final double z;

    public Vector(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(Vector pt)
    {
        x = pt.x;
        y = pt.y;
        z = pt.z;
    }

    public Vector()
    {
        x = 0.0D;
        y = 0.0D;
        z = 0.0D;
    }

    public double getX()
    {
        return x;
    }

    public int getBlockX()
    {
        return (int) Math.round(x);
    }

    public Vector setX(double x)
    {
        return new Vector(x, y, z);
    }

    public Vector setX(int x)
    {
        return new Vector(x, y, z);
    }

    public double getY()
    {
        return y;
    }

    public int getBlockY()
    {
        return (int) Math.round(y);
    }

    public Vector setY(double y)
    {
        return new Vector(x, y, z);
    }

    public Vector setY(int y)
    {
        return new Vector(x, y, z);
    }

    public double getZ()
    {
        return z;
    }

    public int getBlockZ()
    {
        return (int) Math.round(z);
    }

    public Vector setZ(double z)
    {
        return new Vector(x, y, z);
    }

    public Vector setZ(int z)
    {
        return new Vector(x, y, z);
    }

    public Vector add(Vector other)
    {
        return new Vector(x + other.x, y + other.y, z + other.z);
    }

    public Vector add(double x, double y, double z)
    {
        return new Vector(this.x + x, this.y + y, this.z + z);
    }

    public Vector add(int x, int y, int z)
    {
        return new Vector(this.x + x, this.y + y, this.z + z);
    }

    public Vector add(Vector[] others)
    {
        double newX = x;
        double newY = y;
        double newZ = z;

        for (int i = 0; i < others.length; i++)
        {
            newX += others[i].x;
            newY += others[i].y;
            newZ += others[i].z;
        }
        return new Vector(newX, newY, newZ);
    }

    public Vector subtract(Vector other)
    {
        return new Vector(x - other.x, y - other.y, z - other.z);
    }

    public Vector subtract(double x, double y, double z)
    {
        return new Vector(this.x - x, this.y - y, this.z - z);
    }

    public Vector subtract(int x, int y, int z)
    {
        return new Vector(this.x - x, this.y - y, this.z - z);
    }

    public Vector subtract(Vector[] others)
    {
        double newX = x;
        double newY = y;
        double newZ = z;

        for (int i = 0; i < others.length; i++)
        {
            newX -= others[i].x;
            newY -= others[i].y;
            newZ -= others[i].z;
        }
        return new Vector(newX, newY, newZ);
    }

    public Vector multiply(Vector other)
    {
        return new Vector(x * other.x, y * other.y, z * other.z);
    }

    public Vector multiply(double x, double y, double z)
    {
        return new Vector(this.x * x, this.y * y, this.z * z);
    }

    public Vector multiply(int x, int y, int z)
    {
        return new Vector(this.x * x, this.y * y, this.z * z);
    }

    public Vector multiply(Vector[] others)
    {
        double newX = x;
        double newY = y;
        double newZ = z;

        for (int i = 0; i < others.length; i++)
        {
            newX *= others[i].x;
            newY *= others[i].y;
            newZ *= others[i].z;
        }
        return new Vector(newX, newY, newZ);
    }

    public Vector multiply(double n)
    {
        return new Vector(x * n, y * n, z * n);
    }

    public Vector multiply(float n)
    {
        return new Vector(x * n, y * n, z * n);
    }

    public Vector multiply(int n)
    {
        return new Vector(x * n, y * n, z * n);
    }

    public Vector divide(Vector other)
    {
        return new Vector(x / other.x, y / other.y, z / other.z);
    }

    public Vector divide(double x, double y, double z)
    {
        return new Vector(this.x / x, this.y / y, this.z / z);
    }

    public Vector divide(int x, int y, int z)
    {
        return new Vector(this.x / x, this.y / y, this.z / z);
    }

    public Vector divide(int n)
    {
        return new Vector(x / n, y / n, z / n);
    }

    public Vector divide(double n)
    {
        return new Vector(x / n, y / n, z / n);
    }

    public Vector divide(float n)
    {
        return new Vector(x / n, y / n, z / n);
    }

    public double length()
    {
        return Math.sqrt(Math.pow(x, 2.0D) + Math.pow(y, 2.0D) + Math.pow(z, 2.0D));
    }

    public double distance(Vector pt)
    {
        return Math.sqrt(Math.pow(pt.x - x, 2.0D) + Math.pow(pt.y - y, 2.0D) + Math.pow(pt.z - z, 2.0D));
    }

    public double distanceSq(Vector pt)
    {
        return Math.pow(pt.x - x, 2.0D) + Math.pow(pt.y - y, 2.0D) + Math.pow(pt.z - z, 2.0D);
    }

    public Vector normalize()
    {
        return divide(length());
    }

    public boolean containedWithin(Vector min, Vector max)
    {
        return (x >= min.getX()) && (x <= max.getX()) && (y >= min.getY()) && (y <= max.getY()) && (z >= min.getZ()) && (z <= max.getZ());
    }

    public boolean containedWithinBlock(Vector min, Vector max)
    {
        return (getBlockX() >= min.getBlockX()) && (getBlockX() <= max.getBlockX()) && (getBlockY() >= min.getBlockY()) && (getBlockY() <= max.getBlockY()) && (getBlockZ() >= min.getBlockZ()) && (getBlockZ() <= max.getBlockY());
    }

    public Vector clampY(int min, int max)
    {
        return new Vector(x, Math.max(min, Math.min(max, y)), z);
    }

    public Vector transform2D(double angle, double aboutX, double aboutZ, double translateX, double translateZ)
    {
        angle = Math.toRadians(angle);
        double x = this.x;
        double z = this.z;
        double x2 = x * Math.cos(angle) - z * Math.sin(angle);
        double z2 = x * Math.sin(angle) + z * Math.cos(angle);
        return new Vector(x2 + aboutX + translateX, y, z2 + aboutZ + translateZ);
    }

    public static Vector toBlockPoint(double x, double y, double z)
    {
        return new Vector((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Vector))
        {
            return false;
        }
        Vector other = (Vector) obj;
        return (other.getX() == x) && (other.getY() == y) && (other.getZ() == z);
    }

    @Override
    public int hashCode()
    {
        return new Double(x).hashCode() >> 13 ^ new Double(y).hashCode() >> 7 ^ new Double(z).hashCode();
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public static Vector getMinimum(Vector v1, Vector v2)
    {
        return new Vector(Math.min(v1.getX(), v2.getX()), Math.min(v1.getY(), v2.getY()), Math.min(v1.getZ(), v2.getZ()));
    }

    public static Vector getMaximum(Vector v1, Vector v2)
    {
        return new Vector(Math.max(v1.getX(), v2.getX()), Math.max(v1.getY(), v2.getY()), Math.max(v1.getZ(), v2.getZ()));
    }
}
