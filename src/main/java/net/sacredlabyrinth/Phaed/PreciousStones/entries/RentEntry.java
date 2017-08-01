package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;

import java.time.*;

import static java.time.temporal.ChronoUnit.SECONDS;

public class RentEntry {
    private String playerName;
    private ZonedDateTime endDate;
    private int periodSeconds;

    public RentEntry(String playerName, int periodSeconds) {
        this.playerName = playerName;
        this.periodSeconds = periodSeconds;
        this.endDate = LocalDateTime.now().plusSeconds(periodSeconds).atZone(ZoneId.systemDefault());
    }

    public RentEntry(String packed) {
        String[] unpacked = packed.split("[|]");

        this.playerName = unpacked[0];
        this.periodSeconds = Integer.parseInt(unpacked[1]);
        this.endDate = Instant.ofEpochMilli(Long.parseLong(unpacked[2])).atZone(ZoneId.systemDefault());
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
        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());
        return SECONDS.between(now, endDate) <= 0;
    }

    public int remainingRent() {
        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());
        return (int)SECONDS.between(now, endDate);
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
        long millis = Helper.getMillis();
        return playerName + "|" + periodSeconds + "|" + millis;
    }
}
