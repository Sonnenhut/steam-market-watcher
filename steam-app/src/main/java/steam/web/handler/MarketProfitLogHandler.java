package steam.web.handler;

import javax.inject.Inject;

import org.slf4j.Logger;

import steam.data.market.MarketItem;
import steam.data.market.MarketListing;
import steam.engine.MarketProfit;

public class MarketProfitLogHandler implements MarketListingProfitHandler, MarketItemProfitHandler{

	@Inject
	private Logger logger;
	
	@Override
	public void handleProfit(MarketProfit profit, MarketListing listing) {
		if(profit.isProfitable()) {
			logger.info("handling item '{}', price '{}', profit '{}' is profitable!", listing.getMarketItemKey().getItemName(), profit.getPrice() , profit);
		}
	}
	
	@Override
	public void handleProfit(MarketProfit profit, MarketItem item) {
		if(profit.isProfitable()) {
			logger.info("handling item '{}', price '{}', profit '{}' is profitable!", item.getKey().getItemName(), profit.getPrice() , profit);
		}
	}
}