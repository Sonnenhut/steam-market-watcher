package steam.web.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

import steam.data.market.MarketItem;
import steam.data.market.MarketListing;
import steam.data.market.MarketPrice;
import steam.guard.helper.SteamGuardGoogleMailHelper;
import steam.util.Constants;

public class SteamHandler extends BaseHandler {

	private static final String BASE_URL = "http://steamcommunity.com";
	private static final String LOGIN_URL = BASE_URL + "/login/";
	private static final String MARKET_SEARCH_ITEMS_FOR_GAME_ID_URL = BASE_URL +"/market/search?l=english&appid=";
	private static final Pattern GAME_ID_PATTERN = Pattern.compile("listings/([0-9]*)/");

	@Inject
	private Logger logger;
	
	@Inject
	private SteamGuardGoogleMailHelper sgMailHelper;
	
	@Inject 
	public SteamHandler(WebDriver driver) {
		super(driver);
	}

	public void doLogin() {
		safeGet(LOGIN_URL);
		driver.findElement(By.id("steamAccountName")).clear();
		driver.findElement(By.id("steamAccountName")).sendKeys(Constants.STEAM_USER);
		driver.findElement(By.id("steamPassword")).clear();
		driver.findElement(By.id("steamPassword")).sendKeys(Constants.STEAM_PW);
		driver.findElement(By.id("SteamLogin")).click();
		WebElement authCodeElement = fluentFind(By.id("authcode"));
		if (authCodeElement != null) {
			String sgCode = waitForSteamGuardCode();
			driver.findElement(By.id("authcode")).clear();
			driver.findElement(By.id("authcode")).click();
			driver.findElement(By.id("authcode")).sendKeys(sgCode);

			driver.findElement(By.cssSelector("#auth_buttonset_entercode > div.auth_button.leftbtn > div.auth_button_h5"))
					.click();
			fluentFind(By.id("success_continue_btn")).click();
		}
	}

	private String waitForSteamGuardCode() {
		return sgMailHelper.waitForLatestCode();
	}

	public Map<MarketItem, MarketPrice> readMarketItems(final int gameId, int stopAtPage) {
		logger.trace("Reading all Market items for Game with id '{}'", gameId);
		safeGet(MARKET_SEARCH_ITEMS_FOR_GAME_ID_URL + gameId);
		changeLanguageEnglish();

		return navigateMarketSearchPages(stopAtPage);
	}
	
	public Map<MarketItem, MarketPrice> readAllMarketItems(String url) {
		logger.trace("Reading all Market items for url '{}'", url);
		safeGet(url);
		changeLanguageEnglish();

		return navigateMarketSearchPages(-1);
	}

	public Map<MarketItem, MarketPrice> readMarketItems(String url, int stopAtPage) {
		logger.trace("Reading all Market items for url '{}'", url);
		safeGet(url);
		changeLanguageEnglish();

		return navigateMarketSearchPages(stopAtPage);
	}
	
	private Map<MarketItem, MarketPrice> navigateMarketSearchPages(int stopAtPage) {
		Map<MarketItem, MarketPrice> res = new HashMap<>();
		final int gameId = extractGameIdFromUrl(getCurrentUrl());
		final String maxPagesStr = driver.findElement(By.cssSelector("#searchResults_links .market_paging_pagelink:last-child")).getText();
		final int maxPages = Integer.valueOf(maxPagesStr.trim());
		if(stopAtPage < 0) {
			// when negative value comes in, then we go through them all.
			stopAtPage = maxPages;
		}
		changeLanguageEnglish();
		int currentPage = 0;
		do {
			currentPage++;
			res.putAll(readAllMarketItemsOnPage());
		} while (stopAtPage > currentPage && nextPage());
		logger.trace("Ended reading all market items for game with id '{}'. Found '{}' items.", gameId, res.size());
		return res;
	}

	/**
	 * Reads the first page of market Listings
	 * 
	 * @param item
	 *            given market item to search listings for
	 * @return found listings
	 */
	public Set<MarketListing> readFirstMarketListings(MarketItem item) {
		Set<MarketListing> res = new HashSet<MarketListing>();
		safeGet(item.getUrl());

		// Find the text input element by its name
		List<WebElement> listings = driver.findElements(By.className("market_recent_listing_row"));

		for (WebElement listingElement : listings) {
			WebElement priceWithFeeElement = listingElement.findElement(By.className("market_listing_price_with_fee"));
			MarketPrice price = createPrice(priceWithFeeElement);
			if (price != null) {
				MarketListing listing = new MarketListing(listingElement.getAttribute("id"), item.getKey(), price);
				res.add(listing);
			}
		}
		return res;
	}

