package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.phaed.simpleteams.SimpleTeams;
import net.sacredlabyrinth.phaed.simpleteams.Team;
import net.sacredlabyrinth.phaed.simpleteams.TeamPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author phaed
 */
public final class SimpleTeamsManager
{
    private PreciousStones plugin;
    private SimpleTeams st;

    /**
     *
     * @param plugin
     */
    public SimpleTeamsManager(PreciousStones plugin)
    {
        this.plugin = plugin;
        startST();
    }

    /**
     * Adds a message to the player's team's BB
     * @param player
     * @param message
     */
    public void addBB(Player player, String message)
    {
        if (st == null)
        {
            return;
        }

        Team team = st.tm.getTeam(player);

        if (team != null)
        {
            st.tm.addBb("[PreciousStones]", team, message);
        }
    }

    /**
     * Check if two players are rivals
     * @param playerOne
     * @param playerTwo
     * @return
     */
    public boolean isRival(String playerOne, String playerTwo)
    {
        if (st == null)
        {
            return false;
        }

        Team team1 = st.tm.getTeamByPlayerName(playerOne);
        Team team2 = st.tm.getTeamByPlayerName(playerTwo);

        if (team1 != null && team2 != null)
        {
            if (team1.isRival(team2.getTag()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check whether any of a player's team members are online
     * @param playerName
     * @return
     */
    public boolean isAnyOnline(String playerName)
    {
        if (st == null)
        {
            return false;
        }

        Team team = st.tm.getTeamByPlayerName(playerName);

        if (team != null)
        {
            if(st.tm.isAnyOnline(team))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if two players are teammates
     * @param playerOne
     * @param playerTwo
     * @return
     */
    public boolean isTeamMate(String playerOne, String playerTwo)
    {
        if (st == null)
        {
            return false;
        }

        Team team1 = st.tm.getTeamByPlayerName(playerOne);
        Team team2 = st.tm.getTeamByPlayerName(playerTwo);

        if (team1 != null && team2 != null)
        {
            if (team1.equals(team2))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Starts up the plugin
     */
    public void startST()
    {
        if (st == null)
        {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("SimpleTeams");

            if (test != null)
            {
                if (!plugin.getServer().getPluginManager().isPluginEnabled(test))
                {
                    plugin.getServer().getPluginManager().enablePlugin(test);
                }

                this.st = (SimpleTeams) test;
            }
        }
    }
}
