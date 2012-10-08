package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
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
    private int maskOnDisabledBlock = 49;
    private int maskOnEnabledBlock = 49;
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
    private String requiredPermissionAllow = null;
    private String requiredPermissionUse = null;
    private String requiredPermission = null;
    private GameMode forceEntryGameMode = null;
    private GameMode forceLeavingGameMode = null;
    private String title;
    private int price = 0;
    private int refund = -1;
    private int teleportCost = 0;
    private int teleportBackAfterSeconds = 0;
    private int teleportMaxDistance = 0;
    private int griefRevertInterval = 0;
    private String commandOnEnter = "";
    private String commandOnExit = "";
    private String playerCommandOnEnter = "";
    private String playerCommandOnExit = "";
    private List<Integer> teleportIfHoldingItems = new ArrayList<Integer>();
    private List<Integer> teleportIfNotHoldingItems = new ArrayList<Integer>();
    private List<Integer> teleportIfHasItems = new ArrayList<Integer>();
    private List<Integer> teleportIfNotHasItems = new ArrayList<Integer>();
    private List<BlockTypeEntry> teleportIfWalkingOn = new ArrayList<BlockTypeEntry>();
    private List<BlockTypeEntry> teleportIfNotWalkingOn = new ArrayList<BlockTypeEntry>();
    private List<Integer> treeTypes = new ArrayList<Integer>();
    private List<Integer> shrubTypes = new ArrayList<Integer>();
    private List<String> creatureTypes = new ArrayList<String>();
    private List<Integer> fertileBlocks = new ArrayList<Integer>();
    private List<Integer> limits = new ArrayList<Integer>();
    private List<BlockTypeEntry> translocationBlacklist = new ArrayList<BlockTypeEntry>();
    private List<BlockTypeEntry> preventPlaceBlacklist = new ArrayList<BlockTypeEntry>();
    private List<BlockTypeEntry> preventDestroyBlacklist = new ArrayList<BlockTypeEntry>();
    private List<Integer> preventUse = new ArrayList<Integer>();
    private List<BlockTypeEntry> confiscatedItems = new ArrayList<BlockTypeEntry>();
    private List<BlockTypeEntry> equipItems = new ArrayList<BlockTypeEntry>();
    private List<String> allowedWorlds = new ArrayList<String>();
    private List<String> allowedOnlyInside = new ArrayList<String>();
    private List<String> allowedOnlyOutside = new ArrayList<String>();
    private List<String> canceledCommands = new ArrayList<String>();
    private List<FieldFlag> defaultFlags = new ArrayList<FieldFlag>();
    private List<Integer> allowGrief = new ArrayList<Integer>();
    private HashMap<PotionEffectType, Integer> potions = new HashMap<PotionEffectType, Integer>();
    private List<PotionEffectType> neutralizePotions = new ArrayList<PotionEffectType>();
    private List<String> allowedPlayers = new ArrayList<String>();
    private List<String> deniedPlayers = new ArrayList<String>();

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

        if (map.containsKey("required-permission-use") && Helper.isString(map.get("required-permission-use")))
        {
            requiredPermissionUse = (String) map.get("required-permission-use");
        }

        if (map.containsKey("required-permission-allow") && Helper.isString(map.get("required-permission-allow")))
        {
            requiredPermissionAllow = (String) map.get("required-permission-allow");
        }

        if (map.containsKey("group-on-entry") && Helper.isString(map.get("group-on-entry")))
        {
            groupOnEntry = (String) map.get("group-on-entry");

            if (groupOnEntry != null && groupOnEntry.length() > 0)
            {
                defaultFlags.add(FieldFlag.GROUP_ON_ENTRY);
            }
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

            if (forceEntryGameMode.equals(GameMode.CREATIVE) || forceEntryGameMode.equals(GameMode.SURVIVAL))
            {
                defaultFlags.add(FieldFlag.ENTRY_GAME_MODE);
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

            if (forceLeavingGameMode.equals(GameMode.CREATIVE) || forceLeavingGameMode.equals(GameMode.SURVIVAL))
            {
                defaultFlags.add(FieldFlag.LEAVING_GAME_MODE);
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
            confiscatedItems = Helper.toTypeEntriesBlind((List<Object>) map.get("confiscate-items"));

            if (!confiscatedItems.isEmpty())
            {
                defaultFlags.add(FieldFlag.CONFISCATE_ITEMS);
            }
        }

        if (map.containsKey("always-allow-players") && Helper.isStringList(map.get("always-allow-players")))
        {
            allowedPlayers = (List<String>) map.get("always-allow-players");
        }

        if (map.containsKey("always-deny-players") && Helper.isStringList(map.get("always-deny-players")))
        {
            deniedPlayers = (List<String>) map.get("always-deny-players");
        }

        if (map.containsKey("equip-items") && Helper.isStringList(map.get("equip-items")))
        {
            equipItems = Helper.toTypeEntriesBlind((List<Object>) map.get("equip-items"));

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

        if (map.containsKey("refund") && Helper.isInteger(map.get("refund")))
        {
            refund = (Integer) map.get("refund");
        }

        if (map.containsKey("limits") && Helper.isIntList(map.get("limits")))
        {
            limits = (List<Integer>) map.get("limits");
        }

        if (map.containsKey("translocation-blacklist") && Helper.isStringList(map.get("translocation-blacklists")))
        {
            translocationBlacklist = Helper.toTypeEntriesBlind((List<Object>) map.get("translocation-blacklist"));
        }

        if (map.containsKey("prevent-place-blacklist") && Helper.isStringList(map.get("prevent-place-blacklists")))
        {
            preventPlaceBlacklist = Helper.toTypeEntriesBlind((List<Object>) map.get("prevent-place-blacklist"));
        }

        if (map.containsKey("prevent-destroy-blacklist") && Helper.isStringList(map.get("prevent-destroy-blacklists")))
        {
            preventDestroyBlacklist = Helper.toTypeEntriesBlind((List<Object>) map.get("prevent-destroy-blacklist"));
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

        if (map.containsKey("prevent-vehicle-destroy") && Helper.isBoolean(map.get("prevent-vehicle-destroy")))
        {
            if ((Boolean) map.get("prevent-vehicle-destroy"))
            {
                defaultFlags.add(FieldFlag.PREVENT_VEHICLE_DESTROY);
            }
        }

        if (map.containsKey("prevent-enderman-destroy") && Helper.isBoolean(map.get("prevent-enderman-destroy")))
        {
            if ((Boolean) map.get("prevent-enderman-destroy"))
            {
                defaultFlags.add(FieldFlag.PREVENT_ENDERMAN_DESTROY);
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

        if (map.containsKey("protect-villagers") && Helper.isBoolean(map.get("protect-villagers")))
        {
            if ((Boolean) map.get("protect-villagers"))
            {
                defaultFlags.add(FieldFlag.PROTECT_VILLAGERS);
            }
        }

        if (map.containsKey("protect-crops") && Helper.isBoolean(map.get("protect-crops")))
        {
            if ((Boolean) map.get("protect-crops"))
            {
                defaultFlags.add(FieldFlag.PROTECT_CROPS);
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

        if (map.containsKey("worldguard-repellent") && Helper.isBoolean(map.get("worldguard-repellent")))
        {
            if ((Boolean) map.get("worldguard-repellent"))
            {
                defaultFlags.add(FieldFlag.WORLDGUARD_REPELLENT);
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

        if (map.containsKey("mask-on-disabled") && Helper.isInteger(map.get("mask-on-disabled")))
        {
            if ((Integer) map.get("mask-on-disabled") > 0)
            {
                defaultFlags.add(FieldFlag.MASK_ON_DISABLED);
                maskOnDisabledBlock = (Integer) map.get("mask-on-disabled");
            }
        }

        if (map.containsKey("mask-on-enabled") && Helper.isInteger(map.get("mask-on-enabled")))
        {
            if ((Integer) map.get("mask-on-enabled") > 0)
            {
                defaultFlags.add(FieldFlag.MASK_ON_ENABLED);
                maskOnEnabledBlock = (Integer) map.get("mask-on-enabled");
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

        if (map.containsKey("grief-revert-interval") && Helper.isInteger(map.get("grief-revert-interval")))
        {
            griefRevertInterval = (Integer) map.get("grief-revert-interval");
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

        if (map.containsKey("translocation") && Helper.isBoolean(map.get("translocation")))
        {
            if ((Boolean) map.get("translocation"))
            {
                defaultFlags.add(FieldFlag.TRANSLOCATION);
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

        if (map.containsKey("no-allowing") && Helper.isBoolean(map.get("no-allowing")))
        {
            if ((Boolean) map.get("no-allowing"))
            {
                defaultFlags.add(FieldFlag.NO_ALLOWING);
            }
        }

        if (map.containsKey("teleport-if-walking-on") && Helper.isStringList(map.get("teleport-if-walking-on")))
        {
            teleportIfWalkingOn = Helper.toTypeEntriesBlind((List<Object>) map.get("teleport-if-walking-on"));

            if (teleportIfWalkingOn != null && teleportIfWalkingOn.size() > 0)
            {
                defaultFlags.add(FieldFlag.TELEPORTER);
                defaultFlags.add(FieldFlag.TELEPORT_IF_WALKING_ON);
            }
        }

        if (map.containsKey("teleport-if-not-walking-on") && Helper.isStringList(map.get("teleport-if-not-walking-on")))
        {
            teleportIfNotWalkingOn = Helper.toTypeEntriesBlind((List<Object>) map.get("teleport-if-not-walking-on"));

            if (teleportIfNotWalkingOn != null && teleportIfNotWalkingOn.size() > 0)
            {
                defaultFlags.add(FieldFlag.TELEPORTER);
                defaultFlags.add(FieldFlag.TELEPORT_IF_NOT_WALKING_ON);
            }
        }

        if (map.containsKey("teleport-if-holding-items") && Helper.isIntList(map.get("teleport-if-holding-items")))
        {
            teleportIfHoldingItems = (List<Integer>) map.get("teleport-if-holding-items");

            if (teleportIfHoldingItems != null && teleportIfHoldingItems.size() > 0)
            {
                defaultFlags.add(FieldFlag.TELEPORT_IF_HOLDING_ITEMS);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-if-not-holding-items") && Helper.isIntList(map.get("teleport-if-not-holding-items")))
        {
            teleportIfNotHoldingItems = (List<Integer>) map.get("teleport-if-not-holding-items");

            if (teleportIfNotHoldingItems != null && teleportIfNotHoldingItems.size() > 0)
            {
                defaultFlags.add(FieldFlag.TELEPORT_IF_NOT_HOLDING_ITEMS);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-if-has-items") && Helper.isIntList(map.get("teleport-if-has-items")))
        {
            teleportIfHasItems = (List<Integer>) map.get("teleport-if-has-items");

            if (teleportIfHasItems != null && teleportIfHasItems.size() > 0)
            {
                defaultFlags.add(FieldFlag.TELEPORT_IF_HAS_ITEMS);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-if-not-has-items") && Helper.isIntList(map.get("teleport-if-not-has-items")))
        {
            teleportIfNotHasItems = (List<Integer>) map.get("teleport-if-not-has-items");

            if (teleportIfNotHasItems != null && teleportIfNotHasItems.size() > 0)
            {
                defaultFlags.add(FieldFlag.TELEPORT_IF_NOT_HAS_ITEMS);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-before-death") && Helper.isBoolean(map.get("teleport-before-death")))
        {
            if ((Boolean) map.get("teleport-before-death"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_BEFORE_DEATH);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-on-damage") && Helper.isBoolean(map.get("teleport-on-damage")))
        {
            if ((Boolean) map.get("teleport-on-damage"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_ON_DAMAGE);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-on-feeding") && Helper.isBoolean(map.get("teleport-on-feeding")))
        {
            if ((Boolean) map.get("teleport-on-feeding"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_ON_FEEDING);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-mobs-on-enable") && Helper.isBoolean(map.get("teleport-mobs-on-enable")))
        {
            if ((Boolean) map.get("teleport-mobs-on-enable"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_MOBS_ON_ENABLE);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-animals-on-enable") && Helper.isBoolean(map.get("teleport-animals-on-enable")))
        {
            if ((Boolean) map.get("teleport-animals-on-enable"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_ANIMALS_ON_ENABLE);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-players-on-enable") && Helper.isBoolean(map.get("teleport-players-on-enable")))
        {
            if ((Boolean) map.get("teleport-players-on-enable"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_PLAYERS_ON_ENABLE);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-villagers-on-enable") && Helper.isBoolean(map.get("teleport-villagers-on-enable")))
        {
            if ((Boolean) map.get("teleport-villagers-on-enable"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_VILLAGERS_ON_ENABLE);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-on-fire") && Helper.isBoolean(map.get("teleport-on-fire")))
        {
            if ((Boolean) map.get("teleport-on-fire"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_ON_FIRE);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-on-pvp") && Helper.isBoolean(map.get("teleport-on-pvp")))
        {
            if ((Boolean) map.get("teleport-on-pvp"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_ON_PVP);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-on-block-place") && Helper.isBoolean(map.get("teleport-on-block-place")))
        {
            if ((Boolean) map.get("teleport-on-block-place"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_ON_BLOCK_PLACE);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-on-block-break") && Helper.isBoolean(map.get("teleport-on-block-break")))
        {
            if ((Boolean) map.get("teleport-on-block-break"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_ON_BLOCK_BREAK);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-on-sneak") && Helper.isBoolean(map.get("teleport-on-sneak")))
        {
            if ((Boolean) map.get("teleport-on-sneak"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_ON_SNEAK);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-on-entry") && Helper.isBoolean(map.get("teleport-on-entry")))
        {
            if ((Boolean) map.get("teleport-on-entry"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_ON_ENTRY);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-on-exit") && Helper.isBoolean(map.get("teleport-on-exit")))
        {
            if ((Boolean) map.get("teleport-on-exit"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_ON_EXIT);
                defaultFlags.add(FieldFlag.TELEPORTER);
            }
        }

        if (map.containsKey("teleport-cost") && Helper.isInteger(map.get("teleport-cost")))
        {
            teleportCost = (Integer) map.get("teleport-cost");
        }

        if (map.containsKey("teleport-back-after-seconds") && Helper.isInteger(map.get("teleport-back-after-seconds")))
        {
            teleportBackAfterSeconds = (Integer) map.get("teleport-back-after-seconds");
        }

        if (map.containsKey("teleport-max-distance") && Helper.isInteger(map.get("teleport-max-distance")))
        {
            teleportMaxDistance = (Integer) map.get("teleport-max-distance");
        }

        if (map.containsKey("teleport-explosion-effect") && Helper.isBoolean(map.get("teleport-explosion-effect")))
        {
            if ((Boolean) map.get("teleport-explosion-effect"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_EXPLOSION_EFFECT);
            }
        }

        if (map.containsKey("teleport-relatively") && Helper.isBoolean(map.get("teleport-relatively")))
        {
            if ((Boolean) map.get("teleport-relatively"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_RELATIVELY);
            }
        }

        if (map.containsKey("teleport-announce") && Helper.isBoolean(map.get("teleport-announce")))
        {
            if ((Boolean) map.get("teleport-announce"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_ANNOUNCE);
            }
        }

        if (map.containsKey("teleport-destination") && Helper.isBoolean(map.get("teleport-destination")))
        {
            if ((Boolean) map.get("teleport-destination"))
            {
                defaultFlags.add(FieldFlag.TELEPORT_DESTINATION);
            }
        }

        if (map.containsKey("hidable") && Helper.isBoolean(map.get("hidable")))
        {
            if ((Boolean) map.get("hidable"))
            {
                defaultFlags.add(FieldFlag.HIDABLE);
            }
        }

        if (map.containsKey("command-on-enter") && Helper.isString(map.get("command-on-enter")))
        {
            commandOnEnter = (String) map.get("command-on-enter");
        }

        if (map.containsKey("command-on-exit") && Helper.isString(map.get("command-on-exit")))
        {
            commandOnExit = (String) map.get("command-on-exit");
        }

        if (map.containsKey("player-command-on-enter") && Helper.isString(map.get("player-command-on-enter")))
        {
            playerCommandOnEnter = (String) map.get("player-command-on-enter");
        }

        if (map.containsKey("player-command-on-exit") && Helper.isString(map.get("player-command-on-exit")))
        {
            playerCommandOnExit = (String) map.get("player-command-on-exit");
        }

        if (map.containsKey("command-blacklist") && Helper.isStringList(map.get("command-blacklist")))
        {
            canceledCommands = (List<String>) map.get("command-blacklist");
        }

        defaultFlags.add(FieldFlag.ALL);
    }

    /**
     * Check if the setting has a flag
     *
     * @param flagStr
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
        return defaultFlags.contains(FieldFlag.WELCOME_MESSAGE) ||
                defaultFlags.contains(FieldFlag.FAREWELL_MESSAGE) ||
                defaultFlags.contains(FieldFlag.ENTRY_ALERT) ||
                defaultFlags.contains(FieldFlag.TRANSLOCATION) ||
                defaultFlags.contains(FieldFlag.TELEPORTER) ||
                defaultFlags.contains(FieldFlag.TELEPORT_DESTINATION);
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
        if (limits == null)
        {
            return false;
        }

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
        if (translocationBlacklist == null)
        {
            return true;
        }

        return !translocationBlacklist.contains(type);
    }

    /**
     * Tells you if the player shoudl be teleported teleport based on the block hes standing on
     *
     * @return
     */
    public boolean teleportDueToWalking(Location loc, Field field)
    {
        Block standingOn = new Vec(loc).subtract(0, 1, 0).getBlock();

        if (standingOn.getTypeId() == 0)
        {
            return false;
        }

        boolean teleport = false;

        if (field.hasFlag(FieldFlag.TELEPORT_IF_WALKING_ON))
        {
            teleport = teleportIfWalkingOn.contains(new BlockTypeEntry(standingOn));
        }

        if (field.hasFlag(FieldFlag.TELEPORT_IF_NOT_WALKING_ON))
        {
            teleport = !teleportIfNotWalkingOn.contains(new BlockTypeEntry(standingOn));
        }

        return teleport;
    }

    /**
     * Can destroy (not in blacklist)
     *
     * @param block
     * @return
     */
    public boolean inDestroyBlacklist(Block block)
    {
        if (preventDestroyBlacklist == null)
        {
            return false;
        }

        BlockTypeEntry type = new BlockTypeEntry(block);

        return preventDestroyBlacklist.contains(type);
    }

    /**
     * Can place (not in blacklist)
     *
     * @param block
     * @return
     */
    public boolean inPlaceBlacklist(Block block)
    {
        if (preventPlaceBlacklist == null)
        {
            return false;
        }

        BlockTypeEntry type = new BlockTypeEntry(block);

        return preventPlaceBlacklist.contains(type);
    }

    /**
     * Whether a command is in the canceled list
     *
     * @param command
     * @return
     */
    public boolean isCanceledCommand(String command)
    {
        if (canceledCommands == null)
        {
            return false;
        }

        command = command.replace("/" , "");

        int i = command.indexOf(' ');

        if (i > -1)
        {
            command = command.substring(0, i);
        }

        return canceledCommands.contains(command);
    }

    /**
     * Checks to see if a player should be teleported for holding this item
     *
     * @param itemId
     * @return
     */
    public boolean isTeleportHoldingItem(int itemId)
    {
        return teleportIfHoldingItems.contains(itemId);
    }

    /**
     * Checks to see if a player should be teleported for not holding this item
     *
     * @param itemId
     * @return
     */
    public boolean isTeleportNotHoldingItem(int itemId)
    {
        return teleportIfNotHoldingItems.contains(itemId);
    }

    /**
     * Checks to see if a player should be teleported for having this item
     *
     * @param itemId
     * @return
     */
    public boolean isTeleportHasItem(int itemId)
    {
        return teleportIfHasItems.contains(itemId);
    }

    /**
     * Checks to see if a player should be teleported for not having this item
     *
     * @param itemId
     * @return
     */
    public boolean isTeleportHasNotItem(int itemId)
    {
        return teleportIfNotHasItems.contains(itemId);
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

    /**
     * Retuns a string with all the potions
     *
     * @return
     */
    public String getPotionString()
    {
        String out = "";

        for (PotionEffectType potion : potions.keySet())
        {
            out += Helper.friendlyBlockType(potion.getName()) + ", ";
        }

        return Helper.stripTrailing(out, ", ");
    }

    /**
     * Returns a tring with all the neutralized potions
     *
     * @return
     */
    public String getNeutralizePotionString()
    {
        String out = "";

        for (PotionEffectType potion : neutralizePotions)
        {
            out += Helper.friendlyBlockType(potion.getName()) + ", ";
        }

        return Helper.stripTrailing(out, ", ");
    }

    /**
     * Whether the player is in the allowed list
     *
     * @param playerName
     * @return
     */
    public boolean inAllowedList(String playerName)
    {
        if (allowedPlayers == null)
        {
            return false;
        }

        return allowedPlayers.contains(playerName);
    }

    /**
     * Whether th eplayer is in the denied list
     *
     * @param playerName
     * @return
     */
    public boolean inDeniedList(String playerName)
    {
        if (deniedPlayers == null)
        {
            return false;
        }

        return deniedPlayers.contains(playerName);
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
     * @param world
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

    public int getMaskOnDisabledBlock()
    {
        return maskOnDisabledBlock;
    }

    public int getMaskOnEnabledBlock()
    {
        return maskOnEnabledBlock;
    }

    public String getRequiredPermissionAllow()
    {
        return requiredPermissionAllow;
    }

    public String getRequiredPermissionUse()
    {
        return requiredPermissionUse;
    }

    public int getRefund()
    {
        int refunded = -1;

        if (refund > -1)
        {
            refunded = refund;
        }
        else
        {
            if (price > 0)
            {
                refunded = price;
            }
        }

        return refunded;
    }

    public int getTeleportCost()
    {
        return teleportCost;
    }

    public int getTeleportBackAfterSeconds()
    {
        return teleportBackAfterSeconds;
    }

    public int getTeleportMaxDistance()
    {
        return teleportMaxDistance;
    }

    public int getGriefRevertInterval()
    {
        return griefRevertInterval;
    }

    public String getCommandOnEnter()
    {
        return commandOnEnter;
    }

    public String getCommandOnExit()
    {
        return commandOnExit;
    }

    public String getPlayerCommandOnEnter()
    {
        return playerCommandOnEnter;
    }

    public String getPlayerCommandOnExit()
    {
        return playerCommandOnExit;
    }
}
