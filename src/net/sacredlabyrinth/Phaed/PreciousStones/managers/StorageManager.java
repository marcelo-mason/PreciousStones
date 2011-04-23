package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.io.*;
import java.util.*;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import net.sacredlabyrinth.Phaed.PreciousStones.CloakEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import net.sacredlabyrinth.Phaed.PreciousStones.SnitchEntry;

public class StorageManager
{
    private File old;
    private File unbreakable;
    private File forcefield;
    private File temp_forcefield;
    private File temp_unbreakable;
    private PreciousStones plugin;
    
    public StorageManager(PreciousStones plugin)
    {
	this.plugin = plugin;
	
	old = new File(plugin.getDataFolder().getPath() + File.separator + "old");
	unbreakable = new File(plugin.getDataFolder().getPath() + File.separator + "unbreakables.txt");
	forcefield = new File(plugin.getDataFolder().getPath() + File.separator + "forcefields.txt");
	temp_forcefield = new File(plugin.getDataFolder().getPath() + File.separator + "fields.tmp");
	temp_unbreakable = new File(plugin.getDataFolder().getPath() + File.separator + "unbreakables.tmp");
	
	createForceFieldFile();
	createUnbreakableFile();
	createOldDirectory();
	
	load();
	
	if (plugin.settings.saveFrequency > 0)
	{
	    startScheduler();
	}
    }
    
    /**
     * Load pstones from disk
     */
    public void load()
    {
	loadUnbreakables();
	loadFields();
    }
    