	private Map<MarketItem, MarketPrice> readAllMarketItemsOnPage() {
		final int gameId = extractGameIdFromUrl(getCurrentUrl());
		Map<MarketItem, MarketPrice> res = new HashMap<>();
		List<WebElement> elements = driver.findElements(By.className("market_listing_row_link"));
		for (WebElement element : elements) {
			final String itemUrl = element.getAttribute("href");
			final String itemName = element.findElement(By.className("market_listing_item_name")).getText();
			final WebElement priceElement = element.findElement(By.cssSelector(".market_listing_their_price span.market_table_value span"));
			final MarketPrice price = createPrice(priceElement);
			final MarketItem item = new MarketItem(gameId, itemName, itemUrl);
			res.put(item, price);
			logger.trace("Found item with name '{}'. Url: '{}'.", itemName, itemUrl);
		}
		return res;
	}

	private void changeLanguageEnglish() {
		logger.trace("Changing language to english");
		if (isLoggedIn()) {
			driver.findElement(By.id("account_pulldown")).click();
			driver.findElement(By.id("account_language_pulldown")).click();
		} else {
			driver.findElement(By.id("language_pulldown")).click();
		}
		List<WebElement> languages = driver.findElements(By.cssSelector("#language_dropdown .popup_menu_item"));
		for (WebElement language : languages) {
			if (language.getAttribute("href").contains("english")) {
				language.click();
				break;
			}
		}
		// ((JavascriptExecutor)driver).executeScript("ChangeLanguage( 'english'
		// ); return false;");
		// wait until page is reloaded...
		logger.trace("Waiting for browser refresh");
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
	}

	private boolean isLoggedIn() {
		return isElementPresent(By.id("account_pulldown"));
	}

	private boolean nextPage() {
		boolean res = false;
		if (logger.isTraceEnabled()) {
			logger.trace("Next page. currently at page: '{}'", getCurrentPage());
		}
		WebElement buttonNext = driver.findElement(By.id("searchResults_btn_next"));
		if (!buttonNext.getAttribute("class").contains("disabled")) {
			buttonNext.click();
			// wait until the new page number is active. (...and hopefully
			// loaded)
			waitUntilTextChanges(By.cssSelector(".market_paging_pagelink.active"));
			res = true;
		} else {
			logger.trace("This was the last page, no more pages.");
		}
		return res;
	}

	private void waitOnErrorPage() {
		if (isErrorPageShown()) {
			final long fiveMins = 300000;
			final long thrirtySecs = 30000;
			long start = System.currentTimeMillis();
			long elapsedTime;
			do {
				logger.info("waiting until error page is gone");
				try {
					Thread.sleep(thrirtySecs);
				} catch (InterruptedException e) {
					logger.warn("unable to sleep, was interrupted", e);
				}
				elapsedTime = System.currentTimeMillis() - start;
				driver.navigate().refresh();
				// wait a maximum amount of 5 minutes.
				logger.info("elapsed time of waiting in millis '{}'", elapsedTime);
			} while (elapsedTime < fiveMins && isErrorPageShown());
			if (isErrorPageShown()) {
				throw new RuntimeException("unable to wait for error Page to go away. still there");
			}
			logger.info("finished waiting.");
		}
	}
	
	private boolean isErrorPageShown() {
		return isElementPresent(By.className("error_ctn"));
	}

	private int getCurrentPage() {
		String pageNrString = driver.findElement(By.cssSelector(".market_paging_pagelink.active")).getText();
		return Integer.valueOf(pageNrString.trim());
	}
	
	/**
	 * Safely get the url. After Url a check is performed to see if an error page is shown. we reload the page for 5 mins and see if it gets better...
	 * @param url the url to get.
	 */
	private void safeGet(String url) {
		final long start = System.currentTimeMillis();
		logger.trace("getting url: '{}'",url);
		driver.get(url);
		waitOnErrorPage();
		logger.trace("got url took: '{}' ms", System.currentTimeMillis() - start);
	}
	
	private String getCurrentUrl() {
		return driver.getCurrentUrl();
	}

	protected static MarketPrice createPrice(WebElement priceWithFeeElement) {
		return MarketPrice.getPrice(priceWithFeeElement.getAttribute("innerHTML"));
	}
	
	protected static int extractGameIdFromUrl(String url) {
		String gameIdString;
		int res = 0;
		Matcher matcher = GAME_ID_PATTERN.matcher(url);
		if(matcher.find()) {
			gameIdString = matcher.group(1);
			res = Integer.valueOf(gameIdString);
		}
		return res;
	}

}