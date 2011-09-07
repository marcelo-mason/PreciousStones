package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

import java.util.*;

public class AreaCache
{
    private HashMap<FieldFlag, List<Field>> areaFields = new HashMap<FieldFlag, List<Field>>();

    public void addFields(FieldFlag flag, Collection<Field> fields)
    {
        List<Field> flagFields = new LinkedList<Field>();
        flagFields.addAll(fields);
        areaFields.put(flag, flagFields);
    }

    public boolean haveFields(FieldFlag flag)
    {
        return areaFields.containsKey(flag);
    }

    public List<Field> getFields(FieldFlag flag)
    {
        return Collections.unmodifiableList(areaFields.get(flag));
    }
}
