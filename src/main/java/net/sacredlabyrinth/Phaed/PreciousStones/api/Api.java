package net.sacredlabyrinth.Phaed.PreciousStones.api;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Api implements IApi
{
    private PreciousStones plugin;

    public Api()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * If the block can be placed in the location
     *
     * @param player   the player attempting the placement of the block
     * @param location the location where the block is being placed
     * @return whether it can be placed in the location or not
     */
    public boolean canPlace(Player player, Location location)
    {
        Field field = plugin.getForceFieldManager().getEnabledSourceField(location, FieldFlag.PREVENT_PLACE);

        if (field != null)
        {
            if (FieldFlag.PREVENT_PLACE.applies(field, player))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * If the specific block can be broken
     *
     * @param player   the player attempting the destruction of the block
     * @param location the location of the block in question
     * @return whether it can be broken or not
     */
    public boolean canBreak(Player player, Location location)
    {
        Field field = plugin.getForceFieldManager().getEnabledSourceField(location, FieldFlag.PREVENT_DESTROY);

        if (field != null)
        {
            if (FieldFlag.PREVENT_DESTROY.applies(field, player))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Whether the block is a field block or an unprotectable block. these blocks are stored on the database,
     * thus cannot be removed or have their types changed without causing inconsistencies in the plugin.
     *
     * @param location
     * @return
     */
    public boolean isPStone(Location location)
    {
        return plugin.getForceFieldManager().getField(location) != null || plugin.getUnbreakableManager().getUnbreakable(location) != null;
    }

    /**
     * If an enabled field with the specified enabled flag is currently protecting the area,
     * use this to know whether a block is being affected by a field.
     *
     * @param flag     the flag that is protecting the area (use FieldFlag.ALL to target any flag)
     * @param location the location that is being protected
     * @return whether a field with the specified flag is protecting the area
     */
    public boolean isFieldProtectingArea(FieldFlag flag, Location location)
    {
        return plugin.getForceFieldManager().getEnabledSourceField(location, flag) != null;
    }

    /**
     * Returns the enabled fields with the specified enabled flag that are currently protecting the area
     *
     * @param flag     the flag that is protecting the area (use FieldFlag.ALL to target any flag)
     * @param location the location that is being protected
     * @return the fields with the specified flag that are protecting the area
     */
    public List<Field> getFieldsProtectingArea(FieldFlag flag, Location location)
    {
        return plugin.getForceFieldManager().getEnabledSourceFields(location, flag);
    }

    /**
     * Whether the flag applies to the player on that specific location.
     * i.e.:
     * if you pass in FieldFlag.PREVENT_ENTRY, it will tell you if the flag prevents the player from entering the field
     * if you pass in FieldFlag.HEAL, it will tell you if the flag heals the player
     * if you pass in FieldFlag.PREVENT_PLACE, it will tell you if the flag prevents the player from placing
     * if you pass in FieldFlag.LAUNCHER, it will tell you if the flag will launch the player
     * <p/>
     * This takes into account who the flag applies to by default (allowed/non-allowed) and any modification
     * flags that are in used in the field (apply-to-reverse, apply-to-all)
     *
     * @param player   the player who will be affected by the flag
     * @param flag     the flag that you want to test against
     * @param location the location you want to test against
     * @return
     */
    public boolean flagAppliesToPlayer(Player player, FieldFlag flag, Location location)
    {
        Field field = plugin.getForceFieldManager().getEnabledSourceField(location, flag);

        if (field != null)
        {
            if (flag.applies(field, player))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a count of fields the player has placed
     *
     * @param player the player whose fields you want counted
     * @param flag   the flag that will identify the field.  Use FieldFlag.ALL to count all of his fields
     * @return the number of fields this player has placed
     */
    public int getPlayerFieldCount(Player player, FieldFlag flag)
    {
        List<Field> fields = plugin.getForceFieldManager().getPlayerFields(player.getName(), flag);

        if (fields == null)
        {
            return 0;
        }

        return fields.size();
    }

    /**
     * Returns all of the fields the player has placed
     *
     * @param player the player whose fields you want
     * @param flag   the flag that will identify the field.  Use FieldFlag.ALL to count all of his fields
     * @return a list of fields the player placed, it is never null.  If the player has not placed any fields it will be empty
     */
    public List<Field> getPlayerFields(Player player, FieldFlag flag)
    {
        List<Field> fields = plugin.getForceFieldManager().getPlayerFields(player.getName(), flag);

        if (fields == null)
        {
            return new ArrayList<Field>();
        }

        return fields;
    }
}
