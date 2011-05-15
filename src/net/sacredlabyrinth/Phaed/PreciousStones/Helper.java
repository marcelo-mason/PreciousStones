package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.List;

import org.bukkit.entity.Player;

/**
 *
 * @author phaed
 */
public class Helper
{
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
		r += s.charAt(i);
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
	    return content;

	String first = content.substring(0, 1).toUpperCase();
	return first + content.substring(1);
    }

    /**
     *
     * @param plugin
     * @param playername
     * @return
     */
    public static Player matchExactPlayer(PreciousStones plugin, String playername)
    {
	List<Player> players = plugin.getServer().matchPlayer(playername);

	for (Player player : players)
	{
	    if (player.getName().equals(playername))
		return player;
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

	for(String word : words)
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
}
