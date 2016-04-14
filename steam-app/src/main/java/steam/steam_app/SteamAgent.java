package steam.steam_app;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import steam.data.DataStore;
import steam.data.market.MarketItem;
import steam.data.market.MarketListing;
import steam.util.Configuration;
import steam.util.Constants;
import steam.util.SteamListingPriceComparator;
import steam.util.WatcherConfig;
import steam.watcher.BaseWatcher;
import steam.watcher.MarketItemsWatcher;
import steam.web.handler.SteamHandler;

public class SteamAgent implements Runnable {

	private static String MARKET_SEARCH_URL = "http://steamcommunity.com/market/search?q=&category_730_ItemSet%5B0%5D=any"
			+ "&category_730_ProPlayer%5B0%5D=any&category_730_TournamentTeam%5B0%5D=any&category_730_Weapon%5B0%5D=any"
			+ "&category_730_Quality%5B0%5D=tag_strange&category_730_Rarity%5B0%5D=tag_Rarity_Rare_Weapon"
			+ "&category_730_Rarity%5B1%5D=tag_Rarity_Uncommon_Weapon&category_730_Rarity%5B2%5D=tag_Rarity_Mythical_Weapon"
			+ "&category_730_Rarity%5B3%5D=tag_Rarity_Legendary_Weapon&category_730_Rarity%5B4%5D=tag_Rarity_Ancient_Weapon"
			+ "&category_730_Rarity%5B5%5D=tag_Rarity_Common&category_730_Rarity%5B6%5D=tag_Rarity_Rare"
			+ "&category_730_Rarity%5B7%5D=tag_Rarity_Legendary&category_730_Rarity%5B8%5D=tag_Rarity_Mythical&category_730_Rarity"
			+ "%5B9%5D=tag_Rarity_Contraband&category_730_Type%5B0%5D=tag_CSGO_Type_Pistol&category_730_Type%5B1"
			+ "%5D=tag_CSGO_Type_SMG&category_730_Type%5B2%5D=tag_CSGO_Type_Rifle&category_730_Type%5B3%5D"
			+ "=tag_CSGO_Type_Shotgun&category_730_Type%5B4%5D=tag_CSGO_Type_SniperRifle&category_730_Type%5"
			+ "B5%5D=tag_CSGO_Type_Machinegun&category_730_Type%5B6%5D=tag_CSGO_Tool_Sticker&category_730_Typ"
			+ "e%5B7%5D=tag_CSGO_Tool_WeaponCase_KeyTag&category_730_Type%5B8%5D=tag_CSGO_Tool_Name_TagTag&app"
			+ "id=730#p12_price_asc";
	private static String ITEM_URL = "http://steamcommunity.com/market/listings/730/AK-47%20%7C%20Redline%20%28Field-Tested%29";
	private static MarketItem MARKET_ITEM = new MarketItem(730, "AK-47 | Redline (Field-Tested)", ITEM_URL);

	@Inject
	private Comparator<MarketListing> LISTING_COMPARATOR = new SteamListingPriceComparator();

	@Inject
	private DataStore ds;

	@Inject
	private Logger logger;

	@Inject
	private Instance<MarketItemsWatcher> marketListingWatcherInstance;

	@Inject
	private Instance<Object> instance;
	
	public void run() {
		// initialize the jobs...
		ds.executeUpdate("DELETE FROM WatchedMarketItem w");
		ds.executeUpdate("DELETE FROM MarketListing l");
		ds.executeUpdate("DELETE FROM MarketListing l");
		ds.executeUpdate("DELETE FROM MarketItem i");
		
		try {
			// driver = WebDriverFactory.getPhantomJSDriver();

			startWatchers();
//			Set<MarketItem> items = handler.readAllMarketItems(MARKET_SEARCH_URL).keySet();
//			ds.persistMarketItems(items);
			
		} catch (Exception e) {
			// TODO: Extract screenshot... somehow!
			logger.info("Error while processing: {}", e);
		} finally {
		}
	}
	
	@SuppressWarnings("unchecked")
	private void startWatchers() throws ClassNotFoundException {
		ExecutorService executor = Executors.newCachedThreadPool();
		Collection<WatcherConfig> watcherConfigs = Configuration.getInstance().readWatcherConfigurations();
		for(WatcherConfig config : watcherConfigs) {
			Class<?> clazz = Class.forName(config.getClazz());
			if(!(BaseWatcher.class.isAssignableFrom(clazz))) {
				throw new IllegalArgumentException("The configured Watcher class " + config.getClazz() + " is not a watcher!");
			}
			// create a new Watcher
			BaseWatcher watcher = instance.select((Class<BaseWatcher>) clazz).get();
			// add the needed Configuration
			watcher.setWatcherConfiguration(config.getConfiguration());
			// start the watcher
			logger.info("starting watcher: {}", watcher);
			executor.execute(watcher);
		}
	}
	
//	private void loadCSGOMarketItemsToDB(int gameId, int maxPages) {
////		handler.doLogin();
//		Collection<MarketItem> marketItems = handler.readMarketItems(gameId, maxPages).keySet();
//		marketItems = filterIgnoredItems(marketItems);
//		ds.persistMarketItems(marketItems);
//	}

	private Collection<MarketItem> filterIgnoredItems(Collection<MarketItem> toFilter) {
		Collection<MarketItem> res = new HashSet<>();
		String[] ignoreSuffixes = Configuration.getInstance().readArray(Configuration.STEAM_MARKET_IGNORE_SUFFIXES_ARR);
		for (MarketItem item : toFilter) {
			boolean isIgnored = false;
			for (String ignoreSuffix : ignoreSuffixes) {
				if (item.getItemName().endsWith(ignoreSuffix)) {
					isIgnored = true;
					break;
				}
			}
			if (!isIgnored) {
				res.add(item);
			}
		}
		return res;
	}

	public static File takeScreenshot(WebDriver driver) {
		File resFile = null;
		if (driver != null) {
			File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

			resFile = new File("c:\\tmp\\" + Constants.DATE_FORMAT.format(new Date()) + ".jpg");
			try {
				FileUtils.copyFile(scrFile, resFile);
			} catch (IOException e) {
				throw new RuntimeException("Unable to create File: " + resFile.getAbsolutePath());
			}
		}
		return resFile;
	}

}
