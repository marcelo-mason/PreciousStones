package net.sacredlabyrinth.Phaed.PreciousStones.storage;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 *
 * @author phaed
 */
public interface DBCore
{
    public Connection getConnection();

    public Boolean checkConnection();

    public void close();

    public ResultSet select(String query);

    public void insert(String query);

    public void update(String query);

    public void delete(String query);

    public Boolean execute(String query);

    public Boolean existsTable(String table);

}
