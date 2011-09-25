package net.sacredlabyrinth.Phaed.register.payment;

import org.bukkit.plugin.Plugin;

public abstract interface Method
{
  public abstract Object getPlugin();

  public abstract String getName();

  public abstract String getVersion();

  public abstract int fractionalDigits();

  public abstract String format(double paramDouble);

  public abstract boolean hasBanks();

  public abstract boolean hasBank(String paramString);

  public abstract boolean hasAccount(String paramString);

  public abstract boolean hasBankAccount(String paramString1, String paramString2);

  public abstract MethodAccount getAccount(String paramString);

  public abstract MethodBankAccount getBankAccount(String paramString1, String paramString2);

  public abstract boolean isCompatible(Plugin paramPlugin);

  public abstract void setPlugin(Plugin paramPlugin);

  public static abstract interface MethodBankAccount
  {
    public abstract double balance();

    public abstract String getBankName();

    public abstract int getBankId();

    public abstract boolean set(double paramDouble);

    public abstract boolean add(double paramDouble);

    public abstract boolean subtract(double paramDouble);

    public abstract boolean multiply(double paramDouble);

    public abstract boolean divide(double paramDouble);

    public abstract boolean hasEnough(double paramDouble);

    public abstract boolean hasOver(double paramDouble);

    public abstract boolean hasUnder(double paramDouble);

    public abstract boolean isNegative();

    public abstract boolean remove();

    public abstract String toString();
  }

  public static abstract interface MethodAccount
  {
    public abstract double balance();

    public abstract boolean set(double paramDouble);

    public abstract boolean add(double paramDouble);

    public abstract boolean subtract(double paramDouble);

    public abstract boolean multiply(double paramDouble);

    public abstract boolean divide(double paramDouble);

    public abstract boolean hasEnough(double paramDouble);

    public abstract boolean hasOver(double paramDouble);

    public abstract boolean hasUnder(double paramDouble);

    public abstract boolean isNegative();

    public abstract boolean remove();

    public abstract String toString();
  }
}