package org.unittra.util.progresslogger;

import org.slf4j.Logger;

public class StreamProgressLogger {
    // [>>>.File.png.>>>>>>>>>>>>>>>>...................]  (5114/5114) 45kbp
    
    private Logger _logger;
    private boolean _up;
    
    long _size;
    long _pointer;
    
    public StreamProgressLogger(Logger logger, boolean up) {
        _logger = logger;
        _up = up;
    }
    
    private void report(long transfered, long total) {
        double t = (double) total / (double) transfered;
        int transfer = (int) (50 * t);
        int rest = 50 - transfer;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < transfer; i++) {
            buffer.append('>');
        }
        for (int i = 0; i < rest; i++) {
            buffer.append('.');
        }
        buffer.append(" (" + transfered + "/" + total + ")");
        _logger.info(buffer.toString());
    }
    
    

}
