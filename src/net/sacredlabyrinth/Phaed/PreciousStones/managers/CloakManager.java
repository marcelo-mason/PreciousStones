package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.inventory.Inventory;

import net.sacredlabyrinth.Phaed.PreciousStones.CloakEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.PSItemStack;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

/**
 * Manages cloaking
 * @author phaed
 */
public class CloakManager
{
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public CloakManager(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Marks the field as a cloak type field (by adding an empty cloak entry inside of it)
     * @param field the field to cloak
     */
    public void initiate(Field field)
    {
        Block block = plugin.ffm.getBlock(field);
        field.addCloakEntry(new CloakEntry(block.getData()));
        field.setDirty(true);
    }

    /**
     *
     */
    public void cloakAll()
    {

    }

    /**
     * Cloaks the field into a block type from its surroundings
     * @param field the field to cloak
     */
    public void cloak(Field field)
    {
        Block block = plugin.ffm.getBlock(field);

        if (plugin.settings.isCloakableType(block))
        {
            HashSet<String> inhabitants = plugin.em.getInhabitants(field);

            if (inhabitants.isEmpty())
            {
                CloakEntry ce = field.getCloakEntry();

                if (ce == null)
                {
                    return;
                }

                byte olddata = ce.getDataByte();
                byte newdata = block.getData();

                if (block.getState() instanceof ContainerBlock)
                {
                    ContainerBlock container = (ContainerBlock) block.getState();
                    Inventory inv = container.getInventory();

                    ce.importStacks(inv.getContents());

                    inv.clear();
                    field.setDirty(true);
                }

                if (olddata != newdata)
                {
                    ce.setDataByte(newdata);
                    field.setDirty(true);
                }

                block.setType(getCloakMaterial(block));
            }
        }
    }

    /**
     * Turns the field back into its original block type
     * @param field the field to decloak
     */
    public void decloak(Field field)
    {
        Block block = plugin.ffm.getBlock(field);

        if (plugin.settings.isCloakType(block))
        {
            CloakEntry ce = field.getCloakEntry();

            if (ce == null)
            {
                return;
            }

            byte data = ce.getDataByte();

            block.setData(data);
            block.setType(Material.getMaterial(field.getTypeId()));

            if (block.getState() instanceof ContainerBlock)
            {
                ContainerBlock container = (ContainerBlock) block.getState();
                Inventory inv = container.getInventory();

                if (ce.getStacks() != null && ce.getStacks().size() == inv.getSize())
                {
                    inv.setContents(ce.exportStacks());
                    deleteStacks(ce);
                }
            }
        }
    }

    /**
     * Delete a cloaked container's items stacks from the entry and database
     * @param ce cloakentry containing the stacks to delete
     */
    public void deleteStacks(CloakEntry ce)
    {
        List<PSItemStack> stacks = ce.getStacks();

        try
        {
            plugin.getDatabase().delete(stacks);
        }
        catch (Exception ex)
        {
        }

        ce.clearStacks();
    }

    /**
     * Returns an approved material from the blocks surroundings
     * @param block the field's block
     * @return the material this block should be cloaked as
     */
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