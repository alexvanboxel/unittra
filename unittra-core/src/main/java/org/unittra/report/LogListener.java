package org.unittra.report;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.unittra.context.TC;


public abstract class LogListener extends RunListener {
    protected TC _testContext;
    private boolean _headWritten = false;
    private boolean _footWritten = false;
    
    enum State {
        UNKNOWN, IGNORE, FAIL
    }
    
    private Description _currentTest;
    private State _currentState;
    
    public LogListener(TC tc) {
        _testContext = tc;
    }
    
    public abstract void writeHead() throws Exception;
    
    public abstract void writeSuccess(Description description) throws Exception;
    
    public abstract void writeIgnored(Description description) throws Exception;
    
    public abstract void writeFailure(Description description, Failure failure) throws Exception;
    
    public abstract void writeFoot() throws Exception;
    
    @Override
    public final void testStarted(Description description) throws Exception {
        _currentTest = description;
        _currentState = State.UNKNOWN;
    }
    
    @Override
    public final void testFailure(Failure failure) throws Exception {
        _currentState = State.FAIL;
        writeFailure(_currentTest, failure);
    }
    
    @Override
    public final void testIgnored(Description description) throws Exception {
        _currentState = State.IGNORE;
        writeIgnored(description);
    }
    
    @Override
    public final void testFinished(Description description) throws Exception {
        if (_currentState == State.UNKNOWN) {
            writeSuccess(_currentTest);
        }
        _currentState = State.UNKNOWN;
        _currentTest = null;
    }
    
    protected void head() throws Exception {
        if (!_headWritten) {
            writeHead();
            _headWritten = true;
        }
    }
    
    protected void foot() throws Exception {
        if (!_footWritten) {
            writeFoot();
            _footWritten = true;
        }
    }
    
    @Override
    public final void testRunStarted(Description description) throws Exception {
        head();
    }
    
    @Override
    public final void testRunFinished(Result result) throws Exception {
        foot();
    }
}