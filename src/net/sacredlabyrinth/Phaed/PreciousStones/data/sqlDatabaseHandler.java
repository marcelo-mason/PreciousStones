package net.sacredlabyrinth.Phaed.PreciousStones.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class sqlDatabaseHandler
{
    /*
     * @author: alta189
     *
     */
    private sqlCore core;
    private Connection connection;
    private Statement batchStatement;
    private File SQLFile;

    public sqlDatabaseHandler(sqlCore core, File SQLFile)
    {
        this.core = core;
        this.SQLFile = SQLFile;
    }

    public Connection getConnection()
    {
        if (connection == null)
        {
            initialize();
        }
        return connection;
    }

    public void closeConnection()
    {
        if (this.connection != null)
        {
            try
            {
                this.connection.close();
            }
            catch (SQLException ex)
            {
                this.core.writeError("Error on Connection close: " + ex, true);
            }
        }
    }

    public Boolean initialize()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + SQLFile.getAbsolutePath());
            return true;
        }
        catch (SQLException ex)
        {
            core.writeError("SQLite exception on initialize " + ex, true);
        }
        catch (ClassNotFoundException ex)
        {
            core.writeError("You need the SQLite library " + ex, true);
        }
        return false;
    }

    public void openBatch() throws SQLException
    {
        Connection con = getConnection();
        con.setAutoCommit(false);
        batchStatement = con.createStatement();
    }

    public void addBatch(String sql) throws SQLException
    {
        batchStatement.addBatch(sql);
    }

    public void closeBatch() throws SQLException
    {
        Connection con = getConnection();
        batchStatement.executeBatch();
        con.commit();
        con.setAutoCommit(true);
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

    public ResultSet sqlQuery(String query)
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
            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked"))
            {
                return retryResult(query);
            }
            else
            {
                core.writeError("Error at SQL Query: " + ex.getMessage(), false);
            }

        }
        return null;
    }

    public void insertQuery(String query)
    {
        try
        {
            Connection con = getConnection();
            con.setAutoCommit(true);
            Statement statement = con.createStatement();

            statement.executeQuery(query);


        }
        catch (SQLException ex)
        {

            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked"))
            {
                retry(query);
            }
            else
            {
                if (!ex.toString().contains("not return ResultSet"))
                {
                    //core.writeError("Error at SQL INSERT Query: " + ex, false);
                }
            }

        }
    }

    public void updateQuery(String query)
    {
        try
        {
            Connection con = getConnection();
            con.setAutoCommit(true);
            Statement statement = con.createStatement();

            statement.executeQuery(query);


        }
        catch (SQLException ex)
        {
            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked"))
            {
                retry(query);
            }
            else
            {
                if (!ex.toString().contains("not return ResultSet"))
                {
                    core.writeError("Error at SQL UPDATE Query: " + ex, false);
                }
            }
        }
    }

    public void deleteQuery(String query)
    {
        try
        {
            Connection con = getConnection();
            con.setAutoCommit(true);
            Statement statement = con.createStatement();

            statement.executeQuery(query);


        }
        catch (SQLException ex)
        {
            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked"))
            {
                retry(query);
            }
            else
            {
                if (!ex.toString().contains("not return ResultSet"))
                {
                    core.writeError("Error at SQL DELETE Query: " + ex, false);
                }
            }
        }
    }

    public Boolean wipeTable(String table)
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
            statement.executeQuery(query);

            return true;
        }
        catch (SQLException ex)
        {
            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked"))
            {
                //retryWipe(query);
            }
            else
            {
                if (!ex.toString().contains("not return ResultSet"))
                {
                    core.writeError("Error at SQL WIPE TABLE Query: " + ex, false);
                }
            }
            return false;
        }
    }

    public Boolean checkTable(String table)
    {
        DatabaseMetaData dbm;
        try
        {
            dbm = this.getConnection().getMetaData();
            ResultSet tables = dbm.getTables(null, null, table, null);
            if (tables.next())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            core.writeError("Failed to check if table \"" + table + "\" exists: " + e.getMessage(), true);
            return false;
        }

    }

    private ResultSet retryResult(String query)
    {
        Boolean passed = false;

        while (!passed)
        {
            try
            {
                Connection con = getConnection();
                con.setAutoCommit(true);
                Statement statement = con.createStatement();

                ResultSet result = statement.executeQuery(query);

                passed = true;

                return result;
            }
            catch (SQLException ex)
            {

                if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked"))
                {
                    passed = false;
                }
                else
                {
                    core.writeError("Error at SQL Query: " + ex.getMessage(), false);
                }
            }
        }

        return null;
    }

    private void retry(String query)
    {
        Boolean passed = false;

        while (!passed)
        {
            try
            {
                Connection con = getConnection();
                con.setAutoCommit(true);
                Statement statement = con.createStatement();

                statement.executeQuery(query);

                passed = true;

                return;
            }
            catch (SQLException ex)
            {

                if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked"))
                {
                    passed = false;
                }
                else
                {
                    core.writeError("Error at SQL Query: " + ex.getMessage(), false);
                }
            }
        }

        return;
    }
}
