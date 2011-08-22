package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author phaed
 */
public class FieldSettings
{
    private boolean validField = true;
    private int typeId;
    private int radius = 0;
    private int height = 0;
    private int launchHeight = 0;
    private int cannonHeight = 0;
    private int mineDelaySeconds = 0;
    private int mineReplaceBlock = 0;
    private int lightningDelaySeconds = 0;
    private int lightningReplaceBlock = 0;
    private String title;
    private int price = 0;
    private List<Integer> limits = new ArrayList<Integer>();
    private List<Integer> preventUse = new ArrayList<Integer>();
    private List<FieldFlag> flags = new ArrayList<FieldFlag>();

    /**
     *
     */
    public enum FieldFlag
    {
        PREVENT_FIRE,
        PREVENT_PLACE,
        PREVENT_DESTROY,
        PREVENT_EXPLOSIONS,
        PREVENT_PVP,
        PREVENT_MOB_DAMAGE,
        PREVENT_MOB_SPAWN,
        PREVENT_ANIMAL_SPAWN,
        PREVENT_ENTRY,
        PREVENT_UNPROTECTABLE,
        PREVENT_FLOW,
        INSTANT_HEAL,
        SLOW_HEAL,
        SLOW_DAMAGE,
        FAST_DAMAGE,
        BREAKABLE,
        WELCOME_MESSAGE,
        FAREWELL_MESSAGE,
        GIVE_AIR,
        SNITCH,
        NO_CONFLICT,
        LAUNCH,
        CANNON,
        MINE,
        LIGHTNING,
        NO_OWNER,
        FORESTER,
        FORESTER_SHRUBS,
        GRIEF_UNDO_INTERVAL,
        GRIEF_UNDO_REQUEST,
        ENTRY_ALERT
    }

