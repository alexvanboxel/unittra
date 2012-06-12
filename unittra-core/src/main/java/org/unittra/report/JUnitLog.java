package org.unittra.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.unittra.context.TC;
import org.unittra.report.LogListener.State;


public class JUnitLog extends RunListener {
    
    private Document _doc = null;
    private Element _root;
    private Description _currentTest;
    private String _previousGroup = null;
    private State _currentState;
    
    private int testCount = 0;
    private int failCount = 0;
    private int ignoreCount = 0;
    private long timeSuiteStart = 0;
    private long timeTestStart = 0;
    
    
    public JUnitLog(TC testContext) throws Exception {
    }
    
    private boolean isSuiteChange(Description current) {
        if (current == null) {
            return true;
        }
        String currentGroup = current.getClassName();
        if (!currentGroup.equals(_previousGroup)) {
            return true;
        }
        return false;
    }
    
    
    private void switchTestSuite(Description description) {
        if (isSuiteChange(description)) {
            if (_doc != null) {
                _root.addAttribute(new Attribute("name", _previousGroup));
                _root.addAttribute(new Attribute("time", String.valueOf(((double) (System.currentTimeMillis() - timeSuiteStart)) / 1000)));
                _root.addAttribute(new Attribute("failures", String.valueOf(failCount)));
                _root.addAttribute(new Attribute("errors", "0"));
                _root.addAttribute(new Attribute("skipped", String.valueOf(ignoreCount)));
                _root.addAttribute(new Attribute("tests", String.valueOf(testCount)));
                OutputStream stream;
                try {
                    File dir = new File("target/junit-report/");
                    dir.mkdirs();
                    stream = new FileOutputStream("target/junit-report/TEST-" + _previousGroup + ".xml");
                    Serializer serializer = new Serializer(stream, "utf-8");
                    serializer.write(_doc);
                    serializer.flush();
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (description != null) {
                _previousGroup = description.getClassName();
                startNewSuite();
            } else {
                _previousGroup = null;
            }
        }
    }
    
    private void startNewSuite() {
        _doc = new Document(new Element("testsuite"));
        _root = _doc.getRootElement();
        _root.appendChild(new Element("properties"));
        testCount = 0;
        failCount = 0;
        ignoreCount = 0;
        timeSuiteStart = System.currentTimeMillis();
    }
    
    @Override
    public final void testStarted(Description description) throws Exception {
        testCount++;
        timeTestStart = System.currentTimeMillis();
        _currentTest = description;
        _currentState = State.UNKNOWN;
    }
    
    @Override
    public final void testFailure(Failure failure) throws Exception {
        failCount++;
        Element testCase;
        if(_currentTest == null) {
            // we're in the before class
            _currentTest = failure.getDescription();
            switchTestSuite(_currentTest);
            testCase = createTestCase(failure.getDescription().getClassName(), "BeforeClass", 0);
        } 
        else {
            switchTestSuite(_currentTest);
            testCase = createTestCase(_currentTest.getClassName(), _currentTest.getMethodName(), System.currentTimeMillis() - timeTestStart);
        }
        
        _root.appendChild(testCase);
        Element fail = new Element("failure");
        String message = failure.getMessage();
        if (message == null) {
            if (failure.getException() != null) {
                message = failure.getException().getClass().getCanonicalName();
            } else {
                message = "Could not determine failure message, bug in QASuite?";
            }
        }
        
        fail.addAttribute(new Attribute("message", message));
        fail.addAttribute(new Attribute("type", failure.getException().getClass().getCanonicalName()));
        testCase.appendChild(fail);
        _currentState = State.FAIL;
    }
    
    @Override
    public final void testIgnored(Description description) throws Exception {
        switchTestSuite(description);
        testCount++;
        ignoreCount++;
        Element testCase = createTestCase(description.getClassName(), description.getMethodName(), 0);
        testCase.appendChild(new Element("skipped"));
        _root.appendChild(testCase);
        _currentState = State.IGNORE;
    }
    
    @Override
    public final void testFinished(Description description) throws Exception {
        switchTestSuite(description);
        if (_currentState == State.UNKNOWN) {
            _root.appendChild(createTestCase(description.getClassName(), description.getMethodName(), System.currentTimeMillis() - timeTestStart));
        }
        _currentState = State.UNKNOWN;
        _currentTest = null;
    }
    
    
    @Override
    public final void testRunFinished(Result result) throws Exception {
        switchTestSuite(null);
    }
    
    private Element createTestCase(String className, String name, long milli) {
        Element testCase = new Element("testcase");
        testCase.addAttribute(new Attribute("time", String.valueOf(((double) milli) / 1000)));
        testCase.addAttribute(new Attribute("classname", className));
        testCase.addAttribute(new Attribute("name", name));
        return testCase;
    }
}
