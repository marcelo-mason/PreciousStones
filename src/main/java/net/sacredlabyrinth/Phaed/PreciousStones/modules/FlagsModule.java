package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.DirtyFieldReason;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PaymentEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.RentEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FlagsModule {
    private Field field;
    private List<FieldFlag> flags = new ArrayList<FieldFlag>();
    private List<FieldFlag> disabledFlags = new ArrayList<FieldFlag>();
    private List<FieldFlag> insertedFlags = new ArrayList<FieldFlag>();
    private List<FieldFlag> clearedFlags = new ArrayList<FieldFlag>();

    public FlagsModule(Field field) {
        this.field = field;
    }

    public void addFlag(FieldFlag flag) {
        flags.add(flag);
    }

    /**
     * Check if the field has certain certain properties
     *
     * @param flag
     * @return
     */
    public boolean hasFlag(FieldFlag flag) {
        boolean ret = flags.contains(flag);

        if (!ret) {
            ret = insertedFlags.contains(flag);
        }

        if (disabledFlags.contains(flag)) {
            ret = false;
        }

        return ret;
    }

    /**
     * Check if the field has certain certain properties
     *
     * @param flagStr
     * @return
     */
    public boolean hasFlag(String flagStr) {
        return hasFlag(Helper.toFieldFlag(flagStr));
    }

    /**
     * Return the list of flags and their data as a json string
     *
     * @return the flags
     */
    public String getFlagsAsString() {
        JSONObject json = new JSONObject();

        // writing the list of flags to json

        JSONArray disabledFlags = new JSONArray();
        disabledFlags.addAll(getDisabledFlagsStringList());

        JSONArray clearedFlags = new JSONArray();
        clearedFlags.addAll(getClearedFlagsStringList());

        JSONArray insertedFlags = new JSONArray();
        insertedFlags.addAll(getInsertedFlagsStringList());

        JSONArray renterList = new JSONArray();
        renterList.addAll(field.getRentingModule().getRentersString());

        JSONArray paymentList = new JSONArray();
        paymentList.addAll(field.getRentingModule().getPaymentString());

        JSONArray blacklistedCommandsList = new JSONArray();
        blacklistedCommandsList.addAll(field.getListingModule().getBlacklistedCommands());

        JSONArray whitelistedBlocksList = new JSONArray();
        whitelistedBlocksList.addAll(field.getListingModule().getWhitelistedBlocks());

        if (!paymentList.isEmpty()) {
            json.put("payments", paymentList);
        }

        if (!disabledFlags.isEmpty()) {
            json.put("disabledFlags", disabledFlags);
        }

        if (!insertedFlags.isEmpty()) {
            json.put("insertedFlags", insertedFlags);
        }

        if (!clearedFlags.isEmpty()) {
            json.put("clearedFlags", clearedFlags);
        }

        if (!blacklistedCommandsList.isEmpty()) {
            json.put("blacklistedCommands", blacklistedCommandsList);
        }

        if (!renterList.isEmpty()) {
            json.put("renters", renterList);
        }

        if (field.getRevertingModule().getRevertSecs() > 0) {
            json.put("revertSecs", field.getRevertingModule().getRevertSecs());
        }

        if (field.getRentingModule().hasLimitSeconds()) {
            json.put("limitSeconds", field.getRentingModule().getLimitSeconds());
        }

        if (field.isDisabled()) {
            json.put("disabled", field.isDisabled());
        }

        if (field.getHidingModule().isHidden()) {
            json.put("hidden", field.getHidingModule().isHidden());
        }

        if (field.getForestingModule().getForesterUsed() > 0) {
            json.put("foresterUsed", field.getForestingModule().getForesterUsed());
        }

        return json.toString();
    }

    public ArrayList<String> getDisabledFlagsStringList() {
        ArrayList<String> ll = new ArrayList<String>();
        for (FieldFlag flag : disabledFlags) {
            ll.add(Helper.toFlagStr(flag));
        }
        return ll;
    }

    public ArrayList<String> getInsertedFlagsStringList() {
        ArrayList<String> ll = new ArrayList<String>();
        for (FieldFlag flag : insertedFlags) {
            ll.add(Helper.toFlagStr(flag));
        }
        return ll;
    }

    public ArrayList<String> getClearedFlagsStringList() {
        ArrayList<String> ll = new ArrayList<String>();
        for (FieldFlag flag : clearedFlags) {
            ll.add(Helper.toFlagStr(flag));
        }
        return ll;
    }

    /**
     * Returns inserted flags
     *
     * @return
     */
    public List<FieldFlag> getInsertedFlags() {
        return insertedFlags;
    }

    /**
     * Returns inserted flags
     *
     * @return
     */
    public List<FieldFlag> getClearedFlags() {
        return clearedFlags;
    }

    /**
     * Read the list of flags in from a json string
     *
     * @param flagString the flags to set
     */
    public void setFlags(String flagString) {
        if (flagString != null && !flagString.isEmpty()) {
            JSONObject flags = (JSONObject) JSONValue.parse(flagString);

            if (flags != null) {
                for (Object flag : flags.keySet()) {
                    try {
                        // reading the list of flags from json
                        if (flag.equals("disabledFlags")) {
                            JSONArray disabledFlags = (JSONArray) flags.get(flag);

                            for (Object flagStr : disabledFlags) {
                                // do no toggle of no-toggle flags

                                if (flagStr.toString().equalsIgnoreCase("dynmap-area") || flagStr.toString().equalsIgnoreCase("dynmap-marker")) {
                                    if (hasFlag(FieldFlag.DYNMAP_NO_TOGGLE)) {
                                        continue;
                                    }
                                }

                                disableFlag(flagStr.toString(), true);
                            }
                        } else if (flag.equals("insertedFlags")) {
                            JSONArray localFlags = (JSONArray) flags.get(flag);

                            for (Object flagStr : localFlags) {
                                insertFieldFlag(flagStr.toString());
                            }
                        } else if (flag.equals("clearedFlags")) {
                            JSONArray localFlags = (JSONArray) flags.get(flag);

                            for (Object flagStr : localFlags) {
                                clearFieldFlag(flagStr.toString());
                            }
                        } else if (flag.equals("renters")) {
                            JSONArray renterList = (JSONArray) flags.get(flag);

                            field.getRentingModule().clearRenters();
                            for (Object flagStr : renterList) {
                                RentEntry entry = new RentEntry(flagStr.toString());
                                field.getRentingModule().addRenter(entry);
                            }
                        } else if (flag.equals("blacklistedCommands")) {
                            JSONArray blacklistedCommandsList = (JSONArray) flags.get(flag);

                            for (Object flagStr : blacklistedCommandsList) {
                                field.getListingModule().getBlacklistedCommands().add(flagStr.toString());
                            }
                        } else if (flag.equals("whitelistedBlocks")) {
                            JSONArray whitelistedBlocksList = (JSONArray) flags.get(flag);

                            for (Object flagStr : whitelistedBlocksList) {
                                field.getListingModule().getWhitelistedBlocks().add(new BlockTypeEntry(flagStr.toString()));
                            }
                        } else if (flag.equals("foresterUsed")) {
                            field.getForestingModule().setForesterUsed(((Long) flags.get(flag)).intValue());
                        } else if (flag.equals("revertSecs")) {
                            field.getRevertingModule().setRevertSecs(((Long) flags.get(flag)).intValue());
                        } else if (flag.equals("limitSeconds")) {
                            field.getRentingModule().setLimitSeconds(((Long) flags.get(flag)).intValue());
                        } else if (flag.equals("disabled")) {
                            field.setDisabledNoMask(((Boolean) flags.get(flag)));
                        } else if (flag.equals("hidden")) {
                            field.getHidingModule().setHidden((Boolean) flags.get(flag));
                        } else if (flag.equals("payments")) {
                            JSONArray paymentList = (JSONArray) flags.get(flag);

                            paymentList.clear();
                            for (Object flagStr : paymentList) {
                                field.getRentingModule().addPayment(new PaymentEntry(flagStr.toString()));
                            }
                        }
                    } catch (Exception ex) {
                        System.out.print("Failed reading field flag: " + flag);
                        System.out.print("Value: " + flags.get(flag));

                        for (StackTraceElement el : ex.getStackTrace()) {
                            System.out.print(el.toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * Enable a flag
     *
     * @param flagStr
     */
    public void enableFlag(String flagStr) {
        boolean canEnable = false;

        for (Iterator iter = disabledFlags.iterator(); iter.hasNext(); ) {
            FieldFlag flag = (FieldFlag) iter.next();

            if (Helper.toFlagStr(flag).equals(flagStr)) {
                //remove from the disableFlags list
                iter.remove();
                canEnable = true;
            }
        }

        if (canEnable && !flags.contains(Helper.toFieldFlag(flagStr))) {
            flags.add(Helper.toFieldFlag(flagStr));
            field.addDirty(DirtyFieldReason.FLAGS);
            PreciousStones.debug("DirtyFlags: enableFlag");
        }
    }

    /**
     * Disabled a flag.
     *
     * @param flagStr
     */
    public void disableFlag(String flagStr, boolean skipSave) {
        boolean hasFlag = false;

        for (Iterator iter = flags.iterator(); iter.hasNext(); ) {
            FieldFlag flag = (FieldFlag) iter.next();
            if (Helper.toFlagStr(flag).equals(flagStr)) {
                iter.remove();
                hasFlag = true;
            }
        }

        if (hasFlag && !disabledFlags.contains(Helper.toFieldFlag(flagStr))) {
            disabledFlags.add(Helper.toFieldFlag(flagStr));

            if (!skipSave) {
                field.addDirty(DirtyFieldReason.FLAGS);
                PreciousStones.debug("DirtyFlags: disableFlag");
            }
        }
    }

    /**
     * Whether it has the disabled flag string
     *
     * @param flagStr
     * @return
     */
    public boolean hasDisabledFlag(String flagStr) {
        for (FieldFlag flag : disabledFlags) {
            if (Helper.toFlagStr(flag).equals(flagStr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether it has the disabled flag
     *
     * @param flag
     * @return
     */
    public boolean hasDisabledFlag(FieldFlag flag) {
        return disabledFlags.contains(flag);
    }

    /**
     * Returns the disabled flags
     *
     * @return
     */
    public List<FieldFlag> getDisabledFlags() {
        return Collections.unmodifiableList(disabledFlags);
    }

    /**
     * Toggles a field flag.  returns its state.
     *
     * @param flagStr
     */
    public boolean toggleFieldFlag(String flagStr) {
        boolean hasFlag = hasFlag(flagStr);

        if (hasFlag) {
            disableFlag(flagStr, false);
            return false;
        } else {
            enableFlag(flagStr);
            return true;
        }
    }

    /**
     * Revert all the flags back to default
     */
    public void RevertFlags() {
        //Revert all the flags back to the default
        insertedFlags.clear();
        disabledFlags.clear();
        flags.clear();
        for (FieldFlag flag : field.getSettings().getDefaultFlags()) {
            flags.add(flag);
        }
        field.addDirty(DirtyFieldReason.FLAGS);
        PreciousStones.debug("DirtyFlags: RevertFlags");
    }

    /**
     * Returns all the flags
     *
     * @return
     */
    public List<FieldFlag> getFlags() {
        return Collections.unmodifiableList(flags);
    }

    /**
     * Clear a field flag from the field
     *
     * @param flagStr
     */
    public boolean clearFieldFlag(String flagStr) {
        boolean cleared = false;

        if (insertedFlags.contains(Helper.toFieldFlag(flagStr))) {
            insertedFlags.remove(Helper.toFieldFlag(flagStr));
            cleared = true;
        }

        if (disabledFlags.contains(Helper.toFieldFlag(flagStr))) {
            disabledFlags.remove(Helper.toFieldFlag(flagStr));
            cleared = true;
        }

        if (flags.contains(Helper.toFieldFlag(flagStr))) {
            flags.remove(Helper.toFieldFlag(flagStr));
            cleared = true;
        }

        clearedFlags.add(Helper.toFieldFlag(flagStr));

        return cleared;
    }

    /**
     * Insert a field flag into the field
     *
     * @param flagStr
     */
    public boolean insertFieldFlag(String flagStr) {
        if (!insertedFlags.contains(Helper.toFieldFlag(flagStr))) {
            insertedFlags.add(Helper.toFieldFlag(flagStr));

            if (clearedFlags.contains(Helper.toFieldFlag(flagStr))) {
                clearedFlags.remove(Helper.toFieldFlag(flagStr));
            }

            return true;
        }

        return false;
    }

    /**
     * Force insert a flag if it doesn't exist.
     * This can suck
     *
     * @param flagStr
     */
    public boolean insertFlag(String flagStr) {
        if (!flags.contains(Helper.toFieldFlag(flagStr))) {
            flags.add(Helper.toFieldFlag(flagStr));
            dirtyFlags("insertFlag");
            return true;
        }
        return false;
    }

    /**
     * Imports a collection of field flags to this field
     *
     * @param flags
     */
    public void importFlags(List<FieldFlag> flags) {
        for (FieldFlag flag : flags) {
            insertFieldFlag(Helper.toFlagStr(flag));
        }
    }

    public void setBreakable() {
        if (!flags.contains(FieldFlag.BREAKABLE)) {
            if (!insertedFlags.contains(FieldFlag.BREAKABLE)) {
                insertedFlags.add(FieldFlag.BREAKABLE);
            }
        }
    }

    public void unsetBreakable() {
        if (!flags.contains(FieldFlag.BREAKABLE)) {
            if (insertedFlags.contains(FieldFlag.BREAKABLE)) {
                insertedFlags.remove(FieldFlag.BREAKABLE);
            }
        }
    }

    public void dirtyFlags(String reason) {
        field.addDirty(DirtyFieldReason.FLAGS);
        PreciousStones.debug("DirtyFlags: " + reason);
        PreciousStones.getInstance().getStorageManager().offerField(field);
    }
}
