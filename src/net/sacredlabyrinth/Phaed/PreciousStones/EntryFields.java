package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.LinkedList;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;

/**
 *
 * @author phaed
 */
public class EntryFields
{
    private LinkedList<Field> fields = new LinkedList<Field>();

    /**
     *
     * @param field
     */
    public EntryFields(Field field)
    {
	this.fields.add(field);
    }

    /**
     *
     * @param field
     */
    public void addField(Field field)
    {
	this.fields.add(field);
    }

    /**
     *
     * @param field
     */
    public void removeField(Field field)
    {
	fields.remove(field);
    }

    /**
     *
     * @param field
     * @return
     */
    public boolean containsField(Field field)
    {
	return fields.contains(field);
    }

    /**
     *
     * @return
     */
    public int size()
    {
	return fields.size();
    }

    /**
     *
     * @return
     */
    public LinkedList<Field> getFields()
    {
	LinkedList<Field> out = new LinkedList<Field>();
	out.addAll(fields);
	return out;
    }
}
