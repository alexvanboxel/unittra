package org.unittra.runner;

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.unittra.catalog.Hold;
import org.unittra.context.TC;



public class UnittraRunner extends BlockJUnit4ClassRunner {
    
    public UnittraRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }
    
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        TC context = null;
        try {
            context = TC.i(TC.class);
            context.addTestListeners(notifier);
        } catch (Exception e) {
            assert false : "Error initializing TestContext: " + e.getMessage();
        }
        
        Description description = describeChild(method);
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        
        Hold hold = new Hold(context);
        if(!hold.shouldRun(description,method)) {
            eachNotifier.fireTestIgnored();
            return;
        }
        eachNotifier.fireTestStarted();
        try {
            methodBlock(method).evaluate();
        } catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        } catch (Throwable e) {
            eachNotifier.addFailure(e);
        } finally {
            eachNotifier.fireTestFinished();
        }
    }
    
    protected Object createTest() throws Exception {
//        return CDIUtils.getCDIContainer().instance().select(getTestClass().getJavaClass()).get();
        return getTestClass().getJavaClass().newInstance();
    }
}
