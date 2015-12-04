package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import com.gmail.nossr50.events.fake.FakeBlockBreakEvent;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class McMMOListener implements Listener {
    private PreciousStones plugin;

    /**
     *
     */
    public McMMOListener() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Cancels McMMO block breaking on fields
     *
     * @param event
     */

    @EventHandler(priority = EventPriority.HIGH)
    public void onMcMMBlockBreak(FakeBlockBreakEvent event) {
        if (plugin.getForceFieldManager().isField(event.getBlock())) {
            event.setCancelled(true);
        }
    }
}
