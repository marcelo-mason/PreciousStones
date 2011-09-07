package net.sacredlabyrinth.Phaed.PreciousStones.managers;


import net.sacredlabyrinth.Phaed.PreciousStones.AreaCache;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;

import java.util.*;

public class CacheManager
{
    private PreciousStones plugin;
    private final HashMap<ChunkVec, AreaCache> areaCache = new HashMap<ChunkVec, AreaCache>();

    /**
     * @param plugin
     */
    public CacheManager()
    {
        plugin = PreciousStones.getInstance();
    }

    public void addAreaFields(ChunkVec cv, FieldFlag flag, Collection<Field> fields)
    {
        AreaCache cache = areaCache.get(cv);

        if (cache == null)
        {
            cache = new AreaCache();
        }

        cache.addAreaFields(flag, fields);
    }

    public void addSourceField(Vec vec, FieldFlag flag, Field field)
    {
        AreaCache cache = areaCache.get(vec.toChunkVec());

        if (cache == null)
        {
            cache = new AreaCache();
        }

        cache.addSourceField(vec, flag, field);
    }

    public void addSourceFields(Vec vec, FieldFlag flag, Collection<Field> fields)
    {
        AreaCache cache = areaCache.get(vec.toChunkVec());

        if (cache == null)
        {
            cache = new AreaCache();
        }

        cache.addSourceFields(vec, flag, fields);
    }

    public void invalidateChunk(ChunkVec cv)
    {
        areaCache.remove(cv);
    }

    public List<Field> getAreaCache(ChunkVec cv, FieldFlag flag)
    {
        AreaCache cache = areaCache.get(cv);

        if (cache == null)
        {
            return null;
        }

        List<Field> fields = cache.getAreaFields(flag);

        if (fields == null)
        {
            return null;
        }

        return Collections.unmodifiableList(fields);
    }

    public Field getSingleSourceCache(Vec vec, FieldFlag flag)
    {
        AreaCache cache = areaCache.get(vec.toChunkVec());

        if (cache == null)
        {
            return null;
        }

        return cache.getSourceField(vec, flag);
    }

    public List<Field> getSourceCache(Vec vec, FieldFlag flag)
    {
        AreaCache cache = areaCache.get(vec.toChunkVec());

        if (cache == null)
        {
            return null;
        }

        List<Field> fields = cache.getSourceFields(vec, flag);

        if (fields == null)
        {
            return null;
        }

        return Collections.unmodifiableList(fields);
    }
}
