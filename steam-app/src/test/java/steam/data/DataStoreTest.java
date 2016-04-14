package steam.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import steam.data.market.MarketCurrency;
import steam.data.market.MarketItem;
import steam.data.market.MarketItemKey;
import steam.data.market.MarketListing;
import steam.data.market.MarketPrice;
import steam.test.WeldJUnit4ThreadScopedRunner;

@RunWith(WeldJUnit4ThreadScopedRunner.class)
public class DataStoreTest {

	@Inject
	private DataStore ds;

	private List<MarketItem> items;

	private final MarketCurrency CURR_EUR = MarketCurrency.EUR;
	private final MarketCurrency CURR_OTHER = MarketCurrency.OTHER;
	
	private final String jobId = "123";


	@Test
	public void testQueryNotWatchedItemWithPriceBetween_OneNotWatched() {
		List<WatchedMarketItem> watchedItems = new ArrayList<>();
		watchedItems.add(createWatchedItem(items.get(0).getKey(), jobId));
		watchedItems.add(createWatchedItem(items.get(1).getKey(), jobId));
		ds.persistWatchedMarketItems(watchedItems);

		items.get(0).getListings().add(createMarketListing("0", items.get(0).getKey(), 0.2, CURR_EUR));
		items.get(1).getListings().add(createMarketListing("1", items.get(1).getKey(), 0.2, CURR_EUR));
		items.get(2).getListings().add(createMarketListing("2", items.get(2).getKey(), 0.2, CURR_EUR));
		ds.persistMarketItems(items);

		// ensure that the entities are in the set in the datastore
		Assert.assertFalse(ds.find(MarketItem.class, items.get(0).getKey()).getListings().isEmpty());

		// thest the query
		List<MarketItem> res = ds.queryNotWatchedItemWithPriceBetween(20, 0, 2, CURR_EUR, Collections.emptyList());
		Assert.assertEquals(1, res.size());
	}

	@Test
	public void testQueryNotWatchedItemWithPriceBetween_OneNotWatchedOverMax() {
		List<WatchedMarketItem> watchedItems = new ArrayList<>();
		watchedItems.add(createWatchedItem(items.get(0).getKey(), jobId));
		watchedItems.add(createWatchedItem(items.get(1).getKey(), jobId));
		ds.persistWatchedMarketItems(watchedItems);

		items.get(0).getListings().add(createMarketListing("0", items.get(0).getKey(), 0.2, CURR_EUR));
		items.get(1).getListings().add(createMarketListing("1", items.get(1).getKey(), 0.2, CURR_EUR));
		items.get(2).getListings().add(createMarketListing("2", items.get(2).getKey(), 3, CURR_EUR));
		ds.persistMarketItems(items);

		// ensure that the entities are in the set in the datastore
		Assert.assertFalse(ds.find(MarketItem.class, items.get(0).getKey()).getListings().isEmpty());

		// thest the query
		List<MarketItem> res = ds.queryNotWatchedItemWithPriceBetween(20, 0, 2, CURR_EUR, Collections.emptyList());
		Assert.assertEquals(0, res.size());
	}

	@Test
	public void testQueryNotWatchedItemWithPriceBetween_NoneWatchedOverMax() {
		items.get(0).getListings().add(createMarketListing("0", items.get(0).getKey(), 3, CURR_EUR));
		items.get(1).getListings().add(createMarketListing("1", items.get(1).getKey(), 3, CURR_EUR));
		items.get(2).getListings().add(createMarketListing("2", items.get(2).getKey(), 3, CURR_EUR));
		ds.persistMarketItems(items);

		// ensure that the entities are in the set in the datastore
		Assert.assertFalse(ds.find(MarketItem.class, items.get(0).getKey()).getListings().isEmpty());

		// thest the query
		List<MarketItem> res = ds.queryNotWatchedItemWithPriceBetween(20, 1, 2, CURR_EUR, Collections.emptyList());
		Assert.assertEquals(0, res.size());
	}

