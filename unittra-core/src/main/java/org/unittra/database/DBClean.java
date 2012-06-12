package org.unittra.database;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBClean {
    final static Logger logger = LoggerFactory.getLogger(DBClean.class);
    
    private MetaModel _model;
    private String _url;
    private String _user;
    private String _pass;
    
    public DBClean(String url, String user, String pass) {
        _url = url;
        _user = user;
        _pass = pass;
    }
    
    public void connect() throws SQLException {
        logger.info("Connection to DB at "+_url);
        _model = new MetaModel(DriverManager.getConnection(_url, _user, _pass));
    }
    
    public void close() {
        try {
            _model.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }
    
    public void delete() throws SQLException {
        Set<MetaTable> deleted = new HashSet<MetaTable>();
        for (MetaTable table : _model.getTables()) {
            table.delete(deleted);
        }
    }
    
    public void drop() throws SQLException {
        Set<MetaTable> dropped = new HashSet<MetaTable>();
        for (MetaTable table : _model.getTables()) {
            table.drop(dropped);
        }
    }
    
    public void buildModel() throws SQLException {
        _model.buildTables();
        _model.buildFKey();
    }
}
