package org.unittra.context;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.unittra.catalog.Condition;
import org.unittra.catalog.Issue;
import org.unittra.catalog.TestDescription;
import org.unittra.catalog.TestDescription.Method;
import org.unittra.report.JUnitLogFactory;
import org.unittra.report.RunListenerFactory;
import org.unittra.report.XMLLogFactory;


public class DefaultTestContext extends TC {
    
    /**
     * List of test defined in the catalog. Note that the mentioned tests could mean different things in different modes.
     */
    private ArrayList<TestDescription> _testList = new ArrayList<TestDescription>();
    
    /**
     * List of issues that are mentioned in the catalog.
     */
    private Map<String, Issue> _issuesList = new HashMap<String, Issue>();
    
    /**
     * List of conditions that are mentioned in the catalog.
     */
    private Set<Condition> conditionList = new HashSet<Condition>();
    
    /**
     * Map of globally defined property definitions.
     */
    private Map<String, PropertyDefinition> _properties = new HashMap<String, PropertyDefinition>();
    
    /**
     * Part of the mode state machine. If _run is true it means that all tests are run, except the tests that are listed, if _run is false it means the reverse: no tests are run, except the tests mentioned.
     */
    private boolean _run = true;
    
    /**
     * Special toggle for the mode state machine. If this is true all the tests are run, regarding that they are listed or not.
     */
    private boolean _full = false;
    
    /**
     * URI of the config file that is loaded.
     */
    private URI _configURI;
    
    /**
     * Map of registered JUnit runlisteners.
     */
    Map<String, RunListener> _runListeners = new HashMap<String, RunListener>();
    
    public DefaultTestContext() {
    }
    
    /**
     * Create an ad-hoc test context. This type of test context is used when the test context is not fully mature and is in the process of being build.
     * 
     * @param properties
     */
    public DefaultTestContext(Properties properties) {
        if (properties != null) {
            for (Entry<Object, Object> property : properties.entrySet()) {
                _properties.put((String) property.getKey(), new PropertyDefinition((String) property.getKey(), (String) property.getValue(), "ad-hoc"));
            }
        }
    }
    
    private void addNamedRunListener(RunNotifier runNotifier, String name, RunListenerFactory factory) throws Exception {
        if (_runListeners.get(name) == null) {
            RunListener listener = factory.create(this);
            _runListeners.put(name, listener);
            runNotifier.addListener(listener);
        }
    }
    
    /**
     * Attach our internal logger in at runtime.
     * 
     * @throws Exception
     */
    public synchronized void addTestListeners(RunNotifier runNotifier) throws Exception {
        addNamedRunListener(runNotifier, "xml", new XMLLogFactory());
        addNamedRunListener(runNotifier, "junit", new JUnitLogFactory());
    }
    
    public boolean shouldRun(Description description) {
        if (_full) {
            return _run;
        }
        TestDescription currentTD = new TestDescription(description);
        
        boolean foundHold = false;
        for (TestDescription testDescription : _testList) {
            if (testDescription.contains(currentTD)) {
                List<Condition> conditions = testDescription.getConditions(this);
                List<Issue> issues = testDescription.getIssuesNotDone();
                if (conditions.size() > 0) {
                    foundHold = true;
                }
                if (issues.size() > 0) {
                    foundHold = true;
                }
            }
        }
        //
        if (foundHold) {
            return !_run;
        }
        //
        return _run;
    }
    
    @Override
    public List<Issue> getIssues(Description description) {
        TestDescription current = new TestDescription(description);
        Iterator<TestDescription> iterator = _testList.iterator();
        while (iterator.hasNext()) {
            TestDescription td = iterator.next();
            if (td.equals(current)) {
                return td.getIssues();
            }
        }
        return null;
    }
    
    @Override
    protected void initDefault() {
    }
    
