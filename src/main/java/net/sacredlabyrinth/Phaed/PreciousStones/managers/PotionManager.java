/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;

/**
 * @author telaeris
 */
public class PotionManager {
    private PreciousStones plugin;

    /**
     *
     */
    public PotionManager() {
        plugin = PreciousStones.getInstance();
    }

    public void applyPotions(Player player, Field field) {
        HashMap<PotionEffectType, Integer> potions = field.getSettings().getPotions();
        String names = "";

        for (PotionEffectType pot : potions.keySet()) {
            int intensity = potions.get(pot);

            if (!player.hasPotionEffect(pot)) {
                if (plugin.getPermissionsManager().has(player, "preciousstones.manual.bypass.potions")) {
                    if (plugin.getSettingsManager().isHarmfulPotion(pot)) {
                        return;
                    }
                }

                player.addPotionEffect(new PotionEffect(pot, 72000, intensity));
                plugin.getPermissionsManager().allowFast(player);
                names += pot.getName() + " ";
            }
        }

        if (names.length() > 0) {
            //plugin.getCommunicationManager().showPotion(player, Helper.stripTrailing(names, " "));
        }
    }

    public void removePotions(Player player, Field field) {
        HashMap<PotionEffectType, Integer> potions = field.getSettings().getPotions();

        for (PotionEffectType pot : potions.keySet()) {
            if (player.hasPotionEffect(pot)) {
                player.removePotionEffect(pot);
                plugin.getPermissionsManager().resetFast(player);
            }
        }
    }

    public void neutralizePotions(Player player, Field field) {
        List<PotionEffectType> noPotions = field.getSettings().getNeutralizePotions();

        for (PotionEffectType pot : noPotions) {
            if (player.hasPotionEffect(pot)) {
                player.removePotionEffect(pot);
                plugin.getPermissionsManager().resetFast(player);
                plugin.getCommunicationManager().showNoPotion(player, pot.getName());
            }
        }
    }
}
