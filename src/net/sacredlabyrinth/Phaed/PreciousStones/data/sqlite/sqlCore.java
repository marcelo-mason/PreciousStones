package net.sacredlabyrinth.Phaed.PreciousStones.data.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.logging.Logger;

public class sqlCore
{

    /*
     *  @author: alta189
     *
     */
    private Logger log;
    private String logPrefix;
    public String dbLocation;
    public String dbName;
    private DatabaseHandler manageDB;

    public sqlCore(Logger log, String logPrefix, String dbName, String dbLocation)
    {
        this.log = log;
        this.logPrefix = logPrefix;
        this.dbName = dbName;
        this.dbLocation = dbLocation;

    }

    public void writeInfo(String toWrite)
    {
        if (toWrite != null)
        {
            this.log.info(this.logPrefix + toWrite);
        }
    }

    public void writeError(String toWrite, Boolean severe)
    {
        if (severe)
        {
            if (toWrite != null)
            {
                this.log.severe(this.logPrefix + toWrite);
            }
        }
        else
        {
            if (toWrite != null)
            {
                this.log.warning(this.logPrefix + toWrite);
            }
        }
    }

    public Boolean initialize()
    {
        File dbFolder = new File(dbLocation);
        if (dbName.contains("/") || dbName.contains("\\") || dbName.endsWith(".db"))
        {
            this.writeError("The database name can not contain: /, \\, or .db", true);
            return false;
        }
        if (!dbFolder.exists())
        {
            dbFolder.mkdir();
        }

        File SQLFile = new File(dbFolder.getAbsolutePath() + File.separator + dbName + ".db");

        this.manageDB = new DatabaseHandler(this, SQLFile);

        return this.manageDB.initialize();
    }

    public ResultSet sqlQuery(String query)
    {
        return this.manageDB.sqlQuery(query);
    }

    public Boolean createTable(String query)
    {
        return this.manageDB.createTable(query);
    }

    public void insertQuery(String query)
    {
        this.manageDB.insertQuery(query);
    }

    public void updateQuery(String query)
    {
        this.manageDB.updateQuery(query);
    }

    public void deleteQuery(String query)
    {
        this.manageDB.deleteQuery(query);
    }

    public Boolean checkTable(String table)
    {
        return this.manageDB.checkTable(table);
    }

    public Boolean wipeTable(String table)
    {
        return this.manageDB.wipeTable(table);
    }

    public Connection getConnection()
    {
        return this.manageDB.getConnection();
    }

    public void close()
    {
        this.manageDB.closeConnection();
    }

    public Boolean checkConnection()
    {
        Connection con = this.manageDB.getConnection();

        if (con != null)
        {
            return true;
        }
        return false;
    }
}
