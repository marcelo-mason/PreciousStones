package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;

import java.util.LinkedList;
import java.util.List;

public class SourceCache
{
    private Vec blockVec;
    private List<Field> source = new LinkedList<Field>();

    public SourceCache(Vec blockVec)
    {
        this.blockVec = blockVec;
    }

    public boolean isBlockVec(Vec blockVec)
    {
        return this.blockVec.equals(blockVec);
    }

    public List<Field> getSource()
    {
        return source;
    }

    public void setSource(List<Field> source)
    {
        this.source = new LinkedList<Field>();
        this.source.addAll(source);
    }
}
