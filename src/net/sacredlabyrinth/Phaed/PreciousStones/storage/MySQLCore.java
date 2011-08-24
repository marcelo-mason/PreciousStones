package net.sacredlabyrinth.Phaed.PreciousStones.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

/**
 *
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

    /**
     *
     * @param host
     * @param database
     * @param username
     * @param password
     */
    public MySQLCore(String host, String database, String username, String password)
    {
        this.database = database;
        this.host = host;
        this.username = username;
        this.password = password;
        this.log = PreciousStones.getLogger();

        initialize();
    }

    private void initialize()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database, username, password);
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
    @Override
    public Connection getConnection()
    {
        if (connection == null)
        {
            initialize();
        }

        return connection;
    }

    /**
     * @return whether connection can be established
     */
    @Override
    public Boolean checkConnection()
    {
        return getConnection() != null;
    }

    /**
     * Close connection
     */
    @Override
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
     * @param query
     * @return
     */
    @Override
    public ResultSet select(String query)
    {
        try
        {
            return getConnection().createStatement().executeQuery(query);
        }
        catch (SQLException ex)
        {
            log.severe("Error at SQL Query: " + ex.getMessage());
        }

        return null;
    }

    /**
     * Execute an insert statement
     * @param query
     */
    @Override
    public void insert(String query)
    {
        try
        {
            getConnection().createStatement().executeUpdate(query);
        }
        catch (SQLException ex)
        {
            if (!ex.toString().contains("not return ResultSet"))
            {
                log.severe("Error at SQL INSERT Query: " + ex);
            }
        }
    }

    /**
     * Execute an update statement
     * @param query
     */
    @Override
    public void update(String query)
    {
        try
        {
            getConnection().createStatement().executeUpdate(query);
        }
        catch (SQLException ex)
        {
            if (!ex.toString().contains("not return ResultSet"))
            {
                log.severe("Error at SQL UPDATE Query: " + ex);
            }
        }
    }

    /**
     * Execute a delete statement
     * @param query
     */
    @Override
    public void delete(String query)
    {
        try
        {
            getConnection().createStatement().executeUpdate(query);
        }
        catch (SQLException ex)
        {
            if (!ex.toString().contains("not return ResultSet"))
            {
                log.severe("Error at SQL DELETE Query: " + ex);
            }
        }
    }

    /**
     * Execute a statement
     * @param query
     * @return
     */
    @Override
    public Boolean execute(String query)
    {
        try
        {
            getConnection().createStatement().execute(query);
            return true;
        }
        catch (SQLException ex)
        {
            log.severe(ex.getMessage());
            return false;
        }
    }

    /**
     * Check whether a table exists
     * @param table
     * @return
     */
    @Override
    public Boolean existsTable(String table)
    {
        try
        {
            ResultSet result = getConnection().createStatement().executeQuery("SELECT * FROM " + table);
            return result != null;
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
}
