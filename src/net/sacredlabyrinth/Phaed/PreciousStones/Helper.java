package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import org.bukkit.entity.Player;

/**
 *
 * @author phaed
 */
public class Helper
{
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public Helper(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Helper function to check for integer
     * @param o
     * @return
     */
    public static boolean isInteger(Object o)
    {
        return o instanceof java.lang.Integer;
    }

    /**
     * Helper function to check for byte
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
     * @param input
     * @return
     */
    public static boolean isIntList(Object obj)
    {
        try
        {
            List<Integer> list = (List<Integer>) obj;
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Helper function to check for float
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
     * @param o
     * @return
     */
    public static boolean isString(Object o)
    {
        return o instanceof java.lang.String;
    }

    /**
     * Helper function to check for boolean
     * @param o
     * @return
     */
    public static boolean isBoolean(Object o)
    {
        return o instanceof java.lang.Boolean;
    }

    /**
     * Remove a character from a string
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
     * @param content
     * @return
     */
    public static String capitalize(String content)
    {
        if (content.length() < 2)
        {
            return content;
        }

        String first = content.substring(0, 1).toUpperCase();
        return first + content.substring(1);
    }

    /**
     *
     * @param playername
     * @return
     */
    public Player matchSinglePlayer(String playername)
    {
        List<Player> players = plugin.getServer().matchPlayer(playername);

        if (players.size() == 1)
        {
            return players.get(0);
        }

        return null;
    }

    /**
     * Convert block type names to friendly format
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
     * Return plural word if count is bigger than one
     * @param count
     * @param word
     * @param ending
     * @return
     */
    public static String plural(int count, String word, String ending)
    {
        return count == 1 ? word : word + ending;
    }

    /**
     * Removes color codes from strings
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
     * @param hexValue
     * @return
     */
    public static String toColor(String hexValue)
    {
        return ChatColor.getByCode(Integer.valueOf(hexValue, 16)).toString();
    }

    /**
     * Converts string array to ArrayList<String>, remove empty strings
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
     * Converts ArrayList<String> to string array
     * @param list
     * @return
     */
    public static String[] toArray(List list)
    {
        return (String[]) list.toArray(new String[0]);
    }

    /**
     * Removes first item from a string array
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
     * @param loc
     * @return
     */
    public static String toLocationString(Location loc)
    {
        return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
    }

    /**
     * Escapes single quotes
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

    public static boolean sameBlock(Location loc, Location loc2)
    {
        if (loc.getBlockX() == loc2.getBlockX() && loc.getBlockY() == loc2.getBlockY() && loc.getBlockZ() == loc2.getBlockZ())
        {
            return true;
        }
        return false;
    }
}
