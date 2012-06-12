package org.unittra.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.ComparisonFailure;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.unittra.catalog.Issue;
import org.unittra.context.PropertyDefinition;
import org.unittra.context.TC;


public class XMLLog extends LogListener {
    
    private XMLStreamWriter _writer;
    
    public XMLLog(TC testContext) throws Exception {
        super(testContext);
        File dir = new File("target/report");
        dir.mkdirs();
        File file = new File("target/report/report.xml");
        FileOutputStream out = new FileOutputStream(file);
        OutputStreamWriter xmlLogWriter = new OutputStreamWriter(out, "utf-8");
        _writer = XMLOutputFactory.newInstance().createXMLStreamWriter(xmlLogWriter);
        head();
    }
    
    @Override
    public void writeHead() throws Exception {
        _writer.writeStartDocument("utf-8", "1.0");
        _writer.writeProcessingInstruction("xml-stylesheet href='report-api.xsl' type='text/xsl'");
        _writer.writeStartElement("report");
        // Properties
        _writer.writeStartElement("properties");
        Map<String, PropertyDefinition> properties = TC.i(TC.class).getPropertyMap();
        
        List<String> propertyKeys = new ArrayList<String>(properties.keySet());
        Collections.sort(propertyKeys);
        
        for (String propertyKey : propertyKeys) {
            PropertyDefinition propertyDefinition = properties.get(propertyKey);
            _writer.writeStartElement("property");
            _writer.writeAttribute("name", (String) propertyDefinition.getName());
            _writer.writeAttribute("value", (String) propertyDefinition.getValue());
            for (PropertyDefinition historical : propertyDefinition.getHistory()) {
                if (historical.getSource() != null) {
                    _writer.writeStartElement("history");
                    _writer.writeAttribute("source", historical.getSource());
                    _writer.writeAttribute("value", historical.getValue());
                    _writer.writeEndElement();
                }
            }
            _writer.writeEndElement();
        }
        _writer.writeEndElement();
        // Issue summary
        _writer.writeStartElement("issues");
        Collection<Issue> issues = TC.i(TC.class).getIssues();
        if (issues != null) {
            for (Issue issue : issues) {
                _writer.writeStartElement("issue");
                _writer.writeAttribute("id", issue.getId());
                if (issue.getStatus() != null)
                    _writer.writeAttribute("status", issue.getStatus());
                if (issue.getType() != null)
                    _writer.writeAttribute("type", issue.getType());
                if (issue.getSummary() != null)
                    _writer.writeAttribute("summary", issue.getSummary());
                _writer.writeEndElement();
            }
        }
        _writer.writeEndElement();
        // Begin writing tests
        _writer.writeStartElement("test-results");
        _writer.writeStartElement("test-group");
    }
    
    @Override
    public void writeFoot() throws Exception {
        _writer.writeEndElement();
        _writer.writeEndElement();
        _writer.writeEndElement();
        _writer.writeEndDocument();
        _writer.close();
    }
    
    @Override
    public void writeSuccess(Description description) throws Exception {
        _writer.writeStartElement("test");
        _writer.writeAttribute("name", description.toString());
        _writer.writeAttribute("result", "success");
        _writer.writeEndElement();
        
    }
    
    
    @Override
    public void writeIgnored(Description description) throws Exception {
        _writer.writeStartElement("test");
        _writer.writeAttribute("name", description.toString());
        _writer.writeAttribute("result", "hold");
        
        List<Issue> issues = TC.i(TC.class).getIssues(description);
        if (issues != null) {
            for (Issue issue : issues) {
                _writer.writeStartElement("issue");
                _writer.writeAttribute("id", issue.getId());
                if (issue.getStatus() != null)
                    _writer.writeAttribute("status", issue.getStatus());
                if (issue.getType() != null)
                    _writer.writeAttribute("type", issue.getType());
                if (issue.getSummary() != null)
                    _writer.writeAttribute("summary", issue.getSummary());
                _writer.writeEndElement();
            }
        }
        _writer.writeEndElement();
    }
    
    
    public void writeDiff(String expected, String actual) throws XMLStreamException {
        if (expected != null) {
            _writer.writeStartElement("expected");
            _writer.writeCData(expected);
            _writer.writeEndElement();
        } else {
            _writer.writeEmptyElement("expected");
        }
        if (actual != null) {
            _writer.writeStartElement("actual");
            _writer.writeCData(actual);
            _writer.writeEndElement();
        } else {
            _writer.writeEmptyElement("actual");
        }
    }
    
    
    @Override
    public void writeFailure(Description description, Failure failure) throws Exception {
        _writer.writeStartElement("test");
        if (description != null) {
            _writer.writeAttribute("name", description.toString());
        } else {
            _writer.writeAttribute("name", failure.getDescription().toString());
        }
        
        Throwable throwable = failure.getException();
        if (throwable != null) {
            if ((throwable instanceof ComparisonFailure) || (throwable instanceof org.junit.ComparisonFailure)) {
                _writer.writeAttribute("result", "diff");
            } else {
                _writer.writeAttribute("result", "error");
            }
        } else {
            _writer.writeAttribute("result", "error");
        }
        String message = failure.getMessage();
        if (message != null) {
            _writer.writeStartElement("message");
            _writer.writeCharacters(message);
            _writer.writeEndElement();
        }
        if (throwable != null) {
            if (throwable instanceof ComparisonFailure) {
                ComparisonFailure cf = (ComparisonFailure) throwable;
                writeDiff(cf.getExpected(), cf.getActual());
            } else if (throwable instanceof org.junit.ComparisonFailure) {
                org.junit.ComparisonFailure cf = (org.junit.ComparisonFailure) throwable;
                writeDiff(cf.getExpected(), cf.getActual());
            }
            _writer.writeStartElement("exception");
            StringWriter buffer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(buffer));
            _writer.writeCData(buffer.toString());
            _writer.writeEndElement();
        }
        _writer.writeEndElement();
    }
}
