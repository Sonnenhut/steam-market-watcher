package steam.engine;

import steam.data.market.MarketItemKey;
import steam.data.market.MarketPrice;

public interface MarketPriceEngine {

	MarketProfit isProfitable(MarketItemKey itemKey, MarketPrice price);
}
