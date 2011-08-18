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
    private boolean blockDefined = true;
    private int blockId;
    private int radius = 0;
    private int height = 0;
    private int launchHeight = 0;
    private int cannonHeight = 0;
    private int mineDelaySeconds = 0;
    private int mineReplaceBlock = 0;
    private int lightningDelaySeconds = 0;
    private int lightningReplaceBlock = 0;
    private String title;
    private boolean preventFire = false;
    private boolean preventPlace = false;
    private boolean preventDestroy = false;
    private boolean preventExplosions = false;
    private boolean preventPvP = false;
    private boolean preventMobDamage = false;
    private boolean preventMobSpawn = false;
    private boolean preventAnimalSpawn = false;
    private boolean preventEntry = false;
    private boolean preventUnprotectable = false;
    private boolean preventFlow = false;
    private List<Integer> preventUse = new ArrayList<Integer>();
    private boolean instantHeal = false;
    private boolean slowHeal = false;
    private boolean slowDamage = false;
    private boolean fastDamage = false;
    private boolean breakable = false;
    private boolean welcomeMessage = false;
    private boolean farewellMessage = false;
    private boolean giveAir = false;
    private boolean snitch = false;
    private boolean noConflict = false;
    private boolean launch = false;
    private boolean cannon = false;
    private boolean mine = false;
    private boolean lightning = false;
    private boolean noOwner = false;
    private boolean forester = false;
    private boolean foresterShrubs = false;
    private boolean griefUndoInterval = false;
    private boolean griefUndoRequest = false;
    private boolean entryAlert = false;
    private int price = 0;

    /**
     *
     * @param map
     */
    @SuppressWarnings("unchecked")
    public FieldSettings(LinkedHashMap map)
    {
        if (map.containsKey("block") && Helper.isInteger(map.get("block")))
        {
            blockId = (Integer) map.get("block");
        }
        else
        {
            blockDefined = false;
            return;
        }

        if (map.containsKey("title") && Helper.isString(map.get("title")))
        {
            title = (String) map.get("title");
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

        if (map.containsKey("prevent-fire") && Helper.isBoolean(map.get("prevent-fire")))
        {
            preventFire = (Boolean) map.get("prevent-fire");
        }

        if (map.containsKey("prevent-place") && Helper.isBoolean(map.get("prevent-place")))
        {
            preventPlace = (Boolean) map.get("prevent-place");
        }

        if (map.containsKey("prevent-destroy") && Helper.isBoolean(map.get("prevent-destroy")))
        {
            preventDestroy = (Boolean) map.get("prevent-destroy");
        }

        if (map.containsKey("prevent-explosions") && Helper.isBoolean(map.get("prevent-explosions")))
        {
            preventExplosions = (Boolean) map.get("prevent-explosions");
        }

        if (map.containsKey("prevent-pvp") && Helper.isBoolean(map.get("prevent-pvp")))
        {
            preventPvP = (Boolean) map.get("prevent-pvp");
        }

        if (map.containsKey("prevent-mob-damage") && Helper.isBoolean(map.get("prevent-mob-damage")))
        {
            preventMobDamage = (Boolean) map.get("prevent-mob-damage");
        }

        if (map.containsKey("prevent-mob-spawn") && Helper.isBoolean(map.get("prevent-mob-spawn")))
        {
            preventMobSpawn = (Boolean) map.get("prevent-mob-spawn");
        }

        if (map.containsKey("prevent-animal-spawn") && Helper.isBoolean(map.get("prevent-animal-spawn")))
        {
            preventAnimalSpawn = (Boolean) map.get("prevent-animal-spawn");
        }

        if (map.containsKey("prevent-entry") && Helper.isBoolean(map.get("prevent-entry")))
        {
            preventEntry = (Boolean) map.get("prevent-entry");
        }

        if (map.containsKey("prevent-unprotectable") && Helper.isBoolean(map.get("prevent-unprotectable")))
        {
            preventUnprotectable = (Boolean) map.get("prevent-unprotectable");
        }

        if (map.containsKey("instant-heal") && Helper.isBoolean(map.get("instant-heal")))
        {
            instantHeal = (Boolean) map.get("instant-heal");
        }

        if (map.containsKey("slow-heal") && Helper.isBoolean(map.get("slow-heal")))
        {
            slowHeal = (Boolean) map.get("slow-heal");
        }

        if (map.containsKey("slow-damage") && Helper.isBoolean(map.get("slow-damage")))
        {
            slowDamage = (Boolean) map.get("slow-damage");
        }

        if (map.containsKey("fast-damage") && Helper.isBoolean(map.get("fast-damage")))
        {
            fastDamage = (Boolean) map.get("fast-damage");
        }

        if (map.containsKey("breakable") && Helper.isBoolean(map.get("breakable")))
        {
            breakable = (Boolean) map.get("breakable");
        }

        if (map.containsKey("welcome-message") && Helper.isBoolean(map.get("welcome-message")))
        {
            welcomeMessage = (Boolean) map.get("welcome-message");
        }

        if (map.containsKey("farewell-message") && Helper.isBoolean(map.get("farewell-message")))
        {
            farewellMessage = (Boolean) map.get("farewell-message");
        }

        if (map.containsKey("give-air") && Helper.isBoolean(map.get("give-air")))
        {
            giveAir = (Boolean) map.get("give-air");
        }

        if (map.containsKey("snitch") && Helper.isBoolean(map.get("snitch")))
        {
            snitch = (Boolean) map.get("snitch");
        }

        if (map.containsKey("no-conflict") && Helper.isBoolean(map.get("no-conflict")))
        {
            noConflict = (Boolean) map.get("no-conflict");
        }

        if (map.containsKey("launch") && Helper.isBoolean(map.get("launch")))
        {
            launch = (Boolean) map.get("launch");
        }

        if (map.containsKey("launch-height") && Helper.isInteger(map.get("launch-height")))
        {
            launchHeight = (Integer) map.get("launch-height");
        }

        if (map.containsKey("cannon") && Helper.isBoolean(map.get("cannon")))
        {
            cannon = (Boolean) map.get("cannon");
        }

        if (map.containsKey("cannon-height") && Helper.isInteger(map.get("cannon-height")))
        {
            cannonHeight = (Integer) map.get("cannon-height");
        }

        if (map.containsKey("mine") && Helper.isBoolean(map.get("mine")))
        {
            mine = (Boolean) map.get("mine");
        }

        if (map.containsKey("mine-replace-block") && Helper.isInteger(map.get("mine-replace-block")))
        {
            mineReplaceBlock = (Integer) map.get("mine-replace-block");
        }

        if (map.containsKey("mine-delay-seconds") && Helper.isInteger(map.get("mine-delay-seconds")))
        {
            mineDelaySeconds = (Integer) map.get("mine-delay-seconds");
        }

        if (map.containsKey("lightning") && Helper.isBoolean(map.get("lightning")))
        {
            lightning = (Boolean) map.get("lightning");
        }

        if (map.containsKey("lightning-replace-block") && Helper.isInteger(map.get("lightning-replace-block")))
        {
            lightningReplaceBlock = (Integer) map.get("lightning-replace-block");
        }

        if (map.containsKey("lightning-delay-seconds") && Helper.isInteger(map.get("lightning-delay-seconds")))
        {
            lightningDelaySeconds = (Integer) map.get("lightning-delay-seconds");
        }

        if (map.containsKey("prevent-flow") && Helper.isBoolean(map.get("prevent-flow")))
        {
            preventFlow = (Boolean) map.get("prevent-flow");
        }

        if (map.containsKey("prevent-use") && Helper.isIntList(map.get("prevent-use")))
        {
            preventUse = (List<Integer>) map.get("prevent-use");
        }

        if (map.containsKey("no-owner") && Helper.isBoolean(map.get("no-owner")))
        {
            noOwner = (Boolean) map.get("no-owner");
        }

        if (map.containsKey("forester") && Helper.isBoolean(map.get("forester")))
        {
            forester = (Boolean) map.get("forester");
        }

        if (map.containsKey("forester-shrubs") && Helper.isBoolean(map.get("forester-shrubs")))
        {
            foresterShrubs = (Boolean) map.get("forester-shrubs");
        }

        if (map.containsKey("grief-undo-request") && Helper.isBoolean(map.get("grief-undo-request")))
        {
            griefUndoRequest = (Boolean) map.get("grief-undo-request");
        }

        if (map.containsKey("grief-undo-interval") && Helper.isBoolean(map.get("grief-undo-interval")))
        {
            griefUndoInterval = (Boolean) map.get("grief-undo-interval");
        }

        if (map.containsKey("entry-alert") && Helper.isBoolean(map.get("entry-alert")))
        {
            entryAlert = (Boolean) map.get("entry-alert");
        }

        if (map.containsKey("price") && Helper.isInteger(map.get("price")))
        {
            price = (Integer) map.get("price");
        }
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
    public String getTitleCap()
    {
        if (getTitle() == null)
        {
            return "";
        }

        return Helper.capitalize(getTitle());
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
     * @return
     */
    public boolean canUse(int type)
    {
        if (getPreventUse() == null)
        {
            return true;
        }

        return !preventUse.contains(type);
    }

    /**
     * @return the blockDefined
     */
    public boolean isBlockDefined()
    {
        return blockDefined;
    }

    /**
     * @param blockDefined the blockDefined to set
     */
    public void setBlockDefined(boolean blockDefined)
    {
        this.blockDefined = blockDefined;
    }

    /**
     * @return the blockId
     */
    public int getBlockId()
    {
        return blockId;
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
     * @return the preventFire
     */
    public boolean isPreventFire()
    {
        return preventFire;
    }

    /**
     * @return the preventPlace
     */
    public boolean isPreventPlace()
    {
        return preventPlace;
    }

    /**
     * @return the preventDestroy
     */
    public boolean isPreventDestroy()
    {
        return preventDestroy;
    }

    /**
     * @return the preventExplosions
     */
    public boolean isPreventExplosions()
    {
        return preventExplosions;
    }

    /**
     * @return the preventPvP
     */
    public boolean isPreventPvP()
    {
        return preventPvP;
    }

    /**
     * @return the preventMobDamage
     */
    public boolean isPreventMobDamage()
    {
        return preventMobDamage;
    }

    /**
     * @return the preventMobSpawn
     */
    public boolean isPreventMobSpawn()
    {
        return preventMobSpawn;
    }

    /**
     * @return the preventAnimalSpawn
     */
    public boolean isPreventAnimalSpawn()
    {
        return preventAnimalSpawn;
    }

    /**
     * @return the preventEntry
     */
    public boolean isPreventEntry()
    {
        return preventEntry;
    }

    /**
     * @return the preventUnprotectable
     */
    public boolean isPreventUnprotectable()
    {
        return preventUnprotectable;
    }

    /**
     * @return the preventFlow
     */
    public boolean isPreventFlow()
    {
        return preventFlow;
    }

    /**
     * @return the preventUse
     */
    public List<Integer> getPreventUse()
    {
        return preventUse;
    }

    /**
     * @return the instantHeal
     */
    public boolean isInstantHeal()
    {
        return instantHeal;
    }

    /**
     * @return the slowHeal
     */
    public boolean isSlowHeal()
    {
        return slowHeal;
    }

    /**
     * @return the slowDamage
     */
    public boolean isSlowDamage()
    {
        return slowDamage;
    }

    /**
     * @return the fastDamage
     */
    public boolean isFastDamage()
    {
        return fastDamage;
    }

    /**
     * @return the breakable
     */
    public boolean isBreakable()
    {
        return breakable;
    }

    /**
     * @return the welcomeMessage
     */
    public boolean isWelcomeMessage()
    {
        return welcomeMessage;
    }

    /**
     * @return the farewellMessage
     */
    public boolean isFarewellMessage()
    {
        return farewellMessage;
    }

    /**
     * @return the giveAir
     */
    public boolean isGiveAir()
    {
        return giveAir;
    }

    /**
     * @return the snitch
     */
    public boolean isSnitch()
    {
        return snitch;
    }

    /**
     * @return the noConflict
     */
    public boolean isNoConflict()
    {
        return noConflict;
    }

    /**
     * @return the launch
     */
    public boolean isLaunch()
    {
        return launch;
    }

    /**
     * @return the cannon
     */
    public boolean isCannon()
    {
        return cannon;
    }

    /**
     * @return the mine
     */
    public boolean isMine()
    {
        return mine;
    }

    /**
     * @return the lightning
     */
    public boolean isLightning()
    {
        return lightning;
    }

    /**
     * @return the noOwner
     */
    public boolean isNoOwner()
    {
        return noOwner;
    }

    /**
     * @return the forester
     */
    public boolean isForester()
    {
        return forester;
    }

    /**
     * @return the foresterShrubs
     */
    public boolean isForesterShrubs()
    {
        return foresterShrubs;
    }

    /**
     * @return the griefUndoInterval
     */
    public boolean isGriefUndoInterval()
    {
        return griefUndoInterval;
    }

    /**
     * @return the griefUndoRequest
     */
    public boolean isGriefUndoRequest()
    {
        return griefUndoRequest;
    }

    /**
     * @return the entryAlert
     */
    public boolean isEntryAlert()
    {
        return entryAlert;
    }

    /**
     * @return the price
     */
    public int getPrice()
    {
        return price;
    }
}
