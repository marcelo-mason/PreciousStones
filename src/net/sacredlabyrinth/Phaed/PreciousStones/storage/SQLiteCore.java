package net.sacredlabyrinth.Phaed.PreciousStones.storage;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import java.io.File;
import java.sql.*;
import java.util.logging.Logger;

/**
 * @author cc_madelg
 */
public class SQLiteCore implements DBCore
{
    private Logger log;
    private Connection connection;
    private String dbLocation;
    private String dbName;
    private File file;

    /**
     * @param log
     * @param dbName
     * @param dbLocation
     */
    public SQLiteCore(String dbName, String dbLocation)
    {
        this.dbName = dbName;
        this.dbLocation = dbLocation;
        this.log = PreciousStones.getLog();
    }

    private void initialize()
    {
        if (file == null)
        {
            File dbFolder = new File(dbLocation);

            if (dbName.contains("/") || dbName.contains("\\") || dbName.endsWith(".db"))
            {
                log.severe("The database name can not contain: /, \\, or .db");
                return;
            }
            if (!dbFolder.exists())
            {
                dbFolder.mkdir();
            }

            file = new File(dbFolder.getAbsolutePath() + File.separator + dbName + ".db");
        }

        try
        {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            return;
        }
        catch (SQLException ex)
        {
            log.severe("SQLite exception on initialize " + ex);
        }
        catch (ClassNotFoundException ex)
        {
            log.severe("You need the SQLite library " + ex);
        }
    }

    /**
     * @return connection
     */
    public Connection getConnection()
    {
        try
        {
            if (connection == null || connection.isClosed())
            {
                initialize();
            }
        }
        catch (SQLException e)
        {
            initialize();
        }

        return connection;
    }

    /**
     * @return whether connection can be established
     */
    public Boolean checkConnection()
    {
        return getConnection() != null;
    }

    /**
     * Close connection
     */
    public void close()
    {
        try
        {
            if (connection != null)
            {
                connection.close();
            }
        }
        catch (Exception e)
        {
            log.severe("Failed to close database connection! " + e.getMessage());
        }
    }

    /**
     * Execute a select statement
     *
     * @param query
     * @return
     */
    public ResultSet select(String query)
    {
        try
        {
            Statement statement = getConnection().createStatement();
            return statement.executeQuery(query);
        }
        catch (SQLException ex)
        {
            log.severe("Error at SQL Query: " + ex.getMessage());
            log.severe("Query: " + query);
        }
        return null;
    }

    /**
     * Execute an insert statement
     *
     * @param query
     */
    public long insert(String query)
    {
        if (PreciousStones.getInstance().getSettingsManager().isDebugsql())
        {
            PreciousStones.getLog().info(query);
        }

        try
        {
            Statement statement = getConnection().createStatement();
            ResultSet keys = null;

            try
            {
                statement.executeUpdate(query);
                keys = statement.executeQuery("SELECT last_insert_rowid()");
            }
            finally
            {
                if (keys != null)
                {
                    if (keys.next())
                    {
                        return keys.getLong(1);
                    }
                }
                statement.close();
                return 0;
            }
        }
        catch (SQLException ex)
        {
            if (!ex.toString().contains("not return ResultSet"))
            {
                log.severe("Error at SQL INSERT Query: " + ex);
                log.severe("Query: " + query);
            }
        }

        return 0;
    }

    /**
     * Execute an update statement
     *
     * @param query
     */
    public void update(String query)
    {
        if (PreciousStones.getInstance().getSettingsManager().isDebugsql())
        {
            PreciousStones.getLog().info(query);
        }

        try
        {
            Statement statement = getConnection().createStatement();

            try
            {
                statement.executeQuery(query);
            }
            finally
            {
                statement.close();
            }
        }
        catch (SQLException ex)
        {
            if (!ex.toString().contains("not return ResultSet"))
            {
                log.severe("Error at SQL UPDATE Query: " + ex);
                log.severe("Query: " + query);
            }
        }
    }

    /**
     * Execute a delete statement
     *
     * @param query
     */
    public void delete(String query)
    {
        if (PreciousStones.getInstance().getSettingsManager().isDebugsql())
        {
            PreciousStones.getLog().info(query);
        }

        try
        {
            Statement statement = getConnection().createStatement();

            try
            {
                statement.executeQuery(query);
            }
            finally
            {
                statement.close();
            }
        }
        catch (SQLException ex)
        {
            if (!ex.toString().contains("not return ResultSet"))
            {
                log.severe("Error at SQL DELETE Query: " + ex);
                log.severe("Query: " + query);
            }
        }
    }

    /**
     * Execute a statement
     *
     * @param query
     * @return
     */
    public Boolean execute(String query)
    {
        if (PreciousStones.getInstance().getSettingsManager().isDebugsql())
        {
            PreciousStones.getLog().info(query);
        }

        try
        {
            Statement statement = getConnection().createStatement();
            Boolean result = false;
            try
            {
                result = statement.execute(query);
            }
            finally
            {
                statement.close();
                return result;
            }
        }
        catch (SQLException ex)
        {
            log.severe(ex.getMessage());
            log.severe("Query: " + query);
            return false;
        }
    }

    /**
     * Check whether a table exists
     *
     * @param table
     * @return
     */
    public Boolean existsTable(String table)
    {
        try
        {
            ResultSet tables = getConnection().getMetaData().getTables(null, null, table, null);
            return tables.next();
        }
        catch (SQLException e)
        {
            log.severe("Failed to check if table \"" + table + "\" exists: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check whether a column exists
     *
     * @param table
     * @param column
     * @return
     */
    public Boolean existsColumn(String column, String table)
    {
        try
        {
            Statement statement = getConnection().createStatement();

            try
            {
                statement.executeQuery("SELECT " + column + " FROM " + table);
            }
            finally
            {
                statement.close();
                return true;
            }
        }
        catch (Exception ex)
        {
            return false;
        }
    }
}
