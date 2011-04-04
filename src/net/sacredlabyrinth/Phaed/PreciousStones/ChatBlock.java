package net.sacredlabyrinth.Phaed.PreciousStones;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.regex.*;

public class ChatBlock
{
    private final int colspacing = 12;
    private static final int lineLength = 320;
    private ArrayList<Double> columnSizes = new ArrayList<Double>();
    private ArrayList<Integer> columnSpaces = new ArrayList<Integer>();
    private ArrayList<String> columnAlignments = new ArrayList<String>();
    private LinkedList<String[]> rows = new LinkedList<String[]>();
    private boolean prefix_used = false;
    private String color = "";
    
    public static Logger log = Logger.getLogger("Minecraft");
    
    public void setAlignment(String[] columnAlignment)
    {
	for (String align : columnAlignment)
	    columnAlignments.add(align);
    }
    
    public void setSpacing(int[] columnSpacings)
    {
	for (int spacing : columnSpacings)
	    columnSpaces.add(spacing);
    }
    
    public void setColumnSizes(double[] columnPercentages, String prefix)
    {
	int ll = lineLength;
	
	if (prefix != null)
	{
	    ll = lineLength - (int) msgLength(prefix);
	}
	
	for (double percentage : columnPercentages)
	    columnSizes.add(Math.floor((percentage / 100) * ll));
    }
    
    public boolean hasContent()
    {
	return rows.size() > 0;
    }
    
    public void addRow(String[] contents)
    {
	rows.add(contents);
    }
        
    public int size()
    {
	return rows.size();
    }
        
    public boolean sendBlock(Player player, int amount)
    {
	if (player == null)
	    return false;
	
	if (rows.size() == 0)
	    return false;
	
	// if no column sizes provided them
	// make some up based on the data
	
	if (columnSizes.size() == 0)
	{
	    // generate columns sizes
	    
	    int col_count = rows.get(0).length;
	    
	    for (int i = 0; i < col_count; i++)
	    {
		// add custom column spacing if specified
		
		int spacing = colspacing;
		
		if (columnSpaces.size() >= (i + 1))
		{
		    spacing = columnSpaces.get(i);
		}
		
		columnSizes.add(getMaxWidth(i) + spacing);
	    }
	}
	
	// size up all sections
	
	for (int i = 0; i < amount; i++)
	{
	    if (rows.size() == 0)
		continue;
	    
	    String rowstring = "";
	    
	    String row[] = rows.pollFirst();
	    
	    for (int sid = 0; sid < row.length; sid++)
	    {
		String section = row[sid];
		double colsize = (columnSizes.size() >= (sid + 1)) ? columnSizes.get(sid) : 0;
		String align = (columnAlignments.size() >= (sid + 1)) ? columnAlignments.get(sid) : "l";
		
		if (align.equalsIgnoreCase("r"))
		{
		    if (msgLength(section) > colsize)
		    {
			rowstring += cropLeftToFit(section, colsize);
		    }
		    else if (msgLength(section) < colsize)
		    {
			rowstring += paddLeftToFit(section, colsize);
		    }
		}
		else if (align.equalsIgnoreCase("l"))
		{
		    if (msgLength(section) > colsize)
		    {
			rowstring += cropRightToFit(section, colsize);
		    }
		    else if (msgLength(section) < colsize)
		    {
			rowstring += paddRightToFit(section, colsize);
		    }
		}
		else if (align.equalsIgnoreCase("c"))
		{
		    if (msgLength(section) > colsize)
		    {
			rowstring += cropRightToFit(section, colsize);
		    }
		    else if (msgLength(section) < colsize)
		    {
			rowstring += centerInLineOf(section, colsize);
		    }
		}
	    }
	    
	    String msg = cropRightToFit(rowstring, lineLength);
	    
	    if (color.length() > 0)
		msg = color + msg;
	    
	    player.sendMessage(msg);
	}
	
	return rows.size() > 0;
    }
    
