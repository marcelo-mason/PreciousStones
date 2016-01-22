package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

public class RentEntry {
    private String playerName;
    private DateTime endDate;
    private int periodSeconds;

    public RentEntry(String playerName, int periodSeconds) {
        this.playerName = playerName;
        this.periodSeconds = periodSeconds;
        this.endDate = (new DateTime()).plusSeconds(periodSeconds);
    }

    public RentEntry(String packed) {
        String[] unpacked = packed.split("[|]");

        this.playerName = unpacked[0];
        this.periodSeconds = Integer.parseInt(unpacked[1]);
        this.endDate = new DateTime(Long.parseLong(unpacked[2]));
    }

    public void addSeconds(int seconds) {
        this.periodSeconds += seconds;
        this.endDate = endDate.plusSeconds(seconds);
    }

    public int getPeriodSeconds() {
        return periodSeconds;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isDone() {
        return Seconds.secondsBetween(new DateTime(), endDate).getSeconds() <= 0;
    }

    public int remainingRent() {
        return Seconds.secondsBetween(new DateTime(), endDate).getSeconds();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RentEntry)) {
            return false;
        }

        RentEntry other = (RentEntry) obj;
        return other.getPlayerName().equals(this.getPlayerName());
    }

    @Override
    public int hashCode() {
        return this.getPlayerName().hashCode();
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String serialize() {
        return playerName + "|" + periodSeconds + "|" + endDate.getMillis();
    }
}
