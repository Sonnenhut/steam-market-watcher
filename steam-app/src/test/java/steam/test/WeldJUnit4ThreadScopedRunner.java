package steam.test;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.WeldSEBeanRegistrant;
import org.jboss.weld.environment.se.contexts.ThreadScoped;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Junit Runner that activates the {@link ThreadScoped} context to be able to properly test the Application with cdi. (if needed) 
 *
 */
public class WeldJUnit4ThreadScopedRunner extends BlockJUnit4ClassRunner{
	  
    private final Class<?> klass;  
    private final Weld weld;  
    private final WeldContainer container;  
    private final WeldSEBeanRegistrant extension;
  
    public WeldJUnit4ThreadScopedRunner(final Class<Object> klass) throws InitializationError {  
        super(klass);  
        this.klass = klass;  
        this.weld = new Weld();  
        this.container = weld.initialize();  
        this.extension = container.instance().select(WeldSEBeanRegistrant.class).get();
    }  
  
    @Override  
    protected Object createTest() throws Exception {
    	// get Class from Weld
        final Object test = container.instance().select(klass).get();
//        extension.getThreadContext().activate();
        return test;  
    }
    
    @Override
    public void run(RunNotifier notifier) {
        extension.getThreadContext().activate();
    	super.run(notifier);
    	extension.getThreadContext().invalidate();
    	extension.getThreadContext().deactivate();
    }
}