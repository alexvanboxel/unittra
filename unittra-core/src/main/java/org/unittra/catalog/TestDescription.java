package org.unittra.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.runner.Description;
import org.unittra.context.PropertyDefinition;
import org.unittra.context.TC;

/**
 * Describes a test description.
 */
public class TestDescription {
    
    public enum Method {
        /**
         * Indicates that this test description is constructed from a test method.
         */
        test,
        /**
         * Exact comparison.
         */
        exact,
        /**
         * Regular expression comparison.
         */
        regex
    }
    
    private String scope;
    private String name;
    private Method method;
    private List<Issue> issues;
    private List<Condition> conditions;
    
    public TestDescription(Description description) {
        String d = description.toString();
        int ix = d.indexOf('(');
        this.name = d.substring(0, ix);
        this.scope = d.substring(ix + 1, d.length() - 1);
        this.method = Method.test;
    }
    
    public TestDescription(String name, String scope, List<Issue> issues, List<Condition> conditions, Method method) {
        this.name = name;
        this.scope = scope;
        this.issues = issues;
        this.conditions = conditions;
        this.method = method;
    }
    
    @Override
    public String toString() {
        return "TestDescription scope=\"" + scope + "\", name=\"" + name + "\"";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestDescription) {
            TestDescription tc = (TestDescription) obj;
            if ((tc.name.equals(name)) && (tc.scope.equals(scope))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 
     * 
     * @param tc
     * @return
     */
    public boolean contains(TestDescription tc) {
        if (method == Method.exact) {
            if ((tc.name.equals(this.name)) && (tc.scope.equals(this.scope))) {
                return true;
            }
        } else if (method == Method.regex) {
            Pattern scopePattern = Pattern.compile(scope);
            Pattern namePattern = Pattern.compile(name);
            boolean inScope = scopePattern.matcher(tc.scope).matches();
            boolean inName = namePattern.matcher(tc.name).matches();
            return inScope && inName;
        } else {
            throw new RuntimeException("Unknown compare method.");
        }
        
        return false;
    }
    
    public List<Issue> getIssues() {
        return Collections.unmodifiableList(issues);
    }
    
    public List<Condition> getConditions() {
        return Collections.unmodifiableList(conditions);
    }
    
    /**
     * Returns true if all related issues are marked as done.
     * 
     * @return
     */
//    public boolean isMarkedAsDone() {
//        if (issues != null && issues.size() != 0) {
//            int count = 0;
//            for (Issue issue : issues) {
//                if (issue.isMarkedAsDone()) {
//                    count++;
//                }
//            }
//            if (count == issues.size()) {
//                return true;
//            }
//        }
//        return false;
//    }

    public List<Issue> getIssuesNotDone() {
        List<Issue> conditions = new ArrayList<Issue>();
        if (issues != null && issues.size() != 0) {
            for (Issue issue : issues) {
                if (!issue.isMarkedAsDone()) {
                    conditions.add(issue);
                }
            }
        }
        return conditions;
    }
    
    public List<Condition> getConditions(TC context) {
        List<Condition> conditions = new ArrayList<Condition>();
        Map<String, PropertyDefinition> properties = context.getPropertyMap();
        for (Condition condition : this.conditions) {
            String propertyName = condition.getPropertyName();
            PropertyDefinition property = properties.get(propertyName);
            if ("eq".equals(condition.getType())) {
                if (property != null && condition.getValue().equals(property.getValue())) {
                    conditions.add(condition);
                }
            } else if ("exists".equals(condition.getType())) {
                if (property != null) {
                    conditions.add(condition);
                }
            } else if ("hold".equals(condition.getType())) {
                conditions.add(condition);
            } else {
                throw new RuntimeException("Unknown condition type :" + condition.getType());
            }
        }
        return conditions;
    }
    
}
