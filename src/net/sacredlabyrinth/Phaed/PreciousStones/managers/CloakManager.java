package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.inventory.Inventory;

import net.sacredlabyrinth.Phaed.PreciousStones.CloakEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

public class CloakManager
{
    private PreciousStones plugin;
    
    public CloakManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    public void initiate(Field field)
    {
	Block block = plugin.ffm.getBlock(field);
	
	CloakEntry ce = new CloakEntry(block.getData());
	
	if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.FURNACE) || block.getType().equals(Material.BURNING_FURNACE))
	{
	    ContainerBlock container = (ContainerBlock) block.getState();
	    Inventory inv = container.getInventory();
	    
	    ce.setStacks(inv.getContents());
	}
	
	field.setCloakEntry(ce);
	plugin.ffm.setDirty();
    }
    
    public void cloak(Field field)
    {
	Block block = plugin.ffm.getBlock(field);
	
	if (plugin.settings.isCloakableType(block))
	{
	    HashSet<String> inhabitants = plugin.em.getInhabitants(field);
	    
	    if (inhabitants.size() == 0)
	    {
		CloakEntry ce = field.getCloakEntry();
		ce.setData(block.getData());
		
		if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.FURNACE) || block.getType().equals(Material.BURNING_FURNACE))
		{
		    ContainerBlock container = (ContainerBlock) block.getState();
		    Inventory inv = container.getInventory();
		    
		    ce.setStacks(inv.getContents());
		    inv.clear();
		}
		block.setType(getCloakMaterial(block));
		plugin.ffm.setDirty();
	    }
	}
    }
    
    public void decloak(Field field)
    {
	Block block = plugin.ffm.getBlock(field);
	
	if (plugin.settings.isCloakType(block))
	{
	    CloakEntry ce = field.getCloakEntry();
	    
	    block.setType(Material.getMaterial(field.getTypeId()));
	    block.setData(ce.getData());
	    
	    if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.FURNACE) || block.getType().equals(Material.BURNING_FURNACE))
	    {
		ContainerBlock container = (ContainerBlock) block.getState();
		Inventory inv = container.getInventory();
		
		if (ce.getStacks() != null && ce.getStacks().length > 0)
		{
		    inv.setContents(ce.getStacks());
		}
	    }
	    plugin.ffm.setDirty();
	}
    }
    
    public Material getCloakMaterial(Block block)
    {
	Material mat = Material.STONE;
	
	for (int x = -1; x <= 1; x++)
	{
	    for (int y = -1; y <= 1; y++)
	    {
		for (int z = -1; z <= 1; z++)
		{
		    if (x == 0 && y == 0 && z == 0)
		    {
			continue;
		    }
		    
		    Block adjacent = block.getRelative(x, y, z);
		    
		    if (plugin.settings.isCloakType(adjacent))
		    {
			mat = adjacent.getType();
			break;
		    }
		}
	    }
	}
	
	return mat;
    }
}
