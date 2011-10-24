package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * @author phaed
 */
public final class SimpleClansManager
{
    private PreciousStones plugin;
    private SimpleClans simpleClans;

    /**
     *
     */
    public SimpleClansManager()
    {
        plugin = PreciousStones.getInstance();
        getSimpleClans();
    }

    /**
     * Whether SimpleClans was loaded
     *
     * @return
     */
    public boolean hasSimpleClans()
    {
        return simpleClans != null;
    }

    /**
     * Announce to players clan
     *
     * @param playerName
     * @param message
     */
    public void clanAnnounce(String playerName, String message)
    {
        if (simpleClans == null)
        {
            return;
        }

        ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(playerName);

        if (cp != null)
        {
            cp.getClan().clanAnnounce("PreciousStones", message);
        }
    }

    /**
     * Announce to players that a rival is in their base
     *
     * @param field
     * @param rivalName
     */
    public void bypassAnnounce(Field field, String rivalName)
    {
        if (simpleClans == null)
        {
            return;
        }

        ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(field.getOwner());
        ClanPlayer rivalCp = simpleClans.getClanManager().getClanPlayer(rivalName);

        if (cp != null && rivalCp != null)
        {
            cp.getClan().audioAnnounce("PreciousStones", Helper.capitalize(rivalName) + " of rival clan " + Helper.stripColors(rivalCp.getClan().getColorTag()) + " has entered one of " + Helper.posessive(Helper.stripColors(cp.getClan().getColorTag())) + " bases [" + field.getX() + " " + field.getY() + " " + field.getZ() + " " + field.getWorld() + "]");
        }
    }

    /**
     * Adds a message to the player's clan's BB
     *
     * @param player
     * @param message
     */
    public void addBB(Player player, String message)
    {
        if (simpleClans == null)
        {
            return;
        }

        ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(player);

        if (cp != null)
        {
            cp.getClan().addBb("[PreciousStones]", message);
        }
    }

    /**
     * Check whether any of a player's clan members are online
     *
     * @param playerName
     * @return
     */
    public boolean isAnyOnline(String playerName)
    {
        if (simpleClans == null)
        {
            return false;
        }

        ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(playerName);

        if (cp != null)
        {
            if (cp.getClan().isAnyOnline())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if
     *
     * @param field
     * @param offenderName
     * @return
     */
    public boolean inWar(Field field, String offenderName)
    {
        if (simpleClans == null)
        {
            return false;
        }

        ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(offenderName);
        ClanPlayer cpOwner = simpleClans.getClanManager().getClanPlayer(field.getOwner());

        if (cp != null)
        {
            List<Clan> warringClans = cp.getClan().getWarringClans();

            String ownerClan = "";

            if (cpOwner != null)
            {
                ownerClan = cpOwner.getTag();
            }

            for (Clan warring : warringClans)
            {
                if (ownerClan.equals(warring))
                {
                    return true;
                }

                if (field.isAllowed(warring.getTag()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param field
     * @param player
     * @return
     */
    public boolean isAllyOwner(String owner, String playerName)
    {
        Player player = plugin.getServer().getPlayerExact(playerName);

        if (player != null)
        {
            ClanPlayer cpOwner = simpleClans.getClanManager().getClanPlayer(owner);

            if (cpOwner != null)
            {
                return cpOwner.isAlly(player);
            }
        }

        return false;
    }

    /**
     * Check if two players are clanmates
     *
     * @param playerOne
     * @param playerTwo
     * @return
     */
    public boolean isClanMate(String playerOne, String playerTwo)
    {
        if (simpleClans == null)
        {
            return false;
        }

        ClanPlayer cp1 = simpleClans.getClanManager().getClanPlayer(playerOne);
        ClanPlayer cp2 = simpleClans.getClanManager().getClanPlayer(playerTwo);

        if (cp1 != null && cp2 != null)
        {
            if (cp1.getClan().equals(cp2.getClan()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a player is in a clan
     *
     * @param playerName
     * @param clanName
     * @return
     */
    public boolean isInClan(String playerName, String clanName)
    {
        if (simpleClans == null)
        {
            return false;
        }

        ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(playerName);

        if (cp != null)
        {
            if (cp.getTag().equals(clanName))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @param playerName
     * @return
     */
    public String getClan(String playerName)
    {
        if (simpleClans == null)
        {
            return null;
        }

        ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(playerName);

        if (cp != null)
        {
            return cp.getTag();
        }

        return null;
    }

    private void getSimpleClans()
    {
        if (simpleClans == null)
        {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("SimpleClans");

            if (test != null)
            {
                this.simpleClans = (SimpleClans) test;
            }
        }
    }
}
