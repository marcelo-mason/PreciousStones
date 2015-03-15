package net.sacredlabyrinth.Phaed.PreciousStones.uuid;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;


/**
 *
 * @author NeT32
 */
public class UUIDMigration {

    public static boolean canReturnUUID() {
        try {
            Bukkit.class.getDeclaredMethod("getPlayer", UUID.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static UUID findPlayerUUID(String playerDisplayName) {
        Player OnlinePlayer = PreciousStones.getInstance().getServer().getPlayerExact(playerDisplayName);

        if (OnlinePlayer != null) {
            return OnlinePlayer.getUniqueId();
        } else {
            try {
                return UUIDFetcher.getUUIDOf(playerDisplayName);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
