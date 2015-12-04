package net.sacredlabyrinth.Phaed.PreciousStones.entries;

public class PaymentEntry {
    private String player;
    private BlockTypeEntry item;
    private int amount;
    private String fieldName;

    public PaymentEntry(String player, String fieldName, BlockTypeEntry item, int amount) {
        this.player = player;
        this.item = item;
        this.amount = amount;
        this.fieldName = fieldName;
    }

    public PaymentEntry(String packed) {
        String[] unpacked = packed.split("[|]");

        this.player = unpacked[0];
        this.fieldName = unpacked[1];
        this.amount = Integer.parseInt(unpacked[2]);

        if (packed.length() > 3) {
            this.item = new BlockTypeEntry(unpacked[3]);
        }
    }

    @Override
    public String toString() {
        if (item == null) {
            return player + "|" + fieldName + "|" + amount;
        }

        return player + "|" + fieldName + "|" + amount + "|" + item;
    }

    public String getPlayer() {
        return player;
    }

    public BlockTypeEntry getItem() {
        return item;
    }

    public boolean isItemPayment() {
        return item != null;
    }

    public int getAmount() {
        return amount;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