    public void sendBlock(Player player, String prefix)
    {
	if (player == null)
	    return;
	
	if (rows.size() == 0)
	    return;
	
	boolean prefix_used = prefix == null ? true : false;
	
	String empty_prefix = ChatBlock.makeEmpty(prefix);
	
	// if no column sizes provided them
	// make some up based on the data
	
	if (columnSizes.size() == 0)
	{
	    // generate columns sizes
	    
	    int col_count = rows.get(0).length;
	    
	    for (int i = 0; i < col_count; i++)
	    {
		// add custom column spacing if specified
		
		int spacing = colspacing;
		
		if (columnSpaces.size() >= (i + 1))
		{
		    spacing = columnSpaces.get(i);
		}
		
		columnSizes.add(getMaxWidth(i) + spacing);
	    }
	}
	
	// size up all sections
	
	for (String[] row : rows)
	{
	    String rowstring = "";
	    
	    for (int sid = 0; sid < row.length; sid++)
	    {
		String section = row[sid];
		double colsize = (columnSizes.size() >= (sid + 1)) ? columnSizes.get(sid) : 0;
		String align = (columnAlignments.size() >= (sid + 1)) ? columnAlignments.get(sid) : "l";
		
		if (align.equalsIgnoreCase("r"))
		{
		    if (msgLength(section) > colsize)
		    {
			rowstring += cropLeftToFit(section, colsize);
		    }
		    else if (msgLength(section) < colsize)
		    {
			rowstring += paddLeftToFit(section, colsize);
		    }
		}
		else if (align.equalsIgnoreCase("l"))
		{
		    if (msgLength(section) > colsize)
		    {
			rowstring += cropRightToFit(section, colsize);
		    }
		    else if (msgLength(section) < colsize)
		    {
			rowstring += paddRightToFit(section, colsize);
		    }
		}
		else if (align.equalsIgnoreCase("c"))
		{
		    if (msgLength(section) > colsize)
		    {
			rowstring += cropRightToFit(section, colsize);
		    }
		    else if (msgLength(section) < colsize)
		    {
			rowstring += centerInLineOf(section, colsize);
		    }
		}
	    }
	    
	    String msg = cropRightToFit((prefix_used ? empty_prefix : prefix) + " " + rowstring, lineLength);
	    
	    if (color.length() > 0)
		msg = color + msg;
	    
	    player.sendMessage(msg);
	    
	    prefix_used = true;
	}
    }
    
    public void sendBlock(Player player)
    {
	sendBlock(player, null);
    }
    
    // Outputs a message to everybody
    
    public static void sendMessageAll(Player sender, String msg)
    {
	sendMessageAll(sender, msg);
    }
    
    public double getMaxWidth(double col)
    {
	double maxWidth = 0;
	
	for (String[] row : rows)
	{
	    maxWidth = Math.max(maxWidth, msgLength(row[(int) col]));
	}
	
	return maxWidth;
    }
    
    public static String centerInLine(String msg)
    {
	return centerInLineOf(msg, lineLength);
    }
    
    public static String centerInLineOf(String msg, double lineLength)
    {
	double length = msgLength(msg);
	double diff = lineLength - length;
	
	// if too big for line return it as is
	
	if (diff < 0)
	    return msg;
	
	double sideSpace = diff / 2;
	
	// pad the left with space
	
	msg = paddLeftToFit(msg, length + sideSpace);
	
	// padd the right with space
	
	msg = paddRightToFit(msg, length + sideSpace + sideSpace);
	
	return msg;
    }
    
    public static String makeEmpty(String str)
    {
	if (str == null)
	    return "";
	
	return paddLeftToFit("", msgLength(str));
    }
    
    public static String cropRightToFit(String msg, double length)
    {
	if (msg == null || msg.length() == 0 || length == 0)
	    return "";
	
	while (msgLength(msg) >= length)
	{
	    msg = msg.substring(0, msg.length() - 2);
	}
	
	return msg;
    }
    
    public static String cropLeftToFit(String msg, double length)
    {
	if (msg == null || msg.length() == 0 || length == 0)
	    return "";
	
	while (msgLength(msg) >= length)
	{
	    msg = msg.substring(1);
	}
	
	return msg;
    }
    
    // padds left til the string is a certain size
    
    public static String paddLeftToFit(String msg, double length)
    {
	if (msgLength(msg) > length)
	    return msg;
	
	while (msgLength(msg) < length)
	{
	    msg = " " + msg;
	}
	
	return msg;
    }
    
    // padds right til the string is a certain size
    
    public static String paddRightToFit(String msg, double length)
    {
	if (msgLength(msg) > length)
	    return msg;
	
	while (msgLength(msg) < length)
	{
	    msg = msg + " ";
	}
	
	return msg;
    }
    
    // Finds the length on the screen of a string. Ignores colors.
    
    public static double msgLength(String str)
    {
	double length = 0;
	str = cleanColors(str);
	
	// Loop through all the characters, skipping any color characters and their following color codes
	
	for (int x = 0; x < str.length(); x++)
	{
	    int len = charLength(str.charAt(x));
	    if (len > 0)
		length += len;
	    else
		x++;
	}
	return length;
    }
    
    public static String cleanColors(String str)
    {
	String patternStr = "§.";
	String replacementStr = "";
	
	Pattern pattern = Pattern.compile(patternStr);
	Matcher matcher = pattern.matcher(str);
	String out = matcher.replaceAll(replacementStr);
	
	return out;
    }
    
    // Use: Finds the visual length of the character on the screen.
    
    public static int charLength(char x)
    {
	if ("i.:,;|!".indexOf(x) != -1)
	    return 2;
	else if ("l'".indexOf(x) != -1)
	    return 3;
	else if ("tI[]".indexOf(x) != -1)
	    return 4;
	else if ("fk{}<>\"*()".indexOf(x) != -1)
	    return 5;
	else if ("abcdeghjmnopqrsuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ1234567890\\/#?$%-=_+&^".indexOf(x) != -1)
	    return 6;
	else if ("@~".indexOf(x) != -1)
	    return 7;
	else if (x == ' ')
	    return 4;
	else
	    return -1;
    }
    
    public static String[] wordWrap(String msg)
    {
	return wordWrap(msg, 0);
    }
    
