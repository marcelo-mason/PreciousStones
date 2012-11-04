package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.Dates;

import java.util.Date;

public class RentEntry
{
    private String playerName;
    private Date startDate;
    private int periodSeconds;

    public RentEntry(String playerName, int periodSeconds)
    {
        this.playerName = playerName;
        this.startDate = new Date();
        this.periodSeconds = periodSeconds;
    }

    public RentEntry(String packed)
    {
        String[] unpacked = packed.split("[|]");

        this.playerName = unpacked[0];
        this.periodSeconds = Integer.parseInt(unpacked[1]);
        this.startDate = new Date(Long.parseLong(unpacked[1]));
    }

    public void addSeconds(int seconds)
    {
        this.periodSeconds += seconds;
    }

    public int getPeriodSeconds()
    {
        return periodSeconds;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public boolean isDone()
    {
        return Dates.differenceInSeconds(new Date(), startDate) >= periodSeconds;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof RentEntry))
        {
            return false;
        }

        RentEntry other = (RentEntry) obj;
        return other.getPlayerName().equals(this.getPlayerName());
    }

    @Override
    public int hashCode()
    {
        return this.getPlayerName().hashCode();
    }

    public String serialize()
    {
        return playerName + "|" + periodSeconds + "|" + startDate.getTime();
    }
}
