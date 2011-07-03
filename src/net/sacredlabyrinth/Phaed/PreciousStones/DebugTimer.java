package net.sacredlabyrinth.Phaed.PreciousStones;

/**
 *
 * @author afforess
 */
public class DebugTimer
{
    public final long start = System.currentTimeMillis();
    private String name = null;

    public DebugTimer()
    {
    }

    public DebugTimer(String name)
    {
        this.name = name;
    }

    public void logProcessTime()
    {
        PreciousStones.logger.info("Process Time " + (name != null ? "(for " + name + ") " : "") + "took " + (System.currentTimeMillis() - start) + " ms");
    }
}
