package net.sacredlabyrinth.Phaed.PreciousStones.helpers;

import net.sacredlabyrinth.Phaed.PreciousStones.MaterialName;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author phaed
 */
public class Helper {
    private static Map<Integer, Material> materialIdMap;

    /**
     * Dumps stacktrace to log
     */
    public void dumpStackTrace() {
        for (StackTraceElement el : Thread.currentThread().getStackTrace()) {
            PreciousStones.debug(el.toString());
        }
    }

    /**
     * Helper function to check for integer
     *
     * @param o
     * @return
     */
    public static boolean isInteger(Object o) {
        return o instanceof java.lang.Integer;
    }

    /**
     * Helper function to check for doubles
     *
     * @param o
     * @return
     */
    public static boolean isDouble(Object o) {
        return o instanceof java.lang.Double;
    }

    /**
     * Helper function to check for byte
     *
     * @param input
     * @return
     */
    public static boolean isByte(String input) {
        try {
            Byte.parseByte(input);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Helper function to check for short
     *
     * @param input
     * @return
     */
    public static boolean isShort(String input) {
        try {
            Short.parseShort(input);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Helper function to check for integer
     *
     * @param input
     * @return
     */
    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Helper function to check for integer list
     *
     * @param obj
     * @return
     */
    public static boolean isIntList(Object obj) {
        try {
            @SuppressWarnings("unchecked") List<Integer> list = (List<Integer>) obj;
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Helper function to check for string list
     *
     * @param obj
     * @return
     */
    public static boolean isStringList(Object obj) {
        try {
            @SuppressWarnings("unchecked") List<String> list = (List<String>) obj;
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Helper function to check for float
     *
     * @param input
     * @return
     */
    public static boolean isFloat(String input) {
        try {
            Float.parseFloat(input);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Helper function to check for string
     *
     * @param o
     * @return
     */
    public static boolean isString(Object o) {
        return o instanceof java.lang.String;
    }

    /**
     * Helper function to check for boolean
     *
     * @param o
     * @return
     */
    public static boolean isBoolean(Object o) {
        return o instanceof java.lang.Boolean;
    }

    /**
     * Remove a character from a string
     *
     * @param s
     * @param c
     * @return
     */
    public static String removeChar(String s, char c) {
        String r = "";

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != c) {
                r += s.charAt(i);
            }
        }

        return r;
    }

    /**
     * Remove first character from a string
     *
     * @param s
     * @param c
     * @return
     */
    public static String removeFirstChar(String s, char c) {
        String r = "";

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != c) {
                r += s.charAt(i);
                break;
            }
        }

        return r;
    }

    /**
     * Capitalize first word of sentence
     *
     * @param content
     * @return
     */
    public static String capitalize(String content) {
        if (content.length() < 2) {
            return content;
        }

        /*
        Pattern p = Pattern.compile("[^" + ChatColor.COLOR_CHAR + "\\{/][A-Za-z]");

        int i = indexOf(p, content);

        if (i > -1)
        {
            String first = content.substring(i, i + 1).toUpperCase();
            return first + content.substring(i + 1);
        }
        */

        return content;
    }

    /**
     * returns the index of the fist match
     *
     * @param pattern
     * @param s
     * @return
     */
    public static int indexOf(Pattern pattern, String s) {
        Matcher matcher = pattern.matcher(s);
        return matcher.find() ? matcher.start() : -1;
    }

    /**
     * Convert block type names to friendly format
     *
     * @param type
     * @return
     */
    public static String friendlyBlockType(Material type) {
        return MaterialName.getIDName(type);
    }

    /**
     * Convert names to friendly format
     *
     * @param type
     * @return
     */
    public static String friendlyName(String type) {
        String out = "";

        type = type.toLowerCase().replace('_', ' ');

        String[] words = type.split("\\s+");

        for (String word : words) {
            out += capitalize(word) + " ";
        }

        return out.trim();
    }

    /**
     * Removes color codes from strings
     *
     * @param msg
     * @return
     */
    public static String stripColors(String msg) {
        String out = msg.replaceAll("[&][0-9a-f]", "");
        out = out.replaceAll(String.valueOf((char) 194), "");
        return out.replaceAll("[\u00a7][0-9a-f]", "");
    }

    /**
     * Removes trailing separators
     *
     * @param msg
     * @param sep
     * @return
     */
    public static String stripTrailing(String msg, String sep) {
        if (msg.length() < sep.length() * 2) {
            return msg;
        }

        String out = msg;
        String first = msg.substring(0, sep.length());
        String last = msg.substring(msg.length() - sep.length(), msg.length());

        if (first.equals(sep)) {
            out = msg.substring(sep.length());
        }

        if (last.equals(sep)) {
            out = msg.substring(0, msg.length() - sep.length());
        }

        return out;
    }

    /**
     * Hex value to ChatColor
     *
     * @param hexValue
     * @return
     */
    public static String toColor(String hexValue) {
        return ChatColor.getByChar(hexValue).toString();
    }

    /**
     * Converts string array to ArrayList<String>, remove empty strings
     *
     * @param values
     * @return
     */
    public static List<String> fromArray(String... values) {
        List<String> results = new ArrayList<>();
        Collections.addAll(results, values);
        results.remove("");

        return results;
    }

    /**
     * Converts string array to HashSet<String>, remove empty strings
     *
     * @param values
     * @return
     */
    public static HashSet<String> fromArray2(String... values) {
        HashSet<String> results = new HashSet<>();
        Collections.addAll(results, values);
        results.remove("");
        return results;
    }

    /**
     * Converts a player array to ArrayList<Player>
     *
     * @param values
     * @return
     */
    public static List<Player> fromPlayerArray(Player... values) {
        List<Player> results = new ArrayList<>();
        Collections.addAll(results, values);
        return results;
    }

    /**
     * Converts List<String> to string array
     *
     * @param list
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String[] toArray(List<String> list) {
        return list.toArray(new String[0]);
    }

    /**
     * Removes first item from a string array
     *
     * @param args
     * @return
     */
    public static String[] removeFirst(String[] args) {
        List<String> out = fromArray(args);

        if (!out.isEmpty()) {
            out.remove(0);
        }
        return toArray(out);
    }

    /**
     * Converts a string array to a space separated string
     *
     * @param args
     * @return
     */
    public static String toMessage(String[] args) {
        String out = "";

        for (String arg : args) {
            out += arg + " ";
        }

        return out.trim();
    }

    /**
     * Converts a string array to a string with custom separators
     *
     * @param args
     * @param sep
     * @return
     */
    public static String toMessage(String[] args, String sep) {
        String out = "";

        for (String arg : args) {
            out += arg + sep;
        }

        return stripTrailing(out, sep);
    }

    /**
     * Converts a string array to a string with custom separators
     *
     * @param args
     * @param sep
     * @return
     */
    public static String toMessage(Collection<String> args, String sep) {
        String out = "";

        for (String arg : args) {
            out += arg + sep;
        }

        return stripTrailing(out, sep);
    }

    /**
     * Returns a prettier coordinate, does not include world
     *
     * @param loc
     * @return
     */
    public static String toLocationString(Location loc) {
        return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
    }

    /**
     * Escapes single quotes
     *
     * @param str
     * @return
     */
    public static String escapeQuotes(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''");
    }

    /**
     * Whether the two locations refer to the same block
     *
     * @param loc
     * @param loc2
     * @return
     */
    public static boolean isSameBlock(Location loc, Location loc2) {
        if (loc == null && loc2 == null) {
            return true;
        }

        if (loc == null || loc2 == null) {
            return false;
        }

        return loc.getBlockX() == loc2.getBlockX() && loc.getBlockY() == loc2.getBlockY() && loc.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * Whether the two locations refer to the same location, ignoring pitch and yaw
     *
     * @param loc
     * @param loc2
     * @return
     */
    public static boolean isSameLocation(Location loc, Location loc2) {
        if (loc == null && loc2 == null) {
            return true;
        }

        if (loc == null || loc2 == null) {
            return false;
        }

        return loc.getX() == loc2.getX() && loc.getY() == loc2.getY() && loc.getZ() == loc2.getZ();
    }

    /**
     * Converts a FieldFlag to a string
     *
     * @param flag
     * @return
     */
    public static String toFlagStr(FieldFlag flag) {
        if (flag == null) {
            return "";
        }

        return flag.toString().toLowerCase().replace("_", "-");
    }

    /**
     * Converts a string back to a field flag
     *
     * @param flagStr
     * @return
     */
    public static FieldFlag toFieldFlag(String flagStr) {
        try {
            return FieldFlag.valueOf(flagStr.toUpperCase().replace("-", "_"));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Returns a type entry from a string
     *
     * @param rawItem
     * @return
     */
    public static boolean hasData(String rawItem) {
        return rawItem.contains(":");
    }

    /**
     * Helper function to check for type entry formatted strings
     *
     * @param input
     * @return
     */
    public static boolean isTypeEntry(String input) {
        try {
            String out = input.replace(":", "");

            Integer.parseInt(out);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Returns a list of type entries from a string list
     *
     * @param rawList
     * @return
     */
    public static List<BlockTypeEntry> toTypeEntriesBlind(List<Object> rawList) {
        List<BlockTypeEntry> types = new ArrayList<>();

        for (Object rawItem : rawList) {
            BlockTypeEntry entry = new BlockTypeEntry(rawItem.toString());

            if (entry.isValid()) {
                types.add(entry);
            }
        }

        return types;
    }

    /**
     * Returns a list of type entries from a string list
     *
     * @param rawList
     * @return
     */
    public static List<BlockTypeEntry> toTypeEntries(List<String> rawList) {
        List<BlockTypeEntry> types = new ArrayList<>();

        for (String rawItem : rawList) {
            BlockTypeEntry entry = new BlockTypeEntry(rawItem);

            if (entry.isValid()) {
                types.add(entry);
            }
        }

        return types;
    }

    /**
     * Used in GriefBlock
     *
     * @param packed
     * @return
     */
    public static Location locationFromPacked(String packed) {
        String[] unpacked = packed.split("[|]");

        int x = Integer.parseInt(unpacked[1]);
        int y = Integer.parseInt(unpacked[2]);
        int z = Integer.parseInt(unpacked[3]);
        String world = unpacked[4];

        World w = Bukkit.getServer().getWorld(world);

        return new Location(w, x, y, z);
    }

    public static boolean isDoor(Block block) {
        return block.getType() == Material.OAK_DOOR || block.getType() == Material.IRON_DOOR || block.getType() == Material.ACACIA_DOOR
                 || block.getType() == Material.BIRCH_DOOR || block.getType() == Material.DARK_OAK_DOOR || block.getType() == Material.JUNGLE_DOOR; // doors
    }

    public static String getDetails(Block block) {
        return "[" + block.getType() + "|" + block.getLocation().getBlockX() + " " + block.getLocation().getBlockY() + " " + block.getLocation().getBlockZ() + "]";
    }

    @SuppressWarnings("deprecation")
    public static boolean isOnline(String playerName) {
        return Bukkit.getServer().getPlayer(playerName) != null;
    }

    /**
     * Drop block to ground
     *
     * @param block
     */
    public static boolean dropBlock(Block block) {
        if (block.getType() != Material.AIR) {
            try {
                World world = block.getWorld();
                ItemStack is = new ItemStack(block.getType());
                world.dropItemNaturally(block.getLocation(), is);
                return true;
            } catch (Exception ex) {
                // fail silently
            }
        }
        return false;
    }

    /**
     * Drop block to ground and wipe out existing
     *
     * @param block
     */
    public static void dropBlockWipe(Block block) {
        if (dropBlock(block)) {
            block.setType(Material.AIR);
        }
    }

    /**
     * Removes any non-integers and then parses it
     *
     * @param intString
     * @return
     */
    public static int forceParseInteger(String intString) {
        intString = intString.replaceAll("[^0-9]", "");

        if (!Helper.isInteger(intString)) {
            return 0;
        }

        return Integer.parseInt(intString);
    }

    public static int getWidthFromCoords(int a, int b) {
        if (a < 0 && b < 0) {
            if (a < b) {
                return Math.abs(a - b) + 1;
            } else {
                return Math.abs(b - a) + 1;
            }

        }

        if (a >= 0 && b >= 0) {
            if (a > b) {
                return Math.abs(a - b) + 1;
            } else {
                return Math.abs(b - a) + 1;
            }
        }

        if (a < 0 && b >= 0) {
            return Math.abs(a - b) + 1;
        }

        if (a >= 0 && b < 0) {
            return Math.abs(b - a) + 1;
        }

        return 0;
    }

    /**
     * Returns the material string if its a vanilla block, the id if not
     *
     * @param type
     * @return
     */
    public static String getMaterialString(Material type) {
        return MaterialName.getIDName(type);
    }

    public static boolean isAirOrWater(Location loc) {
        Material type = loc.getWorld().getBlockAt(loc).getType();
        return type == Material.AIR || type == Material.WATER;
    }

    /**
     * Gets the player that was responsible for the damage. If no player was
     * responsible, <code>null</code> is returned.
     * <p>
     * <ul>
     * <li>If the entity was damaged by a player, that player
     * is returned.</li>
     * <li>If the entity was damaged by a projectile, and the projectile
     * was shot by a player, that player is returned.</li>
     * </ul>
     *
     * @param event The damage event.
     * @return The player, or <code>null</code> if not found.
     */
    public static Player getDamagingPlayer(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return null;
        }

        Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
        if (damager instanceof Player) {
            return (Player) damager;
        }
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }

        return null;
    }

    /**
     * Returns the closest player to the target within a radius
     *
     * @param target
     * @return closest player
     */
    public static Player getClosestPlayer(Location target, int radius) {
        Collection<Player> players = getOnlinePlayers();

        double closestDistance = radius;
        Player closestPlayer = null;

        for (Player player : players) {
            Location loc = player.getLocation();

            if (!loc.getWorld().getName().equals(target.getWorld().getName())) {
                continue;
            }

            double distance = loc.distance(target);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        return closestPlayer;
    }

    /**
     * Returns the closest player to the target within a radius
     *
     * @param names
     * @return closest player
     */
    @SuppressWarnings("deprecation")
    public static Player getClosestPlayer(Location target, int radius, Set<String> names) {
        ArrayList<Player> players = new ArrayList<>();

        for (String name : names) {
            Player player = Bukkit.getServer().getPlayer(name);
            if (player != null) {
                players.add(player);
            }
        }

        double closestDistance = radius;
        Player closestPlayer = null;

        for (Player player : players) {
            Location loc = player.getLocation();

            if (!loc.getWorld().getName().equals(target.getWorld().getName())) {
                continue;
            }

            double distance = loc.distance(target);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        return closestPlayer;
    }

    public static Collection<Player> getOnlinePlayers() {
        try {
            Method method = Bukkit.class.getDeclaredMethod("getOnlinePlayers");
            Object players = method.invoke(null);

            if (players instanceof Player[]) {
                return new ArrayList<>(Arrays.asList((Player[]) players));
            } else {
                return ((Collection<Player>) players);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Get the method name for a depth in call stack. <br />
     * Utility function
     *
     * @param depth depth in the call stack (0 means current method, 1 means call method, ...)
     * @return method name
     */
    public static String getMethodName(final int depth) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();

        //System. out.println(ste[ste.length-depth].getClassName()+"#"+ste[ste.length-depth].getMethodName());
        // return ste[ste.length - depth].getMethodName();  //Wrong, fails for depth = 0
        return ste[ste.length - 1 - depth].getMethodName(); //Thank you Tom Tresansky
    }

    /**
     * Whether the material is a boat type
     *
     * @param mat the material
     * @return whether its a boat
     */
    public static boolean isBoat(Material mat) {
        return mat == Material.OAK_BOAT ||
                mat == Material.ACACIA_BOAT ||
                mat == Material.BIRCH_BOAT ||
                mat == Material.DARK_OAK_BOAT ||
                mat == Material.JUNGLE_BOAT ||
                mat == Material.SPRUCE_BOAT;

    }

    /**
     * Get milliseconds from epoch
     *
     * @return milliseconds
     */
    public static long getMillis() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Merges two lists withotu any duplicate items
     *
     * @param one first list
     * @param two second list
     * @return first list merged with second list with only unique items
     */
    public static <T> void addUnique(List<T> one, List<T> two) {
        for (T x : two) {
            if (!one.contains(x)) {
                one.add(x);
            }
        }
    }

    /**
     * Format double to clean string without zero
     *
     * @return formatted number
     */
    public static String formatDouble(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            return String.format("%s", d);
        }
    }

    @SuppressWarnings("deprecation")
    public static Material getMaterial(int id) {
        if (materialIdMap == null) {
            materialIdMap = new HashMap<>();

            Object[] allMaterials = Material.AIR.getDeclaringClass().getEnumConstants();
            for (Object o : allMaterials) {
                Material material = (Material)o;
                materialIdMap.put(material.getId(), material);
            }
        }
        Material material = materialIdMap.get(id);
        if (material.isLegacy()) {
            Material converted = Bukkit.getUnsafe().fromLegacy(material);
            if (converted != null && converted != Material.AIR) {
                material = converted;
            }
        }
        return material;
    }

    @SuppressWarnings("deprecation")
    public static int getMaterialId(Material material) {
        return material.getId();
    }
}
