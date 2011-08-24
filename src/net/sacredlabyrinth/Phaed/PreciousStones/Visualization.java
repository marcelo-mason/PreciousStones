
package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;

/**
 *
 * @author phaed
 */
public class Visualization
{
    private Queue<Location> locs = new LinkedList<Location>();
    private List<Field> fields = new LinkedList<Field>();
    private boolean running;

    /**
     *
     * @param loc
     */
    public void addLocation(Location loc)
    {
        locs.add(loc);
    }

    /**
     *
     * @param field
     */
    public void addField(Field field)
    {
        fields.add(field);
    }

    /**
     * @return the locations
     */
    public Queue<Location> getLocs()
    {
        Queue<Location> l = new LinkedList<Location>();
        l.addAll(locs);
        return l;
    }

    /**
     * @return the fields
     */
    public List<Field> getFields()
    {
        List<Field> f = new LinkedList<Field>();
        f.addAll(fields);
        return f;
    }

    /**
     * @return the running
     */
    public boolean isRunning()
    {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running)
    {
        this.running = running;
    }
}
