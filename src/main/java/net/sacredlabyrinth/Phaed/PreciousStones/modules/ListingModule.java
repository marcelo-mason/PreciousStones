package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;

import java.util.ArrayList;
import java.util.List;

public class ListingModule {
    private Field field;
    private List<String> blacklistedCommands = new ArrayList<String>();
    private List<BlockTypeEntry> whitelistedBlocks = new ArrayList<BlockTypeEntry>();

    public ListingModule(Field field) {
        this.field = field;
    }

    public void addBlacklistedCommand(String command) {
        if (!getBlacklistedCommands().contains(command)) {
            getBlacklistedCommands().add(command);
        }
        field.getFlagsModule().dirtyFlags("addBlacklistedCommand");
    }

    public void clearBlacklistedCommands() {
        getBlacklistedCommands().clear();
        field.getFlagsModule().dirtyFlags("clearBlacklistedCommands");
    }

    public boolean isBlacklistedCommand(String command) {
        if (field.hasFlag(FieldFlag.COMMAND_BLACKLISTING)) {
            command = command.replace("/", "");

            int i = command.indexOf(' ');

            if (i > -1) {
                command = command.substring(0, i);
            }

            PreciousStones.debug(command);

            return getBlacklistedCommands().contains(command);
        }
        return false;
    }

    public boolean hasBlacklistedComands() {
        return getBlacklistedCommands().size() > 0;
    }

    public String getBlacklistedCommandsList() {
        String out = "";

        for (String cmd : getBlacklistedCommands()) {
            out += cmd + ", ";
        }

        return Helper.stripTrailing(out, ", ");
    }

    public void addWhitelistedBlock(BlockTypeEntry type) {
        if (!getWhitelistedBlocks().contains(type)) {
            getWhitelistedBlocks().add(type);
        }
        field.getFlagsModule().dirtyFlags("addWhitelistedBlock");
    }

    public void deleteWhitelistedBlock(BlockTypeEntry type) {
        getWhitelistedBlocks().remove(type);
        field.getFlagsModule().dirtyFlags("deleteWhitelistedBlock");
    }

    public List<String> getBlacklistedCommands() {
        return blacklistedCommands;
    }

    public List<BlockTypeEntry> getWhitelistedBlocks() {
        return whitelistedBlocks;
    }
}