    /**
     *
     * @param map
     */
    @SuppressWarnings("unchecked")
    public FieldSettings(LinkedHashMap map)
    {
        if (map.containsKey("block") && Helper.isInteger(map.get("block")))
        {
            typeId = (Integer) map.get("block");
        }
        else
        {
            validField = false;
            return;
        }

        if (map.containsKey("title") && Helper.isString(map.get("title")))
        {
            title = (String) map.get("title");
        }
        else
        {
            validField = false;
            return;
        }

        if (map.containsKey("radius") && Helper.isInteger(map.get("radius")))
        {
            radius = (Integer) map.get("radius");
        }

        if (map.containsKey("custom-height"))
        {
            if (Helper.isInteger(map.get("custom-height")))
            {
                height = (Integer) map.get("custom-height");

            }
            if (height == 0)
            {
                height = radius;
            }
        }

        if (map.containsKey("launch-height") && Helper.isInteger(map.get("launch-height")))
        {
            launchHeight = (Integer) map.get("launch-height");
        }

        if (map.containsKey("cannon-height") && Helper.isInteger(map.get("cannon-height")))
        {
            cannonHeight = (Integer) map.get("cannon-height");
        }

        if (map.containsKey("mine-replace-block") && Helper.isInteger(map.get("mine-replace-block")))
        {
            mineReplaceBlock = (Integer) map.get("mine-replace-block");
        }

        if (map.containsKey("mine-delay-seconds") && Helper.isInteger(map.get("mine-delay-seconds")))
        {
            mineDelaySeconds = (Integer) map.get("mine-delay-seconds");
        }

        if (map.containsKey("lightning-replace-block") && Helper.isInteger(map.get("lightning-replace-block")))
        {
            lightningReplaceBlock = (Integer) map.get("lightning-replace-block");
        }

        if (map.containsKey("lightning-delay-seconds") && Helper.isInteger(map.get("lightning-delay-seconds")))
        {
            lightningDelaySeconds = (Integer) map.get("lightning-delay-seconds");
        }

        if (map.containsKey("prevent-use") && Helper.isIntList(map.get("prevent-use")))
        {
            preventUse = (List<Integer>) map.get("prevent-use");
        }

        if (map.containsKey("price") && Helper.isInteger(map.get("price")))
        {
            price = (Integer) map.get("price");
        }

        if (map.containsKey("limits") && Helper.isIntList(map.get("limits")))
        {
            limits = (List<Integer>) map.get("limits");
        }

        if (map.containsKey("prevent-fire") && Helper.isBoolean(map.get("prevent-fire")))
        {
            if ((Boolean) map.get("prevent-fire"))
            {
                flags.add(FieldFlag.PREVENT_FIRE);
            }
        }

        if (map.containsKey("prevent-place") && Helper.isBoolean(map.get("prevent-place")))
        {
            if ((Boolean) map.get("prevent-place"))
            {
                flags.add(FieldFlag.PREVENT_PLACE);
            }
        }

        if (map.containsKey("prevent-destroy") && Helper.isBoolean(map.get("prevent-destroy")))
        {
            if ((Boolean) map.get("prevent-destroy"))
            {
                flags.add(FieldFlag.PREVENT_DESTROY);
            }
        }

        if (map.containsKey("prevent-explosions") && Helper.isBoolean(map.get("prevent-explosions")))
        {
            if ((Boolean) map.get("prevent-explosions"))
            {
                flags.add(FieldFlag.PREVENT_EXPLOSIONS);
            }
        }

        if (map.containsKey("prevent-pvp") && Helper.isBoolean(map.get("prevent-pvp")))
        {
            if ((Boolean) map.get("prevent-pvp"))
            {
                flags.add(FieldFlag.PREVENT_PVP);
            }
        }

        if (map.containsKey("prevent-mob-damage") && Helper.isBoolean(map.get("prevent-mob-damage")))
        {
            if ((Boolean) map.get("prevent-mob-damage"))
            {
                flags.add(FieldFlag.PREVENT_MOB_DAMAGE);
            }
        }

        if (map.containsKey("prevent-mob-spawn") && Helper.isBoolean(map.get("prevent-mob-spawn")))
        {
            if ((Boolean) map.get("prevent-mob-spawn"))
            {
                flags.add(FieldFlag.PREVENT_MOB_SPAWN);
            }
        }

        if (map.containsKey("prevent-animal-spawn") && Helper.isBoolean(map.get("prevent-animal-spawn")))
        {
            if ((Boolean) map.get("prevent-animal-spawn"))
            {
                flags.add(FieldFlag.PREVENT_ANIMAL_SPAWN);
            }
        }

        if (map.containsKey("prevent-entry") && Helper.isBoolean(map.get("prevent-entry")))
        {
            if ((Boolean) map.get("prevent-entry"))
            {
                flags.add(FieldFlag.PREVENT_ENTRY);
            }
        }

        if (map.containsKey("prevent-unprotectable") && Helper.isBoolean(map.get("prevent-unprotectable")))
        {
            if ((Boolean) map.get("prevent-unprotectable"))
            {
                flags.add(FieldFlag.PREVENT_UNPROTECTABLE);
            }
        }

        if (map.containsKey("instant-heal") && Helper.isBoolean(map.get("instant-heal")))
        {
            if ((Boolean) map.get("instant-heal"))
            {
                flags.add(FieldFlag.INSTANT_HEAL);
            }
        }

        if (map.containsKey("slow-heal") && Helper.isBoolean(map.get("slow-heal")))
        {
            if ((Boolean) map.get("slow-heal"))
            {
                flags.add(FieldFlag.SLOW_HEAL);
            }
        }

        if (map.containsKey("slow-damage") && Helper.isBoolean(map.get("slow-damage")))
        {
            if ((Boolean) map.get("slow-damage"))
            {
                flags.add(FieldFlag.SLOW_DAMAGE);
            }
        }

        if (map.containsKey("fast-damage") && Helper.isBoolean(map.get("fast-damage")))
        {
            if ((Boolean) map.get("fast-damage"))
            {
                flags.add(FieldFlag.FAST_DAMAGE);
            }
        }

        if (map.containsKey("breakable") && Helper.isBoolean(map.get("breakable")))
        {
            if ((Boolean) map.get("breakable"))
            {
                flags.add(FieldFlag.BREAKABLE);
            }
        }

        if (map.containsKey("welcome-message") && Helper.isBoolean(map.get("welcome-message")))
        {
            if ((Boolean) map.get("welcome-message"))
            {
                flags.add(FieldFlag.WELCOME_MESSAGE);
            }
        }

        if (map.containsKey("farewell-message") && Helper.isBoolean(map.get("farewell-message")))
        {
            if ((Boolean) map.get("farewell-message"))
            {
                flags.add(FieldFlag.FAREWELL_MESSAGE);
            }
        }

        if (map.containsKey("give-air") && Helper.isBoolean(map.get("give-air")))
        {
            if ((Boolean) map.get("give-air"))
            {
                flags.add(FieldFlag.GIVE_AIR);
            }
        }

        if (map.containsKey("snitch") && Helper.isBoolean(map.get("snitch")))
        {
            if ((Boolean) map.get("snitch"))
            {
                flags.add(FieldFlag.SNITCH);
            }
        }

        if (map.containsKey("no-conflict") && Helper.isBoolean(map.get("no-conflict")))
        {
            if ((Boolean) map.get("no-conflict"))
            {
                flags.add(FieldFlag.NO_CONFLICT);
            }
        }

        if (map.containsKey("no-owner") && Helper.isBoolean(map.get("no-owner")))
        {
            if ((Boolean) map.get("no-owner"))
            {
                flags.add(FieldFlag.NO_OWNER);
            }
        }

        if (map.containsKey("launch") && Helper.isBoolean(map.get("launch")))
        {
            if ((Boolean) map.get("launch"))
            {
                flags.add(FieldFlag.LAUNCH);
            }
        }

        if (map.containsKey("cannon") && Helper.isBoolean(map.get("cannon")))
        {
            if ((Boolean) map.get("cannon"))
            {
                flags.add(FieldFlag.CANNON);
            }
        }

        if (map.containsKey("mine") && Helper.isBoolean(map.get("mine")))
        {
            if ((Boolean) map.get("mine"))
            {
                flags.add(FieldFlag.MINE);
            }
        }

        if (map.containsKey("lightning") && Helper.isBoolean(map.get("lightning")))
        {
            if ((Boolean) map.get("lightning"))
            {
                flags.add(FieldFlag.LIGHTNING);
            }
        }

        if (map.containsKey("prevent-flow") && Helper.isBoolean(map.get("prevent-flow")))
        {
            if ((Boolean) map.get("prevent-flow"))
            {
                flags.add(FieldFlag.PREVENT_FLOW);
            }
        }

        if (map.containsKey("forester") && Helper.isBoolean(map.get("forester")))
        {
            if ((Boolean) map.get("forester"))
            {
                flags.add(FieldFlag.FORESTER);
            }
        }

        if (map.containsKey("forester-shrubs") && Helper.isBoolean(map.get("forester-shrubs")))
        {
            if ((Boolean) map.get("forester-shrubs"))
            {
                flags.add(FieldFlag.FORESTER_SHRUBS);
            }
        }

        if (map.containsKey("grief-undo-request") && Helper.isBoolean(map.get("grief-undo-request")))
        {
            if ((Boolean) map.get("grief-undo-request"))
            {
                flags.add(FieldFlag.GRIEF_UNDO_REQUEST);
            }
        }

        if (map.containsKey("grief-undo-interval") && Helper.isBoolean(map.get("grief-undo-interval")))
        {
            if ((Boolean) map.get("grief-undo-interval"))
            {
                flags.add(FieldFlag.GRIEF_UNDO_INTERVAL);
            }
        }

        if (map.containsKey("entry-alert") && Helper.isBoolean(map.get("entry-alert")))
        {
            if ((Boolean) map.get("entry-alert"))
            {
                flags.add(FieldFlag.ENTRY_ALERT);
            }
        }
    }

