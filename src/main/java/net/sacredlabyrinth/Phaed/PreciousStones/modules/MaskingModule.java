package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class MaskingModule {
    private Field field;

    public MaskingModule(Field field) {
        this.field = field;
    }

    public void mask() {
        mask(null);
    }

    public void unmask() {
        unmask(null);
    }

    public void mask(Player actor) {
        Set<Player> fieldInhabitants = new HashSet<Player>();

        if (actor != null) {
            fieldInhabitants.add(actor);
        } else {
            fieldInhabitants = PreciousStones.getInstance().getForceFieldManager().getFieldInhabitants(field);
        }

        Entity[] entities = field.getBlock().getChunk().getEntities();

        for (Entity entity : entities) {
            if (entity instanceof Player) {
                fieldInhabitants.add((Player) entity);
            }
        }

        for (Player player : fieldInhabitants) {
            if (field.hasFlag(FieldFlag.MASK_ON_ENABLED)) {
                player.sendBlockChange(field.getLocation(), field.getSettings().getMaskOnEnabledBlock(), (byte) 0);
            } else {
                player.sendBlockChange(field.getLocation(), field.getSettings().getMaskOnDisabledBlock(), (byte) 0);
            }
        }
    }

    public void unmask(Player actor) {
        Set<Player> fieldInhabitants = new HashSet<Player>();

        if (actor != null) {
            fieldInhabitants.add(actor);
        } else {
            fieldInhabitants = PreciousStones.getInstance().getForceFieldManager().getFieldInhabitants(field);
        }

        Entity[] entities = field.getBlock().getChunk().getEntities();

        for (Entity entity : entities) {
            if (entity instanceof Player) {
                fieldInhabitants.add((Player) entity);
            }
        }

        for (Player player : fieldInhabitants) {
            player.sendBlockChange(field.getLocation(), field.getTypeId(), (byte) field.getData());
        }
    }
}
