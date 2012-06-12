package org.unittra.cdi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;


import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.unittra.factory.ObjectFactory;


public class CDI implements ObjectFactory{
    private static WeldContainer cdiContainer = new Weld().initialize();
    
    public static WeldContainer getContainer() {
        return cdiContainer;
    }

    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    public <T> T instanceOf(Class<T> c) {
        return null;
    }
    
}
