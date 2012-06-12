package org.unittra.report;

import org.junit.runner.notification.RunListener;
import org.unittra.context.TC;


public interface RunListenerFactory {
    
    RunListener create(TC tc) throws Exception;
    
}
