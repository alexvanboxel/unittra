package org.unittra.util;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Breadcrumb is a JUnit Method rule to keep track of the method that is called
 * within the JUnit framework. This can be used as reference in messages, transactions, ... 
 */
public class Breadcrumb implements MethodRule {
    
    private Class<?> clazz;
    private String method;
    
    public Breadcrumb(Class<?> clazz) {
        this.clazz = clazz;
        this.method = null;
    }
    
    public Breadcrumb() {
        this.clazz = null;
        this.method = null;
    }
    
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        this.clazz = method.getMethod().getDeclaringClass();
        this.method = method.getMethod().getName();
        return base;
    }
    
    public String getMethodName() {
        return method;
    }
    
    public Class<?> getTestClass() {
        return clazz;
    }
    
    public String toString() {
        return method + "." + clazz;
    }
}