	@Test
	public void testQueryNotWatchedItemWithPriceBetween_NoneWatchedUnderMin() {
		items.get(0).getListings().add(createMarketListing("0", items.get(0).getKey(), 0.1, CURR_EUR));
		items.get(1).getListings().add(createMarketListing("1", items.get(1).getKey(), 0.1, CURR_EUR));
		items.get(2).getListings().add(createMarketListing("2", items.get(2).getKey(), 0.1, CURR_EUR));
		ds.persistMarketItems(items);

		// ensure that the entities are in the set in the datastore
		Assert.assertFalse(ds.find(MarketItem.class, items.get(0).getKey()).getListings().isEmpty());

		// thest the query
		List<MarketItem> res = ds.queryNotWatchedItemWithPriceBetween(20, 0.2, 99, CURR_EUR, Collections.emptyList());
		Assert.assertEquals(0, res.size());
	}

	@Test
	public void testQueryNotWatchedItemWithPriceBetween_NoneWatchedOneIgnored() {
		items.get(0).getListings().add(createMarketListing("0", items.get(0).getKey(), 0.2, CURR_EUR));
		items.get(1).getListings().add(createMarketListing("1", items.get(1).getKey(), 0.2, CURR_EUR));
		items.get(2).getListings().add(createMarketListing("2", items.get(2).getKey(), 0.2, CURR_EUR));
		ds.persistMarketItems(items);
		
		List<MarketItemKey> ignoreItemKeys = new ArrayList<>();
		ignoreItemKeys.add(items.get(0).getKey());

		// ensure that the entities are in the set in the datastore
		Assert.assertFalse(ds.find(MarketItem.class, items.get(0).getKey()).getListings().isEmpty());

		// thest the query
		List<MarketItem> res = ds.queryNotWatchedItemWithPriceBetween(20, 0, 5, CURR_EUR, ignoreItemKeys);
		Assert.assertEquals(2, res.size());
	}
	
	@Test
	public void testQueryNotWatchedItemWithPriceBetween_WrongCurrency() {
		items.get(0).getListings().add(createMarketListing("0", items.get(0).getKey(), 0.2, CURR_OTHER));
		items.get(1).getListings().add(createMarketListing("1", items.get(1).getKey(), 0.2, CURR_OTHER));
		items.get(2).getListings().add(createMarketListing("2", items.get(2).getKey(), 0.2, CURR_OTHER));
		ds.persistMarketItems(items);
		
		List<MarketItemKey> ignoreItemKeys = new ArrayList<>();
		ignoreItemKeys.add(items.get(0).getKey());

		// ensure that the entities are in the set in the datastore
		Assert.assertFalse(ds.find(MarketItem.class, items.get(0).getKey()).getListings().isEmpty());

		// thest the query
		List<MarketItem> res = ds.queryNotWatchedItemWithPriceBetween(20, 0, 5, CURR_EUR, ignoreItemKeys);
		Assert.assertEquals(0, res.size());
	}

	private MarketItem createItem(String name) {
		return new MarketItem(730, name, "myUrl");
	}

	private WatchedMarketItem createWatchedItem(MarketItemKey itemKey, String jobId) {
		return new WatchedMarketItem(itemKey, jobId);
	}

	private MarketListing createMarketListing(String id, MarketItemKey itemKey, double priceWithFee, MarketCurrency currency) {
		return new MarketListing(id, itemKey, new MarketPrice(priceWithFee, currency));
	}

	@Before
	public void setUp() {
		// run in-memory, not the real DB
		System.setProperty("javax.persistence.jdbc.url", "jdbc:derby:memory:/tmp/databases/steamDB;create=true");

		
		// set up three Items in DB
		items = new ArrayList<>();
		items.add(createItem("Item1"));
		items.add(createItem("Item2"));
		items.add(createItem("Item3"));
		ds.persistObjects(items, MarketItem.class, false);
	}
	
	@After
	public void tearDown() {
		// delete DB entries
		ds.executeUpdate("DELETE FROM WatchedMarketItem w");
		ds.executeUpdate("DELETE FROM MarketListing l");
		ds.executeUpdate("DELETE FROM MarketItem i");
	}
}
