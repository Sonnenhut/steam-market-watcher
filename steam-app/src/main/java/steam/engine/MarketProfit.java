package steam.engine;

import steam.data.market.MarketItemKey;
import steam.data.market.MarketPrice;

public class MarketProfit {

	private final boolean isProfitable;
	private final MarketItemKey item;
	private final MarketPrice price;
	private double profit = 0.0;

	public MarketProfit(boolean isProfitable, MarketItemKey item, MarketPrice price) {
		this.isProfitable = isProfitable;
		this.item = item;
		this.price = price;
	}

	public double getProfit() {
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public boolean isProfitable() {
		return isProfitable;
	}

	public MarketItemKey getItem() {
		return item;
	}

	public MarketPrice getPrice() {
		return price;
	}

}
