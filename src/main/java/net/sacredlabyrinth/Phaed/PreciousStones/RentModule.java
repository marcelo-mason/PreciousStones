package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.blocks.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.FieldSign;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PaymentEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.RentEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.SignHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.StackHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.PermissionsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RentModule
{
    private Field field;
    private boolean signIsClean;
    private List<RentEntry> renterEntries = new ArrayList<RentEntry>();
    private List<String> renters = new ArrayList<String>();
    private PaymentEntry purchase;
    private List<PaymentEntry> payment = new ArrayList<PaymentEntry>();
    private int limitSeconds;

    public RentModule(Field field)
    {
        this.field = field;
    }

    public List<String> getRenters()
    {
        return Collections.unmodifiableList(renters);
    }

    public void addRenter(RentEntry entry)
    {
        renterEntries.add(entry);
        renters.add(entry.getPlayerName().toLowerCase());
    }

    public boolean hasRenter(String playerName)
    {
        return renters.contains(playerName.toLowerCase());
    }

    public boolean hasRenters()
    {
        return !renters.isEmpty();
    }

    public void clearRenters()
    {
        renterEntries.clear();
        renters.clear();
    }

    public void addPayment(PaymentEntry entry)
    {
        payment.add(entry);
    }

    public void setPurchase(PaymentEntry entry)
    {
        purchase = entry;
    }

    public boolean hasPurchase()
    {
        return purchase != null;
    }

    public PaymentEntry getPurchaseEntry()
    {
        return purchase;
    }

    public boolean hasLimitSeconds()
    {
        return limitSeconds > 0;
    }

    public int getLimitSeconds()
    {
        return limitSeconds;
    }

    public void setLimitSeconds(int limitSeconds)
    {
        this.limitSeconds = limitSeconds;

        field.dirtyFlags("setLimitSeconds");
    }

    public ArrayList<String> getRentersString()
    {
        ArrayList<String> ll = new ArrayList<String>();
        for (RentEntry entry : renterEntries)
        {
            ll.add(entry.serialize());
        }
        return ll;
    }

    public ArrayList<String> getPaymentString()
    {
        ArrayList<String> ll = new ArrayList<String>();
        for (PaymentEntry entry : payment)
        {
            ll.add(entry.toString());
        }
        return ll;
    }

    public RentEntry getRenter(Player player)
    {
        for (RentEntry entry : renterEntries)
        {
            if (entry.getPlayerName().equals(player.getName()))
            {
                return entry;
            }
        }

        return null;
    }

    public void addRent(Player player)
    {
        FieldSign s = getAttachedFieldSign();

        if (s != null)
        {
            int seconds = SignHelper.periodToSeconds(s.getPeriod());

            if (seconds == 0)
            {
                ChatHelper.send(player, "fieldSignRentError");
                return;
            }

            RentEntry renter = getRenter(player);

            if (renter != null)
            {
                renter.addSeconds(seconds);

                ChatHelper.send(player, "fieldSignRentRented", SignHelper.secondsToPeriods(renter.getPeriodSeconds()));
            }
            else
            {
                renterEntries.add(new RentEntry(player.getName(), seconds));
                renters.add(player.getName().toLowerCase());

                if (renterEntries.size() == 1)
                {
                    scheduleNextRentUpdate();
                }
                ChatHelper.send(player, "fieldSignRentRented", s.getPeriod());

                PreciousStones.getInstance().getEntryManager().leaveField(player, field);
                PreciousStones.getInstance().getEntryManager().enterField(player, field);
            }

            field.dirtyFlags("addRent");
        }
    }

    public void removeRenter(RentEntry entry)
    {
        renterEntries.remove(entry);
        renters.remove(entry.getPlayerName().toLowerCase());

        field.dirtyFlags("removeRenter");
    }

    public boolean clearRents()
    {
        if (hasRenters())
        {
            renterEntries.clear();
            renters.clear();
            purchase = null;
            cleanFieldSign();

            field.dirtyFlags("clearRents");
            return true;
        }
        return false;
    }

    public boolean removeRents()
    {
        FieldSign s = getAttachedFieldSign();

        if (s != null)
        {
            s.eject();

            renterEntries.clear();
            renters.clear();

            if (purchase != null)
            {
                field.removeAllowed(purchase.getPlayer());
                purchase = null;
            }

            payment.clear();

            field.dirtyFlags("removeRents");
            return true;
        }

        return false;
    }

    public List<RentEntry> getRenterEntries()
    {
        return Collections.unmodifiableList(renterEntries);
    }

    public void abandonRent(Player player)
    {
        for (RentEntry entry : renterEntries)
        {
            if (entry.getPlayerName().equals(player.getName()))
            {
                removeRenter(entry);
                cleanFieldSign();
                return;
            }
        }
    }

    public FieldSign getAttachedFieldSign()
    {
        return SignHelper.getAttachedFieldSign(field.getBlock());
    }

    public void cleanFieldSign()
    {
        if (!hasRenters())
        {
            FieldSign s = getAttachedFieldSign();

            if (s != null)
            {
                s.setAvailableColor();
                s.cleanRemainingTime();
            }
        }
    }

    public void addPurchase(String playerName, String fieldName, BlockTypeEntry item, int amount)
    {
        purchase = new PaymentEntry(playerName, fieldName, item, amount);

        field.dirtyFlags("addPurchase");
    }

    public void addPayment(String playerName, String fieldName, BlockTypeEntry item, int amount)
    {
        boolean added = false;

        for (PaymentEntry entry : payment)
        {
            if (entry.getPlayer().equals(playerName) && (item == null || entry.getItem().equals(item)))
            {
                entry.setAmount(entry.getAmount() + amount);
                added = true;
            }
        }

        if (!added)
        {
            payment.add(new PaymentEntry(playerName, fieldName, item, amount));
        }

        field.dirtyFlags("addPayment");
    }

    public boolean rent(Player player, FieldSign s)
    {
        if (getLimitSeconds() > 0)
        {
            PreciousStones.debug("field has rent limits in place: " + getLimitSeconds());

            RentEntry renter = getRenter(player);

            if (renter != null)
            {
                int seconds = SignHelper.periodToSeconds(s.getPeriod());

                if (renter.getPeriodSeconds() + seconds > getLimitSeconds())
                {
                    PreciousStones.debug("limit reached");
                    ChatHelper.send(player, "limitReached");
                    return false;
                }
            }
        }

        if (s.getItem() != null)
        {
            PreciousStones.debug("is item rent");

            if (StackHelper.hasItems(player, s.getItem(), s.getPrice()))
            {
                StackHelper.remove(player, s.getItem(), s.getPrice());

                addPayment(player.getName(), s.getField().getName(), s.getItem(), s.getPrice());
                addRent(player);

                PreciousStones.getInstance().getCommunicationManager().logPayment(field.getOwner(), player.getName(), s);
            }
            else
            {
                ChatHelper.send(player, "economyNotEnoughItems");
                return false;
            }
        }
        else
        {
            if (PreciousStones.getInstance().getPermissionsManager().hasEconomy())
            {
                if (PermissionsManager.hasMoney(player, s.getPrice()))
                {
                    PreciousStones.getInstance().getPermissionsManager().playerCharge(player, s.getPrice());

                    addPayment(player.getName(), s.getField().getName(), null, s.getPrice());
                    addRent(player);

                    PreciousStones.getInstance().getCommunicationManager().logPayment(field.getOwner(), player.getName(), s);
                }
                else
                {
                    ChatHelper.send(player, "economyNotEnoughMoney");
                    return false;
                }
            }
        }

        if (s.isShareable())
        {
            s.setSharedColor();
        }
        else if (s.isRentable())
        {
            s.setRentedColor();
        }

        return true;
    }

    public boolean hasPendingPayments()
    {
        return !payment.isEmpty();
    }

    public boolean buy(Player player, FieldSign s)
    {
        if (s.getItem() == null)
        {
            if (PreciousStones.getInstance().getPermissionsManager().hasEconomy())
            {
                if (PermissionsManager.hasMoney(player, s.getPrice()))
                {
                    PreciousStones.getInstance().getPermissionsManager().playerCharge(player, s.getPrice());

                    addPurchase(player.getName(), s.getField().getName(), null, s.getPrice());

                    PreciousStones.getInstance().getCommunicationManager().logPurchase(field.getOwner(), player.getName(), s);
                }
                else
                {
                    ChatHelper.send(player, "economyNotEnoughMoney");
                    return false;
                }
            }
        }
        else
        {
            if (StackHelper.hasItems(player, s.getItem(), s.getPrice()))
            {
                StackHelper.remove(player, s.getItem(), s.getPrice());

                addPurchase(player.getName(), s.getField().getName(), s.getItem(), s.getPrice());

                PreciousStones.getInstance().getCommunicationManager().logPurchase(field.getOwner(), player.getName(), s);
            }
            else
            {
                ChatHelper.send(player, "economyNotEnoughItems");
                return false;
            }
        }

        s.setBoughtColor(player);
        PreciousStones.getInstance().getForceFieldManager().addAllowed(field, player.getName());
        return true;
    }

    public boolean hasPendingPurchase()
    {
        return purchase != null;
    }

    public boolean isBuyer(Player player)
    {
        return purchase != null && purchase.getPlayer().equals(player.getName());
    }

    public void takePayment(Player player)
    {
        for (PaymentEntry entry : payment)
        {
            if (entry.isItemPayment())
            {
                StackHelper.give(player, entry.getItem(), entry.getAmount());

                if (entry.getFieldName().isEmpty())
                {
                    ChatHelper.send(player, "fieldSignItemPaymentReceivedNoName", entry.getAmount(), entry.getItem(), entry.getPlayer());
                }
                else
                {
                    ChatHelper.send(player, "fieldSignItemPaymentReceived", entry.getAmount(), entry.getItem(), entry.getPlayer(), entry.getFieldName());
                }
            }
            else
            {
                PreciousStones.getInstance().getPermissionsManager().playerCredit(player, entry.getAmount());

                if (entry.getFieldName().isEmpty())
                {
                    ChatHelper.send(player, "fieldSignPaymentReceivedNoName", entry.getAmount(), entry.getPlayer());
                }
                else
                {
                    ChatHelper.send(player, "fieldSignPaymentReceived", entry.getAmount(), entry.getPlayer(), entry.getFieldName());
                }
            }
        }

        PreciousStones.getInstance().getCommunicationManager().logPaymentCollect(field.getOwner(), player.getName(), getAttachedFieldSign());

        payment.clear();
        field.dirtyFlags("takePayment");
    }

    public void completePurchase(Player player)
    {
        field.setOwner(purchase.getPlayer());
        field.clearAllowed();

        if (purchase.isItemPayment())
        {
            StackHelper.give(player, purchase.getItem(), purchase.getAmount());

            if (purchase.getFieldName().isEmpty())
            {
                ChatHelper.send(player, "fieldSignItemPaymentReceivedNoName", purchase.getAmount(), purchase.getItem(), purchase.getPlayer());
            }
            else
            {
                ChatHelper.send(player, "fieldSignItemPaymentReceived", purchase.getAmount(), purchase.getItem(), purchase.getPlayer(), purchase.getFieldName());
            }
        }
        else
        {
            PreciousStones.getInstance().getPermissionsManager().playerCredit(player, purchase.getAmount());

            if (purchase.getFieldName().isEmpty())
            {
                ChatHelper.send(player, "fieldSignPaymentReceivedNoName", purchase.getAmount(), purchase.getPlayer());
            }
            else
            {
                ChatHelper.send(player, "fieldSignPaymentReceived", purchase.getAmount(), purchase.getPlayer(), purchase.getFieldName());
            }
        }

        PreciousStones.getInstance().getCommunicationManager().logPurchaseCollect(field.getOwner(), player.getName(), getAttachedFieldSign());

        purchase = null;
        field.dirtyFlags("completePurchase");
    }

    private class Update implements Runnable
    {
        public void run()
        {
            if (hasRenters())
            {
                FieldSign s = getAttachedFieldSign();

                if (s != null)
                {
                    if (s.isRentable() || s.isShareable())
                    {
                        boolean foundSomeone = false;

                        if (PreciousStones.getInstance().getEntryManager().hasInhabitants(field))
                        {
                            Player closest = Helper.getClosestPlayer(field.getLocation(), 64);

                            for (RentEntry entry : renterEntries)
                            {
                                if (entry.getPlayerName().equalsIgnoreCase(closest.getName()))
                                {
                                    s.updateRemainingTime(entry.remainingRent());
                                    foundSomeone = true;
                                    signIsClean = false;
                                }
                            }
                        }

                        if (!foundSomeone && !signIsClean)
                        {
                            s.cleanRemainingTime();
                            signIsClean = true;
                        }
                    }
                }
            }

            for (Iterator iter = renterEntries.iterator(); iter.hasNext(); )
            {
                RentEntry entry = (RentEntry) iter.next();

                if (entry.isDone())
                {
                    renters.remove(entry.getPlayerName().toLowerCase());
                    iter.remove();

                    field.dirtyFlags("RentUpdateRunnable");

                    if (field.getName().isEmpty())
                    {
                        ChatHelper.send(entry.getPlayerName(), "fieldSignRentExpiredNoName");
                    }
                    else
                    {
                        ChatHelper.send(entry.getPlayerName(), "fieldSignRentExpired", field.getName());
                    }
                }
            }

            scheduleNextRentUpdate();
        }
    }

    public void scheduleNextRentUpdate()
    {
        if (!renterEntries.isEmpty())
        {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PreciousStones.getInstance(), new Update(), 20);
        }
    }

}
