package org.unittra.catalog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.unittra.Since;
import org.unittra.Till;
import org.unittra.context.TC;

public class Hold {
    
    private TC tc;
    
    public Hold(TC tc) {
        this.tc = tc;
    }
    
    /** Get the method from the description */
    private Method getMethod(Description description) {
        try {
            Class<?> currentClass = Class.forName(description.getClassName());
            return currentClass.getMethod(description.getMethodName(), (Class<?>[]) null);
        } catch (Exception e) {
        }
        return null;
    }
    
    private <T extends Annotation> T getAnnotation(Class<?> currentClass, Class<T> annotationClass) throws Exception {
        if (currentClass == null) {
            return null;
        }
        T since = currentClass.getAnnotation(annotationClass);
        if (since == null) {
            return getAnnotation(currentClass.getSuperclass(), annotationClass);
        }
        return since;
    }
    
    private <T extends Annotation> T getAnnotation(Description description, Class<T> annotationClass) {
        T since = null;
        // The declaring method has priority over all
        Method method = getMethod(description);
        if (method != null) {
            since = method.getAnnotation(annotationClass);
        }
        // If not found we will search from the sub class to an annotation on the class
        if (since == null) {
            try {
                return getAnnotation(Class.forName(description.getClassName()), annotationClass);
            } catch (Exception e) {
                return null;
            }
        }
        return since;
    }
    
    private boolean holdForSince(Description description) {
        if (tc != null) {
            Since since = getAnnotation(description, Since.class);
            if (since != null) {
                return !tc.since(since.version());
            }
        }
        return false;
    }

    private boolean holdForTill(Description description) {
        if (tc != null) {
            Till till = getAnnotation(description, Till.class);
            if (till != null) {
                return !tc.till(till.version());
            }
        }
        return false;
    }
    
    private boolean holdForIgnore(Description description) {
        if (getAnnotation(description, Ignore.class) != null) {
            return true;
        }
        return false;
    }
    
    private boolean holdForContext(Description description) {
        if (!tc.shouldRun(description)) {
            return true;
        }
        return false;
    }
    
    public boolean shouldRun(Description description, FrameworkMethod method) {
        if (holdForSince(description))
            return false;
        if (holdForIgnore(description))
            return false;
        if (holdForContext(description))
            return false;
         if (holdForTill(description))
            return false;
        return true;
    }
    
    public List<HoldMessage> ignoreMessage(Description description, FrameworkMethod method) {
        List<HoldMessage> messages = new ArrayList<HoldMessage>();
        if (holdForSince(description)) {
            messages.add(new HoldMessage(Since.class,"Only since " + getAnnotation(description, Since.class).version()));
        }
        if (holdForIgnore(description)) {
            messages.add(new HoldMessage(Ignore.class,getAnnotation(description, Ignore.class).value()));
        }
        if (holdForContext(description)) {
            messages.add(new HoldMessage(String.class,""));
        }
        if (holdForTill(description)) {
            messages.add(new HoldMessage(Till.class,"Only Till " + getAnnotation(description, Till.class).version()));
        }

        return messages;
    }
    
}
