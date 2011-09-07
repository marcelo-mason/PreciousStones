package net.sacredlabyrinth.Phaed.PreciousStones.listeners;


import net.sacredlabyrinth.Phaed.PreciousStones.AreaCache;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.SourceCache;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class CacheManager
{
    private PreciousStones plugin;
    private final HashMap<ChunkVec, AreaCache> areaCache = new HashMap<ChunkVec, AreaCache>();
    private final HashMap<String, SourceCache> sourceCache = new HashMap<String, SourceCache>();

    /**
     * @param plugin
     */
    public CacheManager()
    {
        plugin = PreciousStones.getInstance();
    }

    public void addFields(ChunkVec cv, FieldFlag flag, Collection<Field> fields)
    {
        AreaCache cache = areaCache.get(cv);

        if (cache == null)
        {
            cache = new AreaCache();
        }

        cache.addFields(flag, fields);
    }

    public void invalidateChunk(ChunkVec cv)
    {
        areaCache.remove(cv);
    }

    public List<Field> getCompleteCache(ChunkVec cv, FieldFlag... flags)
    {
        List<Field> out = new LinkedList<Field>();
        AreaCache cache = areaCache.get(cv);

        if (cache == null)
        {
            return null;
        }

        for (FieldFlag flag : flags)
        {
            List<Field> fields = cache.getFields(flag);

            if (fields == null)
            {
                return null;
            }
            else
            {
                out.addAll(fields);
            }
        }

        return out;
    }
}
