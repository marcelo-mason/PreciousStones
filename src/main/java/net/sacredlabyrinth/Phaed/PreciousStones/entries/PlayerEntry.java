package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author phaed
 */
public class PlayerEntry {
    private UUID onlineUUID;
    private String name;
    private boolean disabled;
    private boolean online;
    private int density;
    private boolean bypassDisabled;
    private Location outsideLocation;
    private JSONArray confiscatedInventory = new JSONArray();
    private ItemStackEntry confiscatedHelmet = null;
    private ItemStackEntry confiscatedChestplate = null;
    private ItemStackEntry confiscatedLeggings = null;
    private ItemStackEntry confiscatedBoots = null;
    private boolean teleporting = false;
    private int teleportSecondsRemaining = 0;
    private Vec teleportVec = null;
    private boolean teleportPending = false;
    private int task;

    /**
     */
    public PlayerEntry() {
        disabled = PreciousStones.getInstance().getSettingsManager().isOffByDefault();
        density = PreciousStones.getInstance().getSettingsManager().getVisualizeDensity();
    }

    /**
     * Adds in the confiscated inventory and items
     *
     * @param items
     * @param helmet
     * @param chestplate
     * @param leggings
     * @param boots
     */
    public void confiscate(List<ItemStackEntry> items, ItemStackEntry helmet, ItemStackEntry chestplate, ItemStackEntry leggings, ItemStackEntry boots) {
        for (ItemStackEntry entry : items) {
            confiscatedInventory.add(entry.serialize());
        }

        if (helmet != null) {
            confiscatedHelmet = helmet;
        }

        if (chestplate != null) {
            confiscatedChestplate = chestplate;
        }

        if (leggings != null) {
            confiscatedLeggings = leggings;
        }

        if (boots != null) {
            confiscatedBoots = boots;
        }
    }

    /**
     * Returns the list of confiscated items, and removes them from the entry
     *
     * @return
     */
    public List<ItemStackEntry> returnInventory() {
        List<ItemStackEntry> out = new ArrayList<ItemStackEntry>();

        for (Object stackEntry : confiscatedInventory) {
            out.add(new ItemStackEntry((JSONObject) stackEntry));
        }

        confiscatedInventory.clear();
        return out;
    }

    /**
     * Returns confiscated helmet
     *
     * @return
     */
    public ItemStackEntry returnHelmet() {
        ItemStackEntry out = confiscatedHelmet;
        confiscatedHelmet = null;
        return out;
    }

    /**
     * Returns confiscated chestplate
     *
     * @return
     */
    public ItemStackEntry returnChestplate() {
        ItemStackEntry out = confiscatedChestplate;
        confiscatedChestplate = null;
        return out;
    }

    /**
     * Returns confiscated leggings
     *
     * @return
     */
    public ItemStackEntry returnLeggings() {
        ItemStackEntry out = confiscatedLeggings;
        confiscatedLeggings = null;
        return out;
    }

    /**
     * Returns confiscated boots
     *
     * @return
     */
    public ItemStackEntry returnBoots() {
        ItemStackEntry out = confiscatedBoots;
        confiscatedBoots = null;
        return out;
    }

    /**
     * @return
     */
    public boolean isDisabled() {
        return this.disabled;
    }

    /**
     * @param disabled
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * @return the online
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * @param online the online to set
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * @return the outsideLocation
     */
    public Location getOutsideLocation() {
        return outsideLocation;
    }

