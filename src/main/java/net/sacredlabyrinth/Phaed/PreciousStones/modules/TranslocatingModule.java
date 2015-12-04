package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;

public class TranslocatingModule {
    private Field field;
    private boolean translocating;
    private int translocationSize;

    public TranslocatingModule(Field field) {
        this.field = field;
    }

    public boolean isTranslocating() {
        return translocating;
    }

    public void setTranslocating(boolean translocating) {
        this.translocating = translocating;
    }


    public void setTranslocationSize(int translocationSize) {
        this.translocationSize = translocationSize;
    }

    public boolean isOverRedstoneMax() {
        return translocationSize > PreciousStones.getInstance().getSettingsManager().getMaxSizeTranslocationForRedstone();
    }

    public boolean isOverTranslocationMax() {
        return isOverTranslocationMax(0);
    }

    public boolean isOverTranslocationMax(int extra) {
        return translocationSize + extra > PreciousStones.getInstance().getSettingsManager().getMaxSizeTranslocation();
    }

    public int getTranslocationSize() {
        return translocationSize;
    }
}
