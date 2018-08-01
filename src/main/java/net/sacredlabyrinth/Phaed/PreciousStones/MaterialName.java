package net.sacredlabyrinth.Phaed.PreciousStones;

import org.bukkit.Material;

public class MaterialName {
    
    private MaterialName() {
        
    }

    public static String getIDName(Material mate) {
        return mate.name().toLowerCase();
    }

    public static Material getBlockMaterial(String IDName) {
        if (IDName.startsWith("minecraft:")) IDName = IDName.substring(10);
        try {
            return Material.valueOf(IDName.toUpperCase());
        } catch (Exception ex) {
            PreciousStones.getLog().warning("Invalid material name: " + IDName);
        }
        return null;
    }

    public static Material getItemMaterial(String IDName) {
        if (IDName.startsWith("minecraft:")) IDName = IDName.substring(10);
        try {
            return Material.valueOf(IDName.toUpperCase());
        } catch (Exception ex) {
            PreciousStones.getLog().warning("Invalid material name: " + IDName);
        }
        return null;
    }
}