package com.bukkit.Phaed.PreciousStones.managers;

import java.io.*;
import java.util.*;

import com.bukkit.Phaed.PreciousStones.Helper;
import com.bukkit.Phaed.PreciousStones.PreciousStones;
import com.bukkit.Phaed.PreciousStones.Vector;
import com.bukkit.Phaed.PreciousStones.Field;
import com.bukkit.Phaed.PreciousStones.Unbreakable;

public class StorageManager
{
    private File unbreakable;
    private File forcefield;
    private transient PreciousStones plugin;
    
    public StorageManager(PreciousStones plugin)
    {
	this.plugin = plugin;
	
	unbreakable = new File(plugin.getDataFolder().getPath() + File.separator + "unbreakables.txt");
	forcefield = new File(plugin.getDataFolder().getPath() + File.separator + "forcefields.txt");
	
	try
	{
	    if (!unbreakable.exists())
		unbreakable.createNewFile();
	}
	catch (IOException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDesc().getName() + "] Cannot create file " + unbreakable.getName());
	}
	
	try
	{
	    if (!forcefield.exists())
		forcefield.createNewFile();
	}
	catch (IOException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDesc().getName() + "] Cannot create file " + forcefield.getName());
	}
    }
    
    /**
     * Load pstones from disk
     */
    public void load()
    {
	if (loadOldStones())
	    return;
	
	plugin.um.chunkLists.clear();
	int linecount = 0;
	
	Scanner scan;
	try
	{
	    scan = new Scanner(unbreakable);
	    
	    while (scan.hasNextLine())
	    {
		linecount++;
		
		String line = scan.nextLine();
		
		if (!line.contains("["))
		    continue;
		
		line = Helper.removeChar(line, '[');
		line = Helper.removeChar(line, ']');
		String[] u = line.split("\\|");
		
		if (u.length < 4)
		{
		    PreciousStones.log.warning("[" + plugin.getDesc().getName() + "] Corrupt unbreakable block: err1 line " + linecount);
		    continue;
		}
		
		String owner = u[0];
		
		if (!Helper.isLong(u[1]))
		{
		    PreciousStones.log.warning("[" + plugin.getDesc().getName() + "] Corrupt forcefield block: err2 line " + linecount);
		    continue;
		}
		
		long world = Long.parseLong(u[1]);
		
		String[] chunk = u[2].split(",");
		
		if (chunk.length < 2 || !Helper.isInteger(chunk[0]) || !Helper.isInteger(chunk[1]))
		{
		    PreciousStones.log.warning("[" + plugin.getDesc().getName() + "] Corrupt unbreakable block: err3 line " + linecount);
		    continue;
		}
		
		String[] vec = u[3].split(",");
		
		if (vec.length < 3 || !Helper.isInteger(vec[0]) || !Helper.isInteger(vec[1]) || !Helper.isInteger(vec[2]))
		{
		    PreciousStones.log.warning("[" + plugin.getDesc().getName() + "] Corrupt unbreakable block: err4 line " + linecount);
		    continue;
		}
		
		Vector chunkvec = new Vector(Integer.parseInt(chunk[0]), 0, Integer.parseInt(chunk[1]));
		ArrayList<Unbreakable> c;
		
		if (plugin.um.chunkLists.containsKey(chunkvec))
		    c = plugin.um.chunkLists.get(chunkvec);
		else
		    c = new ArrayList<Unbreakable>();
		
		Vector stonevec = new Vector(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]));
		
		c.add(new Unbreakable(stonevec, owner, world));
		
		plugin.um.chunkLists.put(chunkvec, c);
	    }
	    
	    PreciousStones.log.info("[" + plugin.getDesc().getName() + "] loaded " + plugin.um.count() + " unbreakable blocks");
	}
	catch (FileNotFoundException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDesc().getName() + "] Cannot read file " + unbreakable.getName());
	}
	
	plugin.ffm.chunkLists.clear();
	linecount = 0;
	
	try
	{
	    scan = new Scanner(forcefield);
	    
	    while (scan.hasNextLine())
	    {
		linecount++;
		
		String line = scan.nextLine();
		
		if (!line.contains("["))
		    continue;
		
		line = Helper.removeChar(line, '[');
		line = Helper.removeChar(line, ']');
		String[] u = line.split("\\|");
		
		if (u.length < 5)
		{
		    PreciousStones.log.warning("[" + plugin.getDesc().getName() + "] Corrupt forcefield block: err1 line " + linecount);
		    continue;
		}
		
		String owner = u[0];
		
		List<String> temp = Arrays.asList(u[1].split(","));
		ArrayList<String> allowed = new ArrayList<String>();
		
		for(String t : temp)
		{
		    if(t.trim().length() > 0)
			allowed.add(t);
		}
		
		if (!Helper.isLong(u[2]))
		{
		    PreciousStones.log.warning("[" + plugin.getDesc().getName() + "] Corrupt forcefield block: err2 line " + linecount);
		    continue;
		}
		
		long world = Long.parseLong(u[2]);
		
		String[] chunk = u[3].split(",");
		
		if (chunk.length < 2 || !Helper.isInteger(chunk[0]) || !Helper.isInteger(chunk[1]))
		{
		    PreciousStones.log.warning("[" + plugin.getDesc().getName() + "] Corrupt forcefield block: err3 line " + linecount);
		    continue;
		}
		
		String[] vec = u[4].split(",");
		
		if (vec.length < 5 || !Helper.isInteger(vec[0]) || !Helper.isInteger(vec[1]) || !Helper.isInteger(vec[2]) || !Helper.isInteger(vec[3]) || !Helper.isInteger(vec[4]))
		{
		    PreciousStones.log.warning("[" + plugin.getDesc().getName() + "] Corrupt forcefield block: err4 line " + linecount);
		    continue;
		}
		
		Vector chunkvec = new Vector(Integer.parseInt(chunk[0]), 0, Integer.parseInt(chunk[1]));
		ArrayList<Field> c;
		
		if (plugin.ffm.chunkLists.containsKey(chunkvec))
		    c = plugin.ffm.chunkLists.get(chunkvec);
		else
		    c = new ArrayList<Field>();
		
		Vector fieldvec = new Vector(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]), Integer.parseInt(vec[3]), Integer.parseInt(vec[4]));
		
		c.add(new Field(fieldvec, owner, allowed, world));
		
		plugin.ffm.chunkLists.put(chunkvec, c);
	    }
	    
	    PreciousStones.log.info("[" + plugin.getDesc().getName() + "] loaded " + plugin.ffm.count() + " forcefield blocks");
	}
	catch (FileNotFoundException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDesc().getName() + "] Cannot read file " + forcefield.getName());
	}
    }
    
    /**
     * Save pstones to disk
     */
    public void save()
    {
	PreciousStones.log.info("[" + plugin.getDesc().getName() + "] saving " + unbreakable.getName());
	
	BufferedWriter bwriter = null;
	FileWriter fwriter = null;
	try
	{
	    if (!unbreakable.exists())
		unbreakable.createNewFile();
	    
	    fwriter = new FileWriter(unbreakable);
	    bwriter = new BufferedWriter(fwriter);
	    
	    for (Vector chunkvec : plugin.um.chunkLists.keySet())
	    {
		ArrayList<Unbreakable> c = plugin.um.chunkLists.get(chunkvec);
		
		for (Unbreakable unbreakable : c)
		{
		    StringBuilder builder = new StringBuilder();
		    builder.append("[");
		    builder.append(unbreakable.getOwner());
		    builder.append("|");
		    builder.append(unbreakable.getWorldId());
		    builder.append("|");
		    builder.append(chunkvec.getX());
		    builder.append(",");
		    builder.append(chunkvec.getZ());
		    builder.append("|");
		    builder.append(unbreakable.getVector().getX());
		    builder.append(",");
		    builder.append(unbreakable.getVector().getY());
		    builder.append(",");
		    builder.append(unbreakable.getVector().getZ());
		    builder.append("]");
		    bwriter.write(builder.toString());
		    bwriter.newLine();
		}
	    }
	    
	    bwriter.flush();
	}
	catch (IOException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDesc().getName() + "] IO Exception with file " + unbreakable.getName());
	}
	finally
	{
	    try
	    {
		if (bwriter != null)
		{
		    bwriter.flush();
		    bwriter.close();
		}
		if (fwriter != null)
		{
		    fwriter.close();
		}
	    }
	    catch (IOException e)
	    {
		PreciousStones.log.severe("[" + plugin.getDesc().getName() + "] IO Exception with file " + unbreakable.getName() + " (on close)");
	    }
	}
	
	PreciousStones.log.info("[" + plugin.getDesc().getName() + "] saving " + forcefield.getName());
	
	try
	{
	    if (!forcefield.exists())
		forcefield.createNewFile();
	    
	    fwriter = new FileWriter(forcefield);
	    bwriter = new BufferedWriter(fwriter);
	    
	    for (Vector chunkvec : plugin.ffm.chunkLists.keySet())
	    {
		ArrayList<Field> c = plugin.ffm.chunkLists.get(chunkvec);
		
		for (Field field : c)
		{
		    ArrayList<String> allowed = field.getAllowed();
		    Collections.sort(allowed);
		    
		    StringBuilder builder = new StringBuilder();
		    builder.append("[");
		    builder.append(field.getOwner());
		    builder.append("|");
		    for (int i = 0; i < allowed.size(); i++)
		    {
			builder.append(allowed.get(i));
			
			if (i < allowed.size() - 1)
			    builder.append(",");
		    }
		    builder.append("|");
		    builder.append(field.getWorldId());
		    builder.append("|");
		    builder.append(chunkvec.getX());
		    builder.append(",");
		    builder.append(chunkvec.getZ());
		    builder.append("|");
		    builder.append(field.getVector().getX());
		    builder.append(",");
		    builder.append(field.getVector().getY());
		    builder.append(",");
		    builder.append(field.getVector().getZ());
		    builder.append(",");
		    builder.append(field.getVector().getRadius());
		    builder.append(",");
		    builder.append(field.getVector().getHeight());
		    builder.append("]");
		    bwriter.write(builder.toString());
		    bwriter.newLine();
		}
	    }
	    
	    bwriter.flush();
	}
	catch (IOException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDesc().getName() + "] IO Exception with file " + unbreakable.getName());
	}
	finally
	{
	    try
	    {
		if (bwriter != null)
		{
		    bwriter.flush();
		    bwriter.close();
		}
		if (fwriter != null)
		{
		    fwriter.close();
		}
	    }
	    catch (IOException e)
	    {
		PreciousStones.log.severe("[" + plugin.getDesc().getName() + "] IO Exception with file " + unbreakable.getName() + " (on close)");
	    }
	}
	
	deleteOld();
    }
    
    /**
     * Load old stone files into new ones
     */
    public boolean loadOldStones()
    {
	boolean loaded = false;
	
	try
	{
	    File unbreakableFile = new File(plugin.getDataFolder().getPath() + File.separator + "unbreakable.bin");
	    
	    if (unbreakableFile.exists())
	    {
		try
		{
		    FileInputStream fi = new FileInputStream(unbreakableFile);
		    ObjectInputStream oi = new ObjectInputStream(fi);
		    
		    com.bukkit.Phaed.PreciousStones.UnbreakableManager um = (com.bukkit.Phaed.PreciousStones.UnbreakableManager) oi.readObject();
		    um.initiate(plugin);
		    
		    for (Vector vec : um.getChunkLists().keySet())
		    {
			ArrayList<Unbreakable> newlist = new ArrayList<Unbreakable>();
			HashMap<Vector, String> oldlist = um.getChunkLists().get(vec);
			
			for (Vector oldvec : oldlist.keySet())
			{
			    String oldowner = oldlist.get(oldvec);
			    
			    newlist.add(new Unbreakable(oldvec, oldowner, plugin.getServer().getWorlds().get(0).getId()));
			}
			
			plugin.um.chunkLists.put(vec, newlist);
		    }
		    
		    oi.close();
		    fi.close();
		    
		    PreciousStones.log.info("[" + plugin.getDesc().getName() + "] loaded " + plugin.um.count() + " unbreakable stones");
		}
		catch (Exception e)
		{
		    PreciousStones.log.info("[" + plugin.getDesc().getName() + "] loading failed with error. unbreakable.bin");
		    
		    if (e.getMessage() != null)
			PreciousStones.log.info("[" + plugin.getDesc().getName() + "] error: " + e.getMessage());
		}
		
		loaded = true;
	    }
	}
	catch (Exception e)
	{
	    PreciousStones.log.severe("[" + plugin.getDesc().getName() + "] Could not load old format unbreakable.bin file");
	}
	
	try
	{
	    File protectionFile = new File(plugin.getDataFolder().getPath() + File.separator + "protection.bin");
	    
	    if (protectionFile.exists())
	    {
		try
		{
		    FileInputStream fi = new FileInputStream(protectionFile);
		    ObjectInputStream oi = new ObjectInputStream(fi);
		    
		    com.bukkit.Phaed.PreciousStones.ProtectionManager pm = (com.bukkit.Phaed.PreciousStones.ProtectionManager) oi.readObject();
		    pm.initiate(plugin);
		    
		    for (Vector vec : pm.getChunkLists().keySet())
		    {
			ArrayList<Field> newlist = new ArrayList<Field>();
			HashMap<Vector, ArrayList<String>> oldlist = pm.getChunkLists().get(vec);
			
			for (Vector oldvec : oldlist.keySet())
			{
			    ArrayList<String> oldallowed = oldlist.get(oldvec);
			    String oldowner = oldallowed.get(0);
			    oldallowed.remove(0);
			    
			    newlist.add(new Field(oldvec, oldowner, oldallowed, plugin.getServer().getWorlds().get(0).getId()));
			}
			
			plugin.ffm.chunkLists.put(vec, newlist);
		    }
		    
		    oi.close();
		    fi.close();
		    
		    PreciousStones.log.info("[" + plugin.getDesc().getName() + "] loaded " + plugin.ffm.count() + " protection stones");
		}
		catch (Exception e)
		{
		    PreciousStones.log.info("[" + plugin.getDesc().getName() + "] loading failed with error. protection.bin");
		    
		    if (e.getMessage() != null)
			PreciousStones.log.info("[" + plugin.getDesc().getName() + "] error: " + e.getMessage());
		}
		
		loaded = true;
	    }
	}
	catch (Exception e)
	{
	    PreciousStones.log.severe("[" + plugin.getDesc().getName() + "] Could not load old format protection.bin file");
	}
	
	return loaded;
    }
    
    /**
     * Delete old save files
     */
    public void deleteOld()
    {
	try
	{
	    File unbreakableFile = new File(plugin.getDataFolder().getPath() + File.separator + "unbreakable.bin");
	    if (unbreakableFile.exists())
		unbreakableFile.delete();
	    
	    File protectionFile = new File(plugin.getDataFolder().getPath() + File.separator + "protection.bin");
	    if (protectionFile.exists())
		protectionFile.delete();
	}
	catch (Exception e)
	{
	    PreciousStones.log.severe("[" + plugin.getDesc().getName() + "] Could not delete old .bin files");
	}
    }
}
