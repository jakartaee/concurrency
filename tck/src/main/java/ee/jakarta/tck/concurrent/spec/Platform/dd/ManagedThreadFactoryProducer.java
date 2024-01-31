package ee.jakarta.tck.concurrent.spec.Platform.dd;

import javax.naming.InitialContext;

import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class ManagedThreadFactoryProducer {
    
    @Produces
    public ManagedThreadFactory getDefaultManagedThreadFactory() throws Exception {
        return InitialContext.doLookup("java:app/concurrent/ThreadFactoryDefault");
    }
}
