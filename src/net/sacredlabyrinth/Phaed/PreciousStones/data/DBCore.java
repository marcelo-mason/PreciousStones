package net.sacredlabyrinth.Phaed.PreciousStones.data;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 *
 * @author phaed
 */
public interface DBCore
{
    public Boolean initialize();

    public void writeInfo(String toWrite);

    public void writeError(String toWrite, Boolean severe);

    public ResultSet sqlQuery(String query);

    public Boolean createTable(String query);

    public void insertQuery(String query);

    public void updateQuery(String query);

    public void deleteQuery(String query);

    public Boolean checkTable(String table);

    public Boolean wipeTable(String table);

    public Connection getConnection();

    public void close();

    public Boolean checkConnection();
}
