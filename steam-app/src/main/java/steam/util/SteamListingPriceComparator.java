package steam.util;

import java.util.Comparator;

import steam.data.market.MarketListing;

public class SteamListingPriceComparator implements Comparator<MarketListing> {

	@Override
	public int compare(MarketListing listing1, MarketListing listing2) {
		int res = Double.compare(listing1.getPrice().getPriceWithFee(), listing2.getPrice().getPriceWithFee());
		if(res == 0) {
			res = listing1.getId().compareTo(listing2.getId());
		}
		return res;
	}
}
