package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.LinkedList;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;

public class EntryFields
{
    private LinkedList<Field> fields = new LinkedList<Field>();
    
    public EntryFields(Field field)
    {
	this.fields.add(field);
    }
    
    public void addField(Field field)
    {
	this.fields.add(field);
    }
    
    public void removeField(Field field)
    {
	fields.remove(field);
    }
    
    public boolean containsField(Field field)
    {
	return fields.contains(field);
    }
    
    public int size()
    {
	return fields.size();
    }
    
    public LinkedList<Field> getFields()
    {
	LinkedList<Field> out = new LinkedList<Field>();
	out.addAll(fields);
	return out;
    }
}
