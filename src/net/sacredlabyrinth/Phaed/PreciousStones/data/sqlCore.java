package net.sacredlabyrinth.Phaed.PreciousStones.data;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.logging.Logger;

public class sqlCore implements DBCore
{

    /*
     *  @author: alta189
     *
     */
    private Logger log;
    public String dbLocation;
    public String dbName;
    private sqlDatabaseHandler manageDB;

    public sqlCore(Logger log, String dbName, String dbLocation)
    {
        this.log = log;
        this.dbName = dbName;
        this.dbLocation = dbLocation;
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
    public Boolean initialize()
    {
        File dbFolder = new File(dbLocation);

        if (dbName.contains("/") || dbName.contains("\\") || dbName.endsWith(".db"))
        {
            writeError("The database name can not contain: /, \\, or .db", true);
            return false;
        }
        if (!dbFolder.exists())
        {
            dbFolder.mkdir();
        }

        File SQLFile = new File(dbFolder.getAbsolutePath() + File.separator + dbName + ".db");

        manageDB = new sqlDatabaseHandler(this, SQLFile);

        return manageDB.initialize();
    }

    @Override
    public ResultSet sqlQuery(String query)
    {
        return manageDB.sqlQuery(query);
    }

    @Override
    public Boolean createTable(String query)
    {
        return manageDB.createTable(query);
    }

    @Override
    public void insertQuery(String query)
    {
        manageDB.insertQuery(query);
    }

    @Override
    public void updateQuery(String query)
    {
        manageDB.updateQuery(query);
    }

    @Override
    public void deleteQuery(String query)
    {
        manageDB.deleteQuery(query);
    }

    @Override
    public Boolean checkTable(String table)
    {
        return manageDB.checkTable(table);
    }

    @Override
    public Boolean wipeTable(String table)
    {
        return manageDB.wipeTable(table);
    }

    @Override
    public Connection getConnection()
    {
        return manageDB.getConnection();
    }

    @Override
    public void close()
    {
        manageDB.closeConnection();
    }

    @Override
    public Boolean checkConnection()
    {
        Connection con = manageDB.getConnection();

        if (con != null)
        {
            return true;
        }
        return false;
    }
}
