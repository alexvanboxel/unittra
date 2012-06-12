package org.unittra.database;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MetaTable {
    final static Logger logger = LoggerFactory.getLogger(MetaTable.class);
    
    MetaModel _model;

    String TABLE_CAT;
    String TABLE_SCHEM;
    String TABLE_NAME;
    
    Set<MetaTable> relationTo;
    Set<MetaTable> relationFrom;
    ArrayList<MetaColumn> columns;
    
    MetaTable(MetaModel model,ResultSet rs) throws SQLException {
        _model = model;
        TABLE_CAT = rs.getString("TABLE_CAT");
        TABLE_SCHEM = rs.getString("TABLE_SCHEM");
        TABLE_NAME = rs.getString("TABLE_NAME");
        relationTo = new HashSet<MetaTable>();
        relationFrom = new HashSet<MetaTable>();
        columns = new ArrayList<MetaColumn>();
    }
    
    void keys(DatabaseMetaData md) throws SQLException {
        logger.debug("Getting foreign key relations of table {}.",TABLE_NAME);
        ResultSet fk = md.getImportedKeys(null, null, TABLE_NAME);
        while (fk.next()) {
            logger.debug(fk.getString("FK_NAME")+"[" + fk.getString("KEY_SEQ") + "]," +fk.getString("PK_NAME")+ " : " +fk.getString("FKTABLE_NAME") + "(" + fk.getString("FKCOLUMN_NAME") + ")" + " -> " + fk.getString("PKTABLE_NAME") + "(" + fk.getString("PKCOLUMN_NAME") + ")");
            MetaTable foreign = _model.getTable(fk.getString("PKTABLE_NAME"));
            relationTo.add(foreign);
            foreign.relationFrom.add(this);
        }
    }
    
    void columns(DatabaseMetaData md) throws SQLException {
        ResultSet rs = md.getColumns(null, null, TABLE_NAME, null);
        while (rs.next()) {
            MetaColumn column = new MetaColumn(_model,rs);
            columns.add(column);
        }
        rs.close();
    }
    
    void delete(Set<MetaTable> deleted) throws SQLException {
        if (deleted.contains(this)) {
            return;
        }
        logger.debug("Trying to delete database table {}.", TABLE_NAME);
        for (MetaTable from : relationFrom) {
            from.delete(deleted);
        }
        Statement statement = _model.getConnection().createStatement();
        int count = statement.executeUpdate("DELETE FROM " + TABLE_NAME);
        statement.close();
        deleted.add(this);
        logger.info("Deleted {} rows from database table {}.", count, TABLE_NAME);
    }
    
    public void drop(Set<MetaTable> dropped) throws SQLException {
        if (dropped.contains(this)) {
            return;
        }
        logger.debug("Trying to delete database table {}.", TABLE_NAME);
        for (MetaTable from : relationFrom) {
            if(from != this) {
                from.drop(dropped);
            }
        }
        Statement statement = _model.getConnection().createStatement();
        statement.execute("DROP TABLE " + TABLE_NAME);
        statement.close();
        dropped.add(this);
        logger.info("Dropped database table {}.", TABLE_NAME);
    }
    
    public void export(XMLStreamWriter writer, Set<MetaTable> exported) throws Exception {
        if (exported.contains(this)) {
            return;
        }
        for (MetaTable from : relationFrom) {
            from.export(writer, exported);
        }
        writer.writeStartElement("table");
        writer.writeAttribute("TABLE_CAT", TABLE_CAT);
        if (TABLE_SCHEM != null)
            writer.writeAttribute("TABLE_SCHEM", TABLE_SCHEM);
        writer.writeAttribute("TABLE_NAME", TABLE_NAME);
        for (MetaColumn column : columns) {
            column.write(writer);
        }
        Statement statement = _model.getConnection().createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
        while (rs.next()) {
            writer.writeStartElement("row");
            for (MetaColumn column : columns) {
                if (rs.wasNull()) {
                    writer.writeEmptyElement("null");
                } else {
                    writer.writeStartElement("data");
                    writer.writeCharacters(rs.getString(column.COLUMN_NAME));
                    writer.writeEndElement();
                }
            }
            writer.writeEndElement();
        }
        logger.info("Exported database table {}.",TABLE_NAME);
        writer.writeEndElement();
    }
}
