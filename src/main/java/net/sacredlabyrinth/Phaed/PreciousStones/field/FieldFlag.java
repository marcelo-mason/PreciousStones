package net.sacredlabyrinth.Phaed.PreciousStones.field;

import com.google.common.collect.Maps;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @author phaed
 */
public enum FieldFlag {
    ALL,
    ALLOW_PLACE,
    ALLOW_DESTROY,
    PREVENT_FIRE,
    PREVENT_FIRE_SPREAD,
    PREVENT_PLACE,
    PREVENT_DESTROY,
    PREVENT_VEHICLE_DESTROY,
    PREVENT_VEHICLE_CREATE,
    PREVENT_ENDERMAN_DESTROY,
    PREVENT_EXPLOSIONS,
    PREVENT_CREEPER_EXPLOSIONS,
    PREVENT_WITHER_EXPLOSIONS,
    PREVENT_TNT_EXPLOSIONS,
    PREVENT_PVP,
    PREVENT_MOB_DAMAGE,
    PREVENT_MOB_SPAWN,
    PREVENT_ANIMAL_SPAWN,
    PREVENT_ENTRY,
    PREVENT_UNPROTECTABLE,
    PREVENT_FLOW,
    PREVENT_TELEPORT,
    PREVENT_FLIGHT,
    PREVENT_POTION_SPLASH,
    PREVENT_PORTAL_ENTER,
    PREVENT_PORTAL_CREATION,
    PREVENT_PORTAL_DESTINATION,
    PREVENT_ITEM_FRAME_TAKE,
    PROTECT_ARMOR_STANDS,
    PREVENT_ENTITY_INTERACT,
    PROTECT_ANIMALS,
    PROTECT_MOBS,
    PROTECT_VILLAGERS,
    PROTECT_CROPS,
    PROTECT_LWC,
    PROTECT_INVENTORIES,
    ROLLBACK_EXPLOSIONS,
    REMOVE_MOB,
    HEAL,
    DAMAGE,
    REPAIR,
    FEED,
    AIR,
    BREAKABLE,
    WELCOME_MESSAGE,
    FAREWELL_MESSAGE,
    SNITCH,
    NO_CONFLICT,
    LAUNCH,
    CANNON,
    MINE,
    LIGHTNING,
    NO_OWNER,
    FORESTER,
    GRIEF_REVERT,
    GRIEF_REVERT_DROP,
    GRIEF_REVERT_SAFETY,
    ENTRY_ALERT,
    CUBOID,
    ENABLE_ON_SRC,
    VISUALIZE_ON_SRC,
    VISUALIZE_ON_PLACE,
    KEEP_CHUNKS_LOADED,
    PLACE_GRIEF,
    TOGGLE_ON_DISABLED,
    REDEFINE_ON_DISABLED,
    MODIFY_ON_DISABLED,
    BREAKABLE_ON_DISABLED,
    ALLOWED_CAN_BREAK,
    SNEAKING_BYPASS,
    PLACE_DISABLED,
    PREVENT_USE,
    DYNMAP_AREA,
    DYNMAP_MARKER,
    DYNMAP_NO_TOGGLE,
    DYNMAP_DISABLED,
    DYNMAP_HIDE_PLAYERS,
    DYNMAP_SHOW_PLAYERS,
    CAN_CHANGE_OWNER,
    PLOT,
    POTIONS,
    NEUTRALIZE_POTIONS,
    SNEAK_TO_PLACE,
    NO_FALL_DAMAGE,
    CONFISCATE_ITEMS,
    ENABLE_WITH_REDSTONE,
    TRANSLOCATION,
    TRANSLOCATION_SAFETY,
    MASK_ON_ENABLED,
    MASK_ON_DISABLED,
    WORLDGUARD_REPELLENT,
    GROUP_ON_ENTRY,
    ENTRY_GAME_MODE,
    LEAVING_GAME_MODE,
    NO_ALLOWING,
    TELEPORT_ON_ENTRY,
    TELEPORT_ON_EXIT,
    TELEPORT_ON_SNEAK,
    TELEPORT_ON_BLOCK_BREAK,
    TELEPORT_ON_BLOCK_PLACE,
    TELEPORT_ON_PVP,
    TELEPORT_ON_FIRE,
    TELEPORT_ON_FEEDING,
    TELEPORT_ON_DAMAGE,
    TELEPORT_PLAYERS_ON_ENABLE,
    TELEPORT_MOBS_ON_ENABLE,
    TELEPORT_ANIMALS_ON_ENABLE,
    TELEPORT_VILLAGERS_ON_ENABLE,
    TELEPORT_DESTINATION,
    TELEPORT_BEFORE_DEATH,
    TELEPORT_EXPLOSION_EFFECT,
    TELEPORT_ANNOUNCE,
    TELEPORT_RELATIVELY,
    TELEPORT_IF_WALKING_ON,
    TELEPORT_IF_NOT_WALKING_ON,
    TELEPORT_IF_HOLDING_ITEMS,
    TELEPORT_IF_NOT_HOLDING_ITEMS,
    TELEPORT_IF_HAS_ITEMS,
    TELEPORT_IF_NOT_HAS_ITEMS,
    TELEPORT_COST,
    HIDABLE,
    COMMAND_ON_ENTER,
    COMMAND_ON_EXIT,
    UNUSABLE_ITEMS,
    PLAYER_COMMAND_ON_ENTER,
    PLAYER_COMMAND_ON_EXIT,
    COMMANDS_ON_OVERLAP,
    MUST_BE_ABOVE,
    MUST_BE_BELOW,
    NO_GROWTH,
    SINGLE_USE,
    DISABLE_WHEN_ONLINE,
    DISABLE_ON_LOGOFF,
    ENABLE_ON_LOGON,
    NO_PROJECTILE_THROW,
    NO_PLAYER_PLACE,
    NO_DROPPING_ITEMS,
    NO_PLAYER_SPRINT,
    PREVENT_VEHICLE_ENTER,
    PREVENT_VEHICLE_EXIT,
    RENTABLE,
    SHAREABLE,
    BUYABLE,
    DELETE_IF_NO_PERMISSION,
    COMMAND_BLACKLISTING,
    COMMAND_BLACKLIST,
    ANTI_PLOT,
    GLOBAL,
    NO_RESIZE;

