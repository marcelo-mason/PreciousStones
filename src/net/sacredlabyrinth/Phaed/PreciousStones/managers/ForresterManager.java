package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.sacredlabyrinth.Phaed.PreciousStones.ForresterEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author phaed
 */
public final class ForresterManager
{
    private PreciousStones plugin;
    private HashSet<ForresterEntry> forresters = new HashSet<ForresterEntry>();

    /**
     *
     * @param plugin
     */
    public ForresterManager(PreciousStones plugin)
    {
        this.plugin = plugin;
        scheduler();
    }

    /**
     * Add forrester
     * @param field
     */
    public void add(Field field, String playerName)
    {
        forresters.add(new ForresterEntry(field, playerName));
    }

    /**
     * Remove forrester
     * @param field
     */
    public void remove(Field field)
    {
        forresters.remove(new ForresterEntry(field, ""));
    }

    /**
     *
     */
    public void scheduler()
    {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                List<Field> deletion = new ArrayList<Field>();

                for (ForresterEntry fe : forresters)
                {
                    Field field = fe.getField();
                    FieldSettings fs = plugin.settings.getFieldSettings(field);

                    if (fs == null)
                    {
                        plugin.ffm.queueRelease(field);
                        continue;
                    }

                    int treeCount = ((int) (Math.random() * 4)) + (fs.forresterTrees - 2);

                    World world = plugin.getServer().getWorld(field.getWorld());
                    Player player = plugin.getServer().getPlayer(fe.getPlayerName());

                    if (world == null || player == null)
                    {
                        continue;
                    }

                    int minx = field.getX() - field.getRadius();
                    int maxx = field.getX() + field.getRadius();
                    int minz = field.getZ() - field.getRadius();
                    int maxz = field.getZ() + field.getRadius();
                    int miny = field.getY() - (int) Math.floor(((double) field.getHeight()) / 2);
                    int maxy = field.getY() + (int) Math.ceil(((double) field.getHeight()) / 2);

                    Random r = new Random();

                    int xr = r.nextInt(maxx - minx) + minx;
                    int zr = r.nextInt(maxz - minz) + minz;

                    for (int y = maxy; y > miny; y--)
                    {
                        int type = world.getBlockTypeIdAt(xr, y, zr);

                        if (type != 0)
                        {
                            if (type == 2 || type == 3)
                            {
                                Block block = world.getBlockAt(xr, y + 1, zr);

                                Field f = plugin.ffm.isPlaceProtected(block, player);

                                if (f != null)
                                {
                                    if (!plugin.pm.hasPermission(player, "preciousstones.bypass.place"))
                                    {
                                        break;
                                    }
                                }

                                if (block.getTypeId() == 31 || block.getTypeId() == 32 || block.getTypeId() == 37 || block.getTypeId() == 38)
                                {
                                    block.setType(Material.AIR);
                                }

                                TreeType tree = getTree();
                                world.generateTree(block.getLocation(), tree);
                            }

                            break;
                        }
                    }

                    fe.addCount();

                    if (fe.getCount() > treeCount)
                    {
                        deletion.add(field);
                        break;
                    }
                }

                for (Field field : deletion)
                {
                    World world = plugin.getServer().getWorld(field.getWorld());

                    if (world != null)
                    {
                        Block block = world.getBlockAt(field.getX(), field.getY(), field.getZ());
                        block.setType(Material.AIR);
                    }

                    forresters.remove(field);
                    plugin.ffm.queueRelease(field);
                }

                plugin.ffm.flush();
            }
        }, 20L * (long) plugin.settings.forresterInterval, 20L * (long) plugin.settings.forresterInterval);
    }

    /**
     *
     * @return
     */
    public static TreeType getTree()
    {
        Random r = new Random();

        switch (r.nextInt(5))
        {
            case 0:
                return TreeType.TREE;
            case 1:
                return TreeType.BIG_TREE;
            case 2:
                return TreeType.REDWOOD;
            case 3:
                return TreeType.TALL_REDWOOD;
            case 4:
                return TreeType.BIRCH;
        }

        return TreeType.TREE;
    }
}
