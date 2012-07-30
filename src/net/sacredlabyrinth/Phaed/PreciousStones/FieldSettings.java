package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * @author phaed
 */
public class FieldSettings
{
    private int groundBlock = 2;
    private int treeCount = 64;
    private int creatureCount = 6;
    private int growTime = 20;
    private int shrubDensity = 64;
    private boolean validField = true;
    private BlockTypeEntry type;
    private int radius = 0;
    private int heal = 0;
    private int damage = 0;
    private int feed = 0;
    private int repair = 0;
    private int launchHeight = 0;
    private int cannonHeight = 0;
    private int customHeight = 0;
    private int customVolume = 0;
    private int mineDelaySeconds = 0;
    private int lightningDelaySeconds = 0;
    private int lightningReplaceBlock = 0;
    private int mixingGroup = 0;
    private int autoDisableSeconds = 0;
    private boolean mineHasFire = false;
    private int mineStrength = 6;
    private String groupOnEntry = null;
    private String requiredPermission = null;
    private GameMode forceEntryGameMode = null;
    private GameMode forceLeavingGameMode = null;
    private String title;
    private int price = 0;
    private List<Integer> treeTypes = new ArrayList<Integer>();
    private List<Integer> shrubTypes = new ArrayList<Integer>();
    private List<String> creatureTypes = new ArrayList<String>();
    private List<Integer> fertileBlocks = new ArrayList<Integer>();
    private List<Integer> limits = new ArrayList<Integer>();
    private List<BlockTypeEntry> translocatorBlacklist = new ArrayList<BlockTypeEntry>();
    private List<Integer> preventUse = new ArrayList<Integer>();
    private List<BlockTypeEntry> confiscatedItems = new ArrayList<BlockTypeEntry>();
    private List<BlockTypeEntry> equipItems = new ArrayList<BlockTypeEntry>();
    private List<String> allowedWorlds = new ArrayList<String>();
    private List<String> allowedOnlyInside = new ArrayList<String>();
    private List<String> allowedOnlyOutside = new ArrayList<String>();
    private List<FieldFlag> defaultFlags = new LinkedList<FieldFlag>();
    private List<Integer> allowGrief = new ArrayList<Integer>();
    private HashMap<PotionEffectType, Integer> potions = new HashMap<PotionEffectType, Integer>();
    private List<PotionEffectType> neutralizePotions = new ArrayList<PotionEffectType>();

    /**
     * @param map
     */