    /**
     * These flags apply to non-allowed
     */
    private final static FieldFlag[] applyToNonAllowed = new FieldFlag[]
            {
                    FieldFlag.PREVENT_FIRE,
                    FieldFlag.PREVENT_FLOW,
                    FieldFlag.PREVENT_ENTRY,
                    FieldFlag.PREVENT_PLACE,
                    FieldFlag.PREVENT_DESTROY,
                    FieldFlag.PREVENT_VEHICLE_DESTROY,
                    FieldFlag.PREVENT_VEHICLE_CREATE,
                    FieldFlag.PREVENT_MOB_DAMAGE,
                    FieldFlag.PREVENT_USE,
                    FieldFlag.PREVENT_TELEPORT, 
                    FieldFlag.PREVENT_FLIGHT, 
                    FieldFlag.PREVENT_ITEM_FRAME_TAKE, 
                    FieldFlag.PROTECT_ARMOR_STANDS,
                    FieldFlag.PREVENT_ENTITY_INTERACT,
                    FieldFlag.PROTECT_ANIMALS,
                    FieldFlag.PROTECT_CROPS,
                    FieldFlag.PROTECT_MOBS,
                    FieldFlag.PROTECT_INVENTORIES,
                    FieldFlag.PROTECT_VILLAGERS,
                    FieldFlag.PROTECT_LWC,
                    FieldFlag.DAMAGE,
                    FieldFlag.SNITCH,
                    FieldFlag.MINE,
                    FieldFlag.LIGHTNING,
                    FieldFlag.GRIEF_REVERT,
                    FieldFlag.PLACE_GRIEF,
                    FieldFlag.ENTRY_ALERT,
                    FieldFlag.ENTRY_GAME_MODE,
                    FieldFlag.LEAVING_GAME_MODE,
                    FieldFlag.CONFISCATE_ITEMS,
                    FieldFlag.UNUSABLE_ITEMS,
                    FieldFlag.TELEPORT_ON_ENTRY,
                    FieldFlag.TELEPORT_ON_EXIT,
                    FieldFlag.TELEPORT_ON_DAMAGE,
                    FieldFlag.TELEPORT_ON_FEEDING,
                    FieldFlag.TELEPORT_ON_FIRE,
                    FieldFlag.TELEPORT_ON_PVP,
                    FieldFlag.TELEPORT_IF_WALKING_ON,
                    FieldFlag.TELEPORT_IF_NOT_WALKING_ON,
                    FieldFlag.TELEPORT_IF_HOLDING_ITEMS,
                    FieldFlag.TELEPORT_IF_NOT_HOLDING_ITEMS,
                    FieldFlag.TELEPORT_IF_HAS_ITEMS,
                    FieldFlag.TELEPORT_IF_NOT_HAS_ITEMS,
                    FieldFlag.TELEPORT_ON_BLOCK_BREAK,
                    FieldFlag.TELEPORT_ON_BLOCK_PLACE,
                    FieldFlag.TELEPORT_BEFORE_DEATH,
                    FieldFlag.PREVENT_POTION_SPLASH,
                    FieldFlag.NO_PROJECTILE_THROW,
                    FieldFlag.NO_DROPPING_ITEMS,
                    FieldFlag.NO_PLAYER_SPRINT,
                    FieldFlag.PREVENT_VEHICLE_ENTER,
                    FieldFlag.PREVENT_VEHICLE_EXIT,
                    FieldFlag.COMMAND_BLACKLIST,
                    FieldFlag.NO_RESIZE
            };

