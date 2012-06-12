package org.unittra.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Alex Van Boxel
 */
public class PropertyDefinition {
    
    private String _name;
    private String _value;
    private String _source;
    private List<PropertyDefinition> _history = new ArrayList<PropertyDefinition>();
    
    public PropertyDefinition(String name, String value, String source) {
        this(name, value, source, null);
    }
    
    public PropertyDefinition(String name, String value, String source, PropertyDefinition definition) {
        assert name != null;
        assert value != null;
        _name = name;
        _value = value;
        _source = source;
        _history.add(this);
        if (definition != null) {
            _history.addAll(definition.getHistory());
        }
    }
    
    public String getName() {
        return _name;
    }
    
    public String getValue() {
        return _value;
    }
    
    public String getUnresolved() {
        return _value;
    }
    
    public String getSource() {
        return _source;
    }
    
    public List<PropertyDefinition> getHistory() {
        return Collections.unmodifiableList(_history);
    }
}
