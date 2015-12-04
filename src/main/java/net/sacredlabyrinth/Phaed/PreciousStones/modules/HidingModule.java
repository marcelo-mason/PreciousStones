package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class HidingModule {
    private Field field;
    private boolean hidden;

    public HidingModule(Field field) {
        this.field = field;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void hide() {
        if (!field.hasFlag(FieldFlag.HIDABLE)) {
            return;
        }

        if (!isHidden()) {
            hidden = true;
            field.getFlagsModule().dirtyFlags("hide");

            BlockTypeEntry maskType = findMaskType();
            Block block = field.getBlock();
            block.setTypeId(maskType.getTypeId());
            block.setData(maskType.getData());
        }

        if (field.isParent()) {
            for (Field child : field.getChildren()) {
                if (!child.getHidingModule().isHidden()) {
                    child.getHidingModule().hide();
                }
            }
        }

        if (field.isChild()) {
            if (!field.getParent().getHidingModule().isHidden()) {
                field.getParent().getHidingModule().hide();
            }
        }
    }

    /**
     * Unhides the field block turning it back to its normal block type
     */
    public void unHide() {
        if (!field.hasFlag(FieldFlag.HIDABLE)) {
            return;
        }

        if (isHidden()) {
            hidden = false;
            field.getFlagsModule().dirtyFlags("unHide");

            Block block = field.getBlock();
            block.setTypeId(field.getTypeId());
            block.setData((byte) field.getData());
        }

        if (field.isParent()) {
            for (Field child : field.getChildren()) {
                if (child.getHidingModule().isHidden()) {
                    child.getHidingModule().unHide();
                }
            }
        }

        if (field.isChild()) {
            if (field.getParent().getHidingModule().isHidden()) {
                field.getParent().getHidingModule().unHide();
            }
        }
    }

    private BlockTypeEntry findMaskType() {
        List<Vec> vecs = new ArrayList<Vec>();

        Vec center = new Vec(field.getBlock());
        vecs.add(center.add(1, 0, 0));
        vecs.add(center.add(-1, 0, 0));
        vecs.add(center.add(0, 0, 1));
        vecs.add(center.add(0, 0, -1));
        vecs.add(center.add(-1, -1, 0));
        vecs.add(center.add(0, -1, 1));
        vecs.add(center.add(0, 1, 0));

        for (Vec vec : vecs) {
            Block relative = vec.getBlock();

            if (relative.getTypeId() != 0) {
                BlockTypeEntry entry = new BlockTypeEntry(relative);

                if (PreciousStones.getInstance().getSettingsManager().isHidingMaskType(entry)) {
                    return entry;
                }
            }
        }

        return PreciousStones.getInstance().getSettingsManager().getFirstHidingMask();
    }

}
