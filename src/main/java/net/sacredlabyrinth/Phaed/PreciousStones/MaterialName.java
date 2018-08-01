package net.sacredlabyrinth.Phaed.PreciousStones;

import org.bukkit.Material;

import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;

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
            try {
                int materialId = Integer.parseInt(IDName);
                Material converted = Helper.getMaterial(materialId);
                PreciousStones.getLog().warning("Found material id '" + materialId + "' in config. Assuming this means " + converted.name() + " but you should update your configs.");
                return converted;
            } catch (Exception notint) {
                PreciousStones.getLog().warning("Invalid material name: " + IDName);
            }
        }
        return null;
    }
}