    /**
     * Check if the field has a flag
     * @param flag
     * @return
     */
    public boolean hasFlag(FieldFlag flag)
    {
        return flags.contains(flag);
    }

    /**
     *
     * @return
     */
    public boolean hasNameableFlag()
    {
        return flags.contains(FieldFlag.WELCOME_MESSAGE) || flags.contains(FieldFlag.FAREWELL_MESSAGE) || flags.contains(FieldFlag.ENTRY_ALERT);
    }

    /**
     *
     * @return
     */
    public boolean hasGriefUndoFlag()
    {
        return flags.contains(FieldFlag.GRIEF_UNDO_INTERVAL) || flags.contains(FieldFlag.GRIEF_UNDO_REQUEST);
    }

    /**
     *
     * @return
     */
    public boolean hasVeocityFlag()
    {
        return flags.contains(FieldFlag.CANNON) || flags.contains(FieldFlag.LAUNCH);
    }

    /**
     *
     * @return
     */
    public boolean hasForesterFlag()
    {
        return flags.contains(FieldFlag.FORESTER) || flags.contains(FieldFlag.FORESTER_SHRUBS);
    }

    /**
     * 
     * @return
     */
    public boolean hasLimit()
    {
        return !limits.isEmpty();
    }

    /**
     *
     * @return
     */
    public String getTitle()
    {
        if (title == null)
        {
            return "";
        }

        return title;
    }

    /**
     *
     * @return
     */
    public int getHeight()
    {
        if (this.height == 0)
        {
            return (this.getRadius() * 2) + 1;
        }
        else
        {
            return this.height;
        }
    }

    /**
     * Whether a block type can be used in this field
     * @param type
     * @return
     */
    public boolean canUse(int type)
    {
        if (preventUse == null)
        {
            return true;
        }

        return !preventUse.contains(type);
    }

    /**
     * @return the typeId
     */
    public int getTypeId()
    {
        return typeId;
    }

    /**
     * @return the radius
     */
    public int getRadius()
    {
        return radius;
    }

    /**
     * @return the launchHeight
     */
    public int getLaunchHeight()
    {
        return launchHeight;
    }

    /**
     * @return the cannonHeight
     */
    public int getCannonHeight()
    {
        return cannonHeight;
    }

    /**
     * @return the mineDelaySeconds
     */
    public int getMineDelaySeconds()
    {
        return mineDelaySeconds;
    }

    /**
     * @return the mineReplaceBlock
     */
    public int getMineReplaceBlock()
    {
        return mineReplaceBlock;
    }

    /**
     * @return the lightningDelaySeconds
     */
    public int getLightningDelaySeconds()
    {
        return lightningDelaySeconds;
    }

    /**
     * @return the lightningReplaceBlock
     */
    public int getLightningReplaceBlock()
    {
        return lightningReplaceBlock;
    }

    /**
     * @return the price
     */
    public int getPrice()
    {
        return price;
    }

    /**
     * @return the validField
     */
    public boolean isValidField()
    {
        return validField;
    }

    /**
     * @return the limits
     */
    public List<Integer> getLimits()
    {
        if (limits == null)
        {
            return new ArrayList<Integer>();
        }
        return limits;
    }
}
