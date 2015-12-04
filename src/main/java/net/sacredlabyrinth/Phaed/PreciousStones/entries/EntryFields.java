package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * @author phaed
 */
public class EntryFields {
    private ArrayList<Field> fields = new ArrayList<Field>();

    /**
     * @param field
     */
    public EntryFields(Field field) {
        this.fields.add(field);
    }

    /**
     * @param field
     */
    public void addField(Field field) {
        this.fields.add(field);
    }

    /**
     * @param field
     */
    public void removeField(Field field) {
        fields.remove(field);
    }

    /**
     * @param field
     * @return
     */
    public boolean containsField(Field field) {
        return fields.contains(field);
    }

    /**
     * @return
     */
    public int size() {
        return fields.size();
    }

    /**
     * @return
     */
    public List<Field> getFields() {
        return fields;
    }
}
