package steam.util;

import java.util.Collection;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;

import junit.framework.TestCase;

public class ConfigurationTest extends TestCase{
	
	@Test
	public void testConfigOverride() {
		final String result = "test123987";
		Properties props = new Properties();
		props.setProperty(Configuration.SCREENSHOT_DIR, result);
		Configuration reader = Configuration.getInstance(new MapConfiguration(props));
		String test = reader.read(Configuration.SCREENSHOT_DIR);
		
		assertEquals(result, test);
	}
	
	@Test
	public void testReadWatchers_ReadsTestWatcher() throws ConfigurationException {
		Configuration reader = Configuration.getInstance(new XMLConfiguration("test_watchers.xml"));
		Collection<WatcherConfig> res = reader.readWatcherConfigurations();
		assertNotNull(res);
		assertFalse(res.isEmpty());
		for(WatcherConfig config : res) {
			assertEquals("Clazz",config.getClazz());
			assertEquals("420",config.getConfiguration().get("customConfig"));
		}
	}
}
