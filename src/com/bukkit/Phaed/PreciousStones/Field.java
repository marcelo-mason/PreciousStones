package com.bukkit.Phaed.PreciousStones;

import java.util.ArrayList;

public class Field
{
    private final Vector vec;
    private final String owner;
    private final long world;
    private ArrayList<String> allowed = new ArrayList<String>();
    
    public Field(Vector vec, String owner, ArrayList<String> allowed, long world)
    {
	this.vec = vec;
	this.owner = owner;
	this.allowed = allowed;
	this.world = world;
    }
    
    public Vector getVector()
    {
	return vec;
    }
    
    public String getOwner()
    {
	return owner;
    }

    public ArrayList<String> getAllowed()
    {
	return allowed;
    }    
    
    public ArrayList<String> getAllAllowed()
    {
	ArrayList<String> all = new ArrayList<String>();
	all.add(owner);
	all.addAll(allowed);
	return all;
    }  
    
    public long getWorldId()
    {
	return world;
    }

    public boolean isOwner(String playerName)
    {
	return playerName.equals(owner);
    }

    public boolean isAllowed(String playerName)
    {
	return playerName.equals(owner) || allowed.contains(playerName);
    }
    
    public boolean addAllowed(String playerName)
    {
	if(allowed.contains(playerName))
	    return false;
	
	allowed.add(playerName);
	return true;
    }
    
    public boolean removeAllowed(String playerName)
    {
	if(allowed.contains(playerName))
	    return false;
	
	allowed.remove(playerName);
	return true;
    }

    @Override
    public String toString()
    {
	return "owner:" + owner + " world:" + world + " allowed:" + allowed.toString() + " " + vec.toString();
    }
}
