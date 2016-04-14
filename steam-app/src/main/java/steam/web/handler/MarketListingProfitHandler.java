package steam.web.handler;

import steam.data.market.MarketListing;
import steam.engine.MarketProfit;

public interface MarketListingProfitHandler {

	void handleProfit(MarketProfit profit, MarketListing toHandle);
}
