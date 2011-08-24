package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
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
        startST();
    }

    /**
     * Whether SimpleClans was loaded
     * @return
     */
    public boolean hasSimpleClans()
    {
        return simpleClans != null;
    }

    /**
     * Announce to players clan
     * @param playerName
     * @param message
     */
    public void clanAnnounce(String playerName, String message)
    {
        if (simpleClans == null)
        {
            return;
        }

        Clan clan = simpleClans.getClanManager().getClanByPlayerName(playerName);

        if (clan != null)
        {
            simpleClans.getClanManager().clanAnnounce("PreciousStones", clan, message);
        }
    }

    /**
     * Announce to players that a rival is in their base
     * @param field
     * @param rivalName
     */
    public void bypassAnnounce(Field field, String rivalName)
    {
        if (simpleClans == null)
        {
            return;
        }

        Clan clan = simpleClans.getClanManager().getClanByPlayerName(field.getOwner());
        Clan rivalClan = simpleClans.getClanManager().getClanByPlayerName(rivalName);

        if (clan != null && rivalClan != null)
        {
            simpleClans.getClanManager().audioAnnounce("PreciousStones", clan, Helper.capitalize(rivalName) + " of rival clan " + Helper.stripColors(rivalClan.getColorTag()) + " has entered one of " + Helper.posessive(Helper.stripColors(clan.getColorTag())) + " bases [" + field.getX() + " " + field.getY() + " " + field.getZ() + " " + field.getWorld() + "]");
        }
    }

    /**
     * Adds a message to the player's clan's BB
     * @param player
     * @param message
     */
    public void addBB(Player player, String message)
    {
        if (simpleClans == null)
        {
            return;
        }

        Clan clan = simpleClans.getClanManager().getClan(player);

        if (clan != null)
        {
            simpleClans.getClanManager().addBb("[PreciousStones]", clan, message);
        }
    }

    /**
     * Check whether any of a player's clan members are online
     * @param playerName
     * @return
     */
    public boolean isAnyOnline(String playerName)
    {
        if (simpleClans == null)
        {
            return false;
        }

        Clan clan = simpleClans.getClanManager().getClanByPlayerName(playerName);

        if (clan != null)
        {
            if (simpleClans.getClanManager().isAnyOnline(clan))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if two players are clanmates
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

        Clan clan1 = simpleClans.getClanManager().getClanByPlayerName(playerOne);
        Clan clan2 = simpleClans.getClanManager().getClanByPlayerName(playerTwo);

        if (clan1 != null && clan2 != null)
        {
            if (clan1.equals(clan2))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a player is in a clan
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

        Clan clan = simpleClans.getClanManager().getClanByPlayerName(playerName);

        if (clan != null)
        {
            if (clan.getTag().equals(clanName))
            {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param playerName
     * @return
     */
    public String getClan(String playerName)
    {
        if (simpleClans == null)
        {
            return null;
        }

        Clan clan = simpleClans.getClanManager().getClanByPlayerName(playerName);

        if (clan != null)
        {
            return clan.getTag();
        }

        return null;
    }

    private void startST()
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
