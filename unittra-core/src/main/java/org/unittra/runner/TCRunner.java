package org.unittra.runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import junit.runner.Version;

import org.junit.internal.JUnitSystem;
import org.junit.internal.RealSystem;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.unittra.context.TC;

/**
 * <code>JUnitCore</code> is a facade for running tests. It supports running JUnit 4 tests, 
 * JUnit 3.8.x tests, and mixtures. To run tests from the command line, run 
 * <code>java org.junit.runner.JUnitCore TestClass1 TestClass2 ...</code>.
 * For one-shot test runs, use the static method {@link #runClasses(Class[])}. 
 * If you want to add special listeners,
 * create an instance of {@link org.junit.runner.JUnitCore} first and use it to run the tests.
 * 
 * @see org.junit.runner.Result
 * @see org.junit.runner.notification.RunListener
 * @see org.junit.runner.Request
 */
public class TCRunner {
    
    private File finalReport;
    private OutputStreamWriter xmlLogWriter;
    
    public class TCListener extends RunListener {
        
        @Override
        public void testAssumptionFailure(Failure failure) {
        }
        
        @Override
        public void testFailure(Failure failure) throws Exception {
            //System.out.println("##teamcity[testFailed name='" + failure.getDescription().toString() + "' message='" + failure.getMessage() + "' details='']");
            System.out.println("##teamcity[testFailed name='" + failure.getDescription().toString() + "' message='' details='']");
        }
        
        @Override
        public void testFinished(Description description) throws Exception {
            System.out.println("##teamcity[testFinished name='" + description.toString() + "']");
        }
        
        @Override
        public void testIgnored(Description description) throws Exception {
//            Issue message = TC.i().getIssue(description);
//            //System.out.println("##teamcity[testIgnored name='" + description.toString() + "' message='" + message + "']");
//            System.out.println("##teamcity[testIgnored name='" + description.toString() + "' message='']");
        }
        
        @Override
        public void testRunFinished(Result result) throws Exception {
        }
        
        @Override
        public void testRunStarted(Description description) throws Exception {
        }
        
        @Override
        public void testStarted(Description description) throws Exception {
            System.out.println("##teamcity[testStarted name='" + description.toString() + "']");
        }
    };
    
    ////System.out.println("##teamcity[testSuiteStarted name='suite1']");
    ////      System.out.println("##teamcity[testSuiteFinished name='suite1']");
    ////      System.out.println("##teamcity[testSuiteStarted name='suite2']");
    ////      System.out.println("##teamcity[testSuiteFinished name='suite2']");
    
    private RunNotifier fNotifier;
    
    /**
     * Create a new <code>JUnitCore</code> to run tests.
     */
    public TCRunner() {
        fNotifier = new RunNotifier();
    }
    
    /**
     * Run the tests contained in <code>classes</code>. Write feedback while the tests
     * are running and write stack traces for all failed tests after all tests complete. This is
     * similar to {@link #main(String[])}, but intended to be used programmatically.
     * @param classes Classes in which to find tests
     * @return a {@link Result} describing the details of the test run and the failed tests.
     */
    public static Result runClasses(Class<?>... classes) {
        return new TCRunner().run(classes);
    }
    
    private void addXMLLogListener() throws Exception {
        FileOutputStream file = new FileOutputStream(finalReport);
        xmlLogWriter = new OutputStreamWriter(file, "utf-8");
        
        XMLStreamWriter stream = XMLOutputFactory.newInstance().createXMLStreamWriter(xmlLogWriter);
        //    addListener(new XMLLogListener(stream));
    }
    
    /**
     * Do not use. Testing purposes only.
     * @param system 
     */
    public Result runMain(JUnitSystem system, String... args) {
        system.out().println("JUnit version " + Version.id());
        List<Class<?>> classes = new ArrayList<Class<?>>();
        List<Failure> missingClasses = new ArrayList<Failure>();
        for (String each : args)
            try {
                classes.add(Class.forName(each));
            } catch (ClassNotFoundException e) {
                system.out().println("Could not find class: " + each);
                Description description = Description.createSuiteDescription(each);
                Failure failure = new Failure(description, e);
                missingClasses.add(failure);
            }
        RunListener listener = new TCListener();
        addListener(listener);
        try {
            TC context = TC.i(TC.class);
            URI report = context.getResultLogger();
            finalReport = new File(report);
            addXMLLogListener();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result result = run(classes.toArray(new Class[0]));
        for (Failure each : missingClasses)
            result.getFailures().add(each);
        
        // Make sure to close the file.
        if (xmlLogWriter != null)
            try {
                xmlLogWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        
        return result;
    }
    
    /**
     * @return the version number of this release
     */
    public String getVersion() {
        return Version.id();
    }
    
    /**
     * Run all the tests in <code>classes</code>.
     * @param classes the classes containing tests
     * @return a {@link Result} describing the details of the test run and the failed tests.
     */
    public Result run(Class<?>... classes) {
        return run(Request.classes(classes));
    }
    
    /**
     * Run all the tests contained in <code>request</code>.
     * @param request the request describing tests
     * @return a {@link Result} describing the details of the test run and the failed tests.
     */
    public Result run(Request request) {
        return run(request.getRunner());
    }
    
    /**
     * Run all the tests contained in JUnit 3.8.x <code>test</code>. Here for backward compatibility.
     * @param test the old-style test
     * @return a {@link Result} describing the details of the test run and the failed tests.
     */
    public Result run(junit.framework.Test test) {
        return run(new JUnit38ClassRunner(test));
    }
    
    /**
     * Do not use. Testing purposes only.
     */
    public Result run(Runner runner) {
        Result result = new Result();
        RunListener listener = result.createListener();
        addFirstListener(listener);
        try {
            fNotifier.fireTestRunStarted(runner.getDescription());
            runner.run(fNotifier);
            fNotifier.fireTestRunFinished(result);
        } finally {
            removeListener(listener);
        }
        return result;
    }
    
    private void addFirstListener(RunListener listener) {
        fNotifier.addFirstListener(listener);
    }
    
    /**
     * Add a listener to be notified as the tests run.
     * @param listener the listener to add
     * @see org.junit.runner.notification.RunListener
     */
    public void addListener(RunListener listener) {
        fNotifier.addListener(listener);
    }
    
    /**
     * Remove a listener.
     * @param listener the listener to remove
     */
    public void removeListener(RunListener listener) {
        fNotifier.removeListener(listener);
    }
}
