package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.*;

/**
 * @author phaed
 */
public class FieldSettings
{
    private boolean validField = true;
    private int typeId;
    private int radius = 0;
    private int launchHeight = 0;
    private int cannonHeight = 0;
    private int customHeight = 0;
    private int customVolume = 0;
    private int mineDelaySeconds = 0;
    private int mineReplaceBlock = 0;
    private int lightningDelaySeconds = 0;
    private int lightningReplaceBlock = 0;
    private int mixingGroup = 0;
    private String title;
    private int price = 0;
    private List<Integer> limits = new ArrayList<Integer>();
    private List<Integer> preventUse = new ArrayList<Integer>();
    private Set<FieldFlag> flags = new HashSet<FieldFlag>();
    private List<FieldFlag> disabledFlags = new ArrayList<FieldFlag>();

    /**
     * @param map
     */

    public FieldSettings(LinkedHashMap<String, Object> map)
    {
        if (map == null)
        {
            return;
        }

        if (map.containsKey("block") && Helper.isInteger(map.get("block")) && ((Integer) map.get("block") > 0))
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
                customHeight = (Integer) map.get("custom-height");

                if (customHeight % 2 == 0)
                {
                    customHeight++;
                }
            }
        }

        if (map.containsKey("mixing-group") && Helper.isInteger(map.get("mixing-group")))
        {
            mixingGroup = (Integer) map.get("mixing-group");
        }

        if (map.containsKey("custom-volume") && Helper.isInteger(map.get("custom-volume")))
        {
            customVolume = (Integer) map.get("custom-volume");
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

        if (map.containsKey("rollback-explosions") && Helper.isBoolean(map.get("rollback-explosions")))
        {
            if ((Boolean) map.get("rollback-explosions"))
            {
                flags.add(FieldFlag.ROLLBACK_EXPLOSIONS);
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

        if (map.containsKey("remove-mob") && Helper.isBoolean(map.get("remove-mob")))
        {
            if ((Boolean) map.get("remove-mob"))
            {
                flags.add(FieldFlag.REMOVE_MOB);
            }
        }

        if (map.containsKey("remove-animal") && Helper.isBoolean(map.get("remove-animal")))
        {
            if ((Boolean) map.get("remove-animal"))
            {
                flags.add(FieldFlag.REMOVE_ANIMAL);
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

        if (map.containsKey("slow-feeding") && Helper.isBoolean(map.get("slow-feeding")))
        {
            if ((Boolean) map.get("slow-feeding"))
            {
                flags.add(FieldFlag.SLOW_FEEDING);
            }
        }

        if (map.containsKey("slow-repair") && Helper.isBoolean(map.get("slow-repair")))
        {
            if ((Boolean) map.get("slow-repair"))
            {
                flags.add(FieldFlag.SLOW_REPAIR);
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

        if (map.containsKey("grief-revert") && Helper.isBoolean(map.get("grief-revert")))
        {
            if ((Boolean) map.get("grief-revert"))
            {
                flags.add(FieldFlag.GRIEF_REVERT);
            }
        }

        if (map.containsKey("grief-revert-drop") && Helper.isBoolean(map.get("grief-revert-drop")))
        {
            if ((Boolean) map.get("grief-revert-drop"))
            {
                flags.add(FieldFlag.GRIEF_REVERT_DROP);
            }
        }

        if (map.containsKey("entry-alert") && Helper.isBoolean(map.get("entry-alert")))
        {
            if ((Boolean) map.get("entry-alert"))
            {
                flags.add(FieldFlag.ENTRY_ALERT);
            }
        }

        if (map.containsKey("cuboid") && Helper.isBoolean(map.get("cuboid")))
        {
            if ((Boolean) map.get("cuboid"))
            {
                flags.add(FieldFlag.CUBOID);
            }
        }

        if (map.containsKey("visualize-on-rightclick") && Helper.isBoolean(map.get("visualize-on-rightclick")))
        {
            if ((Boolean) map.get("visualize-on-rightclick"))
            {
                flags.add(FieldFlag.VISUALIZE_ON_RIGHT_CLICK);
            }
        }

        if (map.containsKey("visualize-on-place") && Helper.isBoolean(map.get("visualize-on-place")))
        {
            if ((Boolean) map.get("visualize-on-place"))
            {
                flags.add(FieldFlag.VISUALIZE_ON_PLACE);
            }
        }

        if (map.containsKey("keep-chunks-loaded") && Helper.isBoolean(map.get("keep-chunks-loaded")))
        {
            if ((Boolean) map.get("keep-chunks-loaded"))
            {
                flags.add(FieldFlag.KEEP_CHUNKS_LOADED);
            }
        }

        flags.add(FieldFlag.ALL);
    }

    /**
     * Add a flag to the settings
     *
     * @param flagStr
     */
    public boolean insertFlag(String flagStr)
    {
        FieldFlag flag = Helper.toFieldFlag(flagStr);

        if (flag != null && !flags.contains(flag))
        {
            flags.add(flag);
            return true;
        }
        return false;
    }

    /**
     * Enable a flag
     *
     * @param flagStr
     */
    public void enableFlag(String flagStr)
    {
        for (Iterator iter = disabledFlags.iterator(); iter.hasNext(); )
        {
            FieldFlag flag = (FieldFlag) iter.next();

            if (Helper.toFlagStr(flag).equals(flagStr))
            {
                iter.remove();
                flags.add(flag);
            }
        }
    }

    /**
     * Disabled a flag.
     *
     * @param flagStr
     */
    public void disableFlag(String flagStr)
    {
        for (Iterator iter = flags.iterator(); iter.hasNext(); )
        {
            FieldFlag flag = (FieldFlag) iter.next();
            if (Helper.toFlagStr(flag).equals(flagStr))
            {
                iter.remove();
                disabledFlags.add(flag);
            }
        }
    }


    /**
     * Check if the field has a flag
     *
     * @param flag
     * @return
     */
    public boolean hasFlag(String flagStr)
    {
        for (FieldFlag flag : flags)
        {
            if (Helper.toFlagStr(flag).equals(flagStr))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the field has a flag
     *
     * @param flag
     * @return
     */
    public boolean hasFlag(FieldFlag flag)
    {
        return flags.contains(flag);
    }

    /**
     * @return
     */
    public boolean hasNameableFlag()
    {
        return flags.contains(FieldFlag.WELCOME_MESSAGE) || flags.contains(FieldFlag.FAREWELL_MESSAGE) || flags.contains(FieldFlag.ENTRY_ALERT);
    }

    /**
     * @return
     */
    public boolean hasVeocityFlag()
    {
        return flags.contains(FieldFlag.CANNON) || flags.contains(FieldFlag.LAUNCH);
    }

    /**
     * @return
     */
    public boolean hasForesterFlag()
    {
        return flags.contains(FieldFlag.FORESTER) || flags.contains(FieldFlag.FORESTER_SHRUBS);
    }

    /**
     * @return
     */
    public boolean hasLimit()
    {
        return !limits.isEmpty();
    }

    /**
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
     * @return
     */
    public int getHeight()
    {
        if (this.customHeight > 0)
        {
            return this.customHeight;
        }

        return (this.getRadius() * 2) + 1;
    }

    /**
     * Whether a block type can be used in this field
     *
     * @param type
     * @return
     */
    public boolean canUse(int type)
    {
        return preventUse == null || !preventUse.contains(type);

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
        return Collections.unmodifiableList(limits);
    }

    public Set<FieldFlag> getFlags()
    {
        return Collections.unmodifiableSet(flags);
    }

    public List<FieldFlag> getDisabledFlags()
    {
        return Collections.unmodifiableList(disabledFlags);
    }

    public int getCustomVolume()
    {
        return customVolume;
    }

    public int getMixingGroup()
    {
        return mixingGroup;
    }
}
