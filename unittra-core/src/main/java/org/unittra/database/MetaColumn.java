package org.unittra.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

class MetaColumn {
    MetaModel _model;
    
    String COLUMN_NAME;
    String TYPE_NAME;
    Integer COLUMN_SIZE;
    Integer DECIMAL_DIGITS;
    Integer NUM_PREC_RADIX;
    Integer NULLABLE;
    String REMARKS;
    String COLUMN_DEF;
    Integer CHAR_OCTET_LENGTH;
    Integer ORDINAL_POSITION;
    String IS_NULLABLE;
    String SCOPE_CATLOG;
    String SCOPE_SCHEMA;
    String SCOPE_TABLE;
    String SOURCE_DATA_TYPE;
    String IS_AUTOINCREMENT;
    
    public MetaColumn(MetaModel model, ResultSet rs) throws SQLException {
        _model = model;
        COLUMN_NAME = rs.getString("COLUMN_NAME");
        //DATA_TYPE int => SQL type from java.sql.Types 
        TYPE_NAME = rs.getString("TYPE_NAME");
        COLUMN_SIZE = rs.getInt("COLUMN_SIZE");
        DECIMAL_DIGITS = (Integer) rs.getObject("DECIMAL_DIGITS");
        NUM_PREC_RADIX = (Integer) rs.getObject("NUM_PREC_RADIX");
        NULLABLE = (Integer) rs.getInt("NULLABLE");
        //columnNoNulls - might not allow NULL values 
        //columnNullable - definitely allows NULL values 
        //columnNullableUnknown - nullability unknown
        REMARKS = rs.getString("REMARKS");
        COLUMN_DEF = rs.getString("COLUMN_DEF");
        CHAR_OCTET_LENGTH = (Integer) rs.getInt("CHAR_OCTET_LENGTH");
        // int => for char types the maximum number of bytes in the column 
        ORDINAL_POSITION = (Integer) rs.getInt("ORDINAL_POSITION");
        IS_NULLABLE = rs.getString("IS_NULLABLE");
        //YES --- if the parameter can include NULLs 
        //NO --- if the parameter cannot include NULLs 
        //empty string --- if the nullability for the parameter is unknown 
        SCOPE_CATLOG = null;
        SCOPE_SCHEMA = null;
        SCOPE_TABLE = null;
        SOURCE_DATA_TYPE = null;
        IS_AUTOINCREMENT = rs.getString("IS_AUTOINCREMENT");
        //YES --- if the column is auto incremented 
        //NO --- if the column is not auto incremented 
        //empty string --- if it cannot be determined whether the column is auto incremented parameter is unknown             
    }

    public MetaColumn(MetaModel model, XMLStreamReader reader) throws XMLStreamException {
        read(reader);
    }
    
    public void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("column");
        writer.writeAttribute("COLUMN_NAME", COLUMN_NAME);
        writer.writeAttribute("TYPE_NAME", TYPE_NAME);
        writer.writeAttribute("COLUMN_SIZE", COLUMN_SIZE.toString());
        if (DECIMAL_DIGITS != null)
            writer.writeAttribute("DECIMAL_DIGITS", DECIMAL_DIGITS.toString());
        writer.writeAttribute("NUM_PREC_RADIX", NUM_PREC_RADIX.toString());
        writer.writeAttribute("NULLABLE", NULLABLE.toString());
        writer.writeAttribute("REMARKS", REMARKS);
        if (COLUMN_DEF != null)
            writer.writeAttribute("COLUMN_DEF", COLUMN_DEF);
        writer.writeAttribute("CHAR_OCTET_LENGTH", CHAR_OCTET_LENGTH.toString());
        writer.writeAttribute("ORDINAL_POSITION", ORDINAL_POSITION.toString());
        writer.writeAttribute("IS_NULLABLE", IS_NULLABLE.toString());
        if (SCOPE_CATLOG != null)
            writer.writeAttribute("SCOPE_CATLOG", SCOPE_CATLOG);
        if (SCOPE_SCHEMA != null)
            writer.writeAttribute("SCOPE_SCHEMA", SCOPE_SCHEMA);
        if (SCOPE_TABLE != null)
            writer.writeAttribute("SCOPE_TABLE", SCOPE_TABLE);
        if (SOURCE_DATA_TYPE != null)
            writer.writeAttribute("SOURCE_DATA_TYPE", SOURCE_DATA_TYPE);
        writer.writeAttribute("IS_AUTOINCREMENT", IS_AUTOINCREMENT);
        writer.writeEndElement();
    }
    
    public void writeRef(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("column");
        writer.writeAttribute("ref", COLUMN_NAME);
        writer.writeEndElement();
    }
    
    public void read(XMLStreamReader reader) throws XMLStreamException {
        String value;
        COLUMN_NAME = reader.getAttributeValue(null, "COLUMN_NAME");
        TYPE_NAME = reader.getAttributeValue(null, "TYPE_NAME");
        COLUMN_SIZE = Integer.valueOf(reader.getAttributeValue(null, "COLUMN_SIZE"));
        value = reader.getAttributeValue(null, "DECIMAL_DIGITS");
        if(value!=null) 
            DECIMAL_DIGITS = Integer.valueOf(value);
        NUM_PREC_RADIX = Integer.valueOf(reader.getAttributeValue(null, "NUM_PREC_RADIX"));
        NULLABLE = Integer.valueOf(reader.getAttributeValue(null, "NULLABLE"));
        REMARKS = reader.getAttributeValue(null, "REMARKS");
        COLUMN_DEF = reader.getAttributeValue(null, "COLUMN_DEF");
        CHAR_OCTET_LENGTH = Integer.valueOf(reader.getAttributeValue(null, "CHAR_OCTET_LENGTH"));
        ORDINAL_POSITION = Integer.valueOf(reader.getAttributeValue(null, "ORDINAL_POSITION"));
        IS_NULLABLE = reader.getAttributeValue(null, "IS_NULLABLE");
        SCOPE_CATLOG = reader.getAttributeValue(null, "SCOPE_CATLOG");
        SCOPE_SCHEMA = reader.getAttributeValue(null, "SCOPE_SCHEMA");
        SCOPE_TABLE = reader.getAttributeValue(null, "SCOPE_TABLE");
        SOURCE_DATA_TYPE = reader.getAttributeValue(null, "SOURCE_DATA_TYPE");
        IS_AUTOINCREMENT = reader.getAttributeValue(null, "IS_AUTOINCREMENT");
    }  
}
