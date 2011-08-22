
package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.LinkedList;
import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;

/**
 *
 * @author phaed
 */
public class Visualization
{
    private List<Location> locs = new LinkedList<Location>();
    private List<Field> fields = new LinkedList<Field>();

    public void addLocation(Location loc)
    {
        locs.add(loc);
    }

    public void addField(Field field)
    {
        fields.add(field);
    }

    /**
     * @return the locations
     */
    public List<Location> getLocs()
    {
        List<Location> l = new LinkedList<Location>();
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
}
