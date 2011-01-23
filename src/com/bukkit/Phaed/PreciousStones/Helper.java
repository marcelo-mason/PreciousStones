package com.bukkit.Phaed.PreciousStones;

public class Helper
{    
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
    
    
}
