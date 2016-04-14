package steam.util;

import java.util.HashMap;
import java.util.Map;

public class WatcherConfig {

	private final String clazz;
	private final Map<String, String> configuration = new HashMap<>();

	public WatcherConfig(String clazz) {
		super();
		this.clazz = clazz;
	}

	public String getClazz() {
		return clazz;
	}

	public Map<String, String> getConfiguration() {
		return configuration;
	}
	
}
