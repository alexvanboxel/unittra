package org.unittra.util.progresslogger;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamProgressLogger extends InputStream {
    
    private InputStream _passthrough;
    
    public int hashCode() {
        return _passthrough.hashCode();
    }

    public int read(byte[] b) throws IOException {
        return _passthrough.read(b);
    }

    public boolean equals(Object obj) {
        return _passthrough.equals(obj);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return _passthrough.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return _passthrough.skip(n);
    }

    public int available() throws IOException {
        return _passthrough.available();
    }

    public String toString() {
        return _passthrough.toString();
    }

    public void close() throws IOException {
        _passthrough.close();
    }

    public void mark(int readlimit) {
        _passthrough.mark(readlimit);
    }

    public void reset() throws IOException {
        _passthrough.reset();
    }

    public boolean markSupported() {
        return _passthrough.markSupported();
    }

    @Override
    public int read() throws IOException {
        return _passthrough.read();
    }
    
}
