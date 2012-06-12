package org.unittra.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MetaModel {
    
    private Connection _connection;
    private Map<String, MetaTable> _tables = new HashMap<String, MetaTable>();
    
    public MetaModel(Connection connection) {
        _connection = connection;
    }
    
    public void disconnect() throws SQLException {
        _connection.close();
    }
    
    public Connection getConnection() {
        return _connection;
    }
    
    public MetaTable getTable(String name) {
        return _tables.get(name);
    }
    
    public void addTable(MetaTable table) {
        _tables.put(table.TABLE_NAME, table);
    }
    
    public void buildTables() throws SQLException {
        DatabaseMetaData metaData = _connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, null, null);
        while (tables.next()) {
            addTable(new MetaTable(this, tables));
        }
    }
    
    public void buildColumns() throws SQLException {
        DatabaseMetaData metaData = _connection.getMetaData();
        for (MetaTable table : _tables.values()) {
            table.columns(metaData);
        }
    }
    
    public void buildFKey() throws SQLException {
        DatabaseMetaData metaData = _connection.getMetaData();
        for (MetaTable table : _tables.values()) {
            table.keys(metaData);
        }
    }
    
    public Collection<MetaTable> getTables() {
        return _tables.values();
    }
}
