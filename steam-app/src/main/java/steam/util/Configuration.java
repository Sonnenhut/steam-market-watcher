package steam.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

	private static final String DEFAULT_PROPERTIES = "default.properties";
	private static final String WATCHERS_XML = "watchers.xml";
	public static final String SCREENSHOT_DIR = "screenshot.dir";
	public static final String STEAM_TAX = "steam.tax.percent";
	public static final String STEAM_MINIMUM_GAIN = "steam.minimum.gain";
	public static final String STEAM_MAXIMUM_ALLOWED_PRICE = "steam.maximum.allowed.price";
	public static final String STEAM_MINIMUM_ALLOWED_PRICE = "steam.minimum.allowed.price";
	public static final String STEAM_MARKET_IGNORE_SUFFIXES_ARR = "steam.market.ignore.suffixes";
	public static final String STEAM_PROFITABLE_KNIFE_PRICE = "steam.profitable.knife.price";

	private static Logger logger = LoggerFactory.getLogger(Configuration.class);
	private static Configuration INSTANCE = null;

	private final CombinedConfiguration config;

	private Configuration(CombinedConfiguration config) {
		this.config = config;
	}

	public String read(String propertyName) {
		return config.getString(propertyName);
	}

	public String[] readArray(String propertyName) {
		return config.getStringArray(propertyName);
	}

	public int readInteger(String propertyName) {
		return config.getInt(propertyName);
	}

	public double readDouble(String propertyName) {
		return config.getDouble(propertyName);
	}

	public Collection<WatcherConfig> readWatcherConfigurations() {
		Collection<WatcherConfig> res = new ArrayList<>();
				
		List<HierarchicalConfiguration> watchersHConfig = config.configurationsAt("watcher");
		for(HierarchicalConfiguration watcherHConfig : watchersHConfig) {
			final String clazz = watcherHConfig.getString("clazz");
			WatcherConfig watcherConfig = new WatcherConfig(clazz);
			
			for(HierarchicalConfiguration parameterHConfig : watcherHConfig.configurationsAt("configuration")) {
				final String key = parameterHConfig.getString("key");
				final String value = parameterHConfig.getString("value");
				watcherConfig.getConfiguration().put(key, value);
			}
			res.add(watcherConfig);
		}
		return res;
	}

	protected CombinedConfiguration getConfiguration() {
		return config;
	}

	public static Configuration getInstance() {
		if (INSTANCE == null) {
			try {
				// Load a CombinedConfiguration, which basically means: system
				// properties trump .properties files in our case
				CombinedConfiguration config = new CombinedConfiguration();
				config.addConfiguration(new SystemConfiguration());
				config.addConfiguration(new PropertiesConfiguration(DEFAULT_PROPERTIES));
				config.addConfiguration(new XMLConfiguration(WATCHERS_XML));
				config.setNodeCombiner(new OverrideCombiner());
				INSTANCE = new Configuration(config);
			} catch (ConfigurationException e) {
				logger.error("Error while reading properties ", e);
				throw new RuntimeException("Error while reading properties", e);
			}
		}
		return INSTANCE;
	}

	public static Configuration getInstance(AbstractConfiguration overrideConfiguration) {
		CombinedConfiguration config = new CombinedConfiguration();
		config.addConfiguration(overrideConfiguration);
		config.addConfiguration(getInstance().getConfiguration());
		config.setNodeCombiner(new OverrideCombiner());
		return new Configuration(config);
	}
}
