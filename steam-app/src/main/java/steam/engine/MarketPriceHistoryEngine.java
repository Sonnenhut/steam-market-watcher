package steam.engine;

import java.util.Calendar;

import javax.activation.DataSource;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.slf4j.Logger;

import steam.data.DataStore;
import steam.data.market.MarketItem;
import steam.data.market.MarketItemKey;
import steam.data.market.MarketListing;
import steam.data.market.MarketPrice;
import steam.util.Configuration;

/**
 * Reads the {@link DataSource} and checks if the given {@link MarketItem} is
 * profitable depending on recent {@link MarketPrice}s of the item.
 *
 */
@Default
public class MarketPriceHistoryEngine implements MarketPriceEngine{

	@Inject
	private Logger logger;

	@Inject
	private DataStore ds;

	private final int steamTaxPercent;
	private final double steamMinimumGain;
	private final double steamMaxPrice;
	private final double steamMinPrice;

	public MarketPriceHistoryEngine() {
		steamTaxPercent = Configuration.getInstance().readInteger(Configuration.STEAM_TAX);
		steamMinimumGain = Configuration.getInstance().readDouble(Configuration.STEAM_MINIMUM_GAIN);
		steamMaxPrice = Configuration.getInstance().readDouble(Configuration.STEAM_MAXIMUM_ALLOWED_PRICE);
		steamMinPrice = Configuration.getInstance().readDouble(Configuration.STEAM_MAXIMUM_ALLOWED_PRICE);
	}

	public MarketProfit isProfitable(MarketItemKey itemKey, MarketPrice price) {
		double profit = 0.0;
		final double priceToCheck = price.getPriceWithFee();
		logger.trace("Checking if price '{}' for item is profitable: '{}'", priceToCheck, itemKey.getItemName());
		if (steamMaxPrice >= priceToCheck && steamMinPrice <= priceToCheck) {
			final double highestPrice = queryHighestPriceSinceYesterday(itemKey);
			final double possibleProfit = calculateProfit(priceToCheck, highestPrice);
			if (steamMinimumGain <= possibleProfit) {
				logger.trace("Price is profitable: nearest known price is '{}', so gain would be '{}'", highestPrice,
						possibleProfit);
				profit = possibleProfit;
			}
		} else {
			logger.trace("Price '{}' not profitable: over '{}'", priceToCheck, steamMaxPrice);
		}
		return new MarketProfit(profit > 0.0, itemKey, price);
	}

	/**
	 * Calculates the possible profit out of the buying Price and the highest
	 * possible known price
	 * 
	 * @param buyPrice
	 *            the buying price
	 * @param possiblePrice
	 *            the possible price for the same thing
	 * @return the profit or loss that would theoretically be achieved
	 */
	private double calculateProfit(double buyPrice, double possiblePrice) {
		double res = 0.0;
		if (0.0 < possiblePrice) {
			res = addFee(buyPrice) - possiblePrice;
		}
		return res;
	}

	private double removeFee(double priceWithFee) {
		return (priceWithFee / (100 + steamTaxPercent)) * 100;
	}

	private double addFee(double priceWithoutFee) {
		return (priceWithoutFee / 100) * (100 * steamTaxPercent);
	}

	private double queryHighestPriceSinceYesterday(MarketItemKey marketItemKey) {
		// today
		Calendar yesterdayDate = Calendar.getInstance();
		// reset hour, minutes, seconds and millis
		yesterdayDate.set(Calendar.HOUR_OF_DAY, 0);
		yesterdayDate.set(Calendar.MINUTE, 0);
		yesterdayDate.set(Calendar.SECOND, 0);
		yesterdayDate.set(Calendar.MILLISECOND, 0);
		yesterdayDate.add(Calendar.DATE, -1);
		return ds.queryHighestPriceSince(marketItemKey, yesterdayDate);
	}
}
