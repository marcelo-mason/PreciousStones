package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.FieldSign;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.PermissionsManager;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignHelper
{
    /**
     * check if a block is a sign
     *
     * @param block
     * @return
     */
    public static boolean isSign(Block block)
    {
        if (block == null)
        {
            return false;
        }

        return block.getState() instanceof Sign;
    }

    /**
     * Get a sign's attached block
     *
     * @param attachable
     * @return
     */
    public static Block getAttachedBlock(Block attachable)
    {
        MaterialData m = attachable.getState().getData();
        BlockFace face = BlockFace.DOWN;

        if (m instanceof Attachable)
        {
            face = ((Attachable) m).getAttachedFace();
        }
        return attachable.getRelative(face);
    }

    /**
     * Returns the field sign attached to a block
     *
     * @param block the block in question
     * @return the sign block, if attached
     */
    public static FieldSign getAttachedFieldSign(Block block)
    {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP};

        for (BlockFace face : faces)
        {
            Block sign = block.getRelative(face);

            if (isSign(sign))
            {
                Block attached = getAttachedBlock(sign);

                if (Helper.isSameBlock(attached.getLocation(), block.getLocation()))
                {
                    FieldSign s = new FieldSign(sign);

                    if (s.isValid())
                    {
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
    public static boolean cannotBreakFieldSign(Block block, Player player)
    {
        // prevent breaking license block or the block attached to it

        if (block != null)
        {
            if (isSign(block))
            {
                FieldSign s = new FieldSign(block);

                if (s.isValid())
                {
                    if (player == null)
                    {
                        return true;
                    }

                    if (!s.getField().isOwner(player.getName()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Parse a period string to seconds
     *
     * @param period
     * @return
     */
    public static int periodToSeconds(String period)
    {
        int counter = 0;
        int seconds = 0;

        String[] strings = period.split(" ");

        for (String string : strings)
        {

            if (string.contains("w"))
            {
                string = string.replace("w", "");

                if (Helper.isInteger(string))
                {
                    seconds += Integer.parseInt(string) * 60 * 60 * 24 * 7;
                    counter++;
                }
            }

            if (string.contains("d"))
            {
                string = string.replace("d", "");

                if (Helper.isInteger(string))
                {
                    seconds += Integer.parseInt(string) * 60 * 60 * 24;
                    counter++;
                }
            }

            if (string.contains("h"))
            {
                string = string.replace("h", "");

                if (Helper.isInteger(string))
                {
                    seconds += Integer.parseInt(string) * 60 * 60;
                    counter++;
                }
            }

            if (counter < 3)
            {
                if (string.contains("m"))
                {
                    string = string.replace("m", "");

                    if (Helper.isInteger(string))
                    {
                        seconds += Integer.parseInt(string) * 60;
                        counter++;
                    }
                }
            }

            if (counter < 3)
            {
                if (string.contains("s"))
                {
                    string = string.replace("s", "");

                    if (Helper.isInteger(string))
                    {
                        seconds += Integer.parseInt(string);
                        counter++;
                    }
                }
            }
        }

        return seconds;
    }

    public static String secondsToPeriods(int seconds)
    {
        int counter = 0;

        String out = "";

        int w = (60 * 60 * 24 * 7);
        int d = (60 * 60 * 24);
        int h = (60 * 60);
        int m = (60);

        int wd = seconds / w;

        if (wd > 0)
        {
            out += wd + "w ";
            seconds = seconds % w;
            counter++;
        }

        int dd = seconds / d;

        if (dd > 0)
        {
            out += dd + "d ";
            seconds = seconds % d;
            counter++;
        }

        int hd = seconds / h;

        if (hd > 0)
        {
            out += hd + "h ";
            seconds = seconds % h;
            counter++;
        }

        int md = seconds / m;

        if (counter < 3)
        {
            if (md > 0)
            {
                out += md + "m ";
                seconds = seconds % m;
                counter++;
            }
        }

        int sd = seconds;

        if (counter < 3)
        {
            if (sd > 0)
            {
                out += sd + "s";
                counter++;
            }
        }
        return Helper.stripTrailing(out, " ");
    }

    public static boolean isValidPeriod(String period)
    {
        String[] strings = period.split(" ");

        for (String string : strings)
        {
            if (string.contains("w") || string.contains("d") || string.contains("h") || string.contains("m") || string.contains("s"))
            {
                string = string.replaceAll("[wdhms]", "");

                if (!Helper.isInteger(string))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidMultiple(String multiple)
    {
        if (multiple == null || multiple.isEmpty())
        {
            return true;
        }

        if (!multiple.contains("x"))
        {
            return false;
        }

        multiple = multiple.replace("x", "");

        if (!Helper.isInteger(multiple))
        {
            return false;
        }

        return true;
    }

    public static int multipleToInteger(String multiple)
    {
        if (multiple == null || multiple.isEmpty())
        {
            return 0;
        }

        multiple = multiple.replace("x", "");

        return Integer.parseInt(multiple);
    }

    public static BlockTypeEntry extractItemFromParenthesis(String line)
    {
        Pattern p = Pattern.compile("\\((.*?)\\)", Pattern.DOTALL);
        Matcher m = p.matcher(line);
        while (m.find())
        {
            if (Helper.isTypeEntry(m.group(1)))
            {
                return new BlockTypeEntry(m.group(1));
            }
        }
        return null;
    }

    public static int extractPrice(String line)
    {
        line = line.replaceAll("\\((.*?)\\)", "");
        return Helper.forceParseInteger(line);
    }

    public static boolean pay(Player player, FieldSign s)
    {
        if (s.getItem() == null)
        {
            if (PreciousStones.getInstance().getPermissionsManager().hasEconomy())
            {
                if (PermissionsManager.hasMoney(player, s.getPrice()))
                {
                    PreciousStones.getInstance().getPermissionsManager().playerCharge(player, s.getPrice());

                    PlayerEntry entry = PreciousStones.getInstance().getPlayerManager().getPlayerEntry(s.getField().getOwner());
                    entry.addPayment(player.getName(), s.getField().getName(), null, s.getPrice());
                    return true;
                }

                ChatBlock.send(player, "economyNotEnoughMoney");
            }
        }
        else
        {
            if (StackHelper.hasItems(player, s.getItem(), s.getPrice()))
            {
                StackHelper.remove(player, s.getItem(), s.getPrice());

                PlayerEntry entry = PreciousStones.getInstance().getPlayerManager().getPlayerEntry(s.getField().getOwner());
                entry.addPayment(player.getName(), s.getField().getName(), s.getItem(), s.getPrice());
                return true;
            }

            ChatBlock.send(player, "economyNotEnoughItems");
        }

        return false;
    }
}
