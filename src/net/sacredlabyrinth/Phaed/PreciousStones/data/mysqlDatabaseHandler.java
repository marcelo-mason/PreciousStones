package net.sacredlabyrinth.Phaed.PreciousStones.data;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class mysqlDatabaseHandler
{
    private mysqlCore core;
    Connection connection;
    private Statement batchStatement;
    private String dblocation;
    private String username;
    private String password;
    private String database;

    public mysqlDatabaseHandler(mysqlCore core, String dbLocation, String database, String username, String password)
    {
        this.core = core;
        this.dblocation = dbLocation;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    private void openConnection() throws MalformedURLException, InstantiationException, IllegalAccessException
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + dblocation + "/" + database, username, password);
        }
        catch (ClassNotFoundException e)
        {
            core.writeError("ClassNotFoundException! " + e.getMessage(), true);
        }
        catch (SQLException e)
        {
            core.writeError("SQLException! " + e.getMessage(), true);
        }
    }

    public Boolean checkConnection()
    {
        if (connection == null)
        {
            try
            {
                openConnection();
                return true;
            }
            catch (MalformedURLException ex)
            {
                core.writeError("MalformedURLException! " + ex.getMessage(), true);
            }
            catch (InstantiationException ex)
            {
                core.writeError("InstantiationExceptioon! " + ex.getMessage(), true);
            }
            catch (IllegalAccessException ex)
            {
                core.writeError("IllegalAccessException! " + ex.getMessage(), true);
            }
            return false;
        }
        return true;
    }

    public void closeConnection()
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
            core.writeError("Failed to close database connection! " + e.getMessage(), true);
        }
    }

    public Connection getConnection() throws MalformedURLException, InstantiationException, IllegalAccessException
    {
        if (connection == null)
        {
            openConnection();
        }
        return connection;
    }

    public void openBatch() throws SQLException, MalformedURLException, InstantiationException, IllegalAccessException
    {
        Connection con = getConnection();
        con.setAutoCommit(false);
        batchStatement = con.createStatement();
    }

    public void addBatch(String sql) throws SQLException
    {
        batchStatement.addBatch(sql);
    }

    public void closeBatch() throws SQLException, MalformedURLException, IllegalAccessException, InstantiationException
    {
        Connection con = getConnection();
        batchStatement.executeBatch();
        con.commit();
        con.setAutoCommit(true);
    }

    public ResultSet sqlQuery(String query) throws MalformedURLException, InstantiationException, IllegalAccessException
    {
        try
        {
            Connection con = getConnection();
            con.setAutoCommit(true);
            Statement statement = con.createStatement();

            ResultSet result = statement.executeQuery(query);

            return result;
        }
        catch (SQLException ex)
        {
            core.writeError("Error at SQL Query: " + ex.getMessage(), false);
        }
        return null;
    }

    public void insertQuery(String query) throws MalformedURLException, InstantiationException, IllegalAccessException
    {
        try
        {
            Connection con = getConnection();
            con.setAutoCommit(true);
            Statement statement = con.createStatement();

            statement.executeUpdate(query);


        }
        catch (SQLException ex)
        {

            if (!ex.toString().contains("not return ResultSet"))
            {
                //core.writeError("Error at SQL INSERT Query: " + ex, false);
            }
        }
    }

    public void updateQuery(String query) throws MalformedURLException, InstantiationException, IllegalAccessException
    {
        try
        {
            Connection con = getConnection();
            con.setAutoCommit(true);
            Statement statement = con.createStatement();

            statement.executeUpdate(query);


        }
        catch (SQLException ex)
        {

            if (!ex.toString().contains("not return ResultSet"))
            {
                core.writeError("Error at SQL UPDATE Query: " + ex, false);
            }

        }
    }

    public void deleteQuery(String query) throws MalformedURLException, InstantiationException, IllegalAccessException
    {
        try
        {
            Connection con = getConnection();
            con.setAutoCommit(true);
            Statement statement = con.createStatement();

            statement.executeUpdate(query);


        }
        catch (SQLException ex)
        {

            if (!ex.toString().contains("not return ResultSet"))
            {
                core.writeError("Error at SQL DELETE Query: " + ex, false);
            }

        }
    }

    public Boolean checkTable(String table) throws MalformedURLException, InstantiationException, IllegalAccessException
    {
        try
        {
            Connection con = getConnection();
            con.setAutoCommit(true);
            Statement statement = con.createStatement();

            ResultSet result = statement.executeQuery("SELECT * FROM " + table);

            if (result == null)
            {
                return false;
            }
            if (result != null)
            {
                return true;
            }
        }
        catch (SQLException ex)
        {
            if (ex.getMessage().contains("exist"))
            {
                return false;
            }
            else
            {
                core.writeError("Error at SQL Query: " + ex.getMessage(), false);
            }
        }


        if (sqlQuery("SELECT * FROM " + table) == null)
        {
            return true;
        }
        return false;
    }

    public Boolean wipeTable(String table) throws MalformedURLException, InstantiationException, IllegalAccessException
    {
        try
        {
            if (!core.checkTable(table))
            {
                core.writeError("Error at Wipe Table: table, " + table + ", does not exist", true);
                return false;
            }
            Connection con = getConnection();
            con.setAutoCommit(true);
            Statement statement = con.createStatement();
            String query = "DELETE FROM " + table + ";";
            statement.executeUpdate(query);

            return true;
        }
        catch (SQLException ex)
        {
            if (!ex.toString().contains("not return ResultSet"))
            {
                core.writeError("Error at SQL WIPE TABLE Query: " + ex, false);
            }
            return false;
        }
    }

    public Boolean createTable(String query)
    {
        try
        {
            if (query == null)
            {
                core.writeError("SQL Create Table query empty.", true);
                return false;
            }

            Statement statement = connection.createStatement();
            statement.execute(query);
            return true;
        }
        catch (SQLException ex)
        {
            core.writeError(ex.getMessage(), true);
            return false;
        }
    }
}
