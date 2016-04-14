package steam.watcher;

import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import steam.data.market.MarketItem;
import steam.data.market.MarketPrice;
import steam.engine.MarketPriceEngine;
import steam.engine.MarketProfit;
import steam.web.handler.MarketItemProfitHandler;
import steam.web.handler.SteamHandler;

/**
 * Watches the cheapest Price by a given url.
 *
 */
public class MarketItemCheapestPriceWatcher extends BaseWatcher {

	@Inject
	private SteamHandler steamHandler;
	
	@Inject
	private MarketItemProfitHandler profitHandler;
	
	private MarketPriceEngine priceEngine;
	
	private String urlToWatch;

	@Override
	public void run() {
		if(StringUtils.isEmpty(urlToWatch)) {
			throw new IllegalArgumentException("urlToWatch must not be empty!");
		}
		steamHandler.doLogin();
		Map<MarketItem, MarketPrice> itemPrices =  steamHandler.readMarketItems(urlToWatch, -1);
		for(Entry<MarketItem, MarketPrice> entry : itemPrices.entrySet()) {
			MarketProfit profit = priceEngine.isProfitable(entry.getKey().getKey(), entry.getValue());
			profitHandler.handleProfit(profit, entry.getKey());
		}
	}
	
	public void initializeUrl(String url) {
		this.urlToWatch = url;
	}
	
}