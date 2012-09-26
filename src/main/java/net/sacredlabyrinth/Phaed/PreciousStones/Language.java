package net.sacredlabyrinth.Phaed.PreciousStones;

public class Language
{
    public static String get(String key)
    {
        return PreciousStones.getInstance().getLanguageManager().get(key);
    }
}