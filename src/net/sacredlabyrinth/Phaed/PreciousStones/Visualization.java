
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
     * @return the locs
     */
    public List<Location> getLocs()
    {
        return locs;
    }

    /**
     * @return the fields
     */
    public List<Field> getFields()
    {
        return fields;
    }
}
