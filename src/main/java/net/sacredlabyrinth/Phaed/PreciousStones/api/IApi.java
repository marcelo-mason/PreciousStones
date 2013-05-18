package net.sacredlabyrinth.Phaed.PreciousStones.api;


import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface IApi
{
    /**
     * If the block can be placed in the location
     *
     * @param player the player attempting the placement of the block
     * @param location the location where the block is being placed
     * @return whether it can be placed in the location or not
     */
    boolean canPlace(Player player, Location location);

    /**
     * If the specific block can be broken
     *
     * @param player the player attempting the destruction of the block
     * @param location the location of the block in question
     * @return whether it can be broken or not
     */
    boolean canBreak(Player player, Location location);

    /**
     * Whether the block is a field block or an unprotectable block. these blocks are stored on the database,
     * thus cannot be removed or have their types changed without causing inconsistencies in the plugin.
     *
     * @param location the location of the block in question
     * @return whether it is a pstone
     */
    boolean isPStone(Location location);

    /**
     * If an enabled field with the specified enabled flag is currently protecting the area,
     * use this to know whether a block is being affected by a field.
     *
     * @param flag the flag that is protecting the area (use FieldFlag.ALL to target any flag)
     * @param location the location that is being protected
     * @return whether a field with the specified flag is protecting the area
     */
    boolean isFieldProtectingArea(FieldFlag flag, Location location);

    /**
     * Returns the enabled fields with the specified enabled flag that are currently protecting the area
     *
     * @param flag the flag that is protecting the area (use FieldFlag.ALL to target any flag)
     * @param location the location that is being protected
     * @return the fields with the specified flag that are protecting the area
     */
    List<Field> getFieldsProtectingArea(FieldFlag flag, Location location);

    /**
     * Whether the flag applies to the player on that specific location.
     * i.e.:
     * if you pass in FieldFlag.PREVENT_ENTRY, it will tell you if the flag prevents the player from entering the field
     * if you pass in FieldFlag.HEAL, it will tell you if the flag heals the player
     * if you pass in FieldFlag.PREVENT_PLACE, it will tell you if the flag prevents the player from placing
     * if you pass in FieldFlag.LAUNCHER, it will tell you if the flag will launch the player
     *
     * This takes into account who the flag applies to by default (allowed or non-allowed players) and any modification
     * flags that are in used in the field (apply-to-reverse, apply-to-all)
     *
     * @param player the player who will be affected by the flag
     * @param flag the flag that you want to test against
     * @param location the location you want to test against
     * @return whether the flag applies to the player
     */
    boolean flagAppliesToPlayer(Player player, FieldFlag flag, Location location);

    /**
     * Returns a count of fields the player has placed
     *
     * @param player the player whose fields you want counted
     * @param flag the flag that will identify the field.  Use FieldFlag.ALL to count all of his fields
     * @return the number of fields this player has placed
     */
    int getPlayerFieldCount(Player player, FieldFlag flag);

    /**
     * Returns all of the fields the player has placed
     *
     * @param player the player whose fields you want
     * @param flag   the flag that will identify the field.  Use FieldFlag.ALL to count all of his fields
     * @return a list of fields the player placed, it is never null.  If the player has not placed any fields it will be empty
     */
    List<Field> getPlayerFields(Player player, FieldFlag flag);
}
