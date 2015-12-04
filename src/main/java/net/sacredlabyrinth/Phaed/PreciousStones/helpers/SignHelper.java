package net.sacredlabyrinth.Phaed.PreciousStones.helpers;

import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.FieldSign;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignHelper {
    /**
     * check if a block is a sign
     *
     * @param block
     * @return
     */
    public static boolean isSign(Block block) {
        if (block == null) {
            return false;
        }

        return block.getState() instanceof Sign;
    }

    /**
     * Get a sign's attached block
     *
     * @param signBlock
     * @return
     */
    public static Block getAttachedBlock(Block signBlock) {
        MaterialData m = signBlock.getState().getData();
        BlockFace face = BlockFace.DOWN;

        if (m instanceof Attachable) {
            face = ((Attachable) m).getAttachedFace();
        }
        return signBlock.getRelative(face);
    }

    /**
     * Returns the field sign attached to a block
     *
     * @param block the block in question
     * @return the sign block, if attached
     */
    public static FieldSign getAttachedFieldSign(Block block) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP};

        for (BlockFace face : faces) {
            Block sign = block.getRelative(face);

            if (isSign(sign)) {
                Block attached = getAttachedBlock(sign);

                if (Helper.isSameBlock(attached.getLocation(), block.getLocation())) {
                    FieldSign s = new FieldSign(sign);

                    if (s.isValid()) {
                        return s;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Whether the player is forbidden from breaking a field sign
     *
     * @param player
     * @param block
     */
    public static boolean cannotBreakFieldSign(Block block, Player player) {
        // prevent breaking license block or the block attached to it

        if (block != null) {
            if (isSign(block)) {
                FieldSign s = new FieldSign(block);

                if (s.isValid()) {
                    if (player == null) {
                        return true;
                    }

                    if (s.getField().isOwner(player.getName())) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns the field sign out of sign
     *
     * @param sign
     */
    public static FieldSign getFieldSign(Block sign) {
        // prevent breaking license block or the block attached to it

        if (sign != null) {
            if (isSign(sign)) {
                FieldSign s = new FieldSign(sign);

                if (s.isValid()) {
                    return s;
                }
            }
        }

        return null;
    }

    /**
     * Parse a period string to seconds
     *
     * @param period
     * @return
     */
    public static int periodToSeconds(String period) {
        int counter = 0;
        int seconds = 0;

        ArrayList<String> strings = new ArrayList<String>();
        String[] chars = period.replaceAll(" ", "").toLowerCase().split("");
        String word = "";

        for (String ch : chars) {
            word += ch;

            if (ch.equals("w") || ch.equals("d") || ch.equals("h") || ch.equals("m") || ch.equals("s")) {
                strings.add(word);
                word = "";
            }
        }

        for (String string : strings) {
            if (string.contains("w")) {
                string = string.replace("w", "");

                if (Helper.isInteger(string)) {
                    seconds += Integer.parseInt(string) * 60 * 60 * 24 * 7;
                    counter++;
                }
            }

            if (string.contains("d")) {
                string = string.replace("d", "");

                if (Helper.isInteger(string)) {
                    seconds += Integer.parseInt(string) * 60 * 60 * 24;
                    counter++;
                }
            }

            if (string.contains("h")) {
                string = string.replace("h", "");

                if (Helper.isInteger(string)) {
                    seconds += Integer.parseInt(string) * 60 * 60;
                    counter++;
                }
            }

            if (counter < 3) {
                if (string.contains("m")) {
                    string = string.replace("m", "");

                    if (Helper.isInteger(string)) {
                        seconds += Integer.parseInt(string) * 60;
                        counter++;
                    }
                }
            }

            if (counter < 3) {
                if (string.contains("s")) {
                    string = string.replace("s", "");

                    if (Helper.isInteger(string)) {
                        seconds += Integer.parseInt(string);
                        counter++;
                    }
                }
            }
        }

        return seconds;
    }

    public static String secondsToPeriods(int seconds) {
        int counter = 0;

        String out = "";

        int w = (60 * 60 * 24 * 7);
        int d = (60 * 60 * 24);
        int h = (60 * 60);
        int m = (60);

        int wd = seconds / w;

        if (wd > 0) {
            out += wd + "w ";
            seconds = seconds % w;
            counter++;
        }

        int dd = seconds / d;

        if (dd > 0) {
            out += dd + "d ";
            seconds = seconds % d;
            counter++;
        }

        int hd = seconds / h;

        if (hd > 0) {
            out += hd + "h ";
            seconds = seconds % h;
            counter++;
        }

        int md = seconds / m;

        if (counter < 3) {
            if (md > 0) {
                out += md + "m ";
                seconds = seconds % m;
                counter++;
            }
        }

        int sd = seconds;

        if (counter < 3) {
            if (sd > 0) {
                out += sd + "s";
            }
        }
        return Helper.stripTrailing(out, " ");
    }

    public static boolean isValidPeriod(String period) {
        String string = period.replaceAll(" ", "").toLowerCase().replaceAll("[wdhms]", "");
        return Helper.isInteger(string);
    }

    public static BlockTypeEntry extractItemFromParenthesis(String line) {
        Pattern p = Pattern.compile("\\((.*?)\\)", Pattern.DOTALL);
        Matcher m = p.matcher(line);
        if (m.find()) {
            BlockTypeEntry entry = new BlockTypeEntry(m.group(1));

            if (entry.isValid()) {
                return entry;
            }
        }
        return null;
    }

    public static int extractPrice(String line) {
        line = line.replaceAll("\\((.*?)\\)", "");
        return Helper.forceParseInteger(line);
    }
}