    protected void readPropertiesBlock(XMLStreamReader reader) throws Exception {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                if (QName.valueOf("property").equals(reader.getName())) {
                    String name = reader.getAttributeValue(null, "name");
                    String value = reader.getAttributeValue(null, "value");
                    _properties.put(name, readPropertyHistory(name, value, reader));
                }
            } else if (event == XMLStreamReader.END_ELEMENT) {
                if (QName.valueOf("properties").equals(reader.getName())) {
                    _properties.put("test.config", new PropertyDefinition("test.config", _configURI.toString(), "DefaultTestContext.class"));
                    break;
                }
            }
        }
    }
    
    protected PropertyDefinition readPropertyHistory(String name, String value, XMLStreamReader reader) throws Exception {
        ArrayList<PropertyDefinition> _list = new ArrayList<PropertyDefinition>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                if (QName.valueOf("history").equals(reader.getName())) {
                    String source = reader.getAttributeValue(null, "source");
                    String hvalue = reader.getAttributeValue(null, "value");
                    _list.add(0, new PropertyDefinition(name, hvalue, source));
                }
            } else if (event == XMLStreamReader.END_ELEMENT) {
                if (QName.valueOf("property").equals(reader.getName())) {
                    if (_list.size() == 0) {
                        return new PropertyDefinition(name, value, null);
                    }
                    PropertyDefinition propertyDefinition = null;
                    for (PropertyDefinition def : _list) {
                        propertyDefinition = new PropertyDefinition(def.getName(), def.getValue(), def.getSource(), propertyDefinition);
                    }
                    return propertyDefinition;
                }
            }
        }
        throw new RuntimeException("End should have been handled.");
    }
    
    /**
     * Reads a specific block in the configuration file. Normal implementation is a switch over the recognised XML elements in the configuration file.
     * 
     * Sub classes should override this method by going over it's recognised elements. If it doesn't recognise any the elements it should delegate futher search to it's super class.
     * 
     * <pre>
     * if (QName.valueOf(&quot;something&quot;).equals(name)) {
     *     doSomething(reader);
     * } else {
     *     super.readBlock(reader, name);
     * }
     * </pre>
     * 
     * @param reader
     * @param name
     * @throws Exception
     */
    protected void readBlock(XMLStreamReader reader, QName name) throws Exception {
        if (QName.valueOf("run").equals(name)) {
            readTestsBlock(reader);
        } else if (QName.valueOf("issues").equals(name)) {
            readIssuesBlock(reader);
        } else if (QName.valueOf("properties").equals(name)) {
            readPropertiesBlock(reader);
        }
    }
    
    @Override
    protected final void readConfigFile(URI configURI, XMLStreamReader reader) throws Exception {
        _configURI = configURI;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                // positioned on a start element, see if we recognise the block
                QName name = reader.getName();
                readBlock(reader, name);
            }
        }
        postConfigFile();
    }
    
    private void readTestsBlock(XMLStreamReader reader) throws Exception {
        String mode = reader.getAttributeValue(null, "default");
        if ("run".equals(mode)) {
            _run = true;
            _full = false;
        } else if ("hold".equals(mode)) {
            _run = false;
            _full = false;
        } else if ("full".equals(mode)) {
            _run = true;
            _full = true;
        } else if ("none".equals(mode)) {
            _run = false;
            _full = true;
        } else {
            throw new RuntimeException();
        }
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                if (QName.valueOf("test").equals(reader.getName())) {
                    readTestElement(reader);
                }
            } else if (event == XMLStreamReader.END_ELEMENT) {
                if (QName.valueOf("run").equals(reader.getName()))
                    break;
            }
        }
    }
    
    private void readTestElement(XMLStreamReader reader) throws Exception {
        String scope = reader.getAttributeValue(null, "scope");
        String name = reader.getAttributeValue(null, "name");
        String method = reader.getAttributeValue(null, "method");
        
        // Go over all of the issues in the test element
        List<Issue> issues = new ArrayList<Issue>();
        List<Condition> conditions = new ArrayList<Condition>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                if (QName.valueOf("issue").equals(reader.getName())) {
                    String issueId = reader.getAttributeValue(null, "id");
                    Issue issue = _issuesList.get(issueId);
                    if (issue == null) {
                        _issuesList.put(issueId, new Issue(issueId));
                    }
                    issues.add(issue);
                } else if (QName.valueOf("condition").equals(reader.getName())) {
                    String type = reader.getAttributeValue(null, "type");
                    String property = reader.getAttributeValue(null, "property");
                    String value = reader.getAttributeValue(null, "value");
                    String description = reader.getAttributeValue(null, "description");
                    Condition condition = new Condition(type, property, value, description);
                    if (!conditionList.contains(condition)) {
                        conditionList.add(condition);
                    }
                    conditions.add(condition);
                }
            } else if (event == XMLStreamReader.END_ELEMENT) {
                if (QName.valueOf("test").equals(reader.getName()))
                    break;
            }
        }
        
        Method compareMethod = Method.exact;
        if (method != null) {
            compareMethod = Method.valueOf(method);
        }
        
        TestDescription td = new TestDescription(name, scope, issues, conditions, compareMethod);
        _testList.add(td);
    }
    
    private void readIssuesBlock(XMLStreamReader reader) throws Exception {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                if (QName.valueOf("issue").equals(reader.getName())) {
                    String id = reader.getAttributeValue(null, "id");
                    Issue issue = _issuesList.get(id);
                    if (issue == null || !issue.isEnriched()) {
                        _issuesList.put(id, new Issue(id, reader.getAttributeValue(null, "type"), reader.getAttributeValue(null, "status"), reader.getAttributeValue(null, "summery")));
                    }
                }
            } else if (event == XMLStreamReader.END_ELEMENT) {
                if (QName.valueOf("issues").equals(reader.getName()))
                    break;
            }
        }
    }
    
    public URI getResultLogger() {
        return null;
    }
    
    public URI getRunLogger() {
        return URI.create("run.xml");
    }
    
    public URI getFailLogger() {
        return URI.create("fail.xml");
    }
    
    protected void postConfigFile() throws Exception {
        
    }
    
    /**
     * Get a property.
     * 
     * @param name
     * @return
     */
    public String get(String name) {
        PropertyDefinition definition = _properties.get(name);
        if (definition == null) {
            throw new RuntimeException("Could not resolve property with name " + name);
        }
        return (String) definition.getValue();
    }
    
    public Bundle getBundle(String prefix) {
        return new Bundle(_properties, prefix);
    }
    
    public boolean exists(String name) {
        PropertyDefinition definition = _properties.get(name);
        if (definition == null) {
            return false;
        }
        return true;
    }
    
    public Map<String, PropertyDefinition> getPropertyMap() {
        return Collections.unmodifiableMap(_properties);
    }
    
    
    public Collection<Issue> getIssues() {
        return Collections.unmodifiableCollection(_issuesList.values());
    }
    
    @Override
    public void init() {
        
    }
    
    @Override
    public void destroy() {
        
    }
    
    public <T> T getModule(T clazz) {
        throw new RuntimeException("Module of class '" + clazz + "' is unknown.");
    }
    
    @Override
    public boolean since(String version) {
        return true;
    }
    
    @Override
    public boolean till(String version) {
        return true;
    }
}
