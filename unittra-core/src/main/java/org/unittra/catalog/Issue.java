package org.unittra.catalog;

/**
 * Represents an issue tracked in an external system. 
 */
public class Issue {
    
    private String _id;
    private boolean _enriched = false;
    private String _status;
    private String _type;
    private String _summary;
    
    public Issue(String id) {
        _id = id;
    }
    
    public Issue(String id, String type, String status, String summary) {
        _id = id;
        _type = type;
        _status = status;
        _summary = summary;
        _enriched = true;
    }
    
    public boolean isEnriched() {
        return _enriched;
    }
    
    public String getId() {
        return _id;
    }
    
    public String getSummary() {
        return _summary;
    }
    
    public String getStatus() {
        return _status;
    }
    
    public String getType() {
        return _type;
    }
    
    public boolean isMarkedAsDone() {
        if (_status != null) {
            if (_status.equals("peerreview") || _status.equals("resolved") || _status.equals("closed")) {
                return true;
            }
        }
        
        return false;
    }
}
