package com.bukkit.Phaed.PreciousStones;

import java.util.List;

import org.bukkit.entity.Player;

public class Helper
{
    static final String[] Number1 = { "", " Hundrad" };
    static final String[] Number2 = { "", "One", "Two", "Three", "Four", "Five", " Six", " Seven", "Eight", " Nine", "Ten" };
    
    /**
     * Convert number to words
     */
    public static String numberToWord(int number)
    {
	if (number == 0)
	{
	    return "zero";
	}
	String pre = "";
	String str1 = "";
	int i = 0;
	do
	{
	    int n = number % 100;
	    if (n != 0)
	    {
		String s = number(n);
		str1 = s + Number1[i] + str1;
	    }
	    i++;
	    number /= 100;
	}
	while (number > 0);
	return (pre + str1).trim();
    }    
    
    private static String number(int number)
    {
	String str;
	if (number % 100 < 10)
	{
	    str = Number2[number % 100];
	    number /= 100;
	}
	else
	{
	    str = Number2[number % 5];
	    number /= 5;
	}
	if (number == 0)
	    return str;
	return Number2[number] + "hundred" + str;
    }
    
    /**
     * Helper function to check for integer
     */
    public static boolean isInteger(Object o)
    {
	return o instanceof java.lang.Integer;
    }
    
    /**
     * Helper function to check for integer
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
     * Helper function to check for long
     */
    public static boolean isLong(String input)
    {
	try
	{
	    Long.parseLong(input);
	    return true;
	}
	catch (Exception ex)
	{
	    return false;
	}
    }
    
    /**
     * Helper function to check for string
     */
    public static boolean isString(Object o)
    {
	return o instanceof java.lang.String;
    }
    
    /**
     * Helper function to check for boolean
     */
    public static boolean isBoolean(Object o)
    {
	return o instanceof java.lang.Boolean;
    }
    
    /**
     * Remove a character from a string
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
     * Capitalize first word of sentence
     */
    public static String capitalize(String content)
    {
	if (content.length() < 2)
	    return content;
	
	String first = content.substring(0, 1).toUpperCase();
	return first + content.substring(1);
    }
    
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
    
}