    /**
     * Load unbreakables from disk
     */
    public void loadUnbreakables()
    {
	if (temp_unbreakable.exists())
	{
	    backupUnbreakable();
	}
	
	HashMap<ChunkVec, LinkedList<Unbreakable>> loadedUnbreakables = new HashMap<ChunkVec, LinkedList<Unbreakable>>();
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
		
		String[] u = line.split("\\|");
		
		if (u.length < 5)
		{
		    PreciousStones.log.warning("Corrupt unbreakable: seccount" + u.length + " line " + linecount);
		    continue;
		}
		
		String sectype = u[0];
		String secowner = u[1];
		String secworld = u[2];
		String secchunk = u[3];
		String secvec = u[4];
		
		sectype = Helper.removeChar(sectype, '[');
		secvec = Helper.removeChar(secvec, ']');
		
		if (sectype.trim().length() == 0 || Material.getMaterial(sectype) == null || !plugin.settings.isUnbreakableType(sectype))
		{
		    PreciousStones.log.warning(" Corrupt unbreakable: type error " + linecount);
		    continue;
		}
		
		String type = sectype;
		
		if (secowner.trim().length() == 0)
		{
		    PreciousStones.log.warning("Corrupt unbreakable: owner error " + linecount);
		    continue;
		}
		
		String owner = secowner;
		
		if (secworld.trim().length() == 0)
		{
		    PreciousStones.log.warning("Corrupt unbreakable: owner error " + linecount);
		    continue;
		}
		
		String world = secworld;
		
		if (plugin.getServer().getWorld(world) == null)
		{
		    PreciousStones.log.warning("Corrupt unbreakable: world error " + linecount);
		    continue;
		}
		
		String[] chunk = secchunk.split(",");
		
		if (chunk.length < 2 || !Helper.isInteger(chunk[0]) || !Helper.isInteger(chunk[1]))
		{
		    PreciousStones.log.warning("Corrupt unbreakable: chunk error " + linecount);
		    continue;
		}
		
		String[] vec = secvec.split(",");
		
		if (vec.length < 3 || !Helper.isInteger(vec[0]) || !Helper.isInteger(vec[1]) || !Helper.isInteger(vec[2]))
		{
		    PreciousStones.log.warning("Corrupt unbreakable: vec error " + linecount);
		    continue;
		}
		
		Block block = plugin.getServer().getWorld(world).getBlockAt(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]));
		
		if (!plugin.settings.isUnbreakableType(block))
		{
		    PreciousStones.log.warning("orphan unbreakable - skipping " + new Vec(block).toString());
		    plugin.um.setDirty();
		}
		else
		{
		    ChunkVec chunkvec = new ChunkVec(Integer.parseInt(chunk[0]), Integer.parseInt(chunk[1]), world);
		    Unbreakable unbreakable = new Unbreakable(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]), chunkvec, world, Material.getMaterial(type).getId(), owner);
		    
		    LinkedList<Unbreakable> c;
		    
		    if (loadedUnbreakables.containsKey(chunkvec))
			c = loadedUnbreakables.get(chunkvec);
		    else
			c = new LinkedList<Unbreakable>();
		    
		    if (!c.contains(unbreakable))
		    {
			c.add(unbreakable);
		    }
		    else
		    {
			PreciousStones.log.warning("Rejecting duplicate unbreakable: line " + linecount);
		    }
		    
		    loadedUnbreakables.put(chunkvec, c);
		}
	    }
	    
	    PreciousStones.log.info("[" + plugin.getDescription().getName() + "] loaded " + loadedUnbreakables.size() + " unbreakable blocks");
	}
	catch (FileNotFoundException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] Cannot read file " + unbreakable.getName());
	}
	
	plugin.um.importChunks(loadedUnbreakables);
    }
    
    /**
     * Load fields from disk
     */
    public void loadFields()
    {
	if (temp_forcefield.exists())
	{
	    backupForceFields();
	}
	
	HashMap<ChunkVec, LinkedList<Field>> loadedFields = new HashMap<ChunkVec, LinkedList<Field>>();
	int linecount = 0;
	
	Scanner scan;
	try
	{
	    scan = new Scanner(forcefield);
	    
	    while (scan.hasNextLine())
	    {
		linecount++;
		
		String line = scan.nextLine();
		
		if (!line.contains("["))
		    continue;
		
		String[] u = line.split("\\|");
		
		if (u.length < 7)
		{
		    PreciousStones.log.warning("Corrupt forcefield: seccount" + u.length + " line " + linecount);
		    continue;
		}
		
		String sectype = u[0];
		String secowner = u[1];
		String secallowed = u[2];
		String secworld = u[3];
		String secchunk = u[4];
		String secvec = u[5];
		String secname = u[6];
		String secsnitch = "";
		String seccloak = "";
		
		if (u.length > 7)
		{
		    secsnitch = u[7].replace("?", "§");
		}
		
		if (u.length > 8)
		{
		    seccloak = u[8];
		}
		
		sectype = Helper.removeChar(sectype, '[');
		secname = Helper.removeChar(secname, ']');
		secsnitch = Helper.removeChar(secsnitch, ']');
		seccloak = Helper.removeChar(seccloak, ']');
		
		if (sectype.trim().length() == 0 || Material.getMaterial(sectype) == null || !(plugin.settings.isFieldType(sectype) || plugin.settings.isCloakableType(sectype)))
		{
		    PreciousStones.log.warning("Corrupt forcefield : type error " + linecount);
		    continue;
		}
		
		String type = sectype;
		
		if (secowner.trim().length() == 0)
		{
		    PreciousStones.log.warning("Corrupt forcefield: type error " + linecount);
		    continue;
		}
		
		String owner = secowner;
		
		List<String> temp = Arrays.asList(secallowed.split(","));
		ArrayList<String> allowed = new ArrayList<String>();
		
		for (String t : temp)
		{
		    if (t.trim().length() > 0)
			allowed.add(t);
		}
		
		if (secworld.trim().length() == 0)
		{
		    PreciousStones.log.warning("Corrupt forcefield : world error " + linecount);
		    continue;
		}
		
		String world = secworld;
		
		if (plugin.getServer().getWorld(world) == null)
		{
		    PreciousStones.log.warning("Corrupt forcefield: world error " + linecount);
		    continue;
		}
		
		String[] chunk = secchunk.split(",");
		
		if (chunk.length < 2 || !Helper.isInteger(chunk[0]) || !Helper.isInteger(chunk[1]))
		{
		    PreciousStones.log.warning("Corrupt forcefield: chunk error " + linecount);
		    continue;
		}
		
		String[] vec = secvec.split(",");
		
		if (vec.length < 5 || !Helper.isInteger(vec[0]) || !Helper.isInteger(vec[1]) || !Helper.isInteger(vec[2]) || !Helper.isInteger(vec[3]) || !Helper.isInteger(vec[4]))
		{
		    PreciousStones.log.warning("Corrupt forcefield: vec error " + linecount);
		    continue;
		}
		
		String name = secname;
		
		List<String> tempsnitch = Arrays.asList(secsnitch.split(";"));
		ArrayList<SnitchEntry> snitch = new ArrayList<SnitchEntry>();
		
		for (String t : tempsnitch)
		{
		    if (!t.contains("#") || !t.contains("@"))
		    {
			continue;
		    }
		    
		    int a = t.indexOf("@");
		    int b = t.indexOf("#");
		    
		    String playername = t.substring(0, a);
		    String reason = t.substring(a + 1, b);
		    String details = t.substring(b + 1);
		    
		    snitch.add(new SnitchEntry(playername, reason, details));
		}
		
		CloakEntry cloakEntry = null;
		
		if (seccloak.length() > 0)
		{
		    if (seccloak.length() > 3 && (!seccloak.contains(";") || !seccloak.contains("<") || !seccloak.contains("/") || !seccloak.contains(">")))
		    {
			PreciousStones.log.warning("Corrupt forcefield: cloak1 error " + linecount);
			continue;
		    }
		    
		    byte data;
		    ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		    
		    String[] cloaksplit = seccloak.split(";");

		    if (Helper.isByte(cloaksplit[0]))
		    {
			data = Byte.parseByte(cloaksplit[0]);
			
			if (cloaksplit.length > 1)
			{
			    List<String> tempcloak = Arrays.asList(seccloak.split(","));
			    
			    for (String t : tempcloak)
			    {
				if (!t.contains("<") || !t.contains("/") || !t.contains(">"))
				{
				    continue;
				}
				
				int a = t.indexOf("<");
				int b = t.indexOf("/");
				int c = t.indexOf(">");
				
				String item = t.substring(0, a);
				String itemdata = t.substring(a + 1, b);
				String damage = t.substring(b + 1, c);
				String amount = t.substring(c + 1);
				
				if (Helper.isInteger(item) && Helper.isByte(itemdata) && Helper.isShort(damage) && Helper.isInteger(amount))
				{
				    stacks.add(new ItemStack(Integer.parseInt(item), Integer.parseInt(amount), Short.parseShort(damage), Byte.parseByte(itemdata)));
				}
				else
				{
				    stacks.add(new ItemStack(Material.AIR));
				}
			    }
			}
			
			cloakEntry = new CloakEntry(data, stacks);
		    }
		}
		
		Block block = plugin.getServer().getWorld(world).getBlockAt(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]));
		
		// if the field is a cloakable field yet the material type is neither a cloaked or clokable material (means its corrupted) then we orphan it
		// otherwise if the field is not a field type, then we orphan it as well.
			
		if (!plugin.settings.isFieldType(block) && !(plugin.settings.isCloakableType(Material.getMaterial(type).getId()) && (plugin.settings.isCloakType(block) || plugin.settings.isCloakableType(block))))
		{
		    PreciousStones.log.warning("orphan field - skipping " + new Vec(block).toString());
		    plugin.ffm.setDirty();
		    continue;
		}
		
		ChunkVec chunkvec = new ChunkVec(Integer.parseInt(chunk[0]), Integer.parseInt(chunk[1]), world);
		Field field = new Field(Integer.parseInt(vec[0]), Integer.parseInt(vec[1]), Integer.parseInt(vec[2]), Integer.parseInt(vec[3]), Integer.parseInt(vec[4]), chunkvec, world, Material.getMaterial(type).getId(), owner, allowed, name, snitch, cloakEntry);
		
		LinkedList<Field> c;
		
		if (loadedFields.containsKey(chunkvec))
		    c = loadedFields.get(chunkvec);
		else
		    c = new LinkedList<Field>();
		
		if (!c.contains(field))
		{
		    c.add(field);
		}
		else
		{
		    PreciousStones.log.warning("Rejecting duplicate forcefield: line " + linecount);
		}
		loadedFields.put(chunkvec, c);
	    }
	    
	    PreciousStones.log.info("[" + plugin.getDescription().getName() + "] loaded " + loadedFields.size() + " forcefield blocks");
	}
	catch (FileNotFoundException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] Cannot read file " + forcefield.getName());
	}
	
	plugin.ffm.importChunks(loadedFields);
    }
    
    /**
     * Save pstones to disk
     */
    public void save()
    {
	if (plugin.um.isDirty())
	{
	    saveUnbreakables();
	}
	
	if (plugin.ffm.isDirty())
	{
	    saveFields();
	}
	
	cleanOldFolder();
    }
    
    /**
     * Save pstones to disk
     */
    public void saveUnbreakables()
    {
	PreciousStones.log.info("[" + plugin.getDescription().getName() + "] saving " + unbreakable.getName());
	
	BufferedWriter bwriter = null;
	FileWriter fwriter = null;
	try
	{
	    if (!temp_unbreakable.exists())
	    {
		temp_unbreakable.createNewFile();
	    }
	    
	    fwriter = new FileWriter(temp_unbreakable);
	    bwriter = new BufferedWriter(fwriter);
	    
	    HashMap<ChunkVec, LinkedList<Unbreakable>> umList = new HashMap<ChunkVec, LinkedList<Unbreakable>>();
	    umList.putAll(plugin.um.getChunks());
	    
	    for (ChunkVec chunkvec : umList.keySet())
	    {
		LinkedList<Unbreakable> c = umList.get(chunkvec);
		
		for (Unbreakable unbreakable : c)
		{
		    StringBuilder builder = new StringBuilder();
		    builder.append("[");
		    builder.append(unbreakable.getType());
		    builder.append("|");
		    builder.append(unbreakable.getOwner());
		    builder.append("|");
		    builder.append(unbreakable.getWorld());
		    builder.append("|");
		    builder.append(unbreakable.getChunkVec().getX());
		    builder.append(",");
		    builder.append(unbreakable.getChunkVec().getZ());
		    builder.append("|");
		    builder.append(unbreakable.getX());
		    builder.append(",");
		    builder.append(unbreakable.getY());
		    builder.append(",");
		    builder.append(unbreakable.getZ());
		    builder.append("]");
		    bwriter.write(builder.toString());
		    bwriter.newLine();
		}
	    }
	    
	    bwriter.flush();
	}
	catch (IOException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] IO Exception with file " + unbreakable.getName());
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
		PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] IO Exception with file " + unbreakable.getName() + " (on close)");
	    }
	    
	    backupUnbreakable();
	    plugin.um.resetDirty();
	}
    }
    
    /**
     * Save fields to disk
     */
    public void saveFields()
    {
	PreciousStones.log.info("[" + plugin.getDescription().getName() + "] saving " + forcefield.getName());
	
	BufferedWriter bwriter = null;
	FileWriter fwriter = null;
	try
	{
	    if (!temp_forcefield.exists())
	    {
		temp_forcefield.createNewFile();
	    }
	    
	    fwriter = new FileWriter(temp_forcefield);
	    bwriter = new BufferedWriter(fwriter);
	    
	    HashMap<ChunkVec, LinkedList<Field>> ffmList = new HashMap<ChunkVec, LinkedList<Field>>();
	    ffmList.putAll(plugin.ffm.getChunks());
	    
	    for (ChunkVec chunkvec : ffmList.keySet())
	    {
		LinkedList<Field> c = ffmList.get(chunkvec);
		
		for (Field field : c)
		{
		    ArrayList<String> allowed = field.getAllowed();
		    Collections.sort(allowed);
		    
		    StringBuilder builder = new StringBuilder();
		    builder.append("[");
		    builder.append(field.getType());
		    builder.append("|");
		    builder.append(field.getOwner());
		    builder.append("|");
		    for (int i = 0; i < allowed.size(); i++)
		    {
			builder.append(allowed.get(i));
			
			if (i < allowed.size() - 1)
			    builder.append(",");
		    }
		    builder.append("|");
		    builder.append(field.getWorld());
		    builder.append("|");
		    builder.append(field.getChunkVec().getX());
		    builder.append(",");
		    builder.append(field.getChunkVec().getZ());
		    builder.append("|");
		    builder.append(field.getX());
		    builder.append(",");
		    builder.append(field.getY());
		    builder.append(",");
		    builder.append(field.getZ());
		    builder.append(",");
		    builder.append(field.getRadius());
		    builder.append(",");
		    builder.append(field.getHeight());
		    builder.append("|");
		    builder.append(field.getStoredName());
		    builder.append("|");
		    builder.append(field.getSnitchListString());
		    builder.append("|");
		    builder.append(field.getCloakString());
		    builder.append("]");
		    bwriter.write(builder.toString());
		    bwriter.newLine();
		}
	    }
	    
	    bwriter.flush();
	}
	catch (IOException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] IO Exception with file " + forcefield.getName());
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
		PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] IO Exception with file " + forcefield.getName() + " (on close)");
	    }
	    
	    backupForceFields();
	    plugin.ffm.resetDirty();
	}
    }
    
    private void createUnbreakableFile()
    {
	try
	{
	    if (!unbreakable.exists())
		unbreakable.createNewFile();
	}
	catch (IOException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] Cannot create file " + unbreakable.getName());
	    e.printStackTrace();
	}
    }
    
    private void createForceFieldFile()
    {
	try
	{
	    if (!forcefield.exists())
		forcefield.createNewFile();
	}
	catch (IOException e)
	{
	    PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] Cannot create file " + forcefield.getName());
	    e.printStackTrace();
	}
    }
    
    private void createOldDirectory()
    {
	if (old.exists())
	{
	    return;
	}
	
	boolean success = old.mkdir();
	
	if (!success)
	{
	    PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] Could create plugins/PreciousStones/old directory");
	}
    }
    
    private void backupUnbreakable()
    {
	createOldDirectory();
	
	try
	{
	    copy(unbreakable, new File(old, unbreakable.getName() + "." + System.currentTimeMillis()));
	}
	catch (Exception ex)
	{
	    PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] Could not backup the current " + unbreakable.getName());
	}
	finally
	{
	    unbreakable.delete();
	}
	
	try
	{
	    copy(temp_unbreakable, new File(plugin.getDataFolder(), unbreakable.getName()));
	}
	catch (Exception ex)
	{
	    PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] Could not rename tmp file to  final " + unbreakable.getName());
	}
	finally
	{
	    temp_unbreakable.delete();
	}
    }
    
    private void backupForceFields()
    {
	createOldDirectory();
	
	try
	{
	    copy(forcefield, new File(old, forcefield.getName() + "." + System.currentTimeMillis()));
	}
	catch (Exception ex)
	{
	    PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] Could not backup the current " + forcefield.getName());
	}
	finally
	{
	    forcefield.delete();
	}
	
	try
	{
	    copy(temp_forcefield, new File(plugin.getDataFolder(), forcefield.getName()));
	}
	catch (Exception ex)
	{
	    PreciousStones.log.severe("[" + plugin.getDescription().getName() + "] Could not rename tmp file to  final " + forcefield.getName());
	}
	finally
	{
	    temp_forcefield.delete();
	}
    }
    
    private void cleanOldFolder()
    {
	if (old.exists())
	{
	    File[] listFiles = old.listFiles();
	    long purgeTime = System.currentTimeMillis() - (plugin.settings.purgeDays * 24 * 60 * 60 * 1000);
	    for (File listFile : listFiles)
	    {
		if (listFile.lastModified() < purgeTime)
		{
		    listFile.delete();
		}
	    }
	}
    }
    
    private void copy(InputStream src, File dst) throws IOException
    {
	InputStream in = src;
	OutputStream out = new FileOutputStream(dst);
	
	byte[] buf = new byte[1024];
	int len;
	while ((len = in.read(buf)) > 0)
	{
	    out.write(buf, 0, len);
	}
	out.close();
    }
    
    private void copy(File src, File dst) throws IOException
    {
	InputStream in = new FileInputStream(src);
	copy(in, dst);
	in.close();
    }
    
    public void startScheduler()
    {
	plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
	{
	    public void run()
	    {
		save();
	    }
	}, 0, 20 * 60 * plugin.settings.saveFrequency);
    }
}
