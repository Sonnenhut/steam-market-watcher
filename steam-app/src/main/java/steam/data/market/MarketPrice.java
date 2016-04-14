package steam.data.market;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Embeddable
public class MarketPrice {

	private static Logger logger = LoggerFactory.getLogger(MarketPrice.class);

	private static final char[] NO_VALID_PRICE_CHARS = new char[] { '!' };
	private static final Map<MarketCurrency, List<String>> CURRENCY_MAP = new HashMap<>();
	private static final List<String> TO_BE_REMOVED_PRICE_STRINGS = new ArrayList<>(Arrays.asList("\r", "\n"));

	static {
		CURRENCY_MAP.put(MarketCurrency.EUR, Arrays.asList("€"));
		CURRENCY_MAP.put(MarketCurrency.USD, Arrays.asList("USD", "$"));
		for (List<String> currencyStrings : CURRENCY_MAP.values()) {
			TO_BE_REMOVED_PRICE_STRINGS.addAll(currencyStrings);
		}
	}

	@Column
	private double priceWithFee;

	@Column
	@Enumerated(EnumType.STRING)
	private MarketCurrency currency;

	public MarketPrice() {
	}

	public MarketPrice(double priceWithFee, MarketCurrency currency) {
		this.priceWithFee = priceWithFee;
		this.currency = currency;
	}

	private static Number parseNumber(String priceString) {
		NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
		Number number;
		try {
			number = format.parse(stripStringDown(priceString));
		} catch (ParseException e) {
			logger.error("unable to parse String {}", priceString);
			throw new RuntimeException("unable to parse String " + priceString, e);
		}
		return number;
	}

	public double getPriceWithFee() {
		return priceWithFee;
	}

	private static MarketCurrency convertCurrency(String priceString) {
		for (Map.Entry<MarketCurrency, List<String>> entry : CURRENCY_MAP.entrySet()) {
			for (String currencyString : entry.getValue()) {
				if (StringUtils.contains(priceString, currencyString)) {
					// return the found currency
					return entry.getKey();
				}
			}
		}
		return MarketCurrency.OTHER;
	}

	private static String stripStringDown(String priceString) {
		for (String toRemove : TO_BE_REMOVED_PRICE_STRINGS) {
			priceString = StringUtils.replace(priceString, toRemove, "");
		}
		priceString = StringUtils.trim(priceString);
		return priceString;
	}

	public static boolean isValidPrice(String priceString) {
		return !StringUtils.containsAny(priceString, NO_VALID_PRICE_CHARS);
	}

	/**
	 * Turn a String price into a real price, if not possible this will return
	 * null.
	 * 
	 * @param priceWithFeeString
	 *            the price with a fee in it
	 * @return the price object, null otherwise
	 */
	public static MarketPrice getPrice(String priceWithFeeString) {
		if (isValidPrice(priceWithFeeString)) {
			logger.trace("Creating Price Object for withFee={}", stripStringDown(priceWithFeeString));
			final double priceWithFee = parseNumber(priceWithFeeString).doubleValue();
			final MarketCurrency currency = convertCurrency(priceWithFeeString);
			return new MarketPrice(priceWithFee, currency);
		}
		return null;
	}

	public MarketCurrency getCurrency() {
		return currency;
	}

	public void setCurrency(MarketCurrency currency) {
		this.currency = currency;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("withFee", priceWithFee).build();
	}

}
