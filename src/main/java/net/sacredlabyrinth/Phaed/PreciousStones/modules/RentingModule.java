package net.sacredlabyrinth.Phaed.PreciousStones.modules;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.FieldSign;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PaymentEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.RentEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
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

public class RentingModule {
    private Field field;
    private List<RentEntry> renterEntries = new ArrayList<RentEntry>();
    private List<PaymentEntry> payment = new ArrayList<PaymentEntry>();
    private List<String> renters = new ArrayList<String>();
    private boolean signIsClean;
    private int limitSeconds;

    public RentingModule(Field field) {
        this.field = field;
    }

    public List<String> getRenters() {
        return Collections.unmodifiableList(renters);
    }

    public void addRenter(RentEntry entry) {
        renterEntries.add(entry);
        renters.add(entry.getPlayerName().toLowerCase());
        PreciousStones.getInstance().getForceFieldManager().addToRenterCollection(field);
    }

    public boolean hasRenter(String playerName) {
        return renters.contains(playerName.toLowerCase());
    }

    public boolean hasRenters() {
        return !renters.isEmpty();
    }

    public void clearRenters() {
        PreciousStones.getInstance().getForceFieldManager().removeAllRenters(field);
        renterEntries.clear();
        renters.clear();
    }

    public void addPayment(PaymentEntry entry) {
        payment.add(entry);
    }

    public boolean hasLimitSeconds() {
        return limitSeconds > 0;
    }

    public int getLimitSeconds() {
        return limitSeconds;
    }

    public void setLimitSeconds(int limitSeconds) {
        this.limitSeconds = limitSeconds;

        field.getFlagsModule().dirtyFlags("setLimitSeconds");
    }

    public ArrayList<String> getRentersString() {
        ArrayList<String> ll = new ArrayList<String>();
        for (RentEntry entry : renterEntries) {
            ll.add(entry.serialize());
        }
        return ll;
    }

    public ArrayList<String> getPaymentString() {
        ArrayList<String> ll = new ArrayList<String>();
        for (PaymentEntry entry : payment) {
            ll.add(entry.toString());
        }
        return ll;
    }

    public RentEntry getRenter(Player player) {
        for (RentEntry entry : renterEntries) {
            if (entry.getPlayerName().equals(player.getName())) {
                return entry;
            }
        }

        return null;
    }

    public void addRent(Player player) {
        PreciousStones.getInstance().getForceFieldManager().addToRenterCollection(field);
        FieldSign s = field.getAttachedFieldSign();
        if (s != null) {
            int seconds = SignHelper.periodToSeconds(s.getPeriod());

            if (seconds == 0) {
                ChatHelper.send(player, "fieldSignRentError");
                return;
            }

            RentEntry renter = getRenter(player);

            if (renter != null) {
                renter.addSeconds(seconds);

                ChatHelper.send(player, "fieldSignRentRented", SignHelper.secondsToPeriods(renter.getPeriodSeconds()));
            } else {
                renterEntries.add(new RentEntry(player.getName(), seconds));
                renters.add(player.getName().toLowerCase());

                if (renterEntries.size() == 1) {
                    scheduleNextRentUpdate();
                }
                ChatHelper.send(player, "fieldSignRentRented", s.getPeriod());

                PreciousStones.getInstance().getEntryManager().leaveField(player, field);
                PreciousStones.getInstance().getEntryManager().enterField(player, field);
            }

            field.getFlagsModule().dirtyFlags("addRent");
        }
    }

    public void removeRenter(RentEntry entry) {
        String renterName = entry.getPlayerName().toLowerCase();
        PreciousStones.getInstance().getForceFieldManager().removeRenter(field, renterName);
        renterEntries.remove(entry);
        renters.remove(renterName);

        field.getFlagsModule().dirtyFlags("removeRenter");
    }

    public boolean clearRents() {
        if (hasRenters()) {
            renterEntries.clear();
            renters.clear();
            cleanFieldSign();

            field.getFlagsModule().dirtyFlags("clearRents");
            return true;
        }
        return false;
    }

    public boolean removeRents() {
        FieldSign s = field.getAttachedFieldSign();

        if (s != null) {
            s.eject();

            renterEntries.clear();
            renters.clear();
            payment.clear();

            field.getFlagsModule().dirtyFlags("removeRents");
            return true;
        }

        return false;
    }

    public List<RentEntry> getRenterEntries() {
        return Collections.unmodifiableList(renterEntries);
    }

    public void abandonRent(Player player) {
        for (RentEntry entry : renterEntries) {
            if (entry.getPlayerName().equals(player.getName())) {
                removeRenter(entry);
                cleanFieldSign();
                return;
            }
        }
    }

    public void cleanFieldSign() {
        if (!hasRenters()) {
            FieldSign s = field.getAttachedFieldSign();

            if (s != null) {
                s.setAvailableColor();
                s.cleanRemainingTime();
            }
        }
    }

    public void addPayment(String playerName, String fieldName, BlockTypeEntry item, int amount) {
        boolean added = false;

        for (PaymentEntry entry : payment) {
            if (entry.getPlayer().equals(playerName) && (item == null || entry.getItem().equals(item))) {
                entry.setAmount(entry.getAmount() + amount);
                added = true;
            }
        }

        if (!added) {
            payment.add(new PaymentEntry(playerName, fieldName, item, amount));
        }

        field.getFlagsModule().dirtyFlags("addPayment");
    }

