package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.blocks.RelativeBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class TeleportEntry {
    private Entity entity;
    private Vec destination;
    private Field sourceField;
    private Field destinationField;
    private String announce;

    public TeleportEntry(Entity entity, Field sourceField, Field destinationField, String announce) {
        this.entity = entity;
        this.sourceField = sourceField;
        this.destinationField = destinationField;
        this.destination = destinationField.toVec().add(0, 1, 0);
        this.announce = announce;
    }

    public TeleportEntry(Entity entity, RelativeBlock rel, Field sourceField, Field destinationField, String announce) {
        this.entity = entity;
        this.sourceField = sourceField;
        this.destinationField = destinationField;
        this.destination = rel.getAbsoluteVec(destinationField.toVec());
        this.announce = announce;
    }

    public Entity getEntity() {
        return entity;
    }

    public Location getDestination() {
        return destination.getLocation();
    }

    public Field getSourceField() {
        return sourceField;
    }

    public Field getDestinationField() {
        return destinationField;
    }

    public String getAnnounce() {
        return announce;
    }
}