    private final static Map<String, FieldFlag> flags = Maps.newHashMap();


    /**
     * These flags will be hidden completely from the flag lists
     */
    private final static FieldFlag[] hidden = new FieldFlag[]
            {
                    FieldFlag.ALL,
                    FieldFlag.DYNMAP_NO_TOGGLE
            };

    /**
     * These flags are nameable
     */
    private final static FieldFlag[] nameable = new FieldFlag[]
            {
                    FieldFlag.WELCOME_MESSAGE,
                    FieldFlag.FAREWELL_MESSAGE,
                    FieldFlag.ENTRY_ALERT,
                    FieldFlag.TRANSLOCATION,
                    FieldFlag.TELEPORT_IF_WALKING_ON,
                    FieldFlag.TELEPORT_IF_NOT_WALKING_ON,
                    FieldFlag.TELEPORT_IF_HOLDING_ITEMS,
                    FieldFlag.TELEPORT_IF_NOT_HOLDING_ITEMS,
                    FieldFlag.TELEPORT_IF_HAS_ITEMS,
                    FieldFlag.TELEPORT_IF_NOT_HAS_ITEMS,
                    FieldFlag.TELEPORT_BEFORE_DEATH,
                    FieldFlag.TELEPORT_ON_DAMAGE,
                    FieldFlag.TELEPORT_ON_FEEDING,
                    FieldFlag.TELEPORT_MOBS_ON_ENABLE,
                    FieldFlag.TELEPORT_ANIMALS_ON_ENABLE,
                    FieldFlag.TELEPORT_PLAYERS_ON_ENABLE,
                    FieldFlag.TELEPORT_VILLAGERS_ON_ENABLE,
                    FieldFlag.TELEPORT_ON_FIRE,
                    FieldFlag.TELEPORT_ON_PVP,
                    FieldFlag.TELEPORT_ON_BLOCK_PLACE,
                    FieldFlag.TELEPORT_ON_BLOCK_BREAK,
                    FieldFlag.TELEPORT_ON_SNEAK,
                    FieldFlag.TELEPORT_ON_ENTRY,
                    FieldFlag.TELEPORT_ON_EXIT,
                    FieldFlag.TELEPORT_DESTINATION,
                    FieldFlag.SINGLE_USE
            };