    /**
     * @param outsideLocation the outsideLocation to set
     */
    public void setOutsideLocation(Location outsideLocation) {
        this.outsideLocation = outsideLocation;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the list of flags and their data as a json string
     *
     * @return the flags
     */
    public String getFlags() {
        JSONObject json = new JSONObject();

        // writing the list of flags to json

        if (disabled) {
            json.put("disabled", disabled);
        }

        if (!confiscatedInventory.isEmpty()) {
            json.put("confiscated", confiscatedInventory);
        }

        if (confiscatedHelmet != null) {
            json.put("helmet", confiscatedHelmet.serialize());
        }

        if (confiscatedChestplate != null) {
            json.put("chestplate", confiscatedChestplate.serialize());
        }

        if (confiscatedLeggings != null) {
            json.put("leggings", confiscatedLeggings.serialize());
        }

        if (confiscatedBoots != null) {
            json.put("boots", confiscatedBoots.serialize());
        }

        if (teleportSecondsRemaining > 0) {
            json.put("teleportSecondsRemaining", teleportSecondsRemaining);
        }

        if (teleportVec != null) {
            json.put("teleportVec", teleportVec.serialize());
        }

        if (teleportPending) {
            json.put("teleportPending", teleportPending);
        }

        if (bypassDisabled) {
            json.put("bypassDisabled", bypassDisabled);
        }

        json.put("density", density);
        json.put("density", density);

        return json.toString();
    }

    /**
     * Read the list of flags in from a json string
     *
     * @param flagString the flags to set
     */
    public void setFlags(String flagString) {
        if (flagString != null && !flagString.isEmpty()) {
            Object obj = JSONValue.parse(flagString);
            JSONObject flags = (JSONObject) obj;

            if (flags != null) {
                for (Object flag : flags.keySet()) {
                    try {
                        // reading the list of flags from json

                        if (flag.equals("disabled")) {
                            disabled = (Boolean) flags.get(flag);
                        }

                        if (flag.equals("density")) {
                            density = ((Long) flags.get(flag)).intValue();
                        }

                        if (flag.equals("confiscated")) {
                            confiscatedInventory = ((JSONArray) flags.get(flag));
                        }

                        if (flag.equals("helmet")) {
                            confiscatedHelmet = new ItemStackEntry((JSONObject) flags.get(flag));
                        }

                        if (flag.equals("chestplate")) {
                            confiscatedChestplate = new ItemStackEntry((JSONObject) flags.get(flag));
                        }

                        if (flag.equals("leggings")) {
                            confiscatedLeggings = new ItemStackEntry((JSONObject) flags.get(flag));
                        }

                        if (flag.equals("boots")) {
                            confiscatedBoots = new ItemStackEntry((JSONObject) flags.get(flag));
                        }

                        if (flag.equals("teleportSecondsRemaining")) {
                            teleportSecondsRemaining = ((Long) flags.get(flag)).intValue();
                        }

                        if (flag.equals("teleportVec")) {
                            teleportVec = new Vec(flags.get(flag).toString());
                        }

                        if (flag.equals("teleportPending")) {
                            teleportPending = (Boolean) flags.get(flag);
                        }

                        if (flag.equals("bypassDisabled")) {
                            bypassDisabled = (Boolean) flags.get(flag);
                        }

                        // player still needs teleport

                        if (teleportSecondsRemaining > 0) {
                            if (teleportVec != null) {
                                startTeleportCountDown();
                            }
                        } else {
                            if (teleportPending) {
                                tryTeleport();
                            }
                        }
                    } catch (Exception ex) {
                        System.out.print("Failed reading player flag: " + flag);
                        System.out.print("Value: " + flags.get(flag));
                        System.out.print("Error: " + ex.getMessage());

                        for (StackTraceElement el : ex.getStackTrace()) {
                            System.out.print(el.toString());
                        }
                    }
                }
            }
        }
    }

    public void startTeleportCountDown() {
        task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(PreciousStones.getInstance(), new Runnable() {
            @Override
            public void run() {
                teleportSecondsRemaining -= 1;

                if (teleportSecondsRemaining <= 0) {
                    tryTeleport();

                    Bukkit.getServer().getScheduler().cancelTask(task);
                }
            }
        }, 20, 20);
    }

    private void tryTeleport() {
        if (teleportVec == null) {
            teleportSecondsRemaining = 0;
            teleportPending = false;
            return;
        }

        Player player = Bukkit.getServer().getPlayerExact(name);

        if (player != null) {
            player.teleport(teleportVec.getLocation());
            teleportSecondsRemaining = 0;
            teleportVec = null;
            teleportPending = false;
        } else {
            teleportPending = true;
        }
    }

    public int getDensity() {
        return Math.max(density, 1);
    }

    public void setDensity(int density) {
        this.density = density;
    }

    public boolean isTeleporting() {
        return teleporting;
    }

    public void setTeleporting(boolean teleporting) {
        this.teleporting = teleporting;
    }

    public int getTeleportSecondsRemaining() {
        return teleportSecondsRemaining;
    }

    public void setTeleportSecondsRemaining(int teleportSecondsRemaining) {
        this.teleportSecondsRemaining = teleportSecondsRemaining;
    }

    public Vec getTeleportVec() {
        return teleportVec;
    }

    public void setTeleportVec(Vec teleportVec) {
        this.teleportVec = teleportVec;
    }

    public boolean isBypassDisabled() {
        return bypassDisabled;
    }

    public void setBypassDisabled(boolean bypassDisabled) {
        this.bypassDisabled = bypassDisabled;
    }

    public void setOnlineUUID(UUID uuid) {

        this.onlineUUID = uuid;
    }

    public UUID getOnlineUUID() {
        return onlineUUID;
    }
}
