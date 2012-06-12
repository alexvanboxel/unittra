package org.unittra.report;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.unittra.catalog.TestDescription;
import org.unittra.context.TC;


public class RunLog extends RunListener {
    
    public RunLog(TC tc) {
    }
    
    private Description _currentTest;
    private Description _lastFailed;
    protected PrintWriter _failWriter;
    protected PrintWriter _runWriter;
    
    private void writeHeader(PrintWriter writer) {
        writer.println("<test-context>");
        writer.println("<platform-section>");
        writer.println("  <platform name=\"java\" factory=\"" + this.getClass().getName() + "\"/>");
        writer.println("</platform-section>");
    }
    
    private void writeFooter(PrintWriter writer) {
        writer.println("</test-context>");
    }
    
    protected void writeContextSpecific(PrintWriter writer) {
    }
    
    protected void writeTest(PrintWriter writer, TestDescription desc) {
        writer.println("  " + desc.toString());
    }
    
    private void startDump() throws Exception {
        File failFile = new File("fail.xml");
        _failWriter = new PrintWriter(new FileWriter(failFile));
        File runFile = new File("run.xml");
        _runWriter = new PrintWriter(new FileWriter(runFile));
        writeHeader(_failWriter);
        writeContextSpecific(_failWriter);
        writeHeader(_runWriter);
        writeContextSpecific(_runWriter);
        _failWriter.println("<run default=\"hold\">");
        _runWriter.println("<run default=\"run\">");
    }
    
    private void endDump() throws Exception {
        _failWriter.println("</run>");
        writeFooter(_failWriter);
        _failWriter.close();
        _runWriter.println("</run>");
        writeFooter(_runWriter);
        _runWriter.close();
        _failWriter = _runWriter = null;
    }
    
    @Override
    public void testStarted(Description description) throws Exception {
        if (_currentTest == null) {
            startDump();
        }
        _currentTest = description;
    }
    
    @Override
    public void testRunFinished(Result result) throws Exception {
        endDump();
    }
    
    @Override
    public void testFailure(Failure failure) throws Exception {
        _lastFailed = failure.getDescription();
    }
    
    @Override
    public void testFinished(Description description) throws Exception {
        TestDescription td = new TestDescription(description);
        if (description.equals(_lastFailed)) {
            writeTest(_failWriter, td);
        }
        writeTest(_runWriter, td);
    }
    
}