    /**
     * These flags will not be able to be toggled
     */
    private final static FieldFlag[] unToggable = new FieldFlag[]
            {
                    FieldFlag.WORLDGUARD_REPELLENT,
                    FieldFlag.PLACE_DISABLED,
                    FieldFlag.SNEAKING_BYPASS,
                    FieldFlag.BREAKABLE_ON_DISABLED,
                    FieldFlag.MODIFY_ON_DISABLED,
                    FieldFlag.REDEFINE_ON_DISABLED,
                    FieldFlag.PREVENT_UNPROTECTABLE,
                    FieldFlag.TOGGLE_ON_DISABLED,
                    FieldFlag.NO_CONFLICT,
                    FieldFlag.NO_PLAYER_PLACE,
                    FieldFlag.NO_ALLOWING,
                    FieldFlag.CUBOID,
                    FieldFlag.DYNMAP_DISABLED,
                    FieldFlag.HIDABLE,
                    FieldFlag.TRANSLOCATION,
                    FieldFlag.SNEAK_TO_PLACE,
                    FieldFlag.COMMAND_ON_ENTER,
                    FieldFlag.COMMAND_ON_EXIT,
                    FieldFlag.PLAYER_COMMAND_ON_ENTER,
                    FieldFlag.PLAYER_COMMAND_ON_EXIT,
                    FieldFlag.DISABLE_WHEN_ONLINE,
                    FieldFlag.MUST_BE_ABOVE,
                    FieldFlag.MUST_BE_BELOW,
                    FieldFlag.DISABLE_ON_LOGOFF,
                    FieldFlag.ENABLE_ON_LOGON,
                    FieldFlag.RENTABLE,
                    FieldFlag.BUYABLE,
                    FieldFlag.SHAREABLE,
                    FieldFlag.COMMAND_BLACKLIST
            };

    /**
     * Whether the flag applies to allowed
     *
     * @return
     */
    public boolean applies(Field field, Player player) {
        return applies(field, player.getName());
    }

    /**
     * Whether the flag applies to allowed
     *
     * @return
     */
    public boolean applies(Field field, String playerName) {
        // if the field doesn't have the flag then it doesn't apply

        if (!field.hasFlag(this)) {
            return false;
        }

        // find out if the player is allowed on this field

        boolean allowed = PreciousStones.getInstance().getForceFieldManager().isAllowed(field, playerName);

        // reverse the flag if its in the reversible list

        if (field.getSettings().isReversedFlag(this)) {
            allowed = !allowed;
        }

        // if its a flag that applies to non-allowed players,
        // then return true when the player is not allowed

        if (appliesToNonAllowed(this)) {
            allowed = !allowed;
        }

        // allow the if flag is in the allable list

        if (field.getSettings().isAlledFlag(this)) {
            allowed = true;
        }

        // return whether the player is allowed or not

        return allowed;
    }


    /**
     * If the flag applies to non-allowed players
     *
     * @param flag
     * @return
     */
    private static boolean appliesToNonAllowed(FieldFlag flag) {
        for (FieldFlag aa : applyToNonAllowed) {
            if (aa.equals(flag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether this flag is nameable
     *
     * @return
     */
    public boolean isNameable() {
        String flagStr = this.toString();

        for (FieldFlag flag : nameable) {
            if (flag.toString().equalsIgnoreCase(flagStr)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether this flag is un-toggable
     *
     * @return
     */
    public boolean isUnToggable() {
        String flagStr = this.toString();

        for (FieldFlag flag : unToggable) {
            if (flag.toString().equalsIgnoreCase(flagStr)) {
                return true;
            }
        }
        for (FieldFlag flag : hidden) {
            if (flag.toString().equalsIgnoreCase(flagStr)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns all the flags that should be hidden
     *
     * @return
     */
    public static FieldFlag[] getHidden() {
        return hidden;
    }

    /**
     * Returns a FieldFlag based on its flag string
     *
     * @param flagStr
     * @return
     */
    public static FieldFlag getByString(String flagStr) {
        return flags.get(stripModifiers(flagStr));
    }

    /**
     * Returns a FieldFlag based on its flag string
     *
     * @param flagStr
     * @return
     */
    public static boolean isFlag(String flagStr) {
        return flags.get(flagStr) != null;
    }

    /**
     * Removes a flag's trailing modifiers
     *
     * @param flagStr
     * @return
     */
    public static String stripModifiers(String flagStr) {
        if (flagStr == null || flagStr.isEmpty()) {
            return flagStr;
        }

        boolean hasModifier = flagStr.startsWith("^") || flagStr.startsWith("~") || flagStr.startsWith("?");

        while (hasModifier && flagStr.length() > 0) {
            flagStr = flagStr.substring(1);
            hasModifier = flagStr.startsWith("^") || flagStr.startsWith("~") || flagStr.startsWith("?");
        }

        return flagStr;
    }

    /**
     * Returns the flag string for this flag
     *
     * @return
     */

    public String toString() {
        return this.name().replace('_', '-').toLowerCase();
    }

    static {
        for (FieldFlag flag : values()) {
            flags.put(flag.toString(), flag);
        }
    }
}
