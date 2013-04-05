package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author phaed
 */
public class Helper
{
    /**
     * Dumps stacktrace to log
     */
    public void dumpStackTrace()
    {
        for (StackTraceElement el : Thread.currentThread().getStackTrace())
        {
            PreciousStones.debug(el.toString());
        }
    }

    /**
     * Helper function to check for integer
     *
     * @param o
     * @return
     */
    public static boolean isInteger(Object o)
    {
        return o instanceof java.lang.Integer;
    }

    /**
     * Helper function to check for byte
     *
     * @param input
     * @return
     */
    public static boolean isByte(String input)
    {
        try
        {
            Byte.parseByte(input);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for short
     *
     * @param input
     * @return
     */
    public static boolean isShort(String input)
    {
        try
        {
            Short.parseShort(input);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for integer
     *
     * @param input
     * @return
     */
    public static boolean isInteger(String input)
    {
        try
        {
            Integer.parseInt(input);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for integer list
     *
     * @param obj
     * @return
     */
    public static boolean isIntList(Object obj)
    {
        try
        {
            @SuppressWarnings("unchecked") List<Integer> list = (List<Integer>) obj;
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for string list
     *
     * @param obj
     * @return
     */
    public static boolean isStringList(Object obj)
    {
        try
        {
            @SuppressWarnings("unchecked") List<String> list = (List<String>) obj;
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for float
     *
     * @param input
     * @return
     */
    public static boolean isFloat(String input)
    {
        try
        {
            Float.parseFloat(input);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for string
     *
     * @param o
     * @return
     */
    public static boolean isString(Object o)
    {
        return o instanceof java.lang.String;
    }

    /**
     * Helper function to check for boolean
     *
     * @param o
     * @return
     */
    public static boolean isBoolean(Object o)
    {
        return o instanceof java.lang.Boolean;
    }

    /**
     * Remove a character from a string
     *
     * @param s
     * @param c
     * @return
     */
    public static String removeChar(String s, char c)
    {
        String r = "";

        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) != c)
            {
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
    public static String removeFirstChar(String s, char c)
    {
        String r = "";

        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) != c)
            {
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
    public static String capitalize(String content)
    {
        if (content.length() < 2)
        {
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
    public static int indexOf(Pattern pattern, String s)
    {
        Matcher matcher = pattern.matcher(s);
        return matcher.find() ? matcher.start() : -1;
    }

    /**
     * Convert block type names to friendly format
     *
     * @param type
     * @return
     */
    public static String friendlyBlockType(String type)
    {
        String out = "";

        type = type.toLowerCase().replace('_', ' ');

        String[] words = type.split("\\s+");

        for (String word : words)
        {
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
    public static String stripColors(String msg)
    {
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
    public static String stripTrailing(String msg, String sep)
    {
        if (msg.length() < sep.length() * 2)
        {
            return msg;
        }

        String out = msg;
        String first = msg.substring(0, sep.length());
        String last = msg.substring(msg.length() - sep.length(), msg.length());

        if (first.equals(sep))
        {
            out = msg.substring(sep.length());
        }

        if (last.equals(sep))
        {
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
    public static String toColor(String hexValue)
    {
        return ChatColor.getByChar(hexValue).toString();
    }

    /**
     * Converts string array to ArrayList<String>, remove empty strings
     *
     * @param values
     * @return
     */
    public static List<String> fromArray(String... values)
    {
        List<String> results = new ArrayList<String>();
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
    public static HashSet<String> fromArray2(String... values)
    {
        HashSet<String> results = new HashSet<String>();
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
    public static List<Player> fromPlayerArray(Player... values)
    {
        List<Player> results = new ArrayList<Player>();
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
    public static String[] toArray(List<String> list)
    {
        return list.toArray(new String[0]);
    }

    /**
     * Removes first item from a string array
     *
     * @param args
     * @return
     */
    public static String[] removeFirst(String[] args)
    {
        List<String> out = fromArray(args);

        if (!out.isEmpty())
        {
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
    public static String toMessage(String[] args)
    {
        String out = "";

        for (String arg : args)
        {
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
    public static String toMessage(String[] args, String sep)
    {
        String out = "";

        for (String arg : args)
        {
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
    public static String toMessage(List<String> args, String sep)
    {
        String out = "";

        for (String arg : args)
        {
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
    public static String toLocationString(Location loc)
    {
        return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
    }

    /**
     * Escapes single quotes
     *
     * @param str
     * @return
     */
    public static String escapeQuotes(String str)
    {
        if (str == null)
        {
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
    public static boolean isSameBlock(Location loc, Location loc2)
    {
        if (loc == null && loc2 == null)
        {
            return true;
        }

        if (loc == null || loc2 == null)
        {
            return false;
        }

        if (loc.getBlockX() == loc2.getBlockX() && loc.getBlockY() == loc2.getBlockY() && loc.getBlockZ() == loc2.getBlockZ())
        {
            return true;
        }
        return false;
    }

    /**
     * Whether the two locations refer to the same location, ignoring pitch and yaw
     *
     * @param loc
     * @param loc2
     * @return
     */
    public static boolean isSameLocation(Location loc, Location loc2)
    {
        if (loc == null && loc2 == null)
        {
            return true;
        }

        if (loc == null || loc2 == null)
        {
            return false;
        }

        if (loc.getX() == loc2.getX() && loc.getY() == loc2.getY() && loc.getZ() == loc2.getZ())
        {
            return true;
        }
        return false;
    }

    /**
     * Converts a FieldFlag to a string
     *
     * @param flag
     * @return
     */
    public static String toFlagStr(FieldFlag flag)
    {
        if (flag == null)
        {
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
    public static FieldFlag toFieldFlag(String flagStr)
    {
        try
        {
            return FieldFlag.valueOf(flagStr.toUpperCase().replace("-", "_"));
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * Returns a type entry from a string
     *
     * @param rawItem
     * @return
     */
    public static boolean hasData(String rawItem)
    {
        return rawItem.contains(":");
    }

    /**
     * Helper function to check for type entry formatted strings
     *
     * @param input
     * @return
     */
    public static boolean isTypeEntry(String input)
    {
        try
        {
            String out = input.replace(":", "");

            Integer.parseInt(out);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Returns a type entry from a string
     *
     * @param rawItem
     * @return
     */
    public static BlockTypeEntry toTypeEntry(String rawItem)
    {
        if (hasData(rawItem))
        {
            String[] split = rawItem.split("[:]");
            return new BlockTypeEntry(Integer.parseInt(split[0]), Byte.parseByte(split[1]));
        }
        else
        {
            if (!isInteger(rawItem))
            {
                return null;
            }

            return new BlockTypeEntry(Integer.parseInt(rawItem), (byte) 0);
        }
    }

    /**
     * Returns a type entry from a string
     *
     * @param rawItem
     * @return
     */
    public static BlockTypeEntry toSpoutTypeEntry(String rawItem)
    {
        if (hasData(rawItem))
        {
            String[] split = rawItem.split("[:]");
            return new BlockTypeEntry(Integer.parseInt(split[0]), Byte.parseByte(split[1]), true);
        }
        else
        {
            if (!isInteger(rawItem))
            {
                return null;
            }

            return new BlockTypeEntry(Integer.parseInt(rawItem), (byte) 0, true);
        }
    }

    /**
     * Returns a list of type entries from a string list
     *
     * @param rawList
     * @return
     */
    public static List<BlockTypeEntry> toTypeEntriesBlind(List<Object> rawList)
    {
        List<BlockTypeEntry> types = new ArrayList<BlockTypeEntry>();

        for (Object rawItem : rawList)
        {
            BlockTypeEntry type = Helper.toTypeEntry(rawItem.toString());

            if (type != null)
            {
                types.add(type);
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
    public static List<BlockTypeEntry> toTypeEntries(List<String> rawList)
    {
        List<BlockTypeEntry> types = new ArrayList<BlockTypeEntry>();

        for (String rawItem : rawList)
        {
            BlockTypeEntry type = Helper.toTypeEntry(rawItem);

            if (type != null)
            {
                types.add(type);
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
    public static Location locationFromPacked(String packed)
    {
        String[] unpacked = packed.split("[|]");

        int x = Integer.parseInt(unpacked[2]);
        int y = Integer.parseInt(unpacked[3]);
        int z = Integer.parseInt(unpacked[4]);
        String world = unpacked[5];

        World w = Bukkit.getServer().getWorld(world);

        return new Location(w, x, y, z);
    }

    public static boolean isDoor(Block block)
    {
        return block.getType().equals(Material.WOODEN_DOOR) || block.getType().equals(Material.IRON_DOOR);
    }

    public static String getDetails(Block block)
    {
        return "[" + block.getType() + "|" + block.getLocation().getBlockX() + " " + block.getLocation().getBlockY() + " " + block.getLocation().getBlockZ() + "]";
    }

    public static boolean isOnline(String playerName)
    {
        return Bukkit.getServer().getPlayer(playerName) != null;
    }

    /**
     * Drop block to ground
     *
     * @param block
     */
    public static boolean dropBlock(Block block)
    {
        if (block.getTypeId() != 0)
        {
            try
            {
                World world = block.getWorld();
                ItemStack is = new ItemStack(block.getTypeId(), 1, (short) 0, block.getData());
                world.dropItemNaturally(block.getLocation(), is);
                return true;
            }
            catch (Exception ex)
            {
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
    public static void dropBlockWipe(Block block)
    {
        if (dropBlock(block))
        {
            block.setTypeId(0);
        }
    }

    /**
     * Removes any non-integers and then parses it
     *
     * @param intString
     * @return
     */
    public static int forceParseInteger(String intString)
    {
        intString = intString.replaceAll("[^0-9]", "");

        if (!Helper.isInteger(intString))
        {
            return 0;
        }

        return Integer.parseInt(intString);
    }

    public static int getWidthFromCoords(int a, int b)
    {
        if (a < 0 && b < 0)
        {
            if (a < b)
            {
                return Math.abs(a - b);
            }
            else
            {
                return Math.abs(b - a);
            }

        }

        if (a >= 0 && b >= 0)
        {
            if (a > b)
            {
                return Math.abs(a - b);
            }
            else
            {
                return Math.abs(b - a);
            }
        }

        if (a < 0 && b >= 0)
        {
            return Math.abs(a - b);
        }

        if (a >= 0 && b < 0)
        {
            return Math.abs(b - a);
        }

        return 0;
    }
}
