package org.unittra.report;

import org.junit.runner.notification.RunListener;
import org.unittra.context.TC;


public class JUnitLogFactory implements RunListenerFactory {
    
    public RunListener create(TC tc) throws Exception {
        return new JUnitLog(tc);
    }
    
}
