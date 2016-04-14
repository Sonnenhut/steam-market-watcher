package steam.watcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;

import steam.data.DataStore;
import steam.data.WatchedMarketItem;
import steam.data.market.MarketCurrency;
import steam.data.market.MarketItem;
import steam.data.market.MarketItemKey;
import steam.data.market.MarketListing;
import steam.engine.MarketPriceEngine;
import steam.util.Configuration;
import steam.web.handler.MarketListingProfitHandler;
import steam.web.handler.SteamHandler;

/**
 * A Market watcher that reads current items with price between
 * {@link Configuration#STEAM_MAXIMUM_ALLOWED_PRICE} and
 * {@link Configuration#STEAM_MINIMUM_ALLOWED_PRICE}.<br>
 * It marks an {@link MarketItem} as {@link WatchedMarketItem}.<br>
 * This way this instances will exclusively watch said {@link MarketItem}.
 *
 */
public class MarketItemsWatcher extends BaseWatcher implements Runnable {

	private static final String MAX_ITEMS_TO_WATCH = "marketItemsWatcher.maxToWatch";
	private static final String WAIT_FOR_DATA_PERIOD = "marketItemsWatcher.waitForDataPeriod";

	private Collection<MarketItem> itemsToWatch = new ArrayList<>();

	private Collection<MarketItemKey> ignoreItems = new ArrayList<>();

	@Inject
	private Logger logger;

	@Inject
	private SteamHandler handler;

	@Inject
	private MarketListingProfitHandler profitHandler;

	@Inject
	private MarketPriceEngine engine;

	@Inject
	private DataStore ds;

	@Override
	public void run() {
		logger.trace(this + " got instance of DataStorage: " + ds);
		int lastIgnoreCnt = 0;

		handler.doLogin();
		do {
			// if there aren't enough items to Watch or some items were ignored,
			// we need to Initialize the items.
			if (itemsToWatch.size() < getMaxToWatch() || lastIgnoreCnt < ignoreItems.size()) {
				initializeItems();
				lastIgnoreCnt = ignoreItems.size();
			}

			if (itemsToWatch.isEmpty()) {
				// take a nap, there is no data yet
				snooze();
			}

			for (Iterator<MarketItem> iter = itemsToWatch.iterator(); iter.hasNext();) {
				MarketItem item = iter.next();
				Set<MarketListing> listings = readMarketListings(item);
				if (!listings.isEmpty()) {
					handleListings(listings);
				} else {
					// if the item has now listings, discard it.
					ignoreItems.add(item.getKey());
					iter.remove();
				}
			}
		} while (!isInterrupted());
	}

	private synchronized void initializeItems() {
		logger.info("initializing items. currently watched items: '{}'; ignored items: '{}'", itemsToWatch,
				ignoreItems);
		int itemsToFind = getMaxToWatch() - itemsToWatch.size();

		// be sure this only happens once at a time.
		// otherwise there could be two Threads wanting to watch the same items.
		synchronized (MarketItemsWatcher.class) {
			final List<MarketItem> unwatchedItems = ds.queryNotWatchedItemWithPriceBetween(itemsToFind, getMinPrice(),
					getMaxPrice(), MarketCurrency.EUR, ignoreItems);
			setItemsWatched(unwatchedItems);
			this.itemsToWatch.addAll(unwatchedItems);
		}
	}

	private void snooze() {
		try {
			Thread.sleep(getWaitForDataPeriod());
		} catch (InterruptedException e) {
			logger.warn("Was interrupted during snooze!", e);
		}
	}

	private void setItemsWatched(Collection<MarketItem> items) {
		Collection<WatchedMarketItem> watchedItems = new ArrayList<>();
		for (MarketItem item : items) {
			watchedItems.add(new WatchedMarketItem(item.getKey(), Thread.currentThread().getName()));
		}
		ds.persistObjects(watchedItems, WatchedMarketItem.class, false);
	}

	private Set<MarketListing> readMarketListings(MarketItem item) {
		logger.trace("reading listings for item item '{}'", item.getItemName());
		return handler.readFirstMarketListings(item);
	}

	private void handleListings(Set<MarketListing> listings) {
		for (MarketListing listing : listings) {
			profitHandler.handleProfit(engine.isProfitable(listing.getMarketItemKey(), listing.getPrice()), listing);
		}
	}

	private boolean isInterrupted() {
		return Thread.currentThread().isInterrupted();
	}

	private double getMinPrice() {
		return getWatcherConfiguration().readDouble(Configuration.STEAM_MINIMUM_ALLOWED_PRICE);
	}

	private double getMaxPrice() {
		return getWatcherConfiguration().readDouble(Configuration.STEAM_MAXIMUM_ALLOWED_PRICE);
	}

	private int getMaxToWatch() {
		return getWatcherConfiguration().readInteger(MAX_ITEMS_TO_WATCH);
	}

	private int getWaitForDataPeriod() {
		return getWatcherConfiguration().readInteger(WAIT_FOR_DATA_PERIOD);
	}
}
