package net.sacredlabyrinth.Phaed.PreciousStones;

import java.util.ArrayList;
import org.bukkit.inventory.ItemStack;

public class CloakEntry
{
    private byte data;
    private ItemStack[] stacks;
    
    public CloakEntry(byte data)
    {
	this.data = data;
    }
    
    public CloakEntry(byte data, ArrayList<ItemStack> stacks)
    {
	this.data = data;
	
	this.stacks = new ItemStack[stacks.size()];
	this.stacks = stacks.toArray(this.stacks);
    }
    
    public byte getData()
    {
	return data;
    }
    
    public ItemStack[] getStacks()
    {
	return stacks;
    }
    
    public void setData(byte data)
    {
	this.data = data;
    }
    
    public void setStacks(ItemStack[] stacks)
    {
	this.stacks = stacks;
    }
    
    @Override
    public String toString()
    {
	String out = data + "";
	
	if (stacks != null)
	{
	    if (stacks.length > 0)
	    {
		out += ";";
	    }
	    
	    for (ItemStack stack : stacks)
	    {
		if(stack == null)
		{
		    out += "0<0/0>0,";
		}
		
		out += stack.getTypeId() + "<" + (stack.getData() == null ? "0" : stack.getData().getData()) + "/" + stack.getDurability() + ">" + stack.getAmount() + ",";
	    }
	}
	
	return out;
    }
}
