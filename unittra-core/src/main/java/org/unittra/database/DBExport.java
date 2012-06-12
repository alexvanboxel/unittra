package org.unittra.database;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamWriter;

public class DBExport {
    
    private MetaModel _model;
    private String _url;
    private String _user;
    private String _pass;
    
    
    public DBExport(String url, String user, String pass) {
        _url = url;
        _user = user;
        _pass = pass;
    }
    
    public void connect() throws SQLException {
        _model = new MetaModel(DriverManager.getConnection(_url, _user, _pass));        
    }
    
    public void close() {
        try {
            _model.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }
    
    public void export(XMLStreamWriter writer) throws Exception {
        writer.writeStartDocument();
        writer.writeStartElement("export");
        Set<MetaTable> exported = new HashSet<MetaTable>();
        for (MetaTable table : _model.getTables()) {
            table.export(writer, exported);
        }
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
    }
    
    public void buildModel() throws SQLException {
        _model.buildTables();
        _model.buildColumns();
        _model.buildFKey();
    }
}
