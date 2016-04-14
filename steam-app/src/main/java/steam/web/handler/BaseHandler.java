package steam.web.handler;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import steam.steam_app.SteamAgent;

public class BaseHandler {

	private final int WAIT_IN_SECS = 3;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	protected final WebDriver driver;
	

	public BaseHandler(WebDriver driver) {
		this.driver = driver;
	}

	protected WebElement fluentFind(By locator) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_IN_SECS);
		WebElement res = null;
		try {

			res = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		} catch (TimeoutException e) {
			logger.trace("unable to find element with locator: {}", locator);
			logger.trace("unable to find element with locator, TimeoutException", e);
		}
		return res;
	}

	protected boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	protected WebElement waitUntilTextChanges(By locator) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_IN_SECS);
		WebElement res = wait.until(new ElementTextOfLocatorChangedCondition(locator));
		return res;
	}

	private static class ElementTextOfLocatorChangedCondition implements ExpectedCondition<WebElement> {

		private Logger logger = LoggerFactory.getLogger(getClass());

		private final By locator;
		private String lastResultText;

		public ElementTextOfLocatorChangedCondition(By locator) {
			this.locator = locator;
		}

		public WebElement apply(WebDriver driver) {
			try {
				WebElement currentResult = driver.findElement(locator);
				if (lastResultText != null) {
					if (!lastResultText.equals(currentResult.getText())) {
						// when the last Result has a different Text than the
						// current Result, our job is done..
						return currentResult;
					}
				}
				lastResultText = currentResult.getText();
			} catch (StaleElementReferenceException e) {
				// during our processing the element forwarded to us is no
				// longer present.
				logger.warn("Exception while checking for Text change ...", e);
			}
			return null;
		}
	};
}