    // Cuts the message apart into whole words short enough to fit on one line
    
    public static String[] wordWrap(String msg, int prefixLn)
    {
	// Split each word apart
	
	ArrayList<String> split = new ArrayList<String>();
	for (String in : msg.split(" "))
	    split.add(in);
	
	// Create an array list for the output
	
	ArrayList<String> out = new ArrayList<String>();
	
	// While i is less than the length of the array of words
	
	while (!split.isEmpty())
	{
	    int len = 0;
	    
	    // Create an array list to hold individual words
	    
	    ArrayList<String> words = new ArrayList<String>();
	    
	    // Loop through the words finding their length and increasing
	    // j, the end point for the sub string
	    
	    while (!split.isEmpty() && split.get(0) != null && len <= (lineLength - prefixLn))
	    {
		double wordLength = msgLength(split.get(0)) + 4;
		
		// If a word is too long for a line
		
		if (wordLength > (lineLength - prefixLn))
		{
		    String[] tempArray = wordCut(len, split.remove(0));
		    words.add(tempArray[0]);
		    split.add(tempArray[1]);
		}
		
		// If the word is not too long to fit
		
		len += wordLength;
		if (len < (lineLength - prefixLn) + 4)
		    words.add(split.remove(0));
	    }
	    // Merge them and add them to the output array.
	    String merged = combineSplit(0, words.toArray(new String[words.size()]), " ") + " ";
	    out.add(merged.replaceAll("\\s+$", ""));
	}
	// Convert to an array and return
	
	return out.toArray(new String[out.size()]);
    }
    
    public static String combineSplit(int startIndex, String[] string, String seperator)
    {
	StringBuilder builder = new StringBuilder();
	for (int i = startIndex; i < string.length; i++)
	{
	    builder.append(string[i]);
	    builder.append(seperator);
	}
	builder.deleteCharAt(builder.length() - seperator.length());
	
	return builder.toString();
    }
    
    // Use: Cuts apart a word that is too long to fit on one line
    
    public static String[] wordCut(int lengthBefore, String str)
    {
	int length = lengthBefore;
	
	// Loop through all the characters, skipping any color characters and their following color codes
	
	String[] output = new String[2];
	int x = 0;
	while (length < lineLength && x < str.length())
	{
	    int len = charLength(str.charAt(x));
	    if (len > 0)
		length += len;
	    else
		x++;
	    x++;
	}
	if (x > str.length())
	    x = str.length();
	
	// Add the substring to the output after cutting it
	
	output[0] = str.substring(0, x);
	
	// Add the last of the string to the output.
	
	output[1] = str.substring(x);
	return output;
    }
    
    // Outputs a single line out, cutting anything else out
    
    public static void saySingle(Player receiver, String msg)
    {
	if (receiver == null)
	    return;

	receiver.sendMessage(cropRightToFit(colorize(new String[]{msg})[0], lineLength));
    }
    
    // Outputs a message to a user
    
    public static void sendMessage(Player receiver, String msg)
    {
	sendMessage(receiver, null, msg);
    }
    
    // Outputs a message to a user
    
    public static void sendMessage(Player receiver, String prefix, String msg)
    {
	if (receiver == null)
	    return;
	
	int prefix_width = prefix == null ? 0 : (int) msgLength(prefix);
	
	String[] message = colorize(wordWrap(msg, prefix_width));
	
	for (String out : message)
	    receiver.sendMessage((prefix == null ? "" : prefix + " ") + out);
    }
    
    // Send blank lie
    
    public void startColor(String color)
    {
	this.color = color;
    }
    
    // Send blank lie
    
    public static void sendBlank(Player receiver)
    {
	if (receiver == null)
	    return;
	
	receiver.sendMessage(" ");
    }
    
    // Colors each line
    
    public static String[] say(String message)
    {
	return colorize(wordWrap(message));
    }
    
    public static String[] colorize(String[] message)
    {
	return colorizeBase(message, 167);
    }
    
    public static String colorize(String message)
    {
	return colorizeBase((new String[] { message }), 167)[0];
    }
    
    public static String[] colorizeBase(String[] message, int charcode)
    {
	if (message != null && message[0] != null && !message[0].isEmpty())
	{
	    // Go through each line
	    
	    String prevColor = "";
	    String lastColor = "";
	    
	    int counter = 0;
	    for (String msg : message)
	    {
		// Loop through looking for a color code
		
		for (int x = 0; x < msg.length(); x++)
		{
		    // If the char is color code
		    if (msg.codePointAt(x) == charcode)
		    {
			// advance x to the next character
			x = x + 1;
			
			lastColor = ChatColor.getByCode(Integer.parseInt(msg.charAt(x) + "", 16)) + "";
		    }
		}
		// Replace the message with the colorful message
		
		message[counter] = prevColor + msg;
		prevColor = lastColor;
		counter++;
	    }
	}
	
	return message;
    }
    
    public String firstPrefix(String prefix)
    {
	if (prefix_used)
	{
	    return ChatBlock.makeEmpty(prefix);
	}
	else
	{
	    prefix_used = true;
	    return prefix;
	}
    }
}
