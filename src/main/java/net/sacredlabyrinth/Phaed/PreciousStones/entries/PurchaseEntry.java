package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import java.util.Random;

public class PurchaseEntry {
    private String buyer;
    private String owner;
    private BlockTypeEntry item;
    private int amount;
    private String fieldName;
    private String coords;
    private int id;

    public PurchaseEntry(String buyer, String owner, String fieldName, String coords, BlockTypeEntry item, int amount) {
        this.buyer = buyer;
        this.owner = owner;
        this.item = item;
        this.amount = amount;
        this.fieldName = fieldName;
        this.coords = coords;
        this.id = new Random().nextInt(Integer.MAX_VALUE);
    }

    public PurchaseEntry(int id, String buyer, String owner, String fieldName, String coords, BlockTypeEntry item, int amount) {
        this.buyer = buyer;
        this.owner = owner;
        this.item = item;
        this.amount = amount;
        this.coords = coords;
        this.fieldName = fieldName;
        this.id = id;
    }

    public BlockTypeEntry getItem() {
        return item;
    }

    public boolean isItemPayment() {
        return item != null && item.getMaterial() != null;
    }

    public int getAmount() {
        return amount;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getBuyer() {
        return buyer;
    }

    public String getOwner() {
        return owner;
    }

    public int getId() {
        return id;
    }

    public String getCoords() {
        return coords;
    }
}
