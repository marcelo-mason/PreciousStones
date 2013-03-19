package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import com.p000ison.dev.simpleclans2.api.SCCore;
import com.p000ison.dev.simpleclans2.api.clan.Clan;
import com.p000ison.dev.simpleclans2.api.clanplayer.ClanPlayer;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Set;

/**
 * @author phaed
 */
public final class SimpleClansManager
{
    private PreciousStones plugin;
    private SCCore core = null;
    private SimpleClans simpleClans;

    /**
     *
     */
    public SimpleClansManager()
    {
        plugin = PreciousStones.getInstance();
        getSimpleClans();
        hookSimpleClans();
    }

    private void getSimpleClans()
    {
        if (simpleClans == null)
        {
            Plugin test = Bukkit.getServer().getPluginManager().getPlugin("SimpleClans");

            if (test != null)
            {
                this.simpleClans = (SimpleClans) test;
            }
        }
    }

    private boolean hookSimpleClans()
    {
        try {
            Class.forName("com.p000ison.dev.simpleclans2.api.SCCore");
        } catch(ClassNotFoundException e) {
            return false;
        }

        for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins())
        {
            if (plugin instanceof SCCore)
            {
                this.core = (SCCore) plugin;
                return true;
            }
        }

        return false;
    }

    /**
     * Whether SimpleClans was loaded
     *
     * @return
     */
    public boolean hasSimpleClans()
    {
        if (plugin.getSettingsManager().isDisableSimpleClanHook())
        {
            return false;
        }

        return simpleClans != null && core == null;
    }

    /**
     * Whether SimpleClans was loaded
     *
     * @return
     */
    public boolean hasSimpleClans2()
    {
        if (plugin.getSettingsManager().isDisableSimpleClanHook())
        {
            return false;
        }

        return core != null;
    }

    /**
     * Announce to players clan
     *
     * @param playerName
     * @param message
     */
    public void clanAnnounce(String playerName, String message)
    {
        if (hasSimpleClans())
        {
            net.sacredlabyrinth.phaed.simpleclans.ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(playerName);

            if (cp != null)
            {
                cp.getClan().clanAnnounce("PreciousStones", message);
            }
        }
        else if (hasSimpleClans2())
        {
            ClanPlayer cp = core.getClanPlayerManager().getClanPlayer(playerName);

            if (cp != null)
            {
                cp.getClan().announce(message);
            }
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
        if (hasSimpleClans())
        {
            net.sacredlabyrinth.phaed.simpleclans.ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(player);

            if (cp != null)
            {
                cp.getClan().addBb("[PreciousStones]", message);
            }
        }
        else if (hasSimpleClans2())
        {
            ClanPlayer cp = core.getClanPlayerManager().getClanPlayer(player);

            if (cp != null)
            {
                cp.getClan().addBBMessage(message);
            }
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
        if (hasSimpleClans())
        {
            net.sacredlabyrinth.phaed.simpleclans.ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(playerName);

            if (cp != null)
            {
                if (cp.getClan().isAnyOnline())
                {
                    return true;
                }
            }
        }
        else if (hasSimpleClans2())
        {
            ClanPlayer cp = core.getClanPlayerManager().getClanPlayer(playerName);

            if (cp != null)
            {
                Set<ClanPlayer> members = cp.getClan().getAllMembers();

                for (ClanPlayer member : members)
                {
                    Player player = Bukkit.getServer().getPlayerExact(member.getName());

                    if (player != null)
                    {
                        return true;
                    }
                }
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
        if (hasSimpleClans())
        {
            net.sacredlabyrinth.phaed.simpleclans.ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(offenderName);
            net.sacredlabyrinth.phaed.simpleclans.ClanPlayer cpOwner = simpleClans.getClanManager().getClanPlayer(field.getOwner());

            if (cp != null && cpOwner != null)
            {
                List<net.sacredlabyrinth.phaed.simpleclans.Clan> warringClans = cp.getClan().getWarringClans();

                String ownerClan = cpOwner.getTag();

                for (net.sacredlabyrinth.phaed.simpleclans.Clan warring : warringClans)
                {
                    if (ownerClan.equals(warring.getTag()))
                    {
                        return true;
                    }
                }
            }
        }
        else if (hasSimpleClans2())
        {
            ClanPlayer cp = core.getClanPlayerManager().getClanPlayer(offenderName);
            ClanPlayer cpOwner = core.getClanPlayerManager().getClanPlayer(field.getOwner());

            if (cp != null && cpOwner != null)
            {
                Set<Clan> warringClans = cp.getClan().getWarringClans();

                String ownerClan = cpOwner.getClan().getTag();

                for (Clan warring : warringClans)
                {
                    if (ownerClan.equals(warring.getTag()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * @param owner
     * @param playerName
     * @return
     */
    public boolean isAllyOwner(String owner, String playerName)
    {
        if (hasSimpleClans())
        {
            Player player = plugin.getServer().getPlayerExact(playerName);

            if (player != null)
            {
                net.sacredlabyrinth.phaed.simpleclans.ClanPlayer cpOwner = simpleClans.getClanManager().getClanPlayer(owner);

                if (cpOwner != null)
                {
                    return cpOwner.isAlly(player);
                }
            }
        }
        else if (hasSimpleClans2())
        {
            Player player = plugin.getServer().getPlayerExact(playerName);

            if (player != null)
            {
                ClanPlayer cpOwner = core.getClanPlayerManager().getClanPlayer(owner);
                ClanPlayer cpPlayer = core.getClanPlayerManager().getClanPlayer(owner);

                if (cpOwner != null && cpPlayer != null)
                {
                    return cpOwner.getClan().isAlly(cpPlayer.getClan());
                }
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
        if (hasSimpleClans())
        {
            net.sacredlabyrinth.phaed.simpleclans.ClanPlayer cp1 = simpleClans.getClanManager().getClanPlayer(playerOne);
            net.sacredlabyrinth.phaed.simpleclans.ClanPlayer cp2 = simpleClans.getClanManager().getClanPlayer(playerTwo);

            if (cp1 != null && cp2 != null)
            {
                if (cp1.getClan().equals(cp2.getClan()))
                {
                    return true;
                }
            }
        }
        else if (hasSimpleClans2())
        {
            ClanPlayer cp1 = core.getClanPlayerManager().getClanPlayer(playerOne);
            ClanPlayer cp2 = core.getClanPlayerManager().getClanPlayer(playerTwo);

            if (cp1 != null && cp2 != null)
            {
                if (cp1.getClan().equals(cp2.getClan()))
                {
                    return true;
                }
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
        if (hasSimpleClans())
        {
            net.sacredlabyrinth.phaed.simpleclans.ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(playerName);

            if (cp != null)
            {
                if (cp.getTag().equals(clanName))
                {
                    return true;
                }
            }
        }
        else if (hasSimpleClans2())
        {
            ClanPlayer cp = core.getClanPlayerManager().getClanPlayer(playerName);

            if (cp != null)
            {
                if (cp.getClan().getCleanTag().equals(clanName))
                {
                    return true;
                }
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
        if (hasSimpleClans())
        {
            net.sacredlabyrinth.phaed.simpleclans.ClanPlayer cp = simpleClans.getClanManager().getClanPlayer(playerName);

            if (cp != null)
            {
                return cp.getTag();
            }
        }
        else if (hasSimpleClans2())
        {
            ClanPlayer cp = core.getClanPlayerManager().getClanPlayer(playerName);

            if (cp != null)
            {
                return cp.getClan().getCleanTag();
            }
        }

        return null;
    }
}
