package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.FieldSign;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PurchaseEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.StackHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.PermissionsManager;
import org.bukkit.entity.Player;

public class BuyingModule {
    public boolean buy(Player buyer, FieldSign s) {
        Field field = s.getField();
        PreciousStones plugin = PreciousStones.getInstance();
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(field);
        if (plugin.getLimitManager().reachedLimit(buyer, fs)) {
            PreciousStones.debug("field limit reached");
            return false;
        }
        PurchaseEntry purchase = new PurchaseEntry(buyer.getName(), field.getOwner(), field.getName(), field.getCoords(), s.getItem(), s.getPrice());

        if (s.getItem() == null) {
            if (PreciousStones.getInstance().getPermissionsManager().hasEconomy()) {
                if (PermissionsManager.hasMoney(buyer, s.getPrice())) {
                    PreciousStones.getInstance().getPermissionsManager().playerCharge(buyer, s.getPrice());
                    processPurchase(purchase, s);
                } else {
                    ChatHelper.send(buyer, "economyNotEnoughMoney");
                    return false;
                }
            }
        } else {
            if (StackHelper.hasItems(buyer, s.getItem(), s.getPrice())) {
                StackHelper.remove(buyer, s.getItem(), s.getPrice());
                processPurchase(purchase, s);
            } else {
                ChatHelper.send(buyer, "economyNotEnoughItems");
                return false;
            }
        }

        return true;
    }

    public void processPurchase(PurchaseEntry purchase, FieldSign s) {
        Field field = s.getField();
        field.setOwner(purchase.getBuyer());
        field.clearAllowed();

        Player owner = PreciousStones.getInstance().getServer().getPlayerExact(purchase.getOwner());

        if (owner != null) {
            giveMoney(owner, purchase);
        } else {
            PreciousStones.getInstance().getStorageManager().insertPendingPurchasePayment(purchase);
        }

        PreciousStones.getInstance().getCommunicationManager().logPurchase(s.getField().getOwner(), purchase.getBuyer(), purchase, s);
        s.remove();
    }

    public void giveMoney(Player owner, PurchaseEntry purchase) {
        if (purchase.isItemPayment()) {
            StackHelper.give(owner, purchase.getItem(), purchase.getAmount());

            if (purchase.getFieldName().isEmpty()) {
                ChatHelper.send(owner, "fieldSignItemPaymentReceivedNoName", purchase.getAmount(), purchase.getItem(), purchase.getBuyer(), purchase.getCoords());
            } else {
                ChatHelper.send(owner, "fieldSignItemPaymentReceived", purchase.getAmount(), purchase.getItem(), purchase.getBuyer(), purchase.getFieldName(), purchase.getCoords());
            }
        } else {
            PreciousStones.getInstance().getPermissionsManager().playerCredit(owner, purchase.getAmount());

            if (purchase.getFieldName().isEmpty()) {
                ChatHelper.send(owner, "fieldSignPaymentReceivedNoName", purchase.getAmount(), purchase.getBuyer(), purchase.getCoords());
            } else {
                ChatHelper.send(owner, "fieldSignPaymentReceived", purchase.getAmount(), purchase.getBuyer(), purchase.getFieldName(), purchase.getCoords());
            }
        }

        PreciousStones.getInstance().getCommunicationManager().logPurchaseCollect(purchase.getOwner(), purchase.getBuyer(), purchase);
    }
}
