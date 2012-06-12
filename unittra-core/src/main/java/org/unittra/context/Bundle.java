package org.unittra.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Bundle implements Iterable<Bundle.Entry> {
    
    public static class Entry {
        Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        private String key;
        private String value;
        
        public String getKey() {
            return key;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    private String prefix;
    private Map<String, Entry> entries;
    
    private Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();
    
    public Bundle(Map<String, PropertyDefinition> properties, String prefix) {
        this.properties = properties;
        this.prefix = prefix;
        
        // Resolve the bundle
        entries = new HashMap<String, Entry>();
        int prefixLength = prefix.length();
        for (java.util.Map.Entry<String, PropertyDefinition> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.length() == prefix.length()) {
                entries.put("", new Entry("", entry.getValue().getValue()));
            } else if (key.startsWith(prefix)) {
                String subKey = key.substring(prefixLength + 1);
                entries.put(subKey, new Entry(subKey, entry.getValue().getValue()));
            }
        }
    }
    
    public int size() {
        return entries.size();
    }
    
    public String get(String subKey) {
        PropertyDefinition definition = properties.get(prefix + "." + subKey);
        if (definition == null) {
            throw new RuntimeException("Could not resolve propert with name " + subKey + " in bundle " + prefix);
        }
        return (String) definition.getValue();
    }
    
    public String getValue() {
        if (size() != 1) {
            throw new RuntimeException("Doing getValue() only works on atomic bundles.");
        }
        for (Entry entry : entries.values()) {
            return entry.getValue();
        }
        throw new RuntimeException("Internal error, no value found.");
    }
    
    public List<String> getList() {
        List<String> array = new ArrayList<String>();
        for (int ix = 1; ix <= size(); ix++) {
            array.add(entries.get(String.valueOf(ix)).getValue());
        }
        return array;
        
        
    }
    
    public boolean isMultiValue() {
        return false;
    }
    
    public Iterator<Entry> iterator() {
        return entries.values().iterator();
    }
}
