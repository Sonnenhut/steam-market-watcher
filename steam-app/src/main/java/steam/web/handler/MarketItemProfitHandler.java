package steam.web.handler;

import steam.data.market.MarketItem;
import steam.engine.MarketProfit;

public interface MarketItemProfitHandler {

	void handleProfit(MarketProfit profit, MarketItem item);
}
