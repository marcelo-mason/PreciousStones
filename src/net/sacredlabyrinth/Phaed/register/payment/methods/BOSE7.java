package net.sacredlabyrinth.Phaed.register.payment.methods;

import cosine.boseconomy.BOSEconomy;
import net.sacredlabyrinth.Phaed.register.payment.Method;
import org.bukkit.plugin.Plugin;

public class BOSE7
  implements Method
{
  private BOSEconomy BOSEconomy;

  public BOSEconomy getPlugin()
  {
    return BOSEconomy;
  }

  public String getName() {
    return "BOSEconomy";
  }

  public String getVersion() {
    return "0.7.0";
  }

  public int fractionalDigits() {
    return BOSEconomy.getFractionalDigits();
  }

  public String format(double amount) {
    String currency = BOSEconomy.getMoneyNamePlural();

    if (amount == 1.0D) {
      currency = BOSEconomy.getMoneyName();
    }
    return amount + " " + currency;
  }

  public boolean hasBanks() {
    return true;
  }

  public boolean hasBank(String bank) {
    return BOSEconomy.bankExists(bank);
  }

  public boolean hasAccount(String name) {
    return BOSEconomy.playerRegistered(name, false);
  }

  public boolean hasBankAccount(String bank, String name) {
    return (BOSEconomy.isBankOwner(bank, name)) || (BOSEconomy.isBankMember(bank, name));
  }

  public Method.MethodAccount getAccount(String name) {
    if (!hasAccount(name)) {
      return null;
    }
    return new BOSEAccount(name, BOSEconomy);
  }

  public Method.MethodBankAccount getBankAccount(String bank, String name) {
    if (!hasBankAccount(bank, name)) {
      return null;
    }
    return new BOSEBankAccount(bank, BOSEconomy);
  }

  public boolean isCompatible(Plugin plugin) {
    return (plugin.getDescription().getName().equalsIgnoreCase("boseconomy")) && ((plugin instanceof BOSEconomy)) && (!plugin.getDescription().getVersion().equals("0.6.2"));
  }

  public void setPlugin(Plugin plugin)
  {
    BOSEconomy = ((BOSEconomy)plugin);
  }

  public class BOSEBankAccount
    implements Method.MethodBankAccount
  {
    private String bank;
    private BOSEconomy BOSEconomy;

    public BOSEBankAccount(String bank, BOSEconomy bOSEconomy)
    {
      this.bank = bank;
      BOSEconomy = bOSEconomy;
    }

    public String getBankName() {
      return bank;
    }

    public int getBankId() {
      return -1;
    }

    public double balance() {
      return BOSEconomy.getBankMoneyDouble(bank);
    }

    public boolean set(double amount) {
      return BOSEconomy.setBankMoney(bank, amount, true);
    }

    public boolean add(double amount) {
      double balance = balance();
      return BOSEconomy.setBankMoney(bank, balance + amount, false);
    }

    public boolean subtract(double amount) {
      double balance = balance();
      return BOSEconomy.setBankMoney(bank, balance - amount, false);
    }

    public boolean multiply(double amount) {
      double balance = balance();
      return BOSEconomy.setBankMoney(bank, balance * amount, false);
    }

    public boolean divide(double amount) {
      double balance = balance();
      return BOSEconomy.setBankMoney(bank, balance / amount, false);
    }

    public boolean hasEnough(double amount) {
      return balance() >= amount;
    }

    public boolean hasOver(double amount) {
      return balance() > amount;
    }

    public boolean hasUnder(double amount) {
      return balance() < amount;
    }

    public boolean isNegative() {
      return balance() < 0.0D;
    }

    public boolean remove() {
      return BOSEconomy.removeBank(bank);
    }
  }

  public class BOSEAccount
    implements Method.MethodAccount
  {
    private String name;
    private BOSEconomy BOSEconomy;

    public BOSEAccount(String name, BOSEconomy bOSEconomy)
    {
      this.name = name;
      BOSEconomy = bOSEconomy;
    }

    public double balance() {
      return BOSEconomy.getPlayerMoneyDouble(name);
    }

    public boolean set(double amount) {
      return BOSEconomy.setPlayerMoney(name, amount, false);
    }

    public boolean add(double amount) {
      return BOSEconomy.addPlayerMoney(name, amount, false);
    }

    public boolean subtract(double amount) {
      double balance = balance();
      return BOSEconomy.setPlayerMoney(name, balance - amount, false);
    }

    public boolean multiply(double amount) {
      double balance = balance();
      return BOSEconomy.setPlayerMoney(name, balance * amount, false);
    }

    public boolean divide(double amount) {
      double balance = balance();
      return BOSEconomy.setPlayerMoney(name, balance / amount, false);
    }

    public boolean hasEnough(double amount) {
      return balance() >= amount;
    }

    public boolean hasOver(double amount) {
      return balance() > amount;
    }

    public boolean hasUnder(double amount) {
      return balance() < amount;
    }

    public boolean isNegative() {
      return balance() < 0.0D;
    }

    public boolean remove() {
      return false;
    }
  }
}