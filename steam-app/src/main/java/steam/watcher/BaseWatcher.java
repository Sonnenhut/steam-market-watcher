package steam.watcher;

import java.util.Map;

import org.apache.commons.configuration.MapConfiguration;
import org.jboss.weld.environment.se.contexts.ThreadScoped;

import steam.util.Configuration;

public abstract class BaseWatcher implements Runnable {

	private Configuration watcherConfiguration = Configuration.getInstance();
	
	public BaseWatcher() {
	}

	/**
	 * ATTENTION: CDI Injected Objects will be exclusively for this {@link Thread}.
	 * ONLY if used inside this method.<br>
	 * If injected Objects are used inside the constructor, the parent {@link ThreadScoped} is used.
	 */
	public abstract void run() ;
	
	public void setWatcherConfiguration(Map<String, String> config) {
		// returns an instance of the common configuration.
		// BUT: Configurations specified explicitly for this Job will override common Configurations.
		this.watcherConfiguration = Configuration.getInstance(new MapConfiguration(config));
	}
	
	protected Configuration getWatcherConfiguration() {
		// returns an instance of the common configuration.
		// BUT: Configurations specified explicitly for this Job will override common Configurations.
		return watcherConfiguration;
	}
}