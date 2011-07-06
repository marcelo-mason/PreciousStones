package net.sacredlabyrinth.Phaed.PreciousStones.data;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class mysqlCore implements DBCore
{
    private Logger log;
    public String host;
    private mysqlDatabaseHandler manageDB;
    public String username;
    public String password;
    public String database;

    public mysqlCore(Logger log, String host, String database, String username, String password)
    {
        this.log = log;
        this.database = database;
        this.host = host;
        this.username = username;
        this.password = password;
    }

    @Override
    public Boolean initialize()
    {
        manageDB = new mysqlDatabaseHandler(this, host, database, username, password);
        return false;
    }

    @Override
    public void writeInfo(String toWrite)
    {
        if (toWrite != null)
        {
            log.info(toWrite);
        }
    }

    @Override
    public void writeError(String toWrite, Boolean severe)
    {
        if (severe)
        {
            if (toWrite != null)
            {
                log.severe(toWrite);
            }
        }
        else
        {
            if (toWrite != null)
            {
                log.warning(toWrite);
            }
        }
    }

    @Override
    public ResultSet sqlQuery(String query)
    {
        try
        {
            return manageDB.sqlQuery(query);
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public Boolean createTable(String query)
    {
        return manageDB.createTable(query);
    }

    @Override
    public void insertQuery(String query)
    {
        try
        {
            manageDB.insertQuery(query);
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void updateQuery(String query)
    {
        try
        {
            manageDB.updateQuery(query);
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void deleteQuery(String query)
    {
        try
        {
            manageDB.deleteQuery(query);
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Boolean checkTable(String table)
    {
        try
        {
            return manageDB.checkTable(table);
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    @Override
    public Boolean wipeTable(String table)
    {
        try
        {
            return manageDB.wipeTable(table);
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    @Override
    public Connection getConnection()
    {
        try
        {
            return manageDB.getConnection();
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            Logger.getLogger(mysqlCore.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public void close()
    {
        manageDB.closeConnection();
    }

    @Override
    public Boolean checkConnection()
    {
        return manageDB.checkConnection();
    }
}
