/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * @author telaeris
 */
public class CombatTagManager {
    private PreciousStones plugin;
    private CombatTagApi ct;

    /**
     *
     */
    public CombatTagManager() {
        plugin = PreciousStones.getInstance();
        getCombatTag();
    }

    private void getCombatTag() {
        if (ct == null) {
            Plugin test = plugin.getServer().getPluginManager().getPlugin("CombatTag");

            if (test != null) {

                this.ct = new CombatTagApi((CombatTag) test);
            }
        }
    }

    public boolean isInCombat(Player player) {
        //No way to tell without the plugin
        if (ct == null) {
            return false;
        }

        return ct.isInCombat(player.getName());
    }
}
