package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.SignHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;


public class FieldSign {
    private Sign sign;
    private boolean valid = true;
    private String tag;
    private Field field;
    private boolean fieldSign;
    private String period;
    private int price;
    private BlockTypeEntry item;
    private String playerName;
    private String failReason;

    public FieldSign(Block signBlock) {
        if (!SignHelper.isSign(signBlock)) {
            valid = false;
            return;
        }

        sign = ((Sign) signBlock.getState());

        String[] lines = sign.getLines();

        valid = extractData(signBlock, lines);
    }

    public FieldSign(Block signBlock, String[] lines, Player player) {
        if (!SignHelper.isSign(signBlock)) {
            valid = false;
            return;
        }

        valid = extractData(signBlock, lines);
        playerName = player.getName();
    }

    public boolean extractData(Block signBlock, String[] lines) {
        // extract the tag line, exit if not recognized

        tag = ChatColor.stripColor(lines[0]);
        fieldSign = tag.equalsIgnoreCase(ChatHelper.format("fieldSignRent")) || tag.equalsIgnoreCase(ChatHelper.format("fieldSignBuy")) || tag.equalsIgnoreCase(ChatHelper.format("fieldSignShare"));

        if (!fieldSign) {
            // reason removed due to it being triggered by using signs for other plugins
            failReason = null;
            return false;
        }

        // extract the price

        price = SignHelper.extractPrice(ChatColor.stripColor(lines[1]));

        if (price == 0) {
            failReason = "fieldSignNoPrice";
            return false;
        }

        // extract the item from the price
        // use the default item currency if nothing found in parenthesis and no economy plugin is being used

        item = SignHelper.extractItemFromParenthesis(ChatColor.stripColor(lines[1]));

        if (item == null) {
            if (!PreciousStones.getInstance().getPermissionsManager().hasEconomy()) {
                item = PreciousStones.getInstance().getSettingsManager().getDefaulItemCurrency();

                if (item == null) {
                    item = new BlockTypeEntry(Material.GOLD_INGOT);
                }
            }
        }

        Block attachedBlock = SignHelper.getAttachedBlock(signBlock);
        field = PreciousStones.getInstance().getForceFieldManager().getField(attachedBlock);

        // trying to create a rent sign on a block that is not a field

        if (field == null) {
            // reason removed due to it being triggered by using signs for other plugins
            failReason = null;
            return false;
        }

        // trying to create a rent sign on someone elses field

        if (playerName != null) {
            if (!field.isOwner(playerName)) {
                failReason = "fieldSignNotOwner";
                return false;
            }
        }

        // make sure the time period is valid

        if (!isBuyable()) {
            period = ChatColor.stripColor(lines[2]);

            if (!SignHelper.isValidPeriod(period)) {
                failReason = "fieldSignInvalidPeriod";
                return false;
            }
        }

        // creating a rent sign on a non-rent field

        if (isRentable()) {
            if (!field.hasFlag(FieldFlag.RENTABLE)) {
                failReason = "fieldSignNotRentable";
                return false;
            }
        }

        // creating a rent sign on a non-share field

        if (isShareable()) {
            if (!field.hasFlag(FieldFlag.SHAREABLE)) {
                failReason = "fieldSignNotShareable";
                return false;
            }
        }

        // creating a rent sign on a non-buy field

        if (isBuyable()) {
            if (!field.hasFlag(FieldFlag.BUYABLE)) {
                failReason = "fieldSignNotBuyable";
                return false;
            }
        }

        return true;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isRentable() {
        return tag.equalsIgnoreCase(ChatHelper.format("fieldSignRent"));
    }

    public boolean isBuyable() {
        return tag.equalsIgnoreCase(ChatHelper.format("fieldSignBuy"));
    }

    public boolean isShareable() {
        return tag.equalsIgnoreCase(ChatHelper.format("fieldSignShare"));
    }

    public void setRentedColor() {
        sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(sign.getLine(0)));
        sign.update();
    }

    public void setSharedColor() {
        sign.setLine(0, ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.stripColor(sign.getLine(0)));
        sign.update();
    }

    public void setAvailableColor() {
        sign.setLine(0, ChatColor.BOLD + ChatColor.stripColor(sign.getLine(0)));
        sign.setLine(3, "");
        sign.update();
    }

    public void updateRemainingTime(int seconds) {
        sign.setLine(3, ChatColor.BOLD + SignHelper.secondsToPeriods(seconds));
        sign.update();
    }

    public void cleanRemainingTime() {
        sign.setLine(3, "");
        sign.update();
    }

    /**
     * Throws the fieldSign back at the player
     */
    public void eject() {
        Helper.dropBlockWipe(sign.getBlock());
    }

    /**
     * Removes the sign completely
     */
    public void remove() {
        sign.getBlock().setType(Material.AIR);
    }

    /**
     * Returns the block the fieldSign is attacked to
     *
     * @return
     */
    public Block getAttachedBlock() {
        return SignHelper.getAttachedBlock(sign.getBlock());
    }

    public Field getField() {
        if (field != null) {
            return field;
        }

        return PreciousStones.getInstance().getForceFieldManager().getField(getAttachedBlock());
    }

    public boolean isFieldSign() {
        return fieldSign;
    }

    public String getPeriod() {
        return period;
    }

    public int getPrice() {
        return price;
    }

    public BlockTypeEntry getItem() {
        return item;
    }

    public Sign getSign() {
        return sign;
    }

    public String getFailReason() {
        return failReason;
    }
}