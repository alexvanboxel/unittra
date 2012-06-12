package org.unittra.catalog;

import java.util.Map;

public class HoldMessage {
    
    private Class<?> holdClass;
    
    private String message;
    
    private Map<String, String> properties;
    
    
    public HoldMessage(Class<?> holdClass, String message) {
        this.holdClass = holdClass;
        this.message = message;
        this.properties = null;
    }
    
    public HoldMessage(Class<?> holdClass, String message, Map<String, String> properties) {
        this.holdClass = holdClass;
        this.message = message;
        this.properties = properties;
    }
    
    public Class<?> getHoldClass() {
        return holdClass;
    }
    
    public String getMessage() {
        return message;
    }
}
