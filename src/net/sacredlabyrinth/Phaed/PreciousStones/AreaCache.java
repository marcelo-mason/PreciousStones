package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;

import java.util.*;

public class AreaCache
{
    private final HashMap<Vec, HashMap<FieldFlag, List<Field>>> sourceCache = new HashMap<Vec, HashMap<FieldFlag, List<Field>>>();
    private final HashMap<Vec, HashMap<FieldFlag, Field>> sourceSingleCache = new HashMap<Vec, HashMap<FieldFlag, Field>>();
    private HashMap<FieldFlag, List<Field>> areaFields = new HashMap<FieldFlag, List<Field>>();

    public void addAreaFields(FieldFlag flag, Collection<Field> fields)
    {
        List<Field> flagFields = new LinkedList<Field>();
        flagFields.addAll(fields);
        areaFields.put(flag, flagFields);
    }

    public List<Field> getAreaFields(FieldFlag flag)
    {
        return Collections.unmodifiableList(areaFields.get(flag));
    }

    public void addSourceField(Vec vec, FieldFlag flag, Field field)
    {
        HashMap<FieldFlag, Field> fieldList = new HashMap<FieldFlag, Field>();
        fieldList.put(flag, field);
        sourceSingleCache.put(vec, fieldList);
    }

    public void addSourceFields(Vec vec, FieldFlag flag, Collection<Field> fields)
    {
        HashMap<FieldFlag, List<Field>> fieldList = new HashMap<FieldFlag, List<Field>>();
        List<Field> flagFields = new LinkedList<Field>();
        flagFields.addAll(fields);
        fieldList.put(flag, flagFields);
        sourceCache.put(vec, fieldList);
    }

    public Field getSourceField(Vec vec, FieldFlag flag)
    {
        HashMap<FieldFlag, Field> fileLists = sourceSingleCache.get(vec);

        if (fileLists == null)
        {
            return null;
        }

        return fileLists.get(flag);
    }

    public List<Field> getSourceFields(Vec vec, FieldFlag flag)
    {
        HashMap<FieldFlag, List<Field>> fileLists = sourceCache.get(vec);

        if (fileLists == null)
        {
            return null;
        }

        return Collections.unmodifiableList(fileLists.get(flag));
    }
}
