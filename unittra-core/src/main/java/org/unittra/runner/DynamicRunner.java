package org.unittra.runner;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;


public class DynamicRunner extends Suite {
    
    /**
     * Annotation for a method which provides parameters to be injected into the test class constructor by <code>Data</code>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Data {
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public static @interface TestName {
    }
    
    private class TestClassRunnerForParameters extends UnittraRunner {
        private final int index;
        private final Object[] data;
        
        TestClassRunnerForParameters(Class<?> type, Object[] data, int ix) throws InitializationError {
            super(type);
            this.data = data;
            this.index = ix;
        }
        
        @Override
        public Object createTest() throws Exception {
            Constructor<?> constructor = getTestClass().getOnlyConstructor();
            Annotation[][] annotations = constructor.getParameterAnnotations();
            Object[] param = new Object[annotations.length];
            int dix = 1; // data always begins at 1
            for (int pix = 0; pix < param.length; pix++) {
                Annotation[] panno = annotations[pix];
                if (panno.length > 0) {
                    for (Annotation anno : panno) {
                        if (TestName.class.equals(anno.annotationType())) {
                            param[pix] = data[0];
                            continue;
                        }
                    }
                } else {
                    param[pix] = data[dix++];
                }
            }

            return getTestClass().getOnlyConstructor().newInstance(param);
        }
        
        @Override
        protected String getName() {
            if ((data.length >= 1) && (data[0] instanceof java.lang.String)) {
                String testcase = String.valueOf(data[0]);
                return testcase;
            } else {
                return String.format("[%s]", index);
            }
        }
        
        /**
         * Must format as <testname>_<testcase> to match with QA-suite catalog
         */
        @Override
        protected String testName(final FrameworkMethod method) {
            return String.format("%s_%s", method.getName(), getName());
        }
        
        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
        }
        
        @Override
        protected Statement classBlock(RunNotifier notifier) {
            return childrenInvoker(notifier);
        }
    }
    
    private final ArrayList<Runner> runners = new ArrayList<Runner>();
    
    /**
     * Only called reflectively. Do not use programmatically.
     */
    public DynamicRunner(Class<?> klass) throws Throwable {
        super(klass, Collections.<Runner> emptyList());
        List<Object[]> parametersList = getParametersList(getTestClass());
        
        int index = 0;
        for (Object[] data : parametersList)
            runners.add(new TestClassRunnerForParameters(getTestClass().getJavaClass(), data, index++));
    }
    
    @Override
    protected List<Runner> getChildren() {
        return runners;
    }
    
    @SuppressWarnings("unchecked")
    private List<Object[]> getParametersList(TestClass klass) throws Throwable {
        return (List<Object[]>) getParametersMethod(klass).invokeExplosively(null);
    }
    
    /**
     * Scans all the methods of the <code>testClass</code> to locate the annotated method for <code>Data</code>.
     * 
     * @param testClass
     *            the class the scan for parameters
     * @return the data for the tests
     * @throws Exception
     */
    private FrameworkMethod getParametersMethod(TestClass testClass) throws Exception {
        List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Data.class);
        for (FrameworkMethod each : methods) {
            int modifiers = each.getMethod().getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
                return each;
        }
        
        throw new Exception("No public static data method on class " + testClass.getName());
    }
}
