package org.broker.orbi.rest;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.broker.orbi.rest.client.ProvidersEndpoint;

/**
 *
 * @author smantzouratos
 */
public class APIApplication extends Application {
    private Set<Object> singletons = new HashSet();
    private Set<Class<?>> empty = new HashSet();
 
    public APIApplication() {
        this.singletons.add(new OrbiSDReportEndpoint());
        this.singletons.add(new ProvidersEndpoint());
    }
 
    public Set<Class<?>> getClasses()
    {
        return this.empty;
    }
 
    public Set<Object> getSingletons()
    {
        return this.singletons;
    }
}
