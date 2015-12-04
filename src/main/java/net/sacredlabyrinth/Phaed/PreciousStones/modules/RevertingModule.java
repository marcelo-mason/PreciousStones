package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.DirtyFieldReason;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.blocks.GriefBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RevertingModule {
    private Field field;
    private int revertSecs;
    private List<GriefBlock> grief = new ArrayList<GriefBlock>();

    public RevertingModule(Field field) {
        this.field = field;
    }

    /**
     * Gets the amount of seconds between each automatic grief revert
     *
     * @return
     */
    public int getRevertSecs() {
        return revertSecs;
    }

    /**
     * Sets the amount of seconds between each automatic grief revert
     *
     * @param revertSecs
     */
    public void setRevertSecs(int revertSecs) {
        this.revertSecs = revertSecs;
        field.addDirty(DirtyFieldReason.FLAGS);
        PreciousStones.debug("DirtyFlags: setRevertSecs");
    }

    /**
     * Add a grief block to the collection
     *
     * @param gb
     */
    public void addGriefBlock(GriefBlock gb) {
        if (!grief.contains(gb)) {
            grief.add(gb);
        }
        field.addDirty(DirtyFieldReason.GRIEF_BLOCKS);
    }


    /**
     * @return the grief
     */
    public Queue<GriefBlock> getGrief() {
        Queue<GriefBlock> g = new LinkedList<GriefBlock>();
        g.addAll(grief);
        grief.clear();
        return g;
    }
}
