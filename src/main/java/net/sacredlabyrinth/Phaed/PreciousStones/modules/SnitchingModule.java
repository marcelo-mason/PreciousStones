package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.blocks.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.SnitchEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SnitchingModule
{
    private Field field;
    private List<SnitchEntry> snitches = new ArrayList<SnitchEntry>();

    public SnitchingModule(Field field)
    {
        this.field = field;
    }

    /**
     * Clear snitch list
     */
    public void clearSnitch()
    {
        snitches.clear();
    }

    /**
     * @return the snitches
     */
    public List<SnitchEntry> getSnitches()
    {
        return Collections.unmodifiableList(snitches);
    }

}