    public boolean rent(Player player, FieldSign s) {
        if (getLimitSeconds() > 0) {
            PreciousStones.debug("field has rent limits in place: " + getLimitSeconds());

            RentEntry renter = getRenter(player);

            if (renter != null) {
                int seconds = SignHelper.periodToSeconds(s.getPeriod());

                if (renter.getPeriodSeconds() + seconds > getLimitSeconds()) {
                    PreciousStones.debug("limit reached");
                    ChatHelper.send(player, "limitReached");
                    return false;
                }
            }
        }

        PreciousStones plugin = PreciousStones.getInstance();
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(s.getField());
        if (plugin.getLimitManager().reachedLimit(player, fs)) {
            PreciousStones.debug("field limit reached");
            return false;
        }

        if (s.getItem() != null) {
            PreciousStones.debug("is item rent");

            if (StackHelper.hasItems(player, s.getItem(), s.getPrice())) {
                StackHelper.remove(player, s.getItem(), s.getPrice());

                addPayment(player.getName(), s.getField().getName(), s.getItem(), s.getPrice());
                addRent(player);

                PreciousStones.getInstance().getCommunicationManager().logPayment(field.getOwner(), player.getName(), s);
            } else {
                ChatHelper.send(player, "economyNotEnoughItems");
                return false;
            }
        } else {
            if (PreciousStones.getInstance().getPermissionsManager().hasEconomy()) {
                if (PermissionsManager.hasMoney(player, s.getPrice())) {
                    PreciousStones.getInstance().getPermissionsManager().playerCharge(player, s.getPrice());

                    addPayment(player.getName(), s.getField().getName(), null, s.getPrice());
                    addRent(player);

                    PreciousStones.getInstance().getCommunicationManager().logPayment(field.getOwner(), player.getName(), s);
                } else {
                    ChatHelper.send(player, "economyNotEnoughMoney");
                    return false;
                }
            }
        }

        if (s.isShareable()) {
            s.setSharedColor();
        } else if (s.isRentable()) {
            s.setRentedColor();
        }

        return true;
    }

    public boolean hasPendingPayments() {
        return !payment.isEmpty();
    }

    public void takePayment(Player player) {
        for (PaymentEntry entry : payment) {
            if (entry.isItemPayment()) {
                StackHelper.give(player, entry.getItem(), entry.getAmount());

                if (entry.getFieldName().isEmpty()) {
                    ChatHelper.send(player, "fieldSignItemPaymentReceivedNoName", entry.getAmount(), entry.getItem(), entry.getPlayer());
                } else {
                    ChatHelper.send(player, "fieldSignItemPaymentReceived", entry.getAmount(), entry.getItem(), entry.getPlayer(), entry.getFieldName());
                }
            } else {
                PreciousStones.getInstance().getPermissionsManager().playerCredit(player, entry.getAmount());

                if (entry.getFieldName().isEmpty()) {
                    ChatHelper.send(player, "fieldSignPaymentReceivedNoName", entry.getAmount(), entry.getPlayer());
                } else {
                    ChatHelper.send(player, "fieldSignPaymentReceived", entry.getAmount(), entry.getPlayer(), entry.getFieldName());
                }
            }
        }

        PreciousStones.getInstance().getCommunicationManager().logPaymentCollect(field.getOwner(), player.getName(), field.getAttachedFieldSign());

        payment.clear();
        field.getFlagsModule().dirtyFlags("takePayment");
    }

    private class Update implements Runnable {
        protected void updateRent() {
            if (hasRenters()) {
                FieldSign s = field.getAttachedFieldSign();

                if (s != null) {
                    if (s.isRentable() || s.isShareable()) {
                        boolean foundSomeone = false;

                        if (PreciousStones.getInstance().getEntryManager().hasInhabitants(field)) {
                            Player closest = Helper.getClosestPlayer(field.getLocation(), 64);
                            if (closest != null) {
                                for (RentEntry entry : renterEntries) {
                                    if (entry.getPlayerName().equalsIgnoreCase(closest.getName())) {
                                        s.updateRemainingTime(entry.remainingRent());
                                        foundSomeone = true;
                                        signIsClean = false;
                                    }
                                }
                            }
                        }

                        if (!foundSomeone && !signIsClean) {
                            s.cleanRemainingTime();
                            signIsClean = true;
                        }
                    }
                }
            }

            for (Iterator iter = renterEntries.iterator(); iter.hasNext(); ) {
                RentEntry entry = (RentEntry) iter.next();

                if (entry.isDone()) {
                    renters.remove(entry.getPlayerName().toLowerCase());
                    iter.remove();

                    field.getFlagsModule().dirtyFlags("RentUpdateRunnable");

                    if (field.getName().isEmpty()) {
                        ChatHelper.send(entry.getPlayerName(), "fieldSignRentExpiredNoName");
                    } else {
                        ChatHelper.send(entry.getPlayerName(), "fieldSignRentExpired", field.getName());
                    }
                }
            }
        }

        public void run() {
            try {
                updateRent();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            scheduleNextRentUpdate();
        }
    }

    public void scheduleNextRentUpdate() {
        if (!renterEntries.isEmpty()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PreciousStones.getInstance(), new Update(), 20);
        }
    }

}
