package steam.util;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Factory for creating a {@link WebDriver}
 * 
 * @author
 *
 */
public final class WebDriverFactory {
//	public static WebDriver getPhantomJSDriver() {
//		Capabilities caps = new DesiredCapabilities();
//		((DesiredCapabilities) caps).setJavascriptEnabled(true);
//		((DesiredCapabilities) caps).setCapability("takesScreenshot", true);
//		((DesiredCapabilities) caps).setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
//				"C:/Program Files (x86)/PhantomJs/bin/phantomjs.exe");
//		WebDriver driver = new PhantomJSDriver(caps);
//		return driver;
//	}
//
//	public static WebDriver getFFDriver(boolean showImages) {
//////		ProfilesIni profileObj = new ProfilesIni();
////		FirefoxProfile profile = new FirefoxProfile();// profileObj.getProfile("madeupshamelessindian");
////		if (!showImages) {
////			profile.setPreference("permissions.default.image", 2);
////		}
////		profile.setPreference("general.useragent.override", "Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
////		WebDriver driver = new FirefoxDriver(profile);
////		driver.manage().deleteAllCookies();
////		return driver;
//		return testFF();
//	}
//
//	public static WebDriver testFF() {
//		String userAgent = "Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
//		FirefoxProfile profile = new FirefoxProfile();
//		profile.setPreference("general.useragent.override", userAgent);
////		DesiredCapabilities cap = DesiredCapabilities.firefox();
////		cap.setCapability(FirefoxDriver.PROFILE, profile);
//		WebDriver driver = new FirefoxDriver(profile);
//		return driver;
//	}

}