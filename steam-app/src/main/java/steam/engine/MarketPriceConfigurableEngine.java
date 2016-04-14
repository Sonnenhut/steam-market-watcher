package steam.engine;

import javax.inject.Inject;

import org.slf4j.Logger;

import steam.data.market.MarketItemKey;
import steam.data.market.MarketPrice;

/**
 * A {@link MarketPriceEngine} that checks if the price is under
 * a configured value passed by the constructor.
 * 
 * @author Sonnenhut
 *
 */
public class MarketPriceConfigurableEngine implements MarketPriceEngine {

	@Inject
	private Logger logger;

	private final double configuredProfitablePrice;

	public MarketPriceConfigurableEngine(double configuredProfitablePrice) {
		this.configuredProfitablePrice = configuredProfitablePrice;
	}

	@Override
	public MarketProfit isProfitable(MarketItemKey itemKey, MarketPrice price) {
		boolean profitable = false;
		if (configuredProfitablePrice >= price.getPriceWithFee()) {
			profitable = true;
			logger.trace("listing  with name '{}' is profitable. Price '{}', configured profitable price '{}'",
					itemKey.getItemName(),
					price.getPriceWithFee(), configuredProfitablePrice);
		}
		return new MarketProfit(profitable, itemKey, price);
	}
}