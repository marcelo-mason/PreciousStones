package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;

public class ForestingModule {
    private Field field;
    private int foresterUsed;
    private boolean foresting;

    public ForestingModule(Field field) {
        this.field = field;
    }

    public boolean hasForesterUse() {
        return field.getSettings().getForesterUses() - foresterUsed > 0;
    }

    public int foresterUsesLeft() {
        return field.getSettings().getForesterUses() - foresterUsed;
    }

    public void recordForesterUse() {
        foresterUsed++;
        field.getFlagsModule().dirtyFlags("recordForesterUse");
    }

    public int getForesterUsed() {
        return foresterUsed;
    }

    public void setForesterUsed(int foresterUsed) {
        this.foresterUsed = foresterUsed;
    }

    public boolean isForesting() {
        return foresting;
    }

    public void setForesting(boolean foresting) {
        this.foresting = foresting;
    }
}
