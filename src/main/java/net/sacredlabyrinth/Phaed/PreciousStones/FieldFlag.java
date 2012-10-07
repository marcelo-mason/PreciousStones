package net.sacredlabyrinth.Phaed.PreciousStones;

import com.google.common.collect.Maps;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @author phaed
 */
public enum FieldFlag
{
    ALL,
    ALLOW_PLACE,
    ALLOW_DESTROY,
    PREVENT_FIRE,
    PREVENT_PLACE,
    PREVENT_DESTROY,
    PREVENT_VEHICLE_DESTROY,
    PREVENT_ENDERMAN_DESTROY,
    PREVENT_EXPLOSIONS,
    PREVENT_CREEPER_EXPLOSIONS,
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
    PROTECT_ANIMALS,
    PROTECT_MOBS,
    PROTECT_VILLAGERS,
    PROTECT_CROPS,
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
    NO_PLAYER_PLACE,
    APPLY_TO_REVERSE,
    APPLY_TO_ALL,
    ALLOWED_CAN_BREAK,
    SNEAKING_BYPASS,
    PLACE_DISABLED,
    PREVENT_USE,
    DYNMAP_AREA,
    DYNMAP_MARKER,
    DYNMAP_NO_TOGGLE,
    DYNMAP_DISABLED,
    CAN_CHANGE_OWNER,
    PLOT,
    POTIONS,
    NEUTRALIZE_POTIONS,
    SNEAK_TO_PLACE,
    NO_FALL_DAMAGE,
    CONFISCATE_ITEMS,
    EQUIP_ITEMS,
    ENABLE_WITH_REDSTONE,
    TRANSLOCATION,
    MASK_ON_ENABLED,
    MASK_ON_DISABLED,
    WORLDGUARD_REPELLENT,
    GROUP_ON_ENTRY,
    ENTRY_GAME_MODE,
    LEAVING_GAME_MODE,
    NO_ALLOWING,
    TELEPORTER,
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
    TELEPORT_COST
    //TELEPORT_BACK_AFTER_SECONDS
    //preciousstones.bypass.teleport

    //disabled

    //command-on-enable
    //command-on-disable
    //command-on-enter
    //command-on-leave

    ;
    private final static Map<String, FieldFlag> flags = Maps.newHashMap();

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
        FieldFlag.APPLY_TO_ALL,
        FieldFlag.APPLY_TO_REVERSE,
        FieldFlag.CUBOID,
        FieldFlag.DYNMAP_DISABLED,
        FieldFlag.NO_ALLOWING
    };

    /**
     * These flags apply to allowed
     */
    private final static FieldFlag[] applyToAll = new FieldFlag[]
    {
        FieldFlag.BREAKABLE,
        FieldFlag.PREVENT_EXPLOSIONS,
        FieldFlag.PREVENT_CREEPER_EXPLOSIONS,
        FieldFlag.PREVENT_PVP,
        FieldFlag.PREVENT_TNT_EXPLOSIONS,
        FieldFlag.PREVENT_UNPROTECTABLE,
        FieldFlag.FAREWELL_MESSAGE,
        FieldFlag.WELCOME_MESSAGE,
        FieldFlag.ROLLBACK_EXPLOSIONS,
        FieldFlag.WORLDGUARD_REPELLENT,
        FieldFlag.TELEPORT_PLAYERS_ON_ENABLE,
        FieldFlag.TELEPORT_MOBS_ON_ENABLE,
        FieldFlag.TELEPORT_VILLAGERS_ON_ENABLE
    };

    /**
     * These flags apply to allowed
     */
    private final static FieldFlag[] applyToAllowed = new FieldFlag[]
    {
        FieldFlag.ALLOWED_CAN_BREAK,
        FieldFlag.ALLOW_PLACE,
        FieldFlag.ALLOW_DESTROY,
        FieldFlag.REMOVE_MOB,
        FieldFlag.HEAL,
        FieldFlag.FEED,
        FieldFlag.REPAIR,
        FieldFlag.POTIONS,
        FieldFlag.NEUTRALIZE_POTIONS,
        FieldFlag.AIR,
        FieldFlag.LAUNCH,
        FieldFlag.CANNON,
        FieldFlag.VISUALIZE_ON_SRC,
        FieldFlag.ENABLE_ON_SRC,
        FieldFlag.GROUP_ON_ENTRY,
        FieldFlag.NO_FALL_DAMAGE,
        FieldFlag.TRANSLOCATION,
        FieldFlag.TELEPORT_ON_SNEAK
    };

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
            FieldFlag.PREVENT_MOB_DAMAGE,
            FieldFlag.PREVENT_USE,
            FieldFlag.PREVENT_TELEPORT,
            FieldFlag.PREVENT_FLIGHT,
            FieldFlag.PROTECT_ANIMALS,
            FieldFlag.PROTECT_CROPS,
            FieldFlag.PROTECT_MOBS,
            FieldFlag.PROTECT_VILLAGERS,
            FieldFlag.DAMAGE,
            FieldFlag.SNITCH,
            FieldFlag.MINE,
            FieldFlag.LIGHTNING,
            FieldFlag.GRIEF_REVERT,
            FieldFlag.PLACE_GRIEF,
            FieldFlag.ENTRY_ALERT,
            FieldFlag.NO_PLAYER_PLACE,
            FieldFlag.ENTRY_GAME_MODE,
            FieldFlag.LEAVING_GAME_MODE,
            FieldFlag.CONFISCATE_ITEMS,
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
    };

    /**
     * Whether the flag applies to allowed
     *
     * @param flag
     * @return
     */
    public final static boolean applies(Player player, Field field, FieldFlag flag)
    {
        boolean allowed = PreciousStones.getInstance().getForceFieldManager().isApplyToAllowed(field, player.getName());

        if (field.hasFlag(FieldFlag.APPLY_TO_ALL))
        {
            allowed = true;
        }
        else
        {
            for (FieldFlag aa : applyToNonAllowed)
            {
                if (aa.equals(flag))
                {
                    allowed = !allowed;
                    break;
                }
            }

            for (FieldFlag aa : applyToAll)
            {
                if (aa.equals(flag))
                {
                    allowed = true;
                    break;
                }
            }
        }

        return allowed;
    }

    /**
     * These flags will be hidden completely from the flag lists
     */
    private final static FieldFlag[] hidden = new FieldFlag[]
    {
        FieldFlag.ALL,
        FieldFlag.DYNMAP_NO_TOGGLE,
        FieldFlag.TELEPORTER
    };

    /**
     * Whether this flag is un-toggable
     *
     * @return
     */
    public boolean isUnToggable()
    {
        String flagStr = this.toString();

        for (FieldFlag flag : unToggable)
        {
            if (flag.toString().equalsIgnoreCase(flagStr))
            {
                return true;
            }
        }
        for (FieldFlag flag : hidden)
        {
            if (flag.toString().equalsIgnoreCase(flagStr))
            {
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
    public static FieldFlag[] getHidden()
    {
        return hidden;
    }

    /**
     * Returns a FieldFlag based on its flag string
     *
     * @param flagStr
     * @return
     */
    public static FieldFlag getByString(String flagStr)
    {
        return flags.get(flagStr);
    }

    /**
     * Returns the flag string for this flag
     *
     * @return
     */

    public String toString()
    {
        return this.name().replace('_', '-');
    }

    static
    {
        for (FieldFlag flag : values())
        {
            flags.put(flag.toString(), flag);
        }
    }
}
