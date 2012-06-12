package org.unittra.util.progresslogger;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;

public class OutputStreamProgressLogger extends OutputStream {
    
    private Logger _logger;
    private OutputStream _passthrough;
    
    public OutputStreamProgressLogger(Logger logger,OutputStream passthrough) {
        _logger = logger;
        _passthrough = passthrough;
    }
    
    public int hashCode() {
        return _passthrough.hashCode();
    }
    
    public void write(byte[] b) throws IOException {
        _passthrough.write(b);
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        _passthrough.write(b, off, len);
    }
    
    public boolean equals(Object obj) {
        return _passthrough.equals(obj);
    }
    
    public void flush() throws IOException {
        _passthrough.flush();
    }
    
    public void close() throws IOException {
        _passthrough.close();
    }
    
    public String toString() {
        return _passthrough.toString();
    }
    
    @Override
    public void write(int b) throws IOException {
        _passthrough.write(b);
    }
    
}
