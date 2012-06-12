package org.unittra.report;

import org.junit.runner.notification.RunListener;
import org.unittra.context.TC;


public class XMLLogFactory implements RunListenerFactory {
    
    public RunListener create(TC tc) throws Exception {
        return new XMLLog(tc);
    }
    
}
