package net.sacredlabyrinth.Phaed.PreciousStones.storage;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import java.sql.*;
import java.util.logging.Logger;

/**
 * @author cc_madelg
 */
public class MySQLCore implements DBCore
{
    private Logger log;
    private Connection connection;
    private String host;
    private String username;
    private String password;
    private String database;
    private int port;

    /**
     * @param host
     * @param database
     * @param username
     * @param password
     */
    public MySQLCore(String host, int port, String database, String username, String password)
    {
        this.database = database;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.log = PreciousStones.getLog();
    }

    private void initialize()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        }
        catch (ClassNotFoundException e)
        {
            log.severe("ClassNotFoundException! " + e.getMessage());
        }
        catch (SQLException e)
        {
            log.severe("SQLException! " + e.getMessage());
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
                statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
                keys = statement.getGeneratedKeys();
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
                statement.executeUpdate(query);
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
                statement.executeUpdate(query);
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

            try
            {
                statement.execute(query);
            }
            finally
            {
                statement.close();
                return true;
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
            Statement statement = getConnection().createStatement();
            ResultSet result = null;

            try
            {
                result = statement.executeQuery("SELECT * FROM " + table);
            }
            finally
            {
                statement.close();

                if (result == null)
                {
                    return false;
                }

                return result != null;
            }
        }
        catch (SQLException ex)
        {
            if (!ex.getMessage().contains("exist"))
            {
                log.warning("Error at SQL Query: " + ex.getMessage());
            }
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
    public Boolean existsColumn(String table, String column)
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