    public FieldSettings(LinkedHashMap<String, Object> map)
    {
        if (map == null)
        {
            return;
        }

        if (map.containsKey("block"))
        {
            Object item = map.get("block");
            BlockTypeEntry type = null;

            if (Helper.isString(item) && Helper.isTypeEntry((String) item) && Helper.hasData((String) item))
            {
                type = Helper.toTypeEntry((String) item);
            }
            else
            {
                if (Helper.isInteger(item))
                {
                    type = new BlockTypeEntry((Integer) item, ((byte) 0));
                }
                else if (Helper.isInteger((String) item))
                {
                    type = new BlockTypeEntry(Integer.parseInt((String) item), ((byte) 0));
                }
            }

            if (type == null)
            {
                validField = false;
                return;
            }

            this.type = type;
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

        if (map.containsKey("required-permission") && Helper.isString(map.get("required-permission")))
        {
            requiredPermission = (String) map.get("required-permission");
        }

        if (map.containsKey("group-on-entry") && Helper.isString(map.get("group-on-entry")))
        {
            groupOnEntry = (String) map.get("group-on-entry");
        }

        if (map.containsKey("entry-game-mode") && Helper.isString(map.get("entry-game-mode")))
        {
            String gameMode = (String) map.get("entry-game-mode");

            if (gameMode.equalsIgnoreCase("creative"))
            {
                forceEntryGameMode = GameMode.CREATIVE;
            }

            if (gameMode.equalsIgnoreCase("survival"))
            {
                forceEntryGameMode = GameMode.SURVIVAL;
            }
        }

        if (map.containsKey("leaving-game-mode") && Helper.isString(map.get("leaving-game-mode")))
        {
            String gameMode = (String) map.get("leaving-game-mode");

            if (gameMode.equalsIgnoreCase("creative"))
            {
                forceLeavingGameMode = GameMode.CREATIVE;
            }

            if (gameMode.equalsIgnoreCase("survival"))
            {
                forceLeavingGameMode = GameMode.SURVIVAL;
            }
        }

        if (map.containsKey("auto-disable-seconds") && Helper.isInteger(map.get("auto-disable-seconds")))
        {
            autoDisableSeconds = (Integer) map.get("auto-disable-seconds");
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

        if (map.containsKey("launch-velocity") && Helper.isInteger(map.get("launch-velocity")))
        {
            launchHeight = (Integer) map.get("launch-velocity");
        }

        if (map.containsKey("cannon-velocity") && Helper.isInteger(map.get("cannon-velocity")))
        {
            cannonHeight = (Integer) map.get("cannon-velocity");
        }

        if (map.containsKey("mine-delay-seconds") && Helper.isInteger(map.get("mine-delay-seconds")))
        {
            mineDelaySeconds = (Integer) map.get("mine-delay-seconds");
        }

        if (map.containsKey("mine-has-fire") && Helper.isBoolean(map.get("mine-has-fire")))
        {
            mineHasFire = (Boolean) map.get("mine-has-fire");
        }

        if (map.containsKey("lightning-replace-block") && Helper.isInteger(map.get("lightning-replace-block")))
        {
            lightningReplaceBlock = (Integer) map.get("lightning-replace-block");
        }

        if (map.containsKey("lightning-delay-seconds") && Helper.isInteger(map.get("lightning-delay-seconds")))
        {
            lightningDelaySeconds = (Integer) map.get("lightning-delay-seconds");
        }

        if (map.containsKey("tree-count") && Helper.isInteger(map.get("tree-count")))
        {
            treeCount = (Integer) map.get("tree-count");
        }

        if (map.containsKey("grow-time") && Helper.isInteger(map.get("grow-time")))
        {
            growTime = (Integer) map.get("grow-time");
        }

        if (map.containsKey("shrub-density") && Helper.isInteger(map.get("shrub-density")))
        {
            shrubDensity = (Integer) map.get("shrub-density");
        }

        if (map.containsKey("ground-block") && Helper.isInteger(map.get("ground-block")))
        {
            groundBlock = (Integer) map.get("ground-block");
        }

        if (map.containsKey("prevent-use") && Helper.isIntList(map.get("prevent-use")))
        {
            preventUse = (List<Integer>) map.get("prevent-use");

            if (!preventUse.isEmpty())
            {
                defaultFlags.add(FieldFlag.PREVENT_USE);
            }
        }

        if (map.containsKey("confiscate-items") && Helper.isStringList(map.get("confiscate-items")))
        {
            confiscatedItems = Helper.toTypeEntrieBlind((List<Object>) map.get("confiscate-items"));

            if (!confiscatedItems.isEmpty())
            {
                defaultFlags.add(FieldFlag.CONFISCATE_ITEMS);
            }
        }

        if (map.containsKey("equip-items") && Helper.isStringList(map.get("equip-items")))
        {
            equipItems = Helper.toTypeEntrieBlind((List<Object>) map.get("equip-items"));

            if (!equipItems.isEmpty())
            {
                defaultFlags.add(FieldFlag.EQUIP_ITEMS);
            }
        }

        if (map.containsKey("allow-grief") && Helper.isIntList(map.get("allow-grief")))
        {
            allowGrief = (List<Integer>) map.get("allow-grief");
        }

        if (map.containsKey("tree-types") && Helper.isIntList(map.get("tree-types")))
        {
            treeTypes = (List<Integer>) map.get("tree-types");
        }

        if (map.containsKey("shrub-types") && Helper.isIntList(map.get("shrub-types")))
        {
            shrubTypes = (List<Integer>) map.get("shrub-types");
        }

        if (map.containsKey("creature-types") && Helper.isStringList(map.get("creature-types")))
        {
            creatureTypes = (List<String>) map.get("creature-types");
        }

        if (map.containsKey("creature-count") && Helper.isInteger(map.get("creature-count")))
        {
            creatureCount = (Integer) map.get("creature-count");
        }

        if (map.containsKey("fertile-blocks") && Helper.isIntList(map.get("fertile-blocks")))
        {
            fertileBlocks = (List<Integer>) map.get("fertile-blocks");
        }

        if (map.containsKey("allowed-worlds") && Helper.isStringList(map.get("allowed-worlds")))
        {
            allowedWorlds = (List<String>) map.get("allowed-worlds");
        }

        if (map.containsKey("price") && Helper.isInteger(map.get("price")))
        {
            price = (Integer) map.get("price");
        }

        if (map.containsKey("limits") && Helper.isIntList(map.get("limits")))
        {
            limits = (List<Integer>) map.get("limits");
        }

        if (map.containsKey("translocator-blacklist") && Helper.isStringList(map.get("translocator-blacklists")))
        {
            translocatorBlacklist = Helper.toTypeEntrieBlind((List<Object>) map.get("translocator-blacklist"));
        }

        if (map.containsKey("allowed-only-inside") && Helper.isStringList(map.get("allowed-only-inside")))
        {
            allowedOnlyInside = (List<String>) map.get("allowed-only-inside");
        }

        if (map.containsKey("allowed-only-outside") && Helper.isStringList(map.get("allowed-only-outside")))
        {
            allowedOnlyOutside = (List<String>) map.get("allowed-only-outside");
        }

        if (map.containsKey("prevent-fire") && Helper.isBoolean(map.get("prevent-fire")))
        {
            if ((Boolean) map.get("prevent-fire"))
            {
                defaultFlags.add(FieldFlag.PREVENT_FIRE);
            }
        }

        if (map.containsKey("enable-with-redstone") && Helper.isBoolean(map.get("enable-with-redstone")))
        {
            if ((Boolean) map.get("enable-with-redstone"))
            {
                defaultFlags.add(FieldFlag.ENABLE_WITH_REDSTONE);
            }
        }

        if (map.containsKey("allow-place") && Helper.isBoolean(map.get("allow-place")))
        {
            if ((Boolean) map.get("allow-place"))
            {
                defaultFlags.add(FieldFlag.ALLOW_PLACE);
            }
        }

        if (map.containsKey("allow-destroy") && Helper.isBoolean(map.get("allow-destroy")))
        {
            if ((Boolean) map.get("allow-destroy"))
            {
                defaultFlags.add(FieldFlag.ALLOW_DESTROY);
            }
        }

        if (map.containsKey("prevent-place") && Helper.isBoolean(map.get("prevent-place")))
        {
            if ((Boolean) map.get("prevent-place"))
            {
                defaultFlags.add(FieldFlag.PREVENT_PLACE);
            }
        }

        if (map.containsKey("prevent-destroy") && Helper.isBoolean(map.get("prevent-destroy")))
        {
            if ((Boolean) map.get("prevent-destroy"))
            {
                defaultFlags.add(FieldFlag.PREVENT_DESTROY);
            }
        }

        if (map.containsKey("prevent-explosions") && Helper.isBoolean(map.get("prevent-explosions")))
        {
            if ((Boolean) map.get("prevent-explosions"))
            {
                defaultFlags.add(FieldFlag.PREVENT_EXPLOSIONS);
            }
        }

        if (map.containsKey("prevent-creeper-explosions") && Helper.isBoolean(map.get("prevent-creeper-explosions")))
        {
            if ((Boolean) map.get("prevent-creeper-explosions"))
            {
                defaultFlags.add(FieldFlag.PREVENT_CREEPER_EXPLOSIONS);
            }
        }

        if (map.containsKey("prevent-tnt-explosions") && Helper.isBoolean(map.get("prevent-tnt-explosions")))
        {
            if ((Boolean) map.get("prevent-tnt-explosions"))
            {
                defaultFlags.add(FieldFlag.PREVENT_TNT_EXPLOSIONS);
            }
        }

        if (map.containsKey("rollback-explosions") && Helper.isBoolean(map.get("rollback-explosions")))
        {
            if ((Boolean) map.get("rollback-explosions"))
            {
                defaultFlags.add(FieldFlag.ROLLBACK_EXPLOSIONS);
            }
        }

        if (map.containsKey("prevent-pvp") && Helper.isBoolean(map.get("prevent-pvp")))
        {
            if ((Boolean) map.get("prevent-pvp"))
            {
                defaultFlags.add(FieldFlag.PREVENT_PVP);
            }
        }

        if (map.containsKey("prevent-teleport") && Helper.isBoolean(map.get("prevent-teleport")))
        {
            if ((Boolean) map.get("prevent-teleport"))
            {
                defaultFlags.add(FieldFlag.PREVENT_TELEPORT);
            }
        }

        if (map.containsKey("prevent-mob-damage") && Helper.isBoolean(map.get("prevent-mob-damage")))
        {
            if ((Boolean) map.get("prevent-mob-damage"))
            {
                defaultFlags.add(FieldFlag.PREVENT_MOB_DAMAGE);
            }
        }

        if (map.containsKey("prevent-mob-spawn") && Helper.isBoolean(map.get("prevent-mob-spawn")))
        {
            if ((Boolean) map.get("prevent-mob-spawn"))
            {
                defaultFlags.add(FieldFlag.PREVENT_MOB_SPAWN);
            }
        }

        if (map.containsKey("prevent-animal-spawn") && Helper.isBoolean(map.get("prevent-animal-spawn")))
        {
            if ((Boolean) map.get("prevent-animal-spawn"))
            {
                defaultFlags.add(FieldFlag.PREVENT_ANIMAL_SPAWN);
            }
        }

        if (map.containsKey("prevent-entry") && Helper.isBoolean(map.get("prevent-entry")))
        {
            if ((Boolean) map.get("prevent-entry"))
            {
                defaultFlags.add(FieldFlag.PREVENT_ENTRY);
            }
        }

        if (map.containsKey("prevent-unprotectable") && Helper.isBoolean(map.get("prevent-unprotectable")))
        {
            if ((Boolean) map.get("prevent-unprotectable"))
            {
                defaultFlags.add(FieldFlag.PREVENT_UNPROTECTABLE);
            }
        }

        if (map.containsKey("protect-animals") && Helper.isBoolean(map.get("protect-animals")))
        {
            if ((Boolean) map.get("protect-animals"))
            {
                defaultFlags.add(FieldFlag.PROTECT_ANIMALS);
            }
        }

        if (map.containsKey("protect-mobs") && Helper.isBoolean(map.get("protect-mobs")))
        {
            if ((Boolean) map.get("protect-mobs"))
            {
                defaultFlags.add(FieldFlag.PROTECT_MOBS);
            }
        }

        if (map.containsKey("remove-mob") && Helper.isBoolean(map.get("remove-mob")))
        {
            if ((Boolean) map.get("remove-mob"))
            {
                defaultFlags.add(FieldFlag.REMOVE_MOB);
            }
        }

        if (map.containsKey("heal") && Helper.isInteger(map.get("heal")))
        {
            if ((Integer) map.get("heal") > 0)
            {
                defaultFlags.add(FieldFlag.HEAL);
                heal = (Integer) map.get("heal");
            }
        }

        if (map.containsKey("feed") && Helper.isInteger(map.get("feed")))
        {
            if ((Integer) map.get("feed") > 0)
            {
                defaultFlags.add(FieldFlag.FEED);
                feed = (Integer) map.get("feed");
            }
        }

        if (map.containsKey("repair") && Helper.isInteger(map.get("repair")))
        {
            if ((Integer) map.get("repair") > 0)
            {
                defaultFlags.add(FieldFlag.REPAIR);
                repair = (Integer) map.get("repair");
            }
        }

        if (map.containsKey("damage") && Helper.isInteger(map.get("damage")))
        {
            if ((Integer) map.get("damage") > 0)
            {
                defaultFlags.add(FieldFlag.DAMAGE);
                damage = (Integer) map.get("damage");
            }
        }

        if (map.containsKey("breakable") && Helper.isBoolean(map.get("breakable")))
        {
            if ((Boolean) map.get("breakable"))
            {
                defaultFlags.add(FieldFlag.BREAKABLE);
            }
        }

        if (map.containsKey("welcome-message") && Helper.isBoolean(map.get("welcome-message")))
        {
            if ((Boolean) map.get("welcome-message"))
            {
                defaultFlags.add(FieldFlag.WELCOME_MESSAGE);
            }
        }

        if (map.containsKey("farewell-message") && Helper.isBoolean(map.get("farewell-message")))
        {
            if ((Boolean) map.get("farewell-message"))
            {
                defaultFlags.add(FieldFlag.FAREWELL_MESSAGE);
            }
        }

        if (map.containsKey("air") && Helper.isBoolean(map.get("air")))
        {
            if ((Boolean) map.get("air"))
            {
                defaultFlags.add(FieldFlag.AIR);
            }
        }

        if (map.containsKey("snitch") && Helper.isBoolean(map.get("snitch")))
        {
            if ((Boolean) map.get("snitch"))
            {
                defaultFlags.add(FieldFlag.SNITCH);
            }
        }

        if (map.containsKey("no-conflict") && Helper.isBoolean(map.get("no-conflict")))
        {
            if ((Boolean) map.get("no-conflict"))
            {
                defaultFlags.add(FieldFlag.NO_CONFLICT);
            }
        }

        if (map.containsKey("no-owner") && Helper.isBoolean(map.get("no-owner")))
        {
            if ((Boolean) map.get("no-owner"))
            {
                defaultFlags.add(FieldFlag.NO_OWNER);
            }
        }

        if (map.containsKey("launch") && Helper.isBoolean(map.get("launch")))
        {
            if ((Boolean) map.get("launch"))
            {
                defaultFlags.add(FieldFlag.LAUNCH);
            }
        }

        if (map.containsKey("cannon") && Helper.isBoolean(map.get("cannon")))
        {
            if ((Boolean) map.get("cannon"))
            {
                defaultFlags.add(FieldFlag.CANNON);
            }
        }

        if (map.containsKey("mine") && Helper.isInteger(map.get("mine")))
        {
            if ((Integer) map.get("mine") > 0)
            {
                defaultFlags.add(FieldFlag.MINE);
                mineStrength = (Integer) map.get("mine");
            }
        }

        if (map.containsKey("potions") && Helper.isStringList(map.get("potions")))
        {
            defaultFlags.add(FieldFlag.POTIONS);
            List<String> pts = (List<String>) map.get("potions");

            List<Integer> intensities = null;

            if (map.containsKey("potion-intensity") && Helper.isIntList(map.get("potion-intensity")))
            {
                intensities = (List<Integer>) map.get("potion-intensity");
            }

            int pos = 0;

            for (String name : pts)
            {
                int i = 1; // default intensity

                if (intensities != null)
                {
                    i = intensities.get(pos);
                }

                if (PotionEffectType.getByName(name) != null)
                {
                    potions.put(PotionEffectType.getByName(name), i);
                }
                pos++;
            }
        }

        if (map.containsKey("neutralize-potions") && Helper.isStringList(map.get("neutralize-potions")))
        {
            defaultFlags.add(FieldFlag.NEUTRALIZE_POTIONS);
            List<String> pts = (List<String>) map.get("neutralize-potions");

            for (String name : pts)
            {
                if (PotionEffectType.getByName(name) != null)
                {
                    neutralizePotions.add(PotionEffectType.getByName(name));
                }
            }
        }

        if (map.containsKey("lightning") && Helper.isBoolean(map.get("lightning")))
        {
            if ((Boolean) map.get("lightning"))
            {
                defaultFlags.add(FieldFlag.LIGHTNING);
            }
        }

        if (map.containsKey("no-fall-damage") && Helper.isBoolean(map.get("no-fall-damage")))
        {
            if ((Boolean) map.get("no-fall-damage"))
            {
                defaultFlags.add(FieldFlag.NO_FALL_DAMAGE);
            }
        }

        if (map.containsKey("sneak-to-place") && Helper.isBoolean(map.get("sneak-to-place")))
        {
            if ((Boolean) map.get("sneak-to-place"))
            {
                defaultFlags.add(FieldFlag.SNEAK_TO_PLACE);
            }
        }

        if (map.containsKey("plot") && Helper.isBoolean(map.get("plot")))
        {
            if ((Boolean) map.get("plot"))
            {
                defaultFlags.add(FieldFlag.PLOT);
            }
        }

        if (map.containsKey("prevent-flow") && Helper.isBoolean(map.get("prevent-flow")))
        {
            if ((Boolean) map.get("prevent-flow"))
            {
                defaultFlags.add(FieldFlag.PREVENT_FLOW);
            }
        }

        if (map.containsKey("forester") && Helper.isBoolean(map.get("forester")))
        {
            if ((Boolean) map.get("forester"))
            {
                defaultFlags.add(FieldFlag.FORESTER);
            }
        }

        if (map.containsKey("grief-revert") && Helper.isBoolean(map.get("grief-revert")))
        {
            if ((Boolean) map.get("grief-revert"))
            {
                defaultFlags.add(FieldFlag.GRIEF_REVERT);
            }
        }

        if (map.containsKey("grief-revert-drop") && Helper.isBoolean(map.get("grief-revert-drop")))
        {
            if ((Boolean) map.get("grief-revert-drop"))
            {
                defaultFlags.add(FieldFlag.GRIEF_REVERT_DROP);
            }
        }

        if (map.containsKey("entry-alert") && Helper.isBoolean(map.get("entry-alert")))
        {
            if ((Boolean) map.get("entry-alert"))
            {
                defaultFlags.add(FieldFlag.ENTRY_ALERT);
            }
        }

        if (map.containsKey("cuboid") && Helper.isBoolean(map.get("cuboid")))
        {
            if ((Boolean) map.get("cuboid"))
            {
                defaultFlags.add(FieldFlag.CUBOID);
            }
        }

        if (map.containsKey("visualize-on-src") && Helper.isBoolean(map.get("visualize-on-src")))
        {
            if ((Boolean) map.get("visualize-on-src"))
            {
                defaultFlags.add(FieldFlag.VISUALIZE_ON_SRC);
            }
        }

        if (map.containsKey("visualize-on-place") && Helper.isBoolean(map.get("visualize-on-place")))
        {
            if ((Boolean) map.get("visualize-on-place"))
            {
                defaultFlags.add(FieldFlag.VISUALIZE_ON_PLACE);
            }
        }

        if (map.containsKey("keep-chunks-loaded") && Helper.isBoolean(map.get("keep-chunks-loaded")))
        {
            if ((Boolean) map.get("keep-chunks-loaded"))
            {
                defaultFlags.add(FieldFlag.KEEP_CHUNKS_LOADED);
            }
        }

        if (map.containsKey("place-grief") && Helper.isBoolean(map.get("place-grief")))
        {
            if ((Boolean) map.get("place-grief"))
            {
                defaultFlags.add(FieldFlag.PLACE_GRIEF);
            }
        }

        if (map.containsKey("toggle-on-disabled") && Helper.isBoolean(map.get("toggle-on-disabled")))
        {
            if ((Boolean) map.get("toggle-on-disabled"))
            {
                defaultFlags.add(FieldFlag.TOGGLE_ON_DISABLED);
            }
        }

        if (map.containsKey("redefine-on-disabled") && Helper.isBoolean(map.get("redefine-on-disabled")))
        {
            if ((Boolean) map.get("redefine-on-disabled"))
            {
                defaultFlags.add(FieldFlag.REDEFINE_ON_DISABLED);
            }
        }

        if (map.containsKey("modify-on-disabled") && Helper.isBoolean(map.get("modify-on-disabled")))
        {
            if ((Boolean) map.get("modify-on-disabled"))
            {
                defaultFlags.add(FieldFlag.MODIFY_ON_DISABLED);
            }
        }

        if (map.containsKey("enable-on-src") && Helper.isBoolean(map.get("enable-on-src")))
        {
            if ((Boolean) map.get("enable-on-src"))
            {
                defaultFlags.add(FieldFlag.ENABLE_ON_SRC);
            }
        }

        if (map.containsKey("breakable-on-disabled") && Helper.isBoolean(map.get("breakable-on-disabled")))
        {
            if ((Boolean) map.get("breakable-on-disabled"))
            {
                defaultFlags.add(FieldFlag.BREAKABLE_ON_DISABLED);
            }
        }

        if (map.containsKey("no-player-place") && Helper.isBoolean(map.get("no-player-place")))
        {
            if ((Boolean) map.get("no-player-place"))
            {
                defaultFlags.add(FieldFlag.NO_PLAYER_PLACE);
            }
        }

        if (map.containsKey("translocator") && Helper.isBoolean(map.get("translocator")))
        {
            if ((Boolean) map.get("translocator"))
            {
                defaultFlags.add(FieldFlag.TRANSLOCATOR);
            }
        }

        if (map.containsKey("apply-to-reverse") && Helper.isBoolean(map.get("apply-to-reverse")))
        {
            if ((Boolean) map.get("apply-to-reverse"))
            {
                defaultFlags.add(FieldFlag.APPLY_TO_REVERSE);
            }
        }

        if (map.containsKey("apply-to-all") && Helper.isBoolean(map.get("apply-to-all")))
        {
            if ((Boolean) map.get("apply-to-all"))
            {
                defaultFlags.add(FieldFlag.APPLY_TO_ALL);
            }
        }

        if (map.containsKey("prevent-flight") && Helper.isBoolean(map.get("prevent-flight")))
        {
            if ((Boolean) map.get("prevent-flight"))
            {
                defaultFlags.add(FieldFlag.PREVENT_FLIGHT);
            }
        }

        if (map.containsKey("allowed-can-break") && Helper.isBoolean(map.get("allowed-can-break")))
        {
            if ((Boolean) map.get("allowed-can-break"))
            {
                defaultFlags.add(FieldFlag.ALLOWED_CAN_BREAK);
            }
        }

        if (map.containsKey("sneaking-bypass") && Helper.isBoolean(map.get("sneaking-bypass")))
        {
            if ((Boolean) map.get("sneaking-bypass"))
            {
                defaultFlags.add(FieldFlag.SNEAKING_BYPASS);
            }
        }

        if (map.containsKey("dynmap-area") && Helper.isBoolean(map.get("dynmap-area")))
        {
            if ((Boolean) map.get("dynmap-area"))
            {
                defaultFlags.add(FieldFlag.DYNMAP_AREA);
            }
        }

        if (map.containsKey("dynmap-marker") && Helper.isBoolean(map.get("dynmap-marker")))
        {
            if ((Boolean) map.get("dynmap-marker"))
            {
                defaultFlags.add(FieldFlag.DYNMAP_MARKER);
            }
        }

        if (map.containsKey("dynmap-disabled") && Helper.isBoolean(map.get("dynmap-disabled")))
        {
            if ((Boolean) map.get("dynmap-disabled"))
            {
                defaultFlags.add(FieldFlag.DYNMAP_DISABLED);
            }
        }

        if (map.containsKey("dynmap-no-toggle") && Helper.isBoolean(map.get("dynmap-no-toggle")))
        {
            if ((Boolean) map.get("dynmap-no-toggle"))
            {
                defaultFlags.add(FieldFlag.DYNMAP_NO_TOGGLE);
            }
        }

        if (map.containsKey("can-change-owner") && Helper.isBoolean(map.get("can-change-owner")))
        {
            if ((Boolean) map.get("can-change-owner"))
            {
                defaultFlags.add(FieldFlag.CAN_CHANGE_OWNER);
            }
        }

        defaultFlags.add(FieldFlag.ALL);
    }

    /**
     * Check if the setting has a flag
     *
     * @param flag
     * @return
     */
    public boolean hasDefaultFlag(String flagStr)
    {
        for (FieldFlag flag : defaultFlags)
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
    public boolean hasDefaultFlag(FieldFlag flag)
    {
        return defaultFlags.contains(flag);
    }

    /**
     * @return
     */
    public boolean hasNameableFlag()
    {
        return defaultFlags.contains(FieldFlag.WELCOME_MESSAGE) || defaultFlags.contains(FieldFlag.FAREWELL_MESSAGE) || defaultFlags.contains(FieldFlag.ENTRY_ALERT);
    }

    /**
     * @return
     */
    public boolean hasVeocityFlag()
    {
        return defaultFlags.contains(FieldFlag.CANNON) || defaultFlags.contains(FieldFlag.LAUNCH);
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
     * Whether the block can be translocated or not based on the blacklist
     *
     * @param type
     * @return
     */
    public boolean canTranslocate(BlockTypeEntry type)
    {
        return !translocatorBlacklist.contains(type);
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
     * Whether a block type can be used in this field
     *
     * @param type
     * @return
     */
    public boolean canCarry(int type, byte data)
    {
        if (confiscatedItems.isEmpty())
        {
            return true;
        }

        for (BlockTypeEntry entry : confiscatedItems)
        {
            // if the banned item has no data, then that means
            // they want to ban all ids for that block

            // otherwise match the type and data exactly

            if (entry.getData() == 0)
            {
                if (entry.getTypeId() == type)
                {
                    return false;
                }
            }
            else
            {
                if (entry.getTypeId() == type && entry.getData() == data)
                {
                    return false;
                }
            }
        }

        return true;
    }

    public String getPotionString()
    {
        String out = "";

        for(PotionEffectType potion : potions.keySet())
        {
            out += Helper.friendlyBlockType(potion.getName()) + ", ";
        }

        return Helper.stripTrailing(out, ", ");
    }

    public String getNeutralizePotionString()
    {
        String out = "";

        for(PotionEffectType potion : neutralizePotions)
        {
            out += Helper.friendlyBlockType(potion.getName()) + ", ";
        }

        return Helper.stripTrailing(out, ", ");
    }

    /**
     * Whether a block type can be griefed in a grief revert field
     *
     * @param type
     * @return
     */
    public boolean canGrief(int type)
    {
        if (allowGrief == null || allowGrief.isEmpty())
        {
            return false;
        }

        return allowGrief.contains(type);
    }

    /**
     * If the field can be placed in a world
     *
     * @param worldName
     * @return
     */
    public boolean allowedWorld(World world)
    {
        return allowedWorlds == null || allowedWorlds.isEmpty() || allowedWorlds.contains(world.getName());
    }

    /**
     * Whether the field has allowed only fields set
     *
     * @return
     */
    public boolean hasAllowedOnlyInside()
    {
        return allowedOnlyInside != null && !allowedOnlyInside.isEmpty();
    }

    /**
     * If the field is inside and allowed field
     *
     * @return
     */
    public boolean isAllowedOnlyInside(Field field)
    {
        return allowedOnlyInside.contains(field.getSettings().getTitle());
    }

    /**
     * Returns a formatted string with all the allowed only fields
     *
     * @return
     */
    public String getAllowedOnlyInsideString()
    {
        return Helper.toMessage(allowedOnlyInside, " or ");
    }

    /**
     * Whether the field has allowed only fields set
     *
     * @return
     */
    public boolean hasAllowedOnlyOutside()
    {
        return allowedOnlyOutside != null && !allowedOnlyOutside.isEmpty();
    }

    /**
     * If the field is inside and allowed field
     *
     * @return
     */
    public boolean isAllowedOnlyOutside(Field field)
    {
        return allowedOnlyOutside.contains(field.getSettings().getTitle());
    }

    /**
     * Returns a formatted string with all the allowed only fields
     *
     * @return
     */
    public String getAllowedOnlyOutsideString()
    {
        return Helper.toMessage(allowedOnlyOutside, " or ");
    }

    /**
     * @return the block type id
     */
    public int getTypeId()
    {
        return type.getTypeId();
    }

    /**
     * @return the block data
     */
    public byte getData()
    {
        return type.getData();
    }

    /**
     * @return the type entry
     */
    public BlockTypeEntry getTypeEntry()
    {
        return type;
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

    public List<FieldFlag> getDefaultFlags()
    {
        return Collections.unmodifiableList(defaultFlags);
    }

    public int getCustomVolume()
    {
        return customVolume;
    }

    public int getMixingGroup()
    {
        return mixingGroup;
    }

    public String getRequiredPermission()
    {
        return requiredPermission;
    }

    public int getAutoDisableSeconds()
    {
        return autoDisableSeconds;
    }

    public String getGroupOnEntry()
    {
        return groupOnEntry;
    }

    public GameMode getForceEntryGameMode()
    {
        return forceEntryGameMode;
    }

    public GameMode getForceLeavingGameMode()
    {
        return forceLeavingGameMode;
    }

    public int getHeal()
    {
        return heal;
    }

    public int getDamage()
    {
        return damage;
    }

    public int getFeed()
    {
        return feed;
    }

    public int getRepair()
    {
        return repair;
    }

    public List<Integer> getTreeTypes()
    {
        if (treeTypes == null)
        {
            return null;
        }

        return new ArrayList<Integer>(treeTypes);
    }

    public List<Integer> getShrubTypes()
    {
        if (shrubTypes == null)
        {
            return null;
        }

        return new ArrayList<Integer>(shrubTypes);
    }

    public int getShrubDensity()
    {
        return shrubDensity;
    }

    public int getTreeCount()
    {
        return treeCount;
    }

    public int getGrowTime()
    {
        return growTime;
    }

    public boolean isFertileType(int type)
    {
        return fertileBlocks.contains(type);
    }

    public int getGroundBlock()
    {
        return groundBlock;
    }

    public List<String> getCreatureTypes()
    {
        return creatureTypes;
    }

    public int getCreatureCount()
    {
        return creatureCount;
    }

    public boolean isMineHasFire()
    {
        return mineHasFire;
    }

    public int getMineStrength()
    {
        return mineStrength;
    }

    public HashMap<PotionEffectType, Integer> getPotions()
    {
        return potions;
    }

    public List<PotionEffectType> getNeutralizePotions()
    {
        return neutralizePotions;
    }

    public List<BlockTypeEntry> getEquipItems()
    {
        return equipItems;
    }
